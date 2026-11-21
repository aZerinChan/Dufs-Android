"""Build a signed standalone APK using JDK, Android platform 35 and build-tools 35."""
import argparse, os, shutil, subprocess, tempfile, zipfile
from pathlib import Path
from deps import jars

p = argparse.ArgumentParser(__doc__)
p.add_argument('--platform', required=True, type=Path, help='path to Android 35 android.jar')
p.add_argument('--tools', required=True, type=Path, help='directory containing aapt2, zipalign and lib/d8.jar')
p.add_argument('--keystore', required=True, type=Path)
p.add_argument('--out', required=True, type=Path)
p.add_argument('--deps', required=True, type=Path)
p.add_argument('--kind', choices=('release','debug'), default='release')
args = p.parse_args()
root=Path(__file__).resolve().parent
platform,tools,key,out = (x.resolve() for x in (args.platform,args.tools,args.keystore,args.out))
if not key.is_file() or not platform.is_file(): p.error('missing signing keystore or platform JAR')
if not os.environ.get('DUFS_KEYSTORE_PASS'): p.error('set DUFS_KEYSTORE_PASS in the environment')
libs=jars(args.deps.resolve())
def binary(name): return tools/(name+('.exe' if os.name=='nt' else ''))
def run(*cmd): subprocess.run([str(c) for c in cmd],check=True)
with tempfile.TemporaryDirectory(prefix='dufs-build-') as tmp:
    w=Path(tmp); classes=w/'classes'; classes.mkdir(); dex=w/'dex'; dex.mkdir(); generated=w/'generated'; generated.mkdir()
    run(binary('aapt2'),'compile','--dir',root/'res','-o',w/'res.zip')
    manifest=root/'AndroidManifest.xml'
    debug=args.kind=='debug'
    if debug:
        text=manifest.read_text(encoding='utf8').replace('package="local.dufs.manage"','package="local.dufs.manage.debug"').replace('android:name=".MainActivity"','android:name="local.dufs.manage.MainActivity"').replace('android:label="Dufs"','android:label="Dufs Debug" android:debuggable="true"')
        manifest=w/'AndroidManifest.xml'; manifest.write_text(text,encoding='utf8')
    run(binary('aapt2'),'link','-I',platform,'--manifest',manifest,'--java',generated,'--custom-package','local.dufs.manage','-o',w/'res.apk',w/'res.zip')
    run('javac','--release','8','-encoding','UTF-8','-classpath',os.pathsep.join(map(str,[platform]+libs)),'-d',classes,*sorted((root/'src').rglob('*.java')),*sorted(generated.rglob('*.java')))
    flags=['--debug','--no-tree-shaking','--no-minification'] if debug else ['--release']
    run('java','-cp',tools/'lib/d8.jar','com.android.tools.r8.R8',*flags,'--min-api','26','--lib',platform,'--pg-conf',root/'r8.pro','--no-data-resources','--output',dex,*sorted(classes.rglob('*.class')),*libs)
    unsigned=w/'unsigned.apk'; shutil.copyfile(w/'res.apk',unsigned)
    with zipfile.ZipFile(unsigned,'a',zipfile.ZIP_DEFLATED) as apk:
        for entry in sorted(dex.glob('classes*.dex')): apk.write(entry,entry.name)
        for notice in sorted(root.glob('*LICENSE*.txt')): apk.write(notice,'META-INF/licenses/'+notice.name)
        for jar in libs:
            with zipfile.ZipFile(jar) as z:
                for n in z.namelist():
                    if n.endswith('.kotlin_builtins') or n.startswith('okhttp3/') and n.endswith('.gz'):
                        apk.writestr(n,z.read(n))
                    elif n.startswith('META-INF/') and any(k in n.upper() for k in ('LICENSE','NOTICE')) and not n.endswith('/'):
                        apk.writestr('META-INF/licenses/'+jar.stem+'/'+n.split('/')[-1],z.read(n))
    run(binary('zipalign'),'-f','-P','16','4',unsigned,w/'aligned.apk')
    signed=w/'signed.apk'
    signer=tools/'lib/apksigner.jar'
    run('java','-jar',signer,'sign','--ks',key,'--ks-key-alias','dufs','--ks-pass','env:DUFS_KEYSTORE_PASS','--v4-signing-enabled','false','--out',signed,w/'aligned.apk')
    run('java','-jar',signer,'verify','--verbose','--print-certs',signed)
    run(binary('zipalign'),'-c','-P','16','4',signed)
    run(binary('aapt2'),'dump','badging',signed)
    out.parent.mkdir(parents=True,exist_ok=True); shutil.copyfile(signed,out)
print('APK:',out)

"""Local release assembly. Supply the pinned SDK paths and privately held key credentials."""
import argparse, base64, hashlib, json, os, shutil, subprocess, sys, tempfile, zipfile
from pathlib import Path

parser=argparse.ArgumentParser(__doc__)
for arg in ('platform','tools','keystore','dufs','deps','output','scratch'):
    parser.add_argument('--'+arg, type=Path, required=True)
parser.add_argument('--source-zip', type=Path, help='optionally export the verified source ZIP outside --output')
a=parser.parse_args()
root=Path(__file__).resolve().parent
out=a.output.resolve()
if out.exists() and (not out.is_dir() or any(out.iterdir())):
    parser.error('--output must be new or empty')
export=a.source_zip.resolve() if a.source_zip else None
if export is not None and (export.suffix.lower()!='.zip' or export==out or out in export.parents or export.exists()):
    parser.error('--source-zip must be a new .zip file outside --output')
scratch=a.scratch.resolve()
if scratch==out or out in scratch.parents:
    parser.error('--scratch must be outside --output')
scratch.mkdir(parents=True,exist_ok=True)
notices=sorted(x.relative_to(root).as_posix() for x in (root/'LICENSE-FILE').glob('*.txt') if x.is_file())
if not notices:
    parser.error('no third-party license files found in LICENSE-FILE/')
source_names=['README.md','LICENSE','build.py','deps.py','release_local.py','ReleaseSignature.java','r8.pro','AndroidManifest.xml']+notices+sorted(
    x.relative_to(root).as_posix() for folder in ('src','res','tests') for x in (root/folder).rglob('*')
    if x.is_file() and x.suffix in ('.py','.xml','.java'))
def sha(p): return hashlib.sha256(p.read_bytes()).hexdigest()
def check(cmd, chosen_env=None):
    result=subprocess.run([str(x) for x in cmd],env=env if chosen_env is None else chosen_env,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
    text=result.stdout.decode('utf-8',errors='replace')
    if result.returncode:
        print(text); result.check_returncode()
    return text
env=dict(os.environ,TMPDIR=str(scratch),TEMP=str(scratch),TMP=str(scratch),PYTHONUTF8='1',JAVA_TOOL_OPTIONS='-Dfile.encoding=UTF-8')
with tempfile.TemporaryDirectory(prefix='dufs-release-',dir=scratch) as tmp:
    tmp=Path(tmp); src=tmp/'src'; src.mkdir()
    source_zip=tmp/'dufs-0.1.0-source.zip'
    manifest={name:sha(root/name) for name in source_names}
    with zipfile.ZipFile(source_zip,'w',zipfile.ZIP_DEFLATED) as z:
        for name in source_names: z.write(root/name,name)
        z.writestr('source-sha256.json',json.dumps(manifest,indent=2)+'\n')
    with zipfile.ZipFile(source_zip) as z:
        assert z.testzip() is None
        assert set(z.namelist())==set(source_names+['source-sha256.json'])
        z.extractall(src)
    assert all(sha(src/name)==digest for name,digest in manifest.items())
    built=tmp/'dufs-0.1.0.apk'
    build=check([sys.executable,src/'build.py','--platform',a.platform.resolve(),'--tools',a.tools.resolve(),'--keystore',a.keystore.resolve(),'--deps',a.deps.resolve(),'--out',built])
    assert "package: name='local.dufs.manage' versionCode='14' versionName='0.1.0'" in build and "application-label:'Dufs'" in build and "minSdkVersion:'26'" in build and "targetSdkVersion:'35'" in build
    assert 'da2ccb75c8d413b4ee1231f8953183fc5ed7488dbf97b56d6b99fcb52a17ebc2' in build
    assert 'Verified using v2 scheme (APK Signature Scheme v2): true' in build
    exe=a.tools.resolve()/('aapt2.exe' if os.name=='nt' else 'aapt2')
    axml=check([exe,'dump','xmltree','--file','AndroidManifest.xml',built])
    assert 'android.permission.ACCESS_NETWORK_STATE' not in axml
    configs=check([exe,'dump','configurations',built]); assert 'night' in configs.splitlines()
    assert built.stat().st_size < 850*1024, 'R8 size regression'
    for token in ('android.intent.action.MAIN','android.intent.action.SEND"','android.intent.action.SEND_MULTIPLE','android.intent.action.VIEW','content','*/*'):
        assert token in axml,token
    with zipfile.ZipFile(built) as z:
        assert z.testzip() is None
        for name in notices:
            assert hashlib.sha256(z.read('META-INF/licenses/'+Path(name).name)).hexdigest()==manifest[name], name
        assert {'AndroidManifest.xml','resources.arsc','classes.dex'}.issubset(z.namelist())
        dex=b''.join(z.read(n) for n in z.namelist() if n.startswith('classes') and n.endswith('.dex'))
        assert dex.startswith(b'dex\n') and b'Llocal/dufs/manage/MainActivity;' in dex and b'FLAG_SECURE' not in dex
        assert b'Lokhttp3/internal/publicsuffix/PublicSuffixDatabase;' in dex and 'okhttp3/internal/publicsuffix/publicsuffixes.gz' in z.namelist()
        assert b'showAtLocation' in dex and b'setScrollCaptureHint' in dex
        assert any(n.startswith('res/mipmap-anydpi-v26/') for n in z.namelist())
    test=check([sys.executable,src/'tests/run.py','--deps',a.deps.resolve(),'--dufs',a.dufs.resolve()])
    assert test.count('PASS real Dufs')==2
    test=check([sys.executable,src/'tests/check_picker_lifecycle.py'])+check([sys.executable,src/'tests/check_restore.py'])+test
    test=check([sys.executable,src/'tests/check_upload_toolbar.py'])+test
    test=check([sys.executable,src/'tests/check_selection_toolbar.py'])+test
    test=check([sys.executable,src/'tests/check_locale_refresh.py'])+test
    assert 'application-debuggable' not in build
    with zipfile.ZipFile(built) as apk:
        assert not any(n.startswith('lib/') and n.endswith('.so') for n in apk.namelist()), 'Unexpected native ABI requirement'
    cert=tmp/'dufs-release-cert.pem'; sigtool=src/'ReleaseSignature.java'
    check(['java',sigtool,'sign',a.keystore.resolve(),cert,built])
    der=base64.b64decode(''.join(cert.read_text(encoding='ascii').splitlines()[1:-1]),validate=True)
    assert hashlib.sha256(der).hexdigest() in build, 'Detached certificate must match embedded APK signer'
    files=[built,Path(str(built)+'.sig'),cert]
    sums=tmp/'SHA256SUMS.txt'
    sums.write_bytes(''.join(f'{sha(p)}  {p.name}\n' for p in files).encode('ascii'))
    check(['java',sigtool,'verify',cert,built])
    out.mkdir(parents=True,exist_ok=True)
    if any(out.iterdir()): parser.error('--output must be new or empty')
    outputs=[(p,out/p.name) for p in files+[sums]]
    if export is not None:
        export.parent.mkdir(parents=True,exist_ok=True)
        outputs.append((source_zip,export))
    for p,copy in outputs:
        with p.open('rb') as source, copy.open('xb') as destination:
            shutil.copyfileobj(source,destination)
        assert sha(copy)==sha(p)
        print(f'{sha(copy)}  {copy}')
    print(test.strip())
    print('PASS package signing, manifest filters and archive verification')

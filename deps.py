"""Download pinned runtime/build dependencies from Maven Central into a chosen cache."""
import hashlib, os, subprocess, sys
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

BASE='https://repo.maven.apache.org/maven2/'
ARTIFACTS=[
 'com/squareup/okhttp3/okhttp/4.12.0/okhttp-4.12.0',
 'com/squareup/okio/okio-jvm/3.6.0/okio-jvm-3.6.0',
 'org/jetbrains/kotlin/kotlin-stdlib/1.9.10/kotlin-stdlib-1.9.10',
 'org/jetbrains/kotlin/kotlin-stdlib-jdk7/1.9.10/kotlin-stdlib-jdk7-1.9.10',
 'org/jetbrains/kotlin/kotlin-stdlib-jdk8/1.9.10/kotlin-stdlib-jdk8-1.9.10',
 'org/jetbrains/annotations/13.0/annotations-13.0',
 'org/json/json/20250517/json-20250517',
]
SHA256=dict(zip((p.split('/')[-1]+'.jar' for p in ARTIFACTS),[
 'b1050081b14bb7a3a7e55a4d3ef01b5dcfabc453b4573a4fc019767191d5f4e0',
 '67543f0736fc422ae927ed0e504b98bc5e269fda0d3500579337cb713da28412',
 '55e989c512b80907799f854309f3bc7782c5b3d13932442d0379d5c472711504',
 'ac6361bf9ad1ed382c2103d9712c47cdec166232b4903ed596e8876b0681c9b7',
 'a4c74d94d64ce1abe53760fe0389dd941f6fc558d0dab35e47c085a11ec80f28',
 'ace2a10dc8e2d5fd34925ecac03e4988b2c0f851650c94b8cef49ba1bd111478',
 '3ea61b2a06e31edf1c91134fe9106b0ebb16628be169f3db75bc7a2b06b45796',
]))

def get(path,dest):
    url=BASE+path+'.jar'; output=dest/(path.split('/')[-1]+'.jar')
    curl=['curl','-fsSL','--max-time','40']
    if os.name=='nt': curl.insert(1,'--ssl-revoke-best-effort')
    digest=SHA256[output.name]
    if not output.exists() or hashlib.sha256(output.read_bytes()).hexdigest()!=digest:
        temp=output.with_suffix('.part')
        try:
            subprocess.run(curl+[url,'-o',str(temp)],check=True)
            if hashlib.sha256(temp.read_bytes()).hexdigest()!=digest: raise ValueError('Maven checksum mismatch: '+url)
            temp.replace(output)
        finally: temp.unlink(missing_ok=True)
    return output.name, hashlib.sha256(output.read_bytes()).hexdigest()

if __name__=='__main__':
    destination=Path(sys.argv[1]).resolve(); destination.mkdir(parents=True,exist_ok=True)
    with ThreadPoolExecutor() as ex:
        for name,digest in ex.map(lambda path:get(path,destination),ARTIFACTS): print(digest,name)

def jars(directory, test=False):
    paths=[directory/(p.split('/')[-1]+'.jar') for p in (ARTIFACTS if test else ARTIFACTS[:-1])]
    for jar in paths:
        if not jar.is_file(): raise FileNotFoundError('Missing '+str(jar)+'; run python deps.py first')
        if hashlib.sha256(jar.read_bytes()).hexdigest()!=SHA256[jar.name]: raise ValueError('Invalid checksum for '+str(jar))
    return paths

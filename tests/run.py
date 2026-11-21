import argparse, contextlib, http.server, os, socket, subprocess, sys, tempfile, threading, time, urllib.request
from pathlib import Path

root=Path(__file__).resolve().parents[1]; sys.path.insert(0,str(root))
from deps import jars
p=argparse.ArgumentParser(); p.add_argument('--dufs',type=Path); p.add_argument('--deps',type=Path,required=True); args=p.parse_args()
class Redirect(http.server.BaseHTTPRequestHandler):
    attempts=0
    def do_GET(self):
        if self.path == '/not-zip/?zip':
            self.send_response(200); self.send_header('Content-Type','text/html'); self.end_headers()
            self.wfile.write(b'<html>Not a ZIP</html>'); return
        if self.path.startswith(('/retry/','/post-upload/')):
            Redirect.attempts+=1
            self.send_response(503 if Redirect.attempts%2 else 200); self.end_headers(); return
        if self.path.startswith('/denied/'):
            self.send_response(403); self.end_headers(); return
        self.send_response(302); self.send_header('Location','/redirected'); self.end_headers()
    def log_message(self,*a): pass
def fixture(root_dir, redir, mode):
    files=root_dir/mode; files.mkdir(); (files/'restricted').mkdir(); (files/'readonly.txt').write_text('unchanged')
    (files/'writable').mkdir(); (files/'writable.txt').write_text('keep')
    (files/'writable'/'查找 +#.txt').write_bytes(b'search fixture')
    with contextlib.closing(socket.socket()) as s: s.bind(('127.0.0.1',0)); port=s.getsockname()[1]
    host=f'http://127.0.0.1:{port}/sample/'
    flags=['--allow-delete','--allow-search','--allow-archive'] if mode=='full' else []
    with subprocess.Popen([str(args.dufs.resolve()),str(files),'-b','127.0.0.1','-p',str(port),'--path-prefix','/sample','--allow-upload',*flags,'-a','testuser:fixture-pass@/:rw,/restricted:ro,/readonly.txt:ro','-a','reader:fixture-pass@/:ro','-a','scoped:fixture-pass@/:ro,/writable:rw,/writable.txt:rw','--log-format',''],stdout=subprocess.PIPE,stderr=subprocess.STDOUT) as server:
        try:
            for _ in range(60):
                if server.poll() is not None: raise RuntimeError(server.stdout.read().decode(errors='replace'))
                try:
                    with urllib.request.urlopen(host+'__dufs__/health',timeout=.3): break
                except OSError: time.sleep(.1)
            else: raise RuntimeError('Dufs health timed out')
            subprocess.run(cmd+[host,f'http://127.0.0.1:{redir.server_port}/',mode],check=True,timeout=45)
            assert (files/'readonly.txt').read_text()=='unchanged'
            assert (files/'writable.txt').read_text()=='keep' and (files/'writable').is_dir()
        finally: server.terminate()

with tempfile.TemporaryDirectory(prefix='dufs-test-') as tmp:
    d=Path(tmp); build=d/'classes'; build.mkdir(); code=root/'src/local/dufs/manage'
    cp=list(map(str,jars(args.deps.resolve(),test=True)))
    sources=[code/n for n in ('DufsClient.java','DufsDirectory.java','PermissionSnapshot.java','UiGeometry.java','Words.java','DownloadPlan.java','LocaleEast.java','LocaleRtl.java','LocaleAzBn.java')]
    subprocess.run(['javac','-encoding','UTF-8','-cp',os.pathsep.join(cp),'-d',str(build),*map(str,sources),str(root/'tests/ClientTest.java')],check=True)
    cmd=['java','-cp',os.pathsep.join([str(build)]+cp),'local.dufs.manage.ClientTest']
    subprocess.run(cmd,check=True)
    if args.dufs is not None:
        with http.server.HTTPServer(('127.0.0.1',0),Redirect) as redir:
            thread=threading.Thread(target=redir.serve_forever,daemon=True); thread.start()
            try:
                for variant in ('full','upload-only'): fixture(d,redir,variant)
            finally: redir.shutdown()

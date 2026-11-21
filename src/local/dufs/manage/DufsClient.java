package local.dufs.manage;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

final class DufsClient {
    static final String ZIP_MIME = "application/zip";
    static final class Result {
        final int code; final String body;
        Result(int code, String body) { this.code = code; this.body = body; }
    }
    static final class HttpError extends IOException {
        final int code;
        HttpError(int code) { super("HTTP " + code); this.code = code; }
    }
    private final String authorization;
    DufsClient(String user, String password) {
        if (user.indexOf(':') >= 0 || (user.isEmpty() && !password.isEmpty())) throw new IllegalArgumentException();
        authorization = user.isEmpty() ? null : "Basic " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
    }
    static URI address(String input) {
        try {
            String s = input.trim();
            URI u = new URI(s.contains("://") ? s : "http://" + s);
            String scheme = u.getScheme();
            if (!(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")) || u.getHost() == null || u.getPort() > 65535 || u.getPort() == 0 || u.getRawUserInfo() != null || u.getRawQuery() != null || u.getRawFragment() != null) throw new IllegalArgumentException();
            String path = u.getRawPath();
            StringBuilder result = new StringBuilder(scheme.toLowerCase(java.util.Locale.ROOT)).append("://").append(u.getRawAuthority()).append('/');
            for (String p : path.split("/")) if (!p.isEmpty()) result.append(segment(URLDecoder.decode(p.replace("+", "%2B"), "UTF-8"))).append('/');
            return URI.create(result.toString());
        } catch (Exception e) { throw new IllegalArgumentException("服务器地址无效"); }
    }
    static String segment(String name) {
        if (name == null || name.trim().isEmpty() || name.equals(".") || name.equals("..") || name.indexOf('/') >= 0 || name.indexOf('\\') >= 0) throw new IllegalArgumentException("无效文件名");
        for (int i = 0; i < name.length(); i++) if (Character.isISOControl(name.charAt(i))) throw new IllegalArgumentException("无效文件名");
        try { return URLEncoder.encode(name, "UTF-8").replace("+", "%20").replace("*", "%2A"); }
        catch (UnsupportedEncodingException impossible) { throw new AssertionError(impossible); }
    }
    static URI child(URI dir, String name, boolean folder) { return URI.create(dir.toASCIIString() + segment(name) + (folder ? "/" : "")); }
    static URI searchPath(URI base,String path,boolean folder) {
        String[] parts=path.split("/",-1);
        URI result=base;
        for(int i=0;i<parts.length;i++) result=child(result,parts[i],i<parts.length-1 || folder);
        return result;
    }
    static URI customFolder(URI root,String path) {
        String p=path.trim(); if(p.isEmpty() || p.equals("/")) return root;
        if(p.startsWith("/")) p=p.substring(1);
        if(p.endsWith("/")) p=p.substring(0,p.length()-1);
        if(p.isEmpty() || p.length()>1024) throw new IllegalArgumentException();
        return searchPath(root,p,true);
    }
    static String destinationText(URI root,URI target) {
        return "/"+target.getPath().substring(root.getPath().length());
    }
    static boolean moveFolderAllowed(URI here,URI destination,java.util.Set<String> excludedFolders) {
        String path=destination.getPath();
        return !path.equals(here.getPath()) && excludedFolders.stream().noneMatch(path::startsWith);
    }
    private HttpURLConnection request(URI uri, String method, int connect, int read) throws IOException {
        HttpURLConnection c = (HttpURLConnection) uri.toURL().openConnection(Proxy.NO_PROXY);
        c.setInstanceFollowRedirects(false); c.setUseCaches(false); c.setConnectTimeout(connect); c.setReadTimeout(read);
        c.setRequestMethod(method); if (authorization != null) c.setRequestProperty("Authorization", authorization);
        return c;
    }
    private static final class Dav {
        static final OkHttpClient HTTP = new OkHttpClient.Builder().proxy(Proxy.NO_PROXY)
                .followRedirects(false).followSslRedirects(false).retryOnConnectionFailure(false)
                .connectTimeout(5,TimeUnit.SECONDS).callTimeout(15,TimeUnit.SECONDS).build();
        static final OkHttpClient DISPLAY = HTTP.newBuilder().connectTimeout(3,TimeUnit.SECONDS)
                .callTimeout(7,TimeUnit.SECONDS).build();
        static final RequestBody EMPTY = RequestBody.create(new byte[0],null);
    }
    private int dav(URI path, String method, URI dest) throws IOException {
        Request.Builder b = new Request.Builder().url(path.toASCIIString()).method(method, "PROPPATCH".equals(method) ? Dav.EMPTY : null);
        if (authorization != null) b.header("Authorization", authorization);
        if (dest != null) b.header("Destination",dest.toASCIIString()).header("Overwrite","F");
        try (Response r = Dav.HTTP.newCall(b.build()).execute()) { return r.code(); }
    }
    // Legacy Dufs rejects write methods before dispatch. An empty PROPPATCH on
    // an existing file returns a synthetic 207 and never sets any properties.
    boolean legacyFileWriteAccess(URI path) throws IOException { return dav(path,"PROPPATCH",null) == 207; }
    // Dufs checks file ACL and both global MOVE switches before reading the
    // Destination header. Without that required header it cannot rename anything.
    boolean legacyFileMoveAccess(URI path) throws IOException {
        Request.Builder b=new Request.Builder().url(path.toASCIIString()).method("MOVE",null);
        if(authorization!=null) b.header("Authorization",authorization);
        try (Response r=Dav.HTTP.newCall(b.build()).execute()) {
            return r.code()==400 && r.body()!=null && "Invalid Destination".equals(text(r.body().byteStream(),256).trim());
        }
    }
    void mkdir(URI path) throws IOException { success(dav(path,"MKCOL",null)); }
    void delete(URI path) throws IOException { success(dav(path,"DELETE",null)); }
    void move(URI src, URI dest) throws IOException {
        if (!src.getScheme().equals(dest.getScheme()) || !src.getRawAuthority().equals(dest.getRawAuthority()) || src.equals(dest)
                || (src.getRawPath().endsWith("/") && dest.getRawPath().startsWith(src.getRawPath()))) throw new IllegalArgumentException("目标路径无效");
        // Old Dufs ignores Overwrite: F on MOVE. Avoid known collisions even there.
        String address=dest.toASCIIString();
        // A trailing slash can make Dufs return 200 for a virtual, missing directory.
        int existing = status(dest.getRawPath().endsWith("/") ? URI.create(address.substring(0,address.length()-1)) : dest);
        if ((existing>=200 && existing<300) || existing==301 || existing==308) throw new HttpError(412);
        if (existing!=404) throw new HttpError(existing);
        success(dav(src,"MOVE",dest));
    }
    private static void success(int code) throws HttpError { if (code < 200 || code >= 300 || code==207) throw new HttpError(code); }
    Result listForDisplay(URI dir) throws IOException {
        return getJson(dir,"json");
    }
    Result listAfterUpload(URI dir) throws IOException {
        try {
            Result r=listForDisplay(dir);
            if(r.code==200) {
                try { if(!Boolean.FALSE.equals(new JSONObject(r.body).opt("dir_exists"))) return r; }
                catch(JSONException e) { return r; }
            }
            if(r.code!=200 && r.code!=404 && r.code!=502 && r.code!=503 && r.code!=504) return r;
        } catch(SocketException | InterruptedIOException | EOFException e) {
            if(Thread.currentThread().isInterrupted()) throw e;
        }
        try { Thread.sleep(200); }
        catch(InterruptedException e) { Thread.currentThread().interrupt(); throw new InterruptedIOException(); }
        return listForDisplay(dir);
    }
    Result list(URI dir) throws IOException {
        return getJson(dir,"json");
    }
    Result search(URI dir,String query) throws IOException {
        String q=query.trim(); if(q.isEmpty()) throw new IllegalArgumentException();
        return getJson(dir,"json&q="+URLEncoder.encode(q,"UTF-8"));
    }
    private Result getJson(URI dir,String query) throws IOException {
        Request.Builder b=new Request.Builder().url(dir.toASCIIString()+"?"+query).get();
        if(authorization!=null) b.header("Authorization",authorization);
        try (Response r=Dav.DISPLAY.newCall(b.build()).execute()) {
            int code=r.code();
            if (code != 200) return new Result(code, "");
            if(r.body()==null) throw new EOFException();
            return new Result(code,text(r.body().byteStream(),12 * 1024 * 1024));
        }
    }
    int status(URI path) throws IOException {
        HttpURLConnection c = request(path, "HEAD", 4500, 12000);
        try { return c.getResponseCode(); } finally { c.disconnect(); }
    }
    void upload(URI path, InputStream in, long size) throws IOException {
        HttpURLConnection c = request(path, "PUT", 6000, 60000);
        try {
            c.setDoOutput(true); c.setRequestProperty("Content-Type", "application/octet-stream");
            c.setRequestProperty("If-None-Match", "*");
            if (size >= 0) c.setFixedLengthStreamingMode(size); else c.setChunkedStreamingMode(65536);
            try (OutputStream out = c.getOutputStream()) { copy(in, out); }
            int code = c.getResponseCode(); if (code < 200 || code >= 300) throw new HttpError(code);
        } finally { c.disconnect(); }
    }
    void download(URI path, OutputStream out) throws IOException {
        download(path,out,null);
    }
    void downloadArchive(URI dir, OutputStream out) throws IOException {
        if (dir.getRawQuery()!=null || dir.getRawFragment()!=null || !dir.getRawPath().endsWith("/")) throw new IllegalArgumentException();
        download(URI.create(dir.toASCIIString()+"?zip"),out,ZIP_MIME);
    }
    private void download(URI path, OutputStream out, String expectedMime) throws IOException {
        HttpURLConnection c = request(path, "GET", 6000, 60000);
        try {
            int code = c.getResponseCode(); if (code != 200) throw new HttpError(code);
            if (expectedMime!=null) {
                String type=c.getContentType();
                // A virtual directory or index can return 200 without producing an archive.
                if(type==null || !expectedMime.equalsIgnoreCase(type.split(";",2)[0].trim())) throw new HttpError(415);
            }
            try (InputStream in = c.getInputStream()) { copy(in, out); }
        } finally { c.disconnect(); }
    }
    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[65536]; for (int n; (n = in.read(buffer)) != -1;) out.write(buffer, 0, n);
    }
    private static String text(InputStream in, int limit) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(); byte[] buf = new byte[8192];
        for (int n; (n = in.read(buf)) != -1;) { if (out.size() + n > limit) throw new IOException("服务器响应过大"); out.write(buf, 0, n); }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
}

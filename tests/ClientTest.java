package local.dufs.manage;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class ClientTest {
    static void check(boolean ok, String label) { if (!ok) throw new AssertionError(label); }
    public static void main(String[] args) throws Exception {
        check(UiGeometry.popupY(20,780,712,752,150,4)==558,"bottom popup opens above its anchor");
        check(UiGeometry.popupY(20,780,40,80,150,4)==84,"top popup opens below its anchor");
        check(UiGeometry.popupX(5,400,420,156)==244,"popup right edge remains within the viewport");
        String preview=UiGeometry.shareName(String.join("",java.util.Collections.nCopies(40,"🌕"))+".jpg",3);
        check(preview.startsWith("🌕") && preview.contains("…") && preview.endsWith(".jpg 等 3 个文件"),"preview truncates on code-point boundaries and preserves count");
        URI base = DufsClient.address("192.168.99.1:5000/share");
        check(Words.codes().length==11 && Words.labels().length==11 && Words.choose("system","zh-CN").equals("zh-CN") && Words.choose("auto","zh-Hant-HK").equals("zh-TW") && Words.choose("auto","vi-VN").equals("vi") && Words.choose("system","fr").equals("en") && Words.choose("en","zh").equals("en") && Words.choose("auto","bqi-IR").equals("bqi-IR"),"11 choices, system locale fallback and legacy preference migration");
        for(String lang:Words.codes()) if(!lang.equals("auto") && !lang.equals("en") && !lang.equals("zh-CN")) {
            for(String key:new String[]{"连接设置","密码","服务器地址","测试连接","显示密码","账号（可选）","账号说明（可留空）","根目录","正在下载…"})
                check(!Words.text(lang,key).equals(Words.text(false,key)),"localized "+lang+" / "+key);
        }
        DufsDirectory order=DufsDirectory.parse("{\"kind\":\"Index\",\"dir_exists\":true,\"paths\":[{\"name\":\"b\",\"path_type\":\"File\",\"size\":12,\"mtime\":20},{\"name\":\"a\",\"path_type\":\"File\",\"size\":50,\"mtime\":10}]}" );
        check(!order.canArchive,"missing archive flag is denied");
        for (String flags : new String[]{"\"dir_exists\":true,\"allow_archive\":\"true\"", "\"dir_exists\":false,\"allow_archive\":true"})
            check(!DufsDirectory.parse("{\"kind\":\"Index\",\"paths\":[],"+flags+"}").canArchive,"archive requires an existing directory and a boolean capability");
        check(order.sorted(0,false).get(0).name.equals("a") && order.sorted(1,true).get(0).name.equals("b") && order.sorted(2,true).get(0).name.equals("a") && order.sorted(2,false).get(0).name.equals("b"),"independent name/time/size sort directions");
        URI batchDir=DufsClient.child(base,"批量",true);
        DownloadPlan saved=DownloadPlan.decode(DownloadPlan.capture(base,batchDir,"alice",order.sorted(0,true)).encode());
        check(saved.matches(base,"alice") && !saved.matches(base,"bob") && saved.base.equals(batchDir) && saved.resolve(order).get(0).name.equals("b"),"picker recreation restores exact account, folder and selected order");
        try {saved.resolve(DufsDirectory.parse("{\"kind\":\"Index\",\"dir_exists\":true,\"paths\":[{\"name\":\"b\",\"path_type\":\"File\",\"mtime\":21}]}")); throw new AssertionError("replacement must not be downloaded");}
        catch(DufsClient.HttpError e) {check(e.code==412,"changed item requires new selection");}
        check(DufsClient.customFolder(base,"/自定义/下一级/").toASCIIString().startsWith(base.toASCIIString()),"custom folder preserves server prefix");
        try {DufsClient.customFolder(base,"../other");throw new AssertionError("custom folder escaped root");} catch(IllegalArgumentException expected) {}
        URI typed=DufsClient.customFolder(base,"/ 自定义/plus+#%20 /");
        check(DufsClient.customFolder(base,DufsClient.destinationText(base,typed)).equals(typed),"prefill round-trips reserved characters and spaces under the server prefix");
        check(DufsClient.destinationText(base,base).equals("/") && DufsClient.customFolder(base,"").equals(base),"root default and empty input");
        URI sourceFolder=DufsClient.child(base,"reports",true);
        check(!DufsClient.moveFolderAllowed(base,base,java.util.Collections.emptySet()),"same parent is not a move destination");
        java.util.Set<String> excluded=java.util.Collections.singleton(sourceFolder.getPath());
        check(!DufsClient.moveFolderAllowed(base,sourceFolder,excluded),"cannot move into self");
        check(!DufsClient.moveFolderAllowed(base,DufsClient.child(sourceFolder,"nested",true),excluded),"cannot move into a descendant");
        check(DufsClient.moveFolderAllowed(base,DufsClient.customFolder(base,"reports-old"),excluded),"similarly named sibling is allowed");
        DufsDirectory missingWritable=DufsDirectory.parse("{\"kind\":\"Index\",\"dir_exists\":false,\"allow_upload\":true,\"paths\":[]}");
        DufsDirectory existingReadOnly=DufsDirectory.parse("{\"kind\":\"Index\",\"dir_exists\":true,\"allow_upload\":false,\"paths\":[]}");
        check(missingWritable.canUploadHere() && !missingWritable.canCreate(),"server-authorized missing folder permits upload but not move");
        check(!existingReadOnly.canUploadHere() && !existingReadOnly.canCreate(),"existing read-only folder permits neither operation");
        URI visible=DufsClient.child(base,"浏览目录",true), selected=DufsClient.child(base,"上传目录",true);
        check(UiGeometry.shareTarget(base,visible,selected.toASCIIString()).equals(selected),"restored share target takes precedence over main directory");
        check(UiGeometry.shareTarget(base,visible,"http://192.168.99.1:5000/share/%2e%2e/").equals(visible),"reject malformed restored target");
        check(UiGeometry.shareTarget(base,visible,"http://192.168.99.1:5000/share-other/").equals(visible),"reject sibling root as restored target");
        check(base.toString().equals("http://192.168.99.1:5000/share/"), "normalize LAN address");
        check(DufsClient.child(base,"中 文+#%.jpg",false).toASCIIString().equals("http://192.168.99.1:5000/share/%E4%B8%AD%20%E6%96%87%2B%23%25.jpg"), "encode filename exactly once");
        for (String bad : new String[]{"..", "a/b", "a\\b", "a\n"}) {
            try { DufsClient.child(base,bad,false); throw new AssertionError("invalid name accepted"); } catch (IllegalArgumentException expected) {}
        }
        for (String bad : new String[]{"http://user:password@192.168.99.1", "ftp://192.168.99.1", "http://192.168.99.1/?x", "http://192.168.99.1/a/%2e%2e/", "http://192.168.99.1/a%2Fb"}) {
            try { DufsClient.address(bad); throw new AssertionError("invalid address accepted"); } catch (IllegalArgumentException expected) {}
        }
        testSelectionRange();
        UiGeometry.StatusFit oneLine=UiGeometry.fitStatus(13,9,0.5f,(size,compact)->!compact);
        UiGeometry.StatusFit threeLines=UiGeometry.fitStatus(13,9,0.5f,(size,compact)->compact);
        UiGeometry.StatusFit reduced=UiGeometry.fitStatus(13,9,0.5f,(size,compact)->compact && size<=11.5f);
        UiGeometry.StatusFit unfit=UiGeometry.fitStatus(13,9,0.5f,(size,compact)->false);
        check(oneLine.size==13 && !oneLine.compact && oneLine.complete,"short status keeps original metrics");
        check(threeLines.size==13 && threeLines.compact && threeLines.complete,"try compact three lines before shrinking");
        check(reduced.size==11.5f && reduced.compact && reduced.complete,"choose first fitting reduced size");
        check(unfit.size==9 && unfit.compact && !unfit.complete,"keep minimum size and flag unusually long text");
        long instant=java.time.Instant.parse("2025-12-31T23:05:00Z").toEpochMilli();
        java.time.ZoneId zone=java.time.ZoneId.of("Asia/Shanghai");
        java.util.Locale beforeLocale=java.util.Locale.getDefault();
        try {
            java.util.Locale.setDefault(java.util.Locale.SIMPLIFIED_CHINESE);
            String zhDate=UiGeometry.modifiedLabel(instant,zone,"zh-CN");
            String ruDate=UiGeometry.modifiedLabel(instant,zone,"ru");
            String arDate=UiGeometry.modifiedLabel(instant,zone,"ar");
            check(!zhDate.equals(ruDate) && !zhDate.equals(arDate) && !ruDate.equals(arDate),"explicit languages choose their own date format");
            check(!UiGeometry.modifiedLabel(instant,zone,"bqi-IR").equals(zhDate) && UiGeometry.modifiedLabel(instant,zone,"bqi-IR").equals(UiGeometry.modifiedLabel(instant,zone,"fa-IR")),"Bakhtiari uses explicit regional Persian date conventions rather than system Chinese");
            check(!ruDate.equals(UiGeometry.modifiedLabel(instant,java.time.ZoneOffset.UTC,"ru")) && UiGeometry.modifiedLabel(0,zone,"ar").equals("—"),"local timezone and missing date");
        } finally {java.util.Locale.setDefault(beforeLocale);}
        DufsDirectory explicit=DufsDirectory.parse("{\"kind\":\"Index\",\"dir_exists\":true,\"allow_upload\":true,\"allow_delete\":true,\"paths\":["
            + "{\"name\":\"protected\",\"path_type\":\"Dir\",\"allow_upload\":true,\"allow_delete\":true,\"readonly_descendant\":true},"
            + "{\"name\":\"read-only\",\"path_type\":\"File\",\"allow_upload\":false,\"allow_delete\":false}]}" );
        check(!explicit.mayShowActions(explicit.find("protected")), "new Dufs restricted descendants suppress actions");
        check(!explicit.mayShowActions(explicit.find("read-only")), "new Dufs per-file permission overrides parent");
        DufsDirectory fixture=DufsDirectory.parse("{\"kind\":\"Index\",\"dir_exists\":true,\"paths\":["
            +"{\"name\":\"slow\",\"path_type\":\"File\"},{\"name\":\"fast\",\"path_type\":\"File\"},"
            +"{\"name\":\"explicit\",\"path_type\":\"File\",\"allow_delete\":false}]}" );
        java.util.concurrent.ExecutorService pool=java.util.concurrent.Executors.newFixedThreadPool(2);
        try {
            java.util.concurrent.CountDownLatch fast=new java.util.concurrent.CountDownLatch(1);
            PermissionSnapshot before=PermissionSnapshot.capture(fixture,pool,2,1500,item->{
                if(item.name.equals("explicit")) throw new AssertionError("explicit metadata must not trigger a follow-up");
                if(item.name.equals("slow")) { fast.await(); new java.util.concurrent.CountDownLatch(1).await(); }
                fast.countDown(); return new DufsDirectory.Actions(true,true);
            });
            check(before.actions(fixture.find("fast")).move && !before.actions(fixture.find("slow")).any() && before.incomplete==1,"parallel preload preserves fast result and hides timed-out result");
            PermissionSnapshot after=PermissionSnapshot.capture(fixture,pool,2,1500,item->DufsDirectory.Actions.NONE);
            check(!after.actions(fixture.find("fast")).any() && after.incomplete==0 && before.actions(fixture.find("fast")).move,"new refresh snapshot replaces prior permission without mutating it");
            java.util.concurrent.atomic.AtomicInteger selectedRequests=new java.util.concurrent.atomic.AtomicInteger();
            DufsDirectory.Item selectedEntry=fixture.find("fast"), skippedEntry=fixture.find("slow");
            PermissionSnapshot selectedOnly=PermissionSnapshot.captureSelected(fixture,java.util.Collections.singletonList(selectedEntry),pool,2,500,i->{
                check(i==selectedEntry,"unselected item required no request"); selectedRequests.incrementAndGet(); return new DufsDirectory.Actions(true,true);
            });
            check(selectedRequests.get()==1 && selectedOnly.observed(selectedEntry).move && selectedOnly.observed(skippedEntry)==null,"only expressly selected legacy entries are probed");
            java.util.concurrent.CountDownLatch blockedProbe=new java.util.concurrent.CountDownLatch(1);
            java.util.List<DufsDirectory.Item> wave=new java.util.ArrayList<>(java.util.Arrays.asList(skippedEntry,selectedEntry));
            PermissionSnapshot timed=PermissionSnapshot.captureSelected(fixture,wave,pool,1,600,i->{
                blockedProbe.countDown(); new java.util.concurrent.CountDownLatch(1).await(); return DufsDirectory.Actions.NONE;
            });
            check(blockedProbe.getCount()==0 && timed.started.contains(skippedEntry) && !timed.started.contains(selectedEntry),"budget expiry records only the probe that actually started");
            wave.removeIf(timed.started::contains);
            PermissionSnapshot later=PermissionSnapshot.captureSelected(fixture,wave,pool,1,1000,i->new DufsDirectory.Actions(true,true));
            check(later.actions(selectedEntry).move && !later.actions(skippedEntry).any(),"unstarted selection remains eligible next wave, timed-out entry grants nothing");
            PermissionSnapshot.ProbeWave oldWave=new PermissionSnapshot.ProbeWave();
            check(oldWave.claim(skippedEntry),"first worker admitted before deadline");
            PermissionSnapshot sealed=oldWave.seal(fixture);
            check(!oldWave.claim(selectedEntry),"old worker stalled before claim must not start after expiry");
            oldWave.record(skippedEntry,new DufsDirectory.Actions(true,true));
            check(sealed.started.contains(skippedEntry) && !sealed.started.contains(selectedEntry)
                && !sealed.actions(skippedEntry).any(),"seal atomically freezes claims and results; late success grants nothing");
        } finally { pool.shutdownNow(); }
        if (args.length == 0) { System.out.println("PASS URL, filename, bounded preload and refreshed permissions"); return; }
        DufsClient client = new DufsClient("testuser", "fixture-pass");
        URI endpoint = DufsClient.address(args[0]);
        check(client.list(endpoint).code == 200, "Dufs listing reachable");
        check(new DufsClient("testuser", "wrong").list(endpoint).code == 401, "wrong password");
        byte[] a = "file one 内容".getBytes(StandardCharsets.UTF_8), b = "second file".getBytes(StandardCharsets.UTF_8);
        URI first = DufsClient.child(endpoint,"一 #.txt",false), second = DufsClient.child(endpoint,"二 +%.txt",false);
        check(client.status(first) == 404, "initially missing");
        client.upload(first,new ByteArrayInputStream(a),a.length);
        client.upload(second,new ByteArrayInputStream(b),-1);
        client.upload(DufsClient.child(endpoint,"empty.txt",false),new ByteArrayInputStream(new byte[0]),0);
        check(client.status(first) == 200, "existing file recognized");
        DufsClient.Result list = client.list(endpoint);
        check(list.code == 200 && list.body.contains("一 #.txt") && list.body.contains("二 +%.txt"), "authenticated directory listing");
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        client.download(first,data); check(java.util.Arrays.equals(a,data.toByteArray()), "fixed-length upload matches downloaded bytes");
        data.reset(); client.download(second,data); check(java.util.Arrays.equals(b,data.toByteArray()), "chunked upload matches downloaded bytes");
        check(new DufsClient("testuser", "fixture-pass").list(endpoint).code == 200, "fresh client reconnects with same credentials");
        DufsDirectory dir = DufsDirectory.fetch(client,endpoint);
        URI custom=DufsClient.customFolder(endpoint,"自动创建/二级");
        DufsDirectory virtual=DufsDirectory.from(client.listForDisplay(custom));
        check(!virtual.exists && virtual.canUploadHere(),"missing writable directory is a Dufs virtual listing");
        URI customFile=DufsClient.child(custom,"证明.txt",false);
        client.upload(customFile,new ByteArrayInputStream(a),a.length);
        check(DufsDirectory.fetch(client,custom).exists && client.status(customFile)==200,"real PUT creates missing parents before write");
        DufsClient reader = new DufsClient("reader", "fixture-pass");
        DufsDirectory ro = DufsDirectory.fetch(reader,endpoint);
        check(!ro.canCreate() && !ro.canDelete() && !ro.canMove(), "read-only account has no write actions");
        DufsClient scoped=new DufsClient("scoped","fixture-pass");
        DufsDirectory scopedRoot=DufsDirectory.fetch(scoped,endpoint);
        boolean full=args.length>2 && args[2].equals("full");
        check(dir.canArchive==full && ro.canArchive==full && !virtual.canArchive,"ZIP uses the archive flag, not write permission, and hides for virtual folders");
        URI archiveDir=DufsClient.child(endpoint,"目录 +#%",true);
        client.mkdir(archiveDir);
        client.upload(DufsClient.child(archiveDir,"内容 +#.txt",false),new ByteArrayInputStream(a),a.length);
        ByteArrayOutputStream zipBytes=new ByteArrayOutputStream();
        if(full) {
            reader.downloadArchive(archiveDir,zipBytes);
            java.nio.file.Path archive=java.nio.file.Files.createTempFile("dufs-native-",".zip");
            try {
                java.nio.file.Files.write(archive,zipBytes.toByteArray());
                try (java.util.zip.ZipFile zip=new java.util.zip.ZipFile(archive.toFile())) {
                    java.util.zip.ZipEntry entry=zip.getEntry("内容 +#.txt");
                    check(entry!=null && zip.size()==1,"native ZIP contains the exact fixture entry");
                    try (InputStream in=zip.getInputStream(entry)) {check(java.util.Arrays.equals(a,in.readAllBytes()),"native ZIP preserves fixture bytes");}
                }
            } finally {java.nio.file.Files.delete(archive);}
            try {new DufsClient("reader","wrong").downloadArchive(archiveDir, new ByteArrayOutputStream()); throw new AssertionError("ZIP authentication required");}
            catch(DufsClient.HttpError e) {check(e.code==401,"ZIP rejects wrong credentials");}
        } else {
            try {reader.downloadArchive(archiveDir,zipBytes); throw new AssertionError("disabled archive accepted");}
            catch(DufsClient.HttpError e) {check(e.code==404 && zipBytes.size()==0,"disabled ZIP streams no bytes");}
        }
        check(dir.canSearch==full,"menu search uses server capability");
        if(full) {
            DufsDirectory results=DufsDirectory.search(client,endpoint,"查找 +#");
            check(results.items.size()==1 && results.find("writable/查找 +#.txt")!=null,"search yields one nested path and encodes reserved characters");
            ByteArrayOutputStream found=new ByteArrayOutputStream();
            client.download(DufsClient.searchPath(endpoint,results.items.get(0).name,false),found);
            check(found.toString("UTF-8").equals("search fixture"),"search result opened at correct path");
        } else try { DufsDirectory.search(client,endpoint,"查找"); throw new AssertionError("search disabled"); }
        catch(DufsClient.HttpError e) {check(e.code==403,"disabled search cannot masquerade as matches");}
        try { DufsClient.searchPath(endpoint,"writable/../outside",false); throw new AssertionError("unsafe search path"); }
        catch(IllegalArgumentException expected) {}
        check(!scopedRoot.canDelete(),"scoped user's parent directory stays readonly");
        java.util.concurrent.ExecutorService actualPool=java.util.concurrent.Executors.newFixedThreadPool(6);
        try {
            PermissionSnapshot preloaded=PermissionSnapshot.capture(scopedRoot,actualPool,6,10000,item->scopedRoot.itemActions(scoped,endpoint,item));
            PermissionSnapshot readOnly=PermissionSnapshot.capture(ro,actualPool,6,10000,item->ro.itemActions(reader,endpoint,item));
            check(preloaded.incomplete==0 && readOnly.incomplete==0,"real Dufs preload finished");
            check(preloaded.actions(scopedRoot.find("writable.txt")).any()==full && !readOnly.actions(ro.find("writable.txt")).any(),"preloaded menus are scoped by account and global switches");
        } finally { actualPool.shutdownNow(); }
        for (String name : new String[]{"writable", "writable.txt"}) {
            DufsDirectory.Item item=scopedRoot.find(name);
            check(scopedRoot.mayShowActions(item),"entry-level permission can be queried despite readonly parent: "+name);
            DufsDirectory.Actions actual=scopedRoot.itemActions(scoped,endpoint,item);
            check(actual.delete==full && actual.move==full,"actual child rights under readonly parent: "+name);
            check(!ro.itemActions(reader,endpoint,ro.find(name)).any(),"readonly user can't manage scoped child: "+name);
        }
        check(dir.canCreate(), "write account can create directory");
        URI folder = DufsClient.child(endpoint,"新 文件夹",true);
        client.mkdir(folder);
        check(DufsDirectory.fetch(client,folder).exists, "created directory read back");
        URI restricted = DufsClient.child(endpoint,"restricted",true);
        check(!DufsDirectory.fetch(client,restricted).canCreate(), "nested readonly directory");
        check(!dir.itemActions(client, endpoint, dir.find("readonly.txt")).delete, "nested readonly file button hidden");
        if (args.length > 2 && args[2].equals("full")) {
            check(dir.canMove() && dir.canDelete(), "account can move and delete with global flags");
            check(dir.itemActions(client, endpoint, dir.find("一 #.txt")).move, "write file move allowed");
            URI renamed = DufsClient.child(endpoint,"改名 %.txt",false);
            client.move(first,renamed);
            check(client.status(first)==404 && client.status(renamed)==200, "rename matches server state");
            try { client.move(renamed,second); throw new AssertionError("collision accepted"); } catch (DufsClient.HttpError e) { check(e.code==412,"collision rejected"); }
            check(client.status(renamed)==200 && client.status(second)==200, "both files survived collision");
            URI moved = DufsClient.child(folder,"改名 %.txt",false);
            client.move(renamed,moved);
            check(client.status(renamed)==404 && client.status(moved)==200, "move matches server state");
            client.delete(moved); check(client.status(moved)==404, "delete file read back");
            URI newFolder=DufsClient.child(endpoint,"已改名的文件夹",true);
            client.move(folder,newFolder);
            check(!DufsDirectory.fetch(client,folder).exists && DufsDirectory.fetch(client,newFolder).exists,"rename folder read back");
            client.delete(newFolder); check(!DufsDirectory.fetch(client,newFolder).exists, "delete folder read back (Dufs returns a virtual listing for a missing upload directory)");
            try { reader.delete(second); throw new AssertionError("reader deleted file"); } catch (DufsClient.HttpError e) { check(e.code==403,"read-only delete rejected"); }
            check(client.status(second)==200, "reader left file intact");
        } else check(!dir.canMove() && !dir.canDelete(), "upload-only server hides delete/move/rename");
        if (args.length > 1) {
            URI mock=DufsClient.address(args[1]);
            check(client.listForDisplay(mock).code == 302,"redirect not followed");
            ByteArrayOutputStream rejected=new ByteArrayOutputStream();
            try {client.downloadArchive(mock.resolve("not-zip/"),rejected); throw new AssertionError("HTML is not a ZIP");}
            catch(DufsClient.HttpError e) {check(e.code==415 && rejected.size()==0,"non-ZIP 200 body is not written");}
            check(client.listForDisplay(mock.resolve("retry/")).code==503,"ordinary browsing returns the first failure without waiting for retry");
            check(client.listForDisplay(mock.resolve("retry/")).code==200,"explicit refresh still succeeds after a transient failure");
            check(client.listAfterUpload(mock.resolve("post-upload/")).code==200,"post-upload GET retries a transient failure once");
            check(client.listForDisplay(mock.resolve("denied/")).code==403,"auth error remains 403");
        }
        System.out.println("PASS real Dufs auth/upload/download/native ZIP/redirect and filesystem permission checks ("+args[2]+")");
    }
    private static void testSelectionRange() {
        java.util.List<String> rows=java.util.Arrays.asList("a","b","c","d","e");
        java.util.Set<String> picked=new java.util.HashSet<>(java.util.Arrays.asList("d"));
        java.util.function.BiConsumer<String,Boolean> toggle=(k,on)->{if(on) picked.add(k); else picked.remove(k);};
        UiGeometry.SelectionRange range=new UiGeometry.SelectionRange(rows,picked,"b",toggle);
        range.moveTo("e",toggle);
        check(picked.equals(new java.util.HashSet<>(java.util.Arrays.asList("b","c","d","e"))),"fast drag includes skipped rows");
        range.moveTo("a",toggle);
        check(picked.equals(new java.util.HashSet<>(java.util.Arrays.asList("a","b","d"))),"reversing restores original checked and unchecked items");
        UiGeometry.SelectionRange deselect=new UiGeometry.SelectionRange(rows,picked,"b",toggle);
        deselect.moveTo("d",toggle); deselect.moveTo("c",toggle);
        check(picked.equals(new java.util.HashSet<>(java.util.Arrays.asList("a","d"))),"reversing a deselect stroke restores the preceding check");
    }
}

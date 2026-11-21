package local.dufs.manage;

import org.json.*;
import java.net.URI;
import java.util.*;
import java.io.IOException;

final class DufsDirectory {
    static final class Actions {
        static final Actions NONE = new Actions(false,false);
        final boolean delete, move;
        Actions(boolean delete, boolean move) { this.delete=delete; this.move=move; }
        boolean any() { return delete || move; }
    }
    static final class Item {
        final String name; final boolean folder,symlink; final long bytes,modified;
        final Boolean upload, delete;
        final boolean restricted;
        Item(JSONObject j, boolean folder) throws JSONException {
            name=j.getString("name"); bytes=j.optLong("size",0); modified=j.optLong("mtime",0); this.folder=folder;
            symlink=j.optString("path_type").startsWith("Symlink");
            upload=flag(j,"allow_upload"); delete=flag(j,"allow_delete");
            restricted=Boolean.TRUE.equals(flag(j,"readonly_descendant"));
        }
        Actions explicit() { return restricted || Boolean.FALSE.equals(delete) ? Actions.NONE : delete==null || upload==null ? null :
            new Actions(delete,delete && upload); }
    }
    final boolean exists, canSearch, canArchive;
    private final boolean upload, delete;
    final List<Item> items;
    private DufsDirectory(JSONObject j,URI searchBase) throws JSONException {
        if (!"Index".equals(j.getString("kind"))) throw new JSONException("Not a Dufs directory");
        exists=Boolean.TRUE.equals(flag(j,"dir_exists"));
        canSearch=exists && Boolean.TRUE.equals(flag(j,"allow_search"));
        canArchive=exists && Boolean.TRUE.equals(flag(j,"allow_archive"));
        upload=Boolean.TRUE.equals(flag(j,"allow_upload")); delete=Boolean.TRUE.equals(flag(j,"allow_delete"));
        JSONArray arr=j.getJSONArray("paths"); ArrayList<Item> list=new ArrayList<>();
        for (int i=0;i<arr.length();i++) {
            JSONObject v=arr.getJSONObject(i); String type=v.getString("path_type");
            try { if(searchBase==null) DufsClient.segment(v.getString("name")); else DufsClient.searchPath(searchBase,v.getString("name"),false); }
            catch (IllegalArgumentException e) { continue; }
            if (type.equals("Dir") || type.equals("SymlinkDir")) list.add(new Item(v,true));
            else if (type.equals("File") || type.equals("SymlinkFile")) list.add(new Item(v,false));
        }
        list.sort((a,b)->a.folder==b.folder ? a.name.compareToIgnoreCase(b.name) : a.folder ? -1 : 1);
        items=Collections.unmodifiableList(list);
    }
    private static Boolean flag(JSONObject j,String key) { Object o=j.opt(key); return o instanceof Boolean?(Boolean)o:null; }
    static DufsDirectory parse(String json) throws JSONException { return new DufsDirectory(new JSONObject(json),null); }
    static DufsDirectory search(DufsClient client,URI base,String query) throws IOException,JSONException {
        DufsClient.Result r=client.search(base,query);
        if(r.code!=200) throw new DufsClient.HttpError(r.code);
        DufsDirectory data=new DufsDirectory(new JSONObject(r.body),base);
        if(!data.canSearch) throw new DufsClient.HttpError(403);
        return data;
    }
    static DufsDirectory fetch(DufsClient client,URI path) throws IOException, JSONException {
        return from(client.list(path));
    }
    static DufsDirectory from(DufsClient.Result r) throws IOException, JSONException {
        if (r.code!=200) throw new DufsClient.HttpError(r.code);
        return parse(r.body);
    }
    boolean canCreate() { return exists && upload; }
    boolean canUploadHere() { return upload; }
    List<Item> sorted(int field,boolean descending) {
        ArrayList<Item> copy=new ArrayList<>(items);
        Comparator<Item> cmp=field==1?Comparator.comparingLong(i->i.modified):field==2?Comparator.comparingLong(i->i.bytes):Comparator.comparing(i->i.name,String.CASE_INSENSITIVE_ORDER);
        cmp=cmp.thenComparing(i->i.name,String.CASE_INSENSITIVE_ORDER).thenComparing(i->i.name);
        if(descending) cmp=cmp.reversed();
        copy.sort(Comparator.<Item,Boolean>comparing(i->!i.folder).thenComparing(cmp)); return copy;
    }
    boolean canDelete() { return exists && delete; }
    boolean canMove() { return exists && upload && delete; }
    Item find(String name) { for (Item i : items) if(i.name.equals(name)) return i; return null; }
    boolean mayShowActions(Item item) {
        Actions explicit=item.explicit(); return explicit==null || explicit.any();
    }
    Actions itemActions(DufsClient c,URI parent,Item item) throws Exception {
        if(!exists || item==null) return Actions.NONE;
        Actions explicit=item.explicit(); if(explicit!=null) return explicit;
        URI resource=DufsClient.child(parent,item.name,item.folder);
        if(item.folder) {
            DufsDirectory own=fetch(c,resource);
            return new Actions(own.canDelete(),own.canMove());
        }
        if(canDelete()) return c.legacyFileWriteAccess(resource) ? new Actions(true,canMove()) : Actions.NONE;
        return c.legacyFileMoveAccess(resource) ? new Actions(true,true) : Actions.NONE;
    }
}

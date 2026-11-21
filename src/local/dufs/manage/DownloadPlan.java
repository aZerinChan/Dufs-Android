package local.dufs.manage;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import org.json.*;

/** Saved references only: no account password, open stream or network transaction. */
final class DownloadPlan {
    final URI root, base;
    final String user;
    private final JSONArray refs;
    private DownloadPlan(URI root,URI base,String user,JSONArray refs) {
        this.root=root; this.base=base; this.user=user; this.refs=refs;
        if(!base.toASCIIString().startsWith(root.toASCIIString()) || refs.length()==0 || refs.length()>500) throw new IllegalArgumentException();
    }
    static DownloadPlan capture(URI root,URI base,String user,List<DufsDirectory.Item> items) throws JSONException {
        JSONArray names=new JSONArray();
        for(DufsDirectory.Item item:items) names.put(new JSONObject().put("name",item.name).put("folder",item.folder).put("modified",item.modified));
        return new DownloadPlan(root,base,user,names);
    }
    String encode() throws JSONException {return new JSONObject().put("root",root.toASCIIString()).put("base",base.toASCIIString()).put("user",user).put("refs",refs).toString();}
    static DownloadPlan decode(String text) throws JSONException {
        JSONObject o=new JSONObject(text);
        return new DownloadPlan(DufsClient.address(o.getString("root")),DufsClient.address(o.getString("base")),o.getString("user"),o.getJSONArray("refs"));
    }
    boolean matches(URI r,String username) {return root.equals(r)&&user.equals(username);}
    List<DufsDirectory.Item> resolve(DufsDirectory current) throws JSONException,IOException {
        List<DufsDirectory.Item> found=new ArrayList<>();
        for(int i=0;i<refs.length();i++) {
            JSONObject ref=refs.getJSONObject(i); DufsDirectory.Item item=current.find(ref.getString("name"));
            long modified=ref.getLong("modified");
            if(item==null || item.folder!=ref.getBoolean("folder") || (modified>0 && modified!=item.modified)) throw new DufsClient.HttpError(412);
            found.add(item);
        }
        return found;
    }
}

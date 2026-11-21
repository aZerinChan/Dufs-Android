package local.dufs.manage;

import java.net.URI;
import java.util.*;
import java.util.function.BiConsumer;

final class UiGeometry {
    private UiGeometry() {}
    interface StatusMeasure { boolean fits(float size,boolean compact); }
    static final class StatusFit {
        final float size;
        final boolean compact,complete;
        StatusFit(float size,boolean compact,boolean complete) {
            this.size=size; this.compact=compact; this.complete=complete;
        }
    }
    static StatusFit fitStatus(float normal,float minimum,float step,StatusMeasure layout) {
        if(layout.fits(normal,false)) return new StatusFit(normal,false,true);
        for(float size=normal;;size=Math.max(minimum,size-step)) {
            if(layout.fits(size,true)) return new StatusFit(size,true,true);
            if(size<=minimum) return new StatusFit(minimum,true,false);
        }
    }
    static String modifiedLabel(long millis,java.time.ZoneId zone,String language) {
        if(millis<=0) return "—";
        Locale locale=Locale.forLanguageTag("bqi-IR".equals(language)?"fa-IR":language);
        java.text.DateFormat format=java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM,java.text.DateFormat.SHORT,locale);
        format.setTimeZone(TimeZone.getTimeZone(zone.getId()));
        return format.format(new Date(millis));
    }
    static final class SelectionRange {
        private final List<String> rows;
        private final Map<String,Integer> indices=new HashMap<>();
        private final Set<String> initial;
        private final int origin;
        private final boolean value;
        private int end;
        SelectionRange(List<String> rows,Set<String> selection,String key,BiConsumer<String,Boolean> set) {
            this.rows=new ArrayList<>(rows); initial=new HashSet<>(selection);
            for(int i=0;i<rows.size();i++) indices.put(rows.get(i),i);
            origin=end=indices.get(key); value=!initial.contains(key); set.accept(key,value);
        }
        void moveTo(String key,BiConsumer<String,Boolean> set) {
            Integer target=indices.get(key); if(target==null) return;
            while(end!=target) {
                int next=end+Integer.compare(target,end);
                if(Math.abs(next-origin)<Math.abs(end-origin)) {
                    String left=rows.get(end); set.accept(left,initial.contains(left));
                } else set.accept(rows.get(next),value);
                end=next;
            }
        }
    }
    static URI shareTarget(URI root,URI visible,String saved) {
        if(saved!=null) try {
            URI target=DufsClient.address(saved);
            if(target.toASCIIString().startsWith(root.toASCIIString())) return target;
        } catch(IllegalArgumentException ignored) {}
        return visible!=null?visible:root;
    }
    static int popupX(int left,int right,int anchorRight,int width) {
        return Math.max(left,Math.min(anchorRight-width,right-width));
    }
    static int popupY(int top,int bottom,int anchorTop,int anchorBottom,int height,int gap) {
        int below=anchorBottom+gap, above=anchorTop-gap-height;
        int y=below+height<=bottom?below:above>=top?above:below;
        return Math.max(top,Math.min(y,bottom-height));
    }
    static String shareName(String name,int count) {
        return shareName(name,count,true);
    }
    static String shareName(String name,int count,boolean zh) {
        return shareName(name,count,zh?"zh-CN":"en");
    }
    static String shareName(String name,int count,String locale) {
        String fallback=Words.text(locale,"共享文件");
        String safe=(name==null||name.trim().isEmpty()?fallback:name).replaceAll("[\\p{Cc}\\p{Cf}]"," ").trim();
        if(safe.isEmpty()) safe=fallback;
        int n=safe.codePointCount(0,safe.length());
        if(n>34) safe=safe.substring(0,safe.offsetByCodePoints(0,26))+"…"+safe.substring(safe.offsetByCodePoints(0,n-7));
        return safe+(count>1?Words.text(locale," 等 ")+count+Words.text(locale," 个文件"):"");
    }
}

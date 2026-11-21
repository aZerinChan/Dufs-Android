package local.dufs.manage;

import android.content.Context;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

final class ToolbarStatus extends TextView {
    private static final int MAX_LINES=3;
    private static final float COMPACT_SPACING=0.87f;
    private boolean clipped;

    ToolbarStatus(Context context) {
        super(context);
        setGravity(Gravity.CENTER_VERTICAL|Gravity.START);
        setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG);
        setMaxLines(3);
        setEllipsize(TextUtils.TruncateAt.END);
        if(Build.VERSION.SDK_INT>=28) setFallbackLineSpacing(true);
    }

    private float px(float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,sp,getResources().getDisplayMetrics());
    }

    boolean isClipped() {
        if(clipped) return true;
        Layout layout=getLayout();
        if(layout==null) return false;
        for(int i=0;i<layout.getLineCount();i++) if(layout.getEllipsisCount(i)>0) return true;
        return false;
    }

    @Override protected void onTextChanged(CharSequence text,int start,int before,int count) {
        super.onTextChanged(text,start,before,count);
        // A fixed-size TextView may otherwise redraw new text without measuring again.
        requestLayout();
    }

    @Override protected void onMeasure(int widthSpec,int heightSpec) {
        int width=MeasureSpec.getSize(widthSpec)-getCompoundPaddingLeft()-getCompoundPaddingRight();
        int height=MeasureSpec.getSize(heightSpec)-getCompoundPaddingTop()-getCompoundPaddingBottom();
        CharSequence text=getText();
        TextPaint paint=new TextPaint(getPaint());
        float normal=px(13), minimum=Math.min(normal,px(9));
        UiGeometry.StatusFit fit=UiGeometry.fitStatus(normal,minimum,Math.max(0.1f,(normal-minimum)/8),(size,compact)-> {
            if(text.length()==0) return true;
            if(width<=0 || height<=0) return false;
            paint.setTextSize(size);
            StaticLayout.Builder measure=StaticLayout.Builder.obtain(text,0,text.length(),paint,width)
                .setTextDirection(getLayoutDirection()==View.LAYOUT_DIRECTION_RTL?TextDirectionHeuristics.FIRSTSTRONG_RTL:TextDirectionHeuristics.FIRSTSTRONG_LTR)
                .setBreakStrategy(getBreakStrategy()).setHyphenationFrequency(getHyphenationFrequency())
                .setIncludePad(!compact).setLineSpacing(0,compact?COMPACT_SPACING:1f).setMaxLines(MAX_LINES+1);
            if(Build.VERSION.SDK_INT>=28) measure.setUseLineSpacingFromFallbacks(true);
            Layout layout=measure.build();
            int lines=layout.getLineCount();
            return lines<=(compact?MAX_LINES:2) && layout.getHeight()<=height && layout.getLineEnd(lines-1)==text.length();
        });
        clipped=!fit.complete;
        if(getTextSize()!=fit.size) setTextSize(TypedValue.COMPLEX_UNIT_PX,fit.size);
        if(getIncludeFontPadding()==fit.compact) setIncludeFontPadding(!fit.compact);
        float spacing=fit.compact?COMPACT_SPACING:1f;
        if(getLineSpacingMultiplier()!=spacing) setLineSpacing(0,spacing);
        super.onMeasure(widthSpec,heightSpec);
    }
}

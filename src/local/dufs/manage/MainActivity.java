package local.dufs.manage;

import android.app.*;
import android.content.*;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.*;
import android.provider.OpenableColumns;
import android.provider.DocumentsContract;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.net.URI;

import java.util.*;
import java.util.concurrent.*;
import org.json.*;
import local.dufs.manage.DufsDirectory.Item;

public final class MainActivity extends Activity {

    private static final int PICK = 10, SAVE = 11, BATCH_SAVE = 12, CHECK_WORKERS = 6;
    private static final int ARCHIVE_SAVE = 13;
    
    private int ink,soft,accent,surface,fill,danger;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final ExecutorService permissionWorker = Executors.newFixedThreadPool(CHECK_WORKERS);
    private final ExecutorService permissionCoordinator=Executors.newSingleThreadExecutor();
    private final ExecutorService names=new ThreadPoolExecutor(2,2,0L,TimeUnit.SECONDS,new SynchronousQueue<>());
    private Future<?> permissionTask;
    private final Handler permissionDelay=new Handler(Looper.getMainLooper());
    private final Runnable selectionProbe=()->checkSelectedPermissions(false);
    private final Set<Item> attemptedSelectionRights=new HashSet<>();
    private final Map<Item,DufsDirectory.Actions> checkedRights=new HashMap<>();
    private Context themed;
    private ScrollView fileScroll;
    private boolean themePending, rowsDirty;
    private String shareTarget, sharePathDraft;
    private java.util.function.Supplier<String> lastStatus=()->tr("准备连接");
    private final ArrayDeque<ArrayList<Uri>> queue = new ArrayDeque<>();
    private Profile profile;
    private URI directory;
    private LinearLayout root, entries;
    private TextView location, status, selectedStatus, pullHint;
    private View tools, home, archiveIcon, uploadIcon, refreshIcon, searchIcon, topIcon;
    private View selectAllIcon, batchDownloadIcon, batchMoveIcon, batchDeleteIcon, closeSelectionIcon;
    private float topX=1f, topY=Float.NaN;
    private boolean showingConnectedStatus, checkingPermissions;
    private int unverifiedPermissions;
    private final LinkedHashSet<String> selected=new LinkedHashSet<>();
    private final Map<String,CheckBox> checks=new LinkedHashMap<>();

    private boolean selecting, dragging;
    private CheckBox dragOrigin;
    private UiGeometry.SelectionRange dragRange;
    private int sortField; private boolean sortDescending;
    private DownloadPlan pendingDownload;
    private Uri pickedTree;
    private boolean batchWriting, reportInterruptedBatch, confirmRestoredDownload;
    private ProgressBar progress;
    private AlertDialog active;
    private PopupWindow popup;
    private boolean busy, dead, pickOpen, needsSetup;
    private DufsDirectory listing;
    private DufsDirectory searchResults;
    private String searchQuery;
    private Bundle saveRequest;
    private Uri saveDestination;
    private boolean confirmRestoredSave;

    private static int mode(Context c) { return c.getSharedPreferences("appearance",MODE_PRIVATE).getInt("theme",0); }
    private void palette() {
        int mode=mode(this); ContextThemeWrapper wrapper=new ContextThemeWrapper(this,R.style.AppTheme);
        String language=getSharedPreferences("appearance",MODE_PRIVATE).getString("language","system");
        if(mode==1 || mode==2 || !language.equals("system")) {
            Configuration override=new Configuration();
            if(!language.equals("system")) override.setLocale(Locale.forLanguageTag(language));
            if(mode==1 || mode==2) override.uiMode=(getResources().getConfiguration().uiMode & ~Configuration.UI_MODE_NIGHT_MASK)
                | (mode==2?Configuration.UI_MODE_NIGHT_YES:Configuration.UI_MODE_NIGHT_NO);
            wrapper.applyOverrideConfiguration(override);
        }
        themed=wrapper;
    }
    private String language() {return Words.choose(getSharedPreferences("appearance",MODE_PRIVATE).getString("language","auto"),getResources().getConfiguration().getLocales().get(0).toLanguageTag());}
    private String tr(String text) {return Words.text(language(),text);}
    private String rootLabel() {return tr("根目录");}
    @Override public void onConfigurationChanged(Configuration c) {
        super.onConfigurationChanged(c);
        if(mode(this)==0) { themePending=true; applyPendingTheme(); }
    }
    private void applyPendingTheme() {
        if(!themePending || active!=null || popup!=null || busy || dragging || pullCaptured || dead) return;
        themePending=false;
        int y=fileScroll==null?0:fileScroll.getScrollY();
        buildUi(); renderRows(); updateBrowserChrome(); restoreScroll(y);
    }
    private void restoreScroll(int y) {
        if(y<=0) return;
        ScrollView s=fileScroll;
        s.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override public boolean onPreDraw() {
                s.getViewTreeObserver().removeOnPreDrawListener(this);
                if(s==fileScroll) s.scrollTo(0,y);
                return true;
            }
        });
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        SharedPreferences floatingPrefs=getPreferences(MODE_PRIVATE);
        topX=floatingPrefs.getFloat("topButtonX",1f);
        topY=floatingPrefs.getFloat("topButtonY",Float.NaN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        sortField=getPreferences(MODE_PRIVATE).getInt("sortField",0); sortDescending=getPreferences(MODE_PRIVATE).getBoolean("sortDescending",false);
        buildUi();
        final String restored=state==null?null:state.getString("directory");
        if (state == null) receive(getIntent());
        else {
            Bundle batches = state.getBundle("batches");
            if (batches != null) for (int i=0; i<batches.size(); i++) {
                ArrayList<Uri> batch = batches.getParcelableArrayList(Integer.toString(i));
                if (batch != null && !batch.isEmpty()) queue.add(batch);
            }
            saveRequest=state.getBundle("saveRequest");
            saveDestination=state.getParcelable("saveDestination");
            confirmRestoredSave=saveDestination!=null;
            if(saveRequest!=null) pickOpen=saveDestination==null;
            if(!queue.isEmpty()) {
                shareTarget=state.getString("shareTarget");
                sharePathDraft=state.getString("sharePathDraft");
            }
            reportInterruptedBatch=state.getBoolean("batchInterrupted");
            String plan=state.getString("batchDownloadPlan");
            if(plan!=null) try {
                pendingDownload=DownloadPlan.decode(plan);
                confirmRestoredDownload=true;
                pickedTree=state.getParcelable("batchDownloadTree"); pickOpen=pickedTree==null;
            } catch(Exception ignored) {pendingDownload=null;}
        }
        setBusy(true,()->tr("读取保存的连接…"));
        worker.execute(() -> {
            try {
                Profile loaded = Profile.load(this);
                if (loaded != null) { ui(() -> { profile = loaded;
                    load(restored!=null && restored.startsWith(loaded.root.toASCIIString()) ? URI.create(restored) : loaded.root);
                }); return; }
                ui(() -> {
                    setBusy(false,()->tr("请填写服务器地址和账号密码"));
                    needsSetup = true; nextShare();
                });
            } catch (Exception e) { ui(() -> { setBusy(false,()->tr("无法读取保存的配置，请重新填写")); needsSetup = true; nextShare(); }); }
        });
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        Bundle batches = new Bundle(); int i=0;
        for (ArrayList<Uri> batch : queue) batches.putParcelableArrayList(Integer.toString(i++), batch);
        state.putBundle("batches", batches);
        if(!queue.isEmpty() && shareTarget!=null) state.putString("shareTarget",shareTarget);
        if(!queue.isEmpty() && sharePathDraft!=null) state.putString("sharePathDraft",sharePathDraft);
        if(saveRequest!=null) state.putBundle("saveRequest",saveRequest);
        if(saveDestination!=null) state.putParcelable("saveDestination",saveDestination);
        state.putBoolean("batchInterrupted",batchWriting || reportInterruptedBatch);
        if(pendingDownload!=null) try {
            state.putString("batchDownloadPlan",pendingDownload.encode());
            if(pickedTree!=null) state.putParcelable("batchDownloadTree",pickedTree);
        } catch(JSONException ignored) {}
        if (directory!=null) state.putString("directory",directory.toASCIIString());
    }
    @Override protected void onNewIntent(Intent intent) { super.onNewIntent(intent); receive(intent); nextShare(); }
    @Override protected void onDestroy() { dead = true; permissionDelay.removeCallbacks(selectionProbe); if(popup!=null) popup.dismiss(); worker.shutdownNow(); permissionCoordinator.shutdownNow(); permissionWorker.shutdownNow(); names.shutdownNow(); super.onDestroy(); }
    @Override public void onBackPressed() {
        if (busy) { Toast.makeText(this,tr("请等当前操作完成"),Toast.LENGTH_SHORT).show(); return; }
        if(selecting) {clearSelection(); repaintRows(); return;}
        if(searchResults!=null) {load(directory); return;}
        if (directory != null && profile != null && !directory.equals(profile.root)) { goUp(); return; }
        super.onBackPressed();
    }
    private void ui(Runnable job) { runOnUiThread(() -> { if (!dead && !isFinishing()) job.run(); }); }

    private void receive(Intent intent) {
        if (intent == null) return;
        String action = intent.getAction(); LinkedHashSet<Uri> all = new LinkedHashSet<>();
        if (!Intent.ACTION_VIEW.equals(action) && !Intent.ACTION_SEND.equals(action) && !Intent.ACTION_SEND_MULTIPLE.equals(action)) return;
        try {
            if (Intent.ACTION_VIEW.equals(action)) addUri(all, intent.getData());
            else if (Intent.ACTION_SEND.equals(action)) addUri(all, intent.getParcelableExtra(Intent.EXTRA_STREAM));
            else {
                ArrayList<Uri> list = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if (list != null) for (Uri u : list) addUri(all,u);
            }
            ClipData clip = intent.getClipData();
            if (clip != null) for (int i=0;i<clip.getItemCount();i++) addUri(all,clip.getItemAt(i).getUri());
        } catch (RuntimeException ignored) {}
        if (all.isEmpty()) Toast.makeText(this,tr("没有收到可上传的文件（不支持分享文本或网址）"),Toast.LENGTH_LONG).show();
        else queue.add(new ArrayList<>(all));
    }
    private static void addUri(Set<Uri> list, Uri uri) { if (uri != null && "content".equals(uri.getScheme()) && uri.getAuthority() != null) list.add(uri); }
    private void nextShare() {
        applyPendingTheme();
        if (busy || active != null || popup != null || pickOpen || isFinishing() || dead) return;
        if(reportInterruptedBatch) {
            reportInterruptedBatch=false;
            show(new AlertDialog.Builder(themed).setTitle(tr("批量下载")).setMessage(tr("批量下载已中断，保存位置可能有不完整的文件。请检查后重新选择。" )).setPositiveButton(tr("知道了"),null).create());
            return;
        }
        if(pickedTree!=null) {resumeBatchDownload(); return;}
        if(saveDestination!=null && resumeSave()) return;
        if (profile == null && (needsSetup || !queue.isEmpty())) { needsSetup = false; editSettings(); return; }
        if (profile == null || queue.isEmpty()) return;
        ArrayList<Uri> batch = queue.peek();
        ShareDraft draft=new ShareDraft(profile,batch);
        URI target=UiGeometry.shareTarget(profile.root,directory,shareTarget);
        if(target.equals(directory) && listing!=null) showShare(draft,target,listing,null);
        else browseShare(draft,target);
        try { names.execute(()-> {
            String label=UiGeometry.shareName(displayName(batch.get(0)),batch.size(),language());
            ui(()->{draft.label=label; if(draft.preview!=null) draft.preview.setText(label);});
        }); } catch(RejectedExecutionException ignored) {}
        root.postDelayed(()->{
            if(draft.label.equals(tr("正在读取文件名…"))) {
                draft.label=tr("共享文件（文件名暂未返回）");
                if(draft.preview!=null) draft.preview.setText(draft.label);
            }
        },900);
    }
    private final class ShareDraft {
        final Profile profile; final ArrayList<Uri> batch;
        String label; TextView preview;
        ShareDraft(Profile p,ArrayList<Uri> b) { profile=p; batch=b; label=b.size()==1?tr("正在读取文件名…"):tr("共享文件 · 共 ")+b.size()+tr(" 个"); }
    }
    private void cancelShare(ShareDraft draft) {
        draft.preview=null; if(queue.peek()==draft.batch) {queue.remove(); shareTarget=null; sharePathDraft=null;} setBusy(false,()->tr("已取消，未上传"));
    }
    private void browseShare(ShareDraft draft,URI target) {
        shareTarget=target.toASCIIString();
        setBusy(true,()->tr("读取上传目录…"));
        worker.execute(()-> {
            try { DufsDirectory dir=DufsDirectory.from(draft.profile.client.listForDisplay(target)); ui(()->showShare(draft,target,dir,null)); }
            catch(Exception e) { ui(()->showShare(draft,target,null,explain(e))); }
        });
    }
    private void showShare(ShareDraft draft,URI target,DufsDirectory dir,String error) {
        shareTarget=target.toASCIIString(); setBusy(false,()->tr("选择上传目录"));
        LinearLayout header=chooserPanel();
        TextView name=body(draft.label,16,ink); name.setMaxLines(2); name.setEllipsize(TextUtils.TruncateAt.END); header.addView(name); draft.preview=name;
        header.addView(body(tr("选择上传目录"),14,soft));
        EditText path=destinationField(header,draft.profile,target);
        String checkedText=path.getText().toString();
        if(sharePathDraft!=null) {path.setText(sharePathDraft); path.setSelection(path.length());}
        boolean initialMatch=checkedText.contentEquals(path.getText());
        boolean allowed=dir!=null && dir.canUploadHere();
        TextView detail=body(error!=null?tr("读取失败：")+error:
            !allowed?tr("此目录不可上传"):!dir.exists?tr("上传时由服务器创建目录"):"",13,allowed?soft:danger);
        header.addView(detail);
        AlertDialog dialog=new AlertDialog.Builder(themed).setView(header).setNegativeButton(tr("取消"),(d,w)->cancelShare(draft))
            .setPositiveButton(tr(allowed && initialMatch?"上传":"检查目录"),null).create();
        detail.setVisibility(initialMatch?View.VISIBLE:View.GONE);
        dialog.setOnCancelListener(d->cancelShare(draft)); show(dialog);
        Button action=dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        path.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s,int start,int count,int after) {}
            public void onTextChanged(CharSequence s,int start,int before,int count) {
                sharePathDraft=s.toString();
                boolean unchanged=checkedText.contentEquals(s);
                action.setText(tr(unchanged && allowed?"上传":"检查目录"));
                detail.setVisibility(unchanged?View.VISIBLE:View.GONE);
            }
            public void afterTextChanged(android.text.Editable s) {}
        });
        action.setOnClickListener(v->{
            if(queue.peek()!=draft.batch || profile!=draft.profile) return;
            String text=path.getText().toString();
            if(checkedText.equals(text) && allowed) {
                draft.preview=null; queue.remove(); shareTarget=null; sharePathDraft=null; send(draft.profile,target,draft.batch);
            } else {
                URI next=checkedText.equals(text)?target:typedDestination(draft.profile,path);
                if(next==null) return;
                sharePathDraft=null;
                browseShare(draft,next);
            }
            dialog.dismiss();
        });
    }
    private LinearLayout chooserPanel() {LinearLayout v=new LinearLayout(themed); v.setOrientation(1); v.setPadding(dp(22),dp(12),dp(22),dp(8)); return v;}
    private interface FolderAction {void open(URI path);}
    private EditText destinationField(LinearLayout box,Profile p,URI target) {
        box.addView(body(tr("相对于服务器根目录（/ 表示根目录）"),13,soft));
        EditText path=field(box,tr("输入相对路径"),InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        path.setText(DufsClient.destinationText(p.root,target));
        path.setSelection(path.length());
        return path;
    }
    private URI typedDestination(Profile p,EditText path) {
        try {return DufsClient.customFolder(p.root,path.getText().toString());}
        catch(IllegalArgumentException e) {path.setError(tr("无效文件名或地址")); return null;}
    }
    private void show(AlertDialog dialog) {
        active = dialog; dialog.setOnDismissListener(d -> { if(active==dialog) { active = null; nextShare(); } }); dialog.show();
        Window w=dialog.getWindow(); if(w!=null) w.setBackgroundDrawable(new InsetDrawable(card(surface),dp(20),dp(8),dp(20),dp(8)));
        for(int id:new int[]{AlertDialog.BUTTON_POSITIVE,AlertDialog.BUTTON_NEGATIVE,AlertDialog.BUTTON_NEUTRAL}) {
            Button b=dialog.getButton(id); if(b!=null) styleButton(b,false);
        }
    }
    private void selectAppearance() {
        if(busy || active!=null || popup!=null) return;
        int selected=mode(this);
        show(new AlertDialog.Builder(themed).setTitle(tr("外观")).setSingleChoiceItems(new String[]{tr("跟随系统"),tr("浅色"),tr("深色")},selected,(d,which)-> {
            if(which==selected) {d.dismiss(); return;}
            if(!getSharedPreferences("appearance",MODE_PRIVATE).edit().putInt("theme",which).commit()) {
                Toast.makeText(this,tr("无法保存外观选择"),Toast.LENGTH_SHORT).show(); return;
            }
            themePending=true; d.dismiss(); applyPendingTheme();
        }).setNegativeButton(tr("取消"),null).create());
    }
    private void editSettings() {
        if (busy || active != null) return;
        LinearLayout form = new LinearLayout(themed); form.setOrientation(1); form.setPadding(dp(22),dp(8),dp(22),0);
        EditText address = field(form,tr("服务器地址"),InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        EditText user = field(form,tr("账号（可选）"),InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        LinearLayout passwordRow=new LinearLayout(themed); passwordRow.setGravity(Gravity.CENTER_VERTICAL); passwordRow.setBackground(card(fill)); passwordRow.setClipToOutline(true); form.addView(passwordRow);
        EditText pass=new EditText(themed); pass.setSingleLine(); pass.setHint(tr("密码")); pass.setSaveEnabled(false);
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); styleField(pass); pass.setBackground(null); pass.setPadding(dp(12),dp(8),dp(10),dp(8));
        passwordRow.addView(pass,new LinearLayout.LayoutParams(0,-2,1));
        ImageButton reveal=icon(R.drawable.ic_eye,tr("显示密码"),()->{});
        reveal.setBackground(new RippleDrawable(ColorStateList.valueOf((accent&0xffffff)|0x22000000),null,card(0xffffffff)));
        passwordRow.addView(reveal,new LinearLayout.LayoutParams(dp(48),dp(48)));
        reveal.setOnClickListener(v -> {
            boolean visible=pass.getTransformationMethod()!=null;
            int start=pass.getSelectionStart(), end=pass.getSelectionEnd();
            pass.setTransformationMethod(visible?null:PasswordTransformationMethod.getInstance());
            if(start>=0 && end>=0) pass.setSelection(Math.min(start,pass.length()),Math.min(end,pass.length()));
            reveal.setImageResource(visible?R.drawable.ic_eye_off:R.drawable.ic_eye);
            String action=tr(visible?"隐藏密码":"显示密码"); reveal.setContentDescription(action); reveal.setTooltipText(action);
        });
        address.setText(profile == null ? "http://host:5000" : profile.root.toString());
        if (profile != null) { user.setText(profile.user); pass.setText(profile.password); }
        TextView error = body("",14,danger); form.addView(error);
        ScrollView scroll = new ScrollView(themed); scroll.addView(form);
        AlertDialog dialog = new AlertDialog.Builder(themed).setTitle(tr("Dufs 连接设置")).setView(scroll)
                .setNegativeButton(tr("取消"), (d,w) -> { if (profile == null) {queue.clear(); shareTarget=null; sharePathDraft=null;} })
                .setNeutralButton(tr("测试连接"),null).setPositiveButton(tr("保存"),null).create();
        dialog.setOnCancelListener(d -> { if (profile == null) {queue.clear(); shareTarget=null; sharePathDraft=null;} });
        show(dialog);
        Button save=dialog.getButton(AlertDialog.BUTTON_POSITIVE), test=dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        View.OnClickListener connect=v -> {
            final boolean persist=v==save;
            final Profile draft;
            error.setTextColor(danger);
            try { draft = new Profile(DufsClient.address(address.getText().toString()),user.getText().toString(),pass.getText().toString()); }
            catch (IllegalArgumentException e) { error.setText(e.getMessage() == null ? tr("账号不能包含冒号；填写密码时需要账号。") : e.getMessage()); return; }
            error.setTextColor(soft); error.setText(tr("正在验证连接…"));
            setConnectionControls(dialog,true,address,user,pass);
            if(persist) setBusy(true,()->tr("正在验证连接…"));
            worker.execute(() -> {
                try {
                    DufsDirectory items=readItems(draft,draft.root);
                    if(persist) draft.save(this);
                    ui(() -> {
                        if(active!=dialog) return;
                        if(persist) {profile=draft; present(draft.root,items,null); dialog.dismiss();}
                        else {
                            setConnectionControls(dialog,false,address,user,pass);
                            error.setTextColor(accent); error.setText(tr("连接成功，未保存。"));
                        }
                    });
                } catch (Exception e) { ui(() -> {
                    if(active!=dialog) return;
                    String m=explain(e);
                    if(persist) setBusy(false,()->tr("连接未保存：")+explain(e));
                    error.setTextColor(danger); error.setText(tr("连接失败：")+m);
                    setConnectionControls(dialog,false,address,user,pass);
                }); }
            });
        };
        save.setOnClickListener(connect); test.setOnClickListener(connect);
        android.text.TextWatcher clearResult=new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s,int start,int count,int after) {}
            public void onTextChanged(CharSequence s,int start,int before,int count) {error.setText("");}
            public void afterTextChanged(android.text.Editable text) {}
        };
        for(EditText f:new EditText[]{address,user,pass}) f.addTextChangedListener(clearResult);
    }
    private void setConnectionControls(AlertDialog dialog,boolean testing,EditText... fields) {
        dialog.setCancelable(!testing);
        for(EditText f:fields) f.setEnabled(!testing);
        for(int id:new int[]{AlertDialog.BUTTON_POSITIVE,AlertDialog.BUTTON_NEGATIVE,AlertDialog.BUTTON_NEUTRAL})
            dialog.getButton(id).setEnabled(!testing);
    }
    private EditText field(LinearLayout form,String hint,int type) {
        EditText value = new EditText(themed); value.setSingleLine(); value.setInputType(type); value.setHint(hint); value.setSaveEnabled(false); styleField(value);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.bottomMargin=dp(6); form.addView(value,lp); return value;
    }
    private DufsDirectory readItems(Profile p, URI dir) throws Exception {
        DufsDirectory value=DufsDirectory.from(p.client.listForDisplay(dir));
        if (!value.exists) throw new DufsClient.HttpError(404);
        return value;
    }

    private void load(URI target) {load(target,false);}
    private void refreshNow() {
        if(busy || profile==null) return;
        if(searchResults!=null) runSearch(profile,directory,searchQuery);
        else load(directory==null?profile.root:directory,listing!=null);
    }
    private void load(URI target,boolean keepStatus) {
        Profile p=profile;
        if(keepStatus) {
            busy=true; updateBrowserChrome();
        } else setBusy(true,()->tr("读取目录…"));
        worker.execute(() -> {
            try { DufsDirectory items=readItems(p,target); ui(() -> { present(target,items,null); nextShare(); }); }
            catch (Exception e) { ui(() -> {
                if(target.equals(directory) && e instanceof DufsClient.HttpError && (((DufsClient.HttpError)e).code==401 || ((DufsClient.HttpError)e).code==403 || ((DufsClient.HttpError)e).code==404)) clearListing();
                setBusy(false,()->tr("读取目录失败：")+explain(e)); nextShare();
            }); }
        });
    }
    private void clearListing() {
        resetSelectionProbe();
        listing=null; searchResults=null; searchQuery=null; checkedRights.clear(); rowsDirty=false; entries.removeAllViews();
    }
    private void present(URI target,DufsDirectory data,java.util.function.Supplier<String> headline) {
        resetSelectionProbe();
        int y=searchResults==null && target.equals(directory)?fileScroll.getScrollY():0;
        clearSelection(); searchResults=null; searchQuery=null; directory=target; listing=data; checkedRights.clear(); renderRows();
        if(y==0) fileScroll.scrollTo(0,0); else restoreScroll(y);
        if(headline==null) setConnectedStatus(false,0); else setBusy(false,headline);
    }
    private void resetSelectionProbe() {
        permissionDelay.removeCallbacks(selectionProbe);
        if(permissionTask!=null) permissionTask.cancel(true);
        permissionTask=null; attemptedSelectionRights.clear();
    }
    private void scheduleSelectionPermissionCheck() {
        permissionDelay.removeCallbacks(selectionProbe);
        if(selecting && !dragging && !selected.isEmpty()) permissionDelay.postDelayed(selectionProbe,160);
    }
    // Legacy Dufs omits per-entry ACLs. Background checks cover only selected entries.
    private void checkSelectedPermissions() {checkSelectedPermissions(true);}
    private void checkSelectedPermissions(boolean requested) {
        if(dead || !selecting || dragging || listing==null || profile==null || permissionTask!=null) return;
        DufsDirectory data=listing; Profile p=profile; URI here=directory;
        List<Item> missing=new ArrayList<>();
        for(Item i:selectedItems()) if(rights(i)==null && (requested || !attemptedSelectionRights.contains(i))) missing.add(i);
        if(missing.isEmpty()) return;
        permissionTask=permissionCoordinator.submit(()->{
            try {
                PermissionSnapshot result=PermissionSnapshot.captureSelected(data,missing,permissionWorker,CHECK_WORKERS,7000,i->data.itemActions(p.client,here,i));
                ui(()->{
                    if(listing!=data || profile!=p) return;
                    permissionTask=null;
                    attemptedSelectionRights.addAll(result.started);
                    for(Item i:missing) if(result.observed(i)!=null) checkedRights.put(i,result.observed(i));
                    if(selecting) updateSelectionBar();
                    else {rowsDirty=true; flushRows();}
                    if(requested && selecting && missing.stream().anyMatch(i->rights(i)==null)) Toast.makeText(this,tr("部分权限未能确认"),Toast.LENGTH_SHORT).show();
                    scheduleSelectionPermissionCheck();
                });
            } catch(InterruptedException e) {Thread.currentThread().interrupt();}
        });
    }
    private void checkItemMenu(View anchor,URI here,Item item) {
        DufsDirectory.Actions access=rights(item);
        if(access!=null) {itemMenu(anchor,here,item,access); return;}
        if(busy || active!=null || popup!=null || listing==null || !here.equals(directory)) return;
        DufsDirectory data=listing; Profile p=profile;
        setBusy(true,()->tr("正在核验所选权限…"));
        worker.execute(()->{
            try {
                DufsDirectory.Actions a=data.itemActions(p.client,here,item);
                ui(()->{
                    if(listing!=data || profile!=p) return;
                    checkedRights.put(item,a); setConnectedStatus(false,0);
                    if(a.any()) itemMenu(anchor,here,item,a);
                    else {rowsDirty=true; flushRows(); Toast.makeText(this,tr("此项目没有可用的管理操作"),Toast.LENGTH_SHORT).show(); nextShare();}
                });
            } catch(Exception e) { ui(()->{setBusy(false,()->tr("无法核验权限：")+explain(e));nextShare();}); }
        });
    }
    private void renderRows() {
        rowsDirty=false;
        entries.removeAllViews(); if(listing==null) return;
        checks.clear(); URI target=directory; boolean searching=searchResults!=null;
        List<Item> list=(searching?searchResults:listing).sorted(sortField,sortDescending);
        for (Item item : list) {
            LinearLayout container=new LinearLayout(themed); container.setGravity(Gravity.CENTER_VERTICAL); container.setBackground(card(surface)); container.setMinimumHeight(dp(48));
            ImageView symbol=new ImageView(themed); symbol.setImageResource(item.folder?R.drawable.ic_dir:R.drawable.ic_file); symbol.setColorFilter(accent); symbol.setPadding(dp(6),dp(6),dp(6),dp(6));
            LinearLayout.LayoutParams glyph=new LinearLayout.LayoutParams(dp(34),dp(34)); glyph.setMarginStart(dp(8)); container.addView(symbol,glyph);
            LinearLayout row = new LinearLayout(themed); row.setOrientation(1); row.setPadding(dp(7),dp(8),dp(7),dp(8));
            container.addView(row,new LinearLayout.LayoutParams(0,-2,1));
            TextView name = body(item.name,16,ink); row.addView(name);
            String when=UiGeometry.modifiedLabel(item.modified,java.time.ZoneId.systemDefault(),language());
            row.addView(body((item.folder?tr("文件夹"):android.text.format.Formatter.formatFileSize(this,item.bytes))+" · "+"\u2068"+when+"\u2069",12,soft));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2); lp.bottomMargin=dp(6); entries.addView(container,lp);
            row.setOnClickListener(v -> { if (busy || active != null) return; if(selecting) {setSelected(item.name,!selected.contains(item.name));return;} URI path = searching?DufsClient.searchPath(target,item.name,item.folder):DufsClient.child(target,item.name,item.folder); if (item.folder) load(path); else chooseDownload(path,item.name.substring(item.name.lastIndexOf('/')+1)); });
            if(!searching) row.setOnLongClickListener(v->{
                if(busy || active!=null) return false;
                selecting=true; selected.add(item.name); scheduleSelectionPermissionCheck();
                // Let View complete the long click (including Android's haptic) before detaching it.
                v.post(()->{if(!dead && listing!=null && selecting) repaintRows();});
                return true;
            });
            DufsDirectory.Actions rights=searching?null:rights(item);
            FrameLayout rowAction=new FrameLayout(themed);
            container.addView(rowAction,new LinearLayout.LayoutParams(dp(52),dp(48)));
            if(selecting) {
                CheckBox check=new CheckBox(themed); check.setChecked(selected.contains(item.name)); check.setContentDescription(item.name);
                // Keep the themed native checkbox and center it in the overflow footprint.
                android.graphics.drawable.Drawable mark=check.getButtonDrawable();
                int inset=mark==null?0:Math.max(0,(dp(52)-mark.getIntrinsicWidth())/2);
                check.setPaddingRelative(inset,0,0,0);
                checks.put(item.name,check); rowAction.addView(check,new FrameLayout.LayoutParams(-1,-1));
                check.setOnClickListener(v->{if(!busy && active==null) setSelected(item.name,check.isChecked()); else check.setChecked(selected.contains(item.name));});
                check.setOnLongClickListener(v->{if(busy || active!=null) return false; startDrag(item.name);return true;});
                check.setOnTouchListener((v,event)->handleDrag(event));
            } else if(!searching && (rights==null || rights.any())) {
                Button more=new Button(themed); more.setText("⋯"); styleButton(more,false); more.setContentDescription(item.name+tr("的操作"));
                more.setOnClickListener(v->checkItemMenu(v,target,item));
                rowAction.addView(more,new FrameLayout.LayoutParams(-1,-1));
            }
        }
        if (list.isEmpty()) entries.addView(body(tr(searching?"未找到匹配的文件或文件夹":"这个目录没有文件"),16,soft));
        updateSelectionBar();

    }
    private DufsDirectory.Actions rights(Item i) {
        DufsDirectory.Actions explicit=i.explicit(); if(explicit!=null) return explicit;
        return checkedRights.get(i);
    }
    private void repaintRows() {int y=fileScroll.getScrollY(); renderRows(); restoreScroll(y);}
    private void flushRows() {
        if(!rowsDirty || popup!=null || dragging || pullCaptured || dead) return;
        int offset=fileScroll.getScrollY(); renderRows(); restoreScroll(offset);
    }
    private static URI parent(URI dir) { String s=dir.toASCIIString(); return URI.create(s.substring(0,s.lastIndexOf('/',s.length()-2)+1)); }
    private void goUp() { load(parent(directory)); }
    private interface Change { void run() throws Exception; }
    private interface NameAction { void run(String name); }
    private void askName(String title,String initial,NameAction action) {
        askName(title,initial,action,false);
    }
    private void askName(String title,String initial,NameAction action,boolean search) {
        LinearLayout box=new LinearLayout(themed); box.setOrientation(1); box.setPadding(dp(22),dp(8),dp(22),0);
        EditText name=field(box,search?tr("文件名关键词"):tr("名称"),InputType.TYPE_CLASS_TEXT); name.setText(initial); name.setSelectAllOnFocus(true);
        if(search) name.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(160)});
        AlertDialog d=new AlertDialog.Builder(themed).setTitle(title).setView(box).setNegativeButton(tr("取消"),null).setPositiveButton(tr("确定"),null).create();
        show(d);
        d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String n=name.getText().toString();
            if(search) { if(n.trim().isEmpty()) {name.setError(tr("请输入关键词"));return;} }
            else try { DufsClient.segment(n); } catch (IllegalArgumentException e) { name.setError(tr("请输入有效名称")); return; }
            action.run(n); d.dismiss();
        });
    }
    private void createFolderDialog() {
        if (busy || active!=null || listing==null || !listing.canCreate()) return;
        Profile p=profile; URI here=directory;
        askName(tr("新建文件夹"),"",n -> change(p,here,"新建文件夹",()-> {
            if(!readItems(p,here).canCreate()) throw new DufsClient.HttpError(403);
            p.client.mkdir(DufsClient.child(here,n,true));
        }));
    }
    private DufsDirectory.Actions freshActions(Profile p, URI here, Item item) throws Exception {
        DufsDirectory live=readItems(p,here); Item current=live.find(item.name);
        if(current==null || current.folder!=item.folder) throw new DufsClient.HttpError(404);
        return live.itemActions(p.client,here,current);
    }
    private void itemMenu(View anchor,URI here,Item item,DufsDirectory.Actions rights) {
        if(busy || active!=null || popup!=null || !here.equals(directory) || !rights.any()) return;
        Profile p=profile; URI source=DufsClient.child(here,item.name,item.folder);
        LinearLayout menu=new LinearLayout(themed); menu.setOrientation(1); menu.setPadding(dp(6),dp(6),dp(6),dp(6));
        PopupWindow current=new PopupWindow(menu,dp(156),ViewGroup.LayoutParams.WRAP_CONTENT,true);
        stylePopup(current);
        if(rights.move) {
            if(listing!=null && listing.canCreate()) popupAction(menu,current,tr("重命名"),false,()->askName(tr("重命名"),item.name,n-> {
                if(!n.equals(item.name)) performMove(p,here,item,source,here,n,"重命名");
            }));
            popupAction(menu,current,tr("移动"),false,()->pickDestination(p,here,item,source,here));
        }
        if(rights.delete) popupAction(menu,current,tr("删除"),true,()->show(new AlertDialog.Builder(themed).setTitle(tr("确认删除？"))
            .setMessage(item.name+(item.folder?tr("\n将删除该文件夹及其内容。"):"")+tr("\n此操作不能在应用内撤销。"))
            .setNegativeButton(tr("取消"),null).setPositiveButton(tr("删除"),(d,w)->change(p,here,"删除",()-> {
                if(!freshActions(p,here,item).delete) throw new DufsClient.HttpError(403);
                p.client.delete(source);
            })).create()));
        popup=current;
        current.setOnDismissListener(()-> popupDismissed(current));
        showAnchored(current,menu,anchor);
    }
    private void popupAction(LinearLayout menu,PopupWindow current,String label,boolean destructive,Runnable action) {
        Button button=new Button(themed); button.setText(label); styleButton(button,false);
        button.setGravity(Gravity.CENTER_VERTICAL|Gravity.START); button.setTextColor(destructive?danger:ink);
        menu.addView(button,new LinearLayout.LayoutParams(-1,-2));
        button.setOnClickListener(v->{popup=null; current.dismiss(); flushRows(); action.run();});
    }
    private void popupDismissed(PopupWindow window) {
        if(popup==window) {popup=null; flushRows(); nextShare();}
    }
    private void showAnchored(PopupWindow window,LinearLayout menu,View anchor) {
        Rect visible=new Rect(); root.getWindowVisibleDisplayFrame(visible); visible.inset(dp(5),dp(5));
        if(visible.isEmpty()) {popupDismissed(window); return;}
        int width=Math.min(dp(156),visible.width());
        menu.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));
        int height=Math.min(visible.height(),menu.getMeasuredHeight());
        int[] point=new int[2],origin=new int[2]; anchor.getLocationOnScreen(point); getWindow().getDecorView().getLocationOnScreen(origin);
        int x=UiGeometry.popupX(visible.left,visible.right,point[0]+anchor.getWidth(),width);
        int y=UiGeometry.popupY(visible.top,visible.bottom,point[1],point[1]+anchor.getHeight(),height,dp(4));
        if(height<menu.getMeasuredHeight()) {ScrollView wrapper=new ScrollView(themed); wrapper.addView(menu); window.setContentView(wrapper);}
        window.setWidth(width); window.setHeight(height);
        if(Build.VERSION.SDK_INT>=29) {
            window.setIsLaidOutInScreen(true); window.setIsClippedToScreen(true);
            window.showAtLocation(root,Gravity.TOP|Gravity.LEFT,x,y);
        } else window.showAtLocation(root,Gravity.TOP|Gravity.LEFT,x-origin[0],y-origin[1]);
    }
    private void mainMenu(View anchor) {
        if(busy || active!=null || popup!=null) return;
        LinearLayout menu=new LinearLayout(themed); menu.setOrientation(1); menu.setPadding(dp(6),dp(6),dp(6),dp(6));
        PopupWindow window=new PopupWindow(menu,dp(156),-2,true); stylePopup(window);
        if(selecting && selectedItems().stream().anyMatch(i->rights(i)==null))
            popupAction(menu,window,tr("核验所选权限"),false,this::checkSelectedPermissions);
        if(listing!=null) popupAction(menu,window,tr("排序"),false,this::sortDialog);
        popupAction(menu,window,tr("语言"),false,this::languageDialog);
        if(listing!=null && listing.canCreate()) popupAction(menu,window,tr("新建文件夹"),false,this::createFolderDialog);
        popupAction(menu,window,tr("连接设置"),false,this::editSettings);
        popupAction(menu,window,tr("外观"),false,this::selectAppearance);
        popup=window; window.setOnDismissListener(()->popupDismissed(window));
        showAnchored(window,menu,anchor);
    }
    private void searchDialog() {
        if(busy || listing==null || !listing.canSearch) return;
        Profile p=profile; URI base=directory;
        askName(tr("搜索当前目录和子目录"),searchQuery==null?"":searchQuery,q->runSearch(p,base,q),true);
    }
    private void runSearch(Profile p,URI base,String query) {
        setBusy(true,()->tr("正在搜索…"));
        worker.execute(()-> {
            try {
                DufsDirectory result=DufsDirectory.search(p.client,base,query);
                ui(()-> {
                    if(profile!=p || !base.equals(directory)) return;
                    clearSelection(); searchQuery=query; searchResults=result; renderRows(); fileScroll.scrollTo(0,0);
                    setBusy(false,()->tr("找到 ")+result.items.size()+tr(" 项")); nextShare();
                });
            } catch(Exception e) {ui(()-> {setBusy(false,()->tr("搜索失败：")+explain(e)); nextShare();});}
        });
    }
    private void pickDestination(Profile p,URI here,Item item,URI source,URI target) {
        chooseMoveFolder(p,here,target,item.folder?Collections.singleton(source.getPath()):Collections.emptySet(),dest->performMove(p,here,item,source,dest,item.name,"移动"));
    }
    private void chooseMoveFolder(Profile p,URI here,URI target,Set<String> skip,FolderAction onChosen) {
        LinearLayout box=chooserPanel(); EditText path=destinationField(box,p,target);
        AlertDialog dialog=new AlertDialog.Builder(themed).setTitle(tr("选择移动目录")).setView(box)
            .setNegativeButton(tr("取消"),null).setPositiveButton(tr("移动"),null).create();
        show(dialog);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            URI destination=typedDestination(p,path);
            if(destination==null) return;
            if(!DufsClient.moveFolderAllowed(here,destination,skip)) {
                path.setError(tr("不能移动到当前目录、所选文件夹或其子目录")); return;
            }
            onChosen.open(destination); // Both move handlers check existence and write permission before MOVE.
            dialog.dismiss();
        });
    }
    private void performMove(Profile p,URI here,Item item,URI source,URI destination,String name,String label) {
        URI dest=DufsClient.child(destination,name,item.folder);
        change(p,here,label,()-> {
            if(!freshActions(p,here,item).move) throw new DufsClient.HttpError(403);
            if(!readItems(p,destination).canCreate()) throw new DufsClient.HttpError(403);
            p.client.move(source,dest);
        });
    }
    private void change(Profile p,URI here,String label,Change action) {
        setBusy(true,()->tr("正在")+tr(label)+"…");
        worker.execute(()-> {
            Exception failure=null;
            try { action.run(); } catch(Exception e) {failure=e;}
            DufsDirectory updated=null; try { updated=readItems(p,here); } catch(Exception ignored) {}
            Exception error=failure; DufsDirectory data=updated;
            java.util.function.Supplier<String> note=()->tr(label)+(error==null?tr("成功"):tr("失败：")+explain(error));
            ui(()-> {
                if(data!=null) present(here,data,note); else { clearListing(); setBusy(false,()->note.get()+tr("（目录未刷新）")); } nextShare();
            });
        });
    }
    private void languageDialog() {
        String[] codes=Words.codes(), labels=Words.labels();
        SharedPreferences prefs=getSharedPreferences("appearance",MODE_PRIVATE);
        int current=Arrays.asList(codes).indexOf(Words.preference(prefs.getString("language","auto")));
        ArrayAdapter<String> choices=new ArrayAdapter<String>(themed,android.R.layout.simple_list_item_single_choice,labels) {
            @Override public View getView(int position,View recycled,ViewGroup parent) {
                CheckedTextView row=(CheckedTextView)super.getView(position,recycled,parent);
                if(recycled==null) {
                    android.graphics.drawable.Drawable mark=row.getCheckMarkDrawable();
                    row.setCheckMarkDrawable(null);
                    row.setCompoundDrawablesWithIntrinsicBounds(mark,null,null,null);
                    row.setCompoundDrawablePadding(dp(12));
                }
                row.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                row.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG);
                row.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                row.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
                return row;
            }
        };
        show(new AlertDialog.Builder(themed).setTitle(tr("语言")).setSingleChoiceItems(choices,current,(d,i)-> {
            prefs.edit().putString("language",codes[i]).apply();
            themePending=true; d.dismiss(); applyPendingTheme();
        }).setNegativeButton(tr("取消"),null).create());
    }
    private void sortDialog() {
        String[] by={tr("名称"),tr("最后修改时间"),tr("文件大小")};
        String[] values=new String[6]; for(int i=0;i<3;i++) {values[i*2]=by[i]+" · "+tr("升序");values[i*2+1]=by[i]+" · "+tr("降序");}
        show(new AlertDialog.Builder(themed).setTitle(tr("排序")).setSingleChoiceItems(values,sortField*2+(sortDescending?1:0),(d,which)-> {
            sortField=which/2; sortDescending=(which&1)!=0; getPreferences(MODE_PRIVATE).edit().putInt("sortField",sortField).putBoolean("sortDescending",sortDescending).apply();
            d.dismiss(); repaintRows();
        }).setNegativeButton(tr("取消"),null).create());
    }
    private void clearSelection() {permissionDelay.removeCallbacks(selectionProbe); attemptedSelectionRights.clear(); selected.clear(); selecting=false; dragging=false; dragOrigin=null; dragRange=null;}
    private void setSelected(String key,boolean on) {
        updateSelected(key,on); scheduleSelectionPermissionCheck(); updateSelectionBar();
    }
    private void updateSelected(String key,boolean on) {
        if(on) selected.add(key); else {
            selected.remove(key);
            if(listing!=null) attemptedSelectionRights.remove(listing.find(key));
        }
        CheckBox check=checks.get(key); if(check!=null) check.setChecked(on);
    }
    private void startDrag(String key) {
        dragOrigin=checks.get(key); dragging=true;
        dragRange=new UiGeometry.SelectionRange(new ArrayList<>(checks.keySet()),selected,key,this::updateSelected);
        fileScroll.requestDisallowInterceptTouchEvent(true); updateSelectionBar();
    }
    private boolean handleDrag(MotionEvent event) {
        if(!dragging) return false;
        int action=event.getActionMasked();
        if(action==MotionEvent.ACTION_MOVE) {
            Rect bounds=new Rect(); int y=(int)event.getRawY(),x=(int)event.getRawX();
            for(Map.Entry<String,CheckBox> row:checks.entrySet()) {
                CheckBox check=row.getValue(); int[] pos=new int[2]; check.getLocationOnScreen(pos);
                if(x>=pos[0]-dp(26) && x<=pos[0]+check.getWidth()+dp(34) && y>=pos[1] && y<pos[1]+check.getHeight() && check.getLocalVisibleRect(bounds)) {
                    dragRange.moveTo(row.getKey(),this::updateSelected); updateSelectionBar(); break;
                }
            }
            int[] top=new int[2]; fileScroll.getLocationOnScreen(top);
            if(y>top[1]+fileScroll.getHeight()-dp(44)) fileScroll.scrollBy(0,dp(16));
            else if(y<top[1]+dp(44)) fileScroll.scrollBy(0,-dp(16));
        } else if(action==MotionEvent.ACTION_UP || action==MotionEvent.ACTION_CANCEL) {
            dragging=false; dragRange=null; fileScroll.requestDisallowInterceptTouchEvent(false);
            MotionEvent cancel=MotionEvent.obtain(event); cancel.setAction(MotionEvent.ACTION_CANCEL);
            // Clear the originating CheckBox's pending click after the range gesture.
            if(dragOrigin!=null) dragOrigin.onTouchEvent(cancel);
            dragOrigin=null; cancel.recycle(); flushRows(); applyPendingTheme(); scheduleSelectionPermissionCheck();
        }
        return true;
    }
    private List<Item> selectedItems() {
        List<Item> out=new ArrayList<>(); if(listing!=null) for(Item i:listing.sorted(sortField,sortDescending)) if(selected.contains(i.name)) out.add(i); return out;
    }
    private void updateSelectionBar() {
        boolean online=profile!=null && listing!=null;
        status.setVisibility(selecting?View.GONE:View.VISIBLE);
        selectedStatus.setVisibility(selecting?View.VISIBLE:View.GONE);
        selectedStatus.setText(tr("已选择")+" "+selected.size()+"/"+(listing==null?0:listing.items.size()));
        String toggleLabel=tr(listing!=null && !listing.items.isEmpty() && selected.size()==listing.items.size()?"取消全选":"全选");
        selectAllIcon.setContentDescription(toggleLabel);
        selectAllIcon.setTooltipText(toggleLabel);
        boolean canUpload=online && listing.canCreate();
        showCompactAction(archiveIcon,!selecting && online && listing.canArchive);
        showCompactAction(uploadIcon,!selecting && canUpload);
        showCompactAction(refreshIcon,!selecting && profile!=null && !canUpload);
        showCompactAction(searchIcon,!selecting && online && listing.canSearch);
        List<Item> items=selecting?selectedItems():Collections.emptyList();
        boolean any=!items.isEmpty();
        showAction(closeSelectionIcon,selecting,true);
        showAction(selectAllIcon,selecting,true);
        showCompactAction(batchDownloadIcon,selecting && any);
        showCompactAction(batchMoveIcon,selecting && any && items.stream().allMatch(i->{DufsDirectory.Actions a=rights(i); return a!=null && a.move;}));
        showCompactAction(batchDeleteIcon,selecting && any && items.stream().allMatch(i->{DufsDirectory.Actions a=rights(i); return a!=null && a.delete;}));
    }
    private void showCompactAction(View icon,boolean available) {
        icon.setVisibility(available?View.VISIBLE:View.GONE);
        icon.setEnabled(available && !busy);
    }
    private void showAction(View icon,boolean inMode,boolean available) {
        icon.setVisibility(!inMode?View.GONE:available?View.VISIBLE:View.INVISIBLE);
        icon.setEnabled(inMode && available && !busy);
    }
    private void toggleSelectAll() {
        boolean add=listing!=null && selected.size()!=listing.items.size(); selected.clear();
        if(add) for(Item i:listing.items) selected.add(i.name);
        else attemptedSelectionRights.clear();
        scheduleSelectionPermissionCheck();
        for(Map.Entry<String,CheckBox> b:checks.entrySet()) b.getValue().setChecked(selected.contains(b.getKey()));
        updateSelectionBar();
    }
    private static final class Batch {
        final Profile profile; final URI base; final List<Item> items;
        Batch(Profile p,URI here,List<Item> items) {profile=p;base=here;this.items=new ArrayList<>(items);}
    }
    private void beginBatchDownload(List<Item> items) {
        if(busy || pickOpen || profile==null) return;
        confirmRestoredDownload=false;
        try {pendingDownload=DownloadPlan.capture(profile.root,directory,profile.user,items);}
        catch(IllegalArgumentException | JSONException e) {setBusy(false,()->tr("每批最多下载 500 个项目"));return;}
        try {pickOpen=true; startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE),BATCH_SAVE);}
        catch(ActivityNotFoundException e) {pickOpen=false;pendingDownload=null;setBusy(false,()->tr("系统未提供保存位置选择器"));}
    }
    private void resumeBatchDownload() {
        DownloadPlan plan=pendingDownload; Uri tree=pickedTree;
        Profile p=profile;
        if(plan==null || p==null || !plan.matches(p.root,p.user)) {
            pendingDownload=null; pickedTree=null;
            setBusy(false,()->tr("下载会话已失效，请重新选择文件")); nextShare(); return;
        }
        if(confirmRestoredDownload) {
            // A previous instance may have started writing after Android took its snapshot.
            show(new AlertDialog.Builder(themed).setTitle(tr("批量下载"))
                .setMessage(tr("上次的下载可能已完成或只保存了一部分。请先检查保存目录，再决定是否重新下载。"))
                .setCancelable(false)
                .setNegativeButton(tr("取消"),(d,w)->{pendingDownload=null; pickedTree=null; confirmRestoredDownload=false;})
                .setPositiveButton(tr("重新下载"),(d,w)->{confirmRestoredDownload=false; resumeBatchDownload();})
                .create());
            return;
        }
        setBusy(true,()->tr("批量下载"));
        worker.execute(()->{
            try {
                List<Item> items=plan.resolve(readItems(p,plan.base));
                // Save the plan and tree until validation hands off to local writes.
                ui(()->{
                    batchWriting=true; pendingDownload=null; pickedTree=null;
                    batchDownload(new Batch(p,plan.base,items),tree);
                });
            } catch(Exception e) {ui(()-> {
                pendingDownload=null; pickedTree=null;
                setBusy(false,()->tr("下载会话已失效，请重新选择文件"));
                show(new AlertDialog.Builder(themed).setTitle(tr("批量下载")).setMessage(tr("下载会话已失效，请重新选择文件")+"\n"+explain(e)).setPositiveButton(tr("知道了"),null).create());
            });}
        });
    }
    private void beginBatchDelete(List<Item> items) {
        if(busy || profile==null) return;
        Batch b=new Batch(profile,directory,items);
        show(new AlertDialog.Builder(themed).setTitle(tr("批量删除")).setMessage(tr("永久删除所选项目及其中的文件？")+" ("+items.size()+")")
            .setNegativeButton(tr("取消"),null).setPositiveButton(tr("删除"),(d,w)->batchChange(b,null)).create());
    }
    private void beginBatchMove(List<Item> items) {
        if(busy || profile==null) return;
        Batch b=new Batch(profile,directory,items); Set<String> skip=new HashSet<>();
        for(Item i:items) if(i.folder) skip.add(DufsClient.child(b.base,i.name,true).getPath());
        chooseMoveFolder(b.profile,b.base,b.base,skip,dest->batchChange(b,dest));
    }
    private void batchChange(Batch batch,URI destination) {
        String title=destination==null?"批量删除":"批量移动"; setBusy(true,()->tr(title)+"…");
        worker.execute(()->{
            int count=0; StringBuilder issues=new StringBuilder();
            try {
                if(destination!=null && !readItems(batch.profile,destination).canCreate()) throw new DufsClient.HttpError(403);
                DufsDirectory fresh=readItems(batch.profile,batch.base);
                for(Item item:batch.items) {
                    try {
                        Item current=fresh.find(item.name);
                        if(current==null || current.folder!=item.folder || (item.modified>0 && current.modified!=item.modified)) throw new DufsClient.HttpError(412);
                        DufsDirectory.Actions access=fresh.itemActions(batch.profile.client,batch.base,current);
                        if(!(destination==null?access.delete:access.move)) throw new DufsClient.HttpError(403);
                        URI src=DufsClient.child(batch.base,item.name,item.folder);
                        if(destination==null) batch.profile.client.delete(src);
                        else batch.profile.client.move(src,DufsClient.child(destination,item.name,item.folder));
                        count++;
                    } catch(Exception e){if(issues.length()<1600) issues.append(item.name).append(": ").append(explain(e)).append('\n');}
                }
            }catch(Exception e){issues.append(explain(e));}
            DufsDirectory newList=null; try {newList=readItems(batch.profile,batch.base);}catch(Exception ignored){}
            DufsDirectory data=newList; int ok=count;
            ui(()->{
                clearSelection();
                if(data!=null) present(batch.base,data,()->tr(title)+" · "+ok+" / "+batch.items.size()); else {clearListing();setBusy(false,()->tr(title));}
                show(batchResult(tr(title),ok,batch.items.size(),issues.toString()));
            });
        });
    }
    private AlertDialog batchResult(String title,int ok,int total,String issues) {
        return new AlertDialog.Builder(themed).setTitle(title).setMessage(tr("成功")+" "+ok+" / "+total+(issues.isEmpty()?"":"\n\n"+issues)).setPositiveButton(tr("知道了"),null).create();
    }
    private void batchDownload(Batch batch,Uri tree) {
        setBusy(true,()->tr("批量下载")); worker.execute(()->{
            int count=0; int[] budget={5000}; StringBuilder issues=new StringBuilder();
            Uri rootDoc;
            try {rootDoc=DocumentsContract.buildDocumentUriUsingTree(tree,DocumentsContract.getTreeDocumentId(tree));}
            catch(IllegalArgumentException e){ui(()->{batchWriting=false;setBusy(false,()->tr("无法打开保存位置"));nextShare();});return;}
            for(Item item:batch.items) try {saveItem(batch.profile,batch.base,item,rootDoc,0,budget); count++;}
            catch(Exception e){if(issues.length()<1600) issues.append(item.name).append(": ").append(explain(e)).append('\n');}
            int ok=count;
            ui(()->{batchWriting=false;setBusy(false,()->tr("批量下载")+" · "+ok+" / "+batch.items.size()); show(batchResult(tr("批量下载"),ok,batch.items.size(),issues.toString()));});
        });
    }
    private void saveItem(Profile p,URI base,Item i,Uri parentDoc,int depth,int[] budget) throws Exception {
        if(Thread.currentThread().isInterrupted() || depth>24 || --budget[0]<0 || i.folder && i.symlink) throw new IOException("目录过深或包含链接");
        String mime=i.folder?DocumentsContract.Document.MIME_TYPE_DIR:"application/octet-stream";
        Uri out=DocumentsContract.createDocument(getContentResolver(),parentDoc,mime,i.name);
        if(out==null) throw new IOException("无法打开保存位置");
        URI from=DufsClient.child(base,i.name,i.folder);
        if(i.folder) for(Item child:readItems(p,from).items) saveItem(p,from,child,out,depth+1,budget);
        else try(OutputStream stream=getContentResolver().openOutputStream(out,"wt")) {if(stream==null) throw new IOException("无法打开保存位置"); p.client.download(from,stream);}
    }
    private void chooseFiles() {
        if (busy || pickOpen) return;
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("*/*").putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        try { pickOpen=true; startActivityForResult(i,PICK); }
        catch (ActivityNotFoundException e) { pickOpen=false; setBusy(false,()->tr("系统未提供文件选择器")); }
    }
    private void chooseDownload(URI path,String name) {
        if(busy || pickOpen || profile==null) return;
        chooseSave(path,name,false);
    }
    private void chooseArchive() {
        if(busy || pickOpen || active!=null || profile==null || directory==null || listing==null || !listing.canArchive) return;
        String path=directory.getPath();
        String name=path.length()>1?path.substring(path.lastIndexOf('/',path.length()-2)+1,path.length()-1):rootLabel();
        chooseSave(directory,name+".zip",true);
    }
    private void chooseSave(URI source,String name,boolean archive) {
        saveRequest=new Bundle();
        saveRequest.putString("root",profile.root.toASCIIString());
        saveRequest.putString("user",profile.user);
        saveRequest.putString("source",source.toASCIIString());
        saveRequest.putBoolean("archive",archive);
        confirmRestoredSave=false;
        try {
            pickOpen=true;
            startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE)
                .setType(archive?DufsClient.ZIP_MIME:"application/octet-stream").putExtra(Intent.EXTRA_TITLE,name),archive?ARCHIVE_SAVE:SAVE);
        } catch(ActivityNotFoundException e) {
            pickOpen=false; clearSave(); setBusy(false,()->tr("系统未提供保存位置选择器"));
        }
    }
    private void clearSave() {saveRequest=null; saveDestination=null; confirmRestoredSave=false;}
    private boolean resumeSave() {
        Bundle request=saveRequest; Uri to=saveDestination;
        if(request!=null && profile!=null && profile.root.toASCIIString().equals(request.getString("root"))
                && profile.user.equals(request.getString("user"))) {
            String source=request.getString("source",""); boolean archive=request.getBoolean("archive");
            if(source.startsWith(profile.root.toASCIIString()) && (!archive || source.equals(UiGeometry.shareTarget(profile.root,null,source).toASCIIString()))) {
                if(confirmRestoredSave) {
                    show(new AlertDialog.Builder(themed).setTitle(tr("下载"))
                        .setMessage(tr("上次的下载可能已完成或只保存了一部分。请先检查保存目录，再决定是否重新下载。"))
                        .setCancelable(false).setNegativeButton(tr("取消"),(d,w)->clearSave())
                        .setPositiveButton(tr("重新下载"),(d,w)->{confirmRestoredSave=false; resumeSave();}).create());
                    return true;
                }
                clearSave(); download(profile,URI.create(source),to,archive); return true;
            }
        }
        clearSave(); setBusy(false,()->tr("下载会话已失效，请重新选择文件"));
        return false;
    }
    @Override protected void onActivityResult(int req,int code,Intent data) {
        super.onActivityResult(req,code,data); if (req!=PICK && req!=SAVE && req!=BATCH_SAVE && req!=ARCHIVE_SAVE) return;
        pickOpen=false;
        if(req==ARCHIVE_SAVE || req==SAVE) {
            saveDestination=code==RESULT_OK && data!=null?data.getData():null;
            if(saveDestination==null) clearSave();
            nextShare(); return;
        }
        if(req==BATCH_SAVE) {
            if(code==RESULT_OK && data!=null && data.getData()!=null) pickedTree=data.getData();
            else {pendingDownload=null; pickedTree=null;}
            nextShare(); return;
        }
        if (code==RESULT_OK && data!=null) {
            LinkedHashSet<Uri> set = new LinkedHashSet<>(); addUri(set,data.getData()); ClipData clip=data.getClipData();
            if (clip!=null) for (int i=0;i<clip.getItemCount();i++) addUri(set,clip.getItemAt(i).getUri());
            if (!set.isEmpty()) queue.add(new ArrayList<>(set));
        }
        nextShare();
    }
    private void download(Profile p, URI from, Uri to, boolean archive) {
        setBusy(true,()->tr("正在下载…"));
        worker.execute(() -> {
            try {
                if(archive && !DufsDirectory.fetch(p.client,from).canArchive) throw new DufsClient.HttpError(403);
                try (OutputStream out=getContentResolver().openOutputStream(to,"wt")) {
                    if (out==null) throw new IOException("无法打开保存位置");
                    if(archive) p.client.downloadArchive(from,out); else p.client.download(from,out);
                }
            } catch (Exception e) { ui(() -> { setBusy(false,()->tr("下载失败（保存位置可能有不完整文件）：")+explain(e)); nextShare(); }); return; }
            ui(() -> { setBusy(false,()->tr("下载完成")); nextShare(); });
        });
    }
    private boolean canRead(Uri uri) {
        return checkUriPermission(uri,android.os.Process.myPid(),android.os.Process.myUid(),Intent.FLAG_GRANT_READ_URI_PERMISSION)==android.content.pm.PackageManager.PERMISSION_GRANTED;
    }
    private String displayName(Uri uri) {
        if (!canRead(uri)) return tr("未授权文件");
        try (Cursor c=getContentResolver().query(uri,new String[]{OpenableColumns.DISPLAY_NAME},null,null,null)) {
            if (c!=null && c.moveToFirst()) { String n=c.getString(0); if(n!=null) return n; }
        } catch (RuntimeException ignored) {}
        return tr("共享文件-")+UUID.randomUUID().toString().substring(0,8);
    }
    private long length(Uri uri) {
        try (Cursor c=getContentResolver().query(uri,new String[]{OpenableColumns.SIZE},null,null,null)) {
            if (c!=null && c.moveToFirst() && !c.isNull(0)) return c.getLong(0);
        } catch (RuntimeException ignored) {}
        return -1;
    }
    private void send(Profile p,URI target,List<Uri> batch) {
        if(selecting) {clearSelection(); repaintRows();}
        setBusy(true,()->tr("开始上传…"));
        worker.execute(() -> {
            int ok=0; StringBuilder issues=new StringBuilder(); int reported=0;
            for (int i=0;i<batch.size() && !Thread.currentThread().isInterrupted();i++) {
                Uri src=batch.get(i); final int n=i+1;
                ui(() -> setBusy(true,()->tr("读取第 ")+n+" / "+batch.size()+tr(" 个文件…")));
                String name=displayName(src);
                ui(() -> setBusy(true,()->tr("上传 ")+n+" / "+batch.size()+" · "+name));
                try {
                    if (!canRead(src)) throw new SecurityException();
                    URI dest=availableName(p,target,name);
                    try (InputStream in=getContentResolver().openInputStream(src)) {
                        if (in==null) throw new IOException("无法打开本地文件"); p.client.upload(dest,in,length(src));
                    }
                    ok++;
                } catch (Exception e) { if (reported++<12) issues.append(name).append("：").append(explain(e)).append('\n'); }
            }
            int count=ok, failures=batch.size()-ok;
            ui(() -> {
                java.util.function.Supplier<String> summary=()->tr("上传完成 · 成功 ")+count+tr("，失败 ")+failures;
                setBusy(true,summary);
                show(new AlertDialog.Builder(themed).setTitle(tr("上传结果")).setMessage(tr("成功 ")+count+" / "+batch.size()+tr(" 个文件")+(failures==0?"":"\n\n"+issues+(failures>12?tr("其余失败未展开"):""))).setPositiveButton(tr("知道了"),null).create());
                worker.execute(()->{
                    try {
                        DufsDirectory result=count==0?readItems(p,target):DufsDirectory.from(p.client.listAfterUpload(target));
                        if(!result.exists) throw new DufsClient.HttpError(404);
                        ui(()->{present(target,result,summary);nextShare();});
                    } catch(Exception e) {ui(()->{
                        boolean keep=target.equals(directory) && listing!=null && searchResults==null && !(e instanceof DufsClient.HttpError && (((DufsClient.HttpError)e).code==401 || ((DufsClient.HttpError)e).code==403 || ((DufsClient.HttpError)e).code==404));
                        if(!keep) {directory=target; clearSelection(); clearListing();}
                        setBusy(false,summary); Toast.makeText(this,tr("文件列表未更新，可手动刷新"),Toast.LENGTH_LONG).show(); nextShare();
                    });}
                });
            });
        });
    }
    private URI availableName(Profile p,URI target,String name) throws IOException {
        int dot=name.lastIndexOf('.'); String stem=dot>0?name.substring(0,dot):name, suffix=dot>0?name.substring(dot):"";
        DufsClient.segment(name);
        for (int i=0;i<100;i++) {
            URI path=DufsClient.child(target,i==0?name:stem+" ("+i+")"+suffix,false); int code=p.client.status(path);
            if (code==404) return path;
            if (!((code>=200 && code<300) || code==301 || code==308)) throw new DufsClient.HttpError(code);
        }
        throw new IOException("同名文件过多");
    }
    private String explain(Exception e) {
        if (e instanceof DufsClient.HttpError) {
            int c=((DufsClient.HttpError)e).code;
            if (c==401) return tr("账号或密码错误，或服务器未接受 Basic 认证 (401)");
            if (c==403) return tr("当前账号或服务器不允许此操作 (403)");
            if (c==405) return tr("服务端不支持此操作，或目标已存在 (405)");
            if (c==412) return tr("同名目标已存在，未覆盖 (412)");
            if (c>=300 && c<400) return tr("服务器要求跳转，请填写最终地址 (HTTP ")+c+")";
            return "HTTP "+c;
        }
        if (e instanceof javax.net.ssl.SSLException) return tr("HTTPS 证书验证失败");
        if (e instanceof JSONException) return tr("未返回 Dufs 目录，请确认地址、路径和版本");
        if (e instanceof SecurityException) return tr("来源应用未授权读取该文件");
        if (e instanceof IllegalArgumentException) return tr("无效文件名或地址");
        if (e instanceof IOException) return tr("连接或文件读写失败，请检查网络、文件和服务状态");
        return tr("操作失败，请检查连接配置");
    }
    private int dp(float v) { return (int)(v*getResources().getDisplayMetrics().density+0.5f); }
    private GradientDrawable card(int color) { GradientDrawable d=new GradientDrawable(); d.setColor(color);d.setCornerRadius(getResources().getDimension(R.dimen.surface_radius));return d; }
    private void stylePopup(PopupWindow window) {
        GradientDrawable background=card(surface);
        int edge=themed.getColor(R.color.popup_edge);
        if(edge!=0) background.setStroke(dp(1),edge);
        window.setBackgroundDrawable(background);
        window.setOutsideTouchable(true);
        window.setElevation(dp(edge==0?8:4));
    }
    private TextView body(String text,int size,int color) { TextView v=new TextView(themed);v.setText(text);v.setTextColor(color);v.setTextSize(size);v.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG);v.setTextLocale(Locale.forLanguageTag(language()));return v; }
    private void styleField(EditText field) {
        field.setBackgroundTintList(null); field.setBackground(card(fill)); field.setTextColor(ink); field.setHintTextColor(themed.getColor(R.color.input_hint));
        field.setMinHeight(dp(48)); field.setPadding(dp(12),dp(8),dp(12),dp(8));
    }
    private void styleButton(Button b,boolean filled) {
        boolean dark=(themed.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;
        b.setAllCaps(false); b.setTextColor(new ColorStateList(new int[][]{new int[]{-android.R.attr.state_enabled}, new int[0]},new int[]{soft,dark?ink:accent}));
        b.setBackgroundTintList(null); b.setStateListAnimator(null); b.setElevation(0);
        b.setBackground(new RippleDrawable(ColorStateList.valueOf((accent&0xffffff)|0x22000000),card(filled?fill:0),card(0xffffffff)));
        b.setMinWidth(0); b.setMinimumWidth(0); b.setMinHeight(dp(48)); b.setMinimumHeight(dp(48));
        b.setPadding(dp(10),0,dp(10),0);
    }
    private Button button(String text,Runnable action) { Button b=new Button(themed);b.setText(text);styleButton(b,true);b.setOnClickListener(v->action.run());return b; }
    private LinearLayout.LayoutParams iconSlot() {
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(46),dp(46)); lp.setMarginStart(dp(4)); return lp;
    }
    private ImageButton icon(int resource,String label,Runnable action) {
        ImageButton b=new ImageButton(themed); b.setImageResource(resource); b.setColorFilter(accent); b.setScaleType(ImageView.ScaleType.CENTER_INSIDE); b.setPadding(dp(12),dp(12),dp(12),dp(12));
        b.setBackground(new RippleDrawable(ColorStateList.valueOf((accent&0xffffff)|0x33000000),card(fill),card(0xffffffff)));
        b.setContentDescription(label); b.setTooltipText(label); b.setOnClickListener(v->action.run());return b;
    }
    private float pullX,pullY;
    private boolean pullEligible, pullCaptured;
    private boolean canPull() {
        return profile!=null && !busy && !selecting && !dragging && active==null && popup==null
            && fileScroll!=null && fileScroll.getScrollY()==0;
    }
    private void showPull(float dy) {
        boolean release=dy>=dp(72);
        float offset=Math.min(dp(80),Math.max(0,dy)*0.55f);
        fileScroll.setTranslationY(release?Math.max(offset,pullHint.getHeight()):offset);
        pullHint.setVisibility(release?View.VISIBLE:View.INVISIBLE);
    }
    @Override public boolean dispatchTouchEvent(MotionEvent event) {
        int a=event.getActionMasked();
        if(a==MotionEvent.ACTION_DOWN) {
            pullX=event.getRawX(); pullY=event.getRawY();
            Rect rect=new Rect();
            pullEligible=canPull() && fileScroll.getGlobalVisibleRect(rect) && rect.contains((int)pullX,(int)pullY)
                && !(topIcon.isShown() && topIcon.getGlobalVisibleRect(rect) && rect.contains((int)pullX,(int)pullY));
            pullCaptured=false;
        }
        float dy=event.getRawY()-pullY, dx=Math.abs(event.getRawX()-pullX);
        if(!canPull() || dx>=dp(45) || dy< -dp(12) || a==MotionEvent.ACTION_POINTER_DOWN || a==MotionEvent.ACTION_CANCEL) pullEligible=false;
        if(a==MotionEvent.ACTION_MOVE && pullEligible && !pullCaptured
                && dy>ViewConfiguration.get(this).getScaledTouchSlop() && dy>dx) {
            MotionEvent cancel=MotionEvent.obtain(event);
            cancel.setAction(MotionEvent.ACTION_CANCEL); super.dispatchTouchEvent(cancel); cancel.recycle();
            pullCaptured=true;
        }
        if(pullCaptured) {
            showPull(pullEligible && a==MotionEvent.ACTION_MOVE?dy:0);
            if(a==MotionEvent.ACTION_UP || a==MotionEvent.ACTION_CANCEL) {
                boolean refresh=a==MotionEvent.ACTION_UP && pullEligible && dy>=dp(72);
                pullEligible=false; pullCaptured=false;
                if(refresh) {
                    int effect=Build.VERSION.SDK_INT>=30?HapticFeedbackConstants.GESTURE_END:HapticFeedbackConstants.VIRTUAL_KEY;
                    fileScroll.performHapticFeedback(effect);
                    refreshNow();
                }
                flushRows(); applyPendingTheme();
            }
            return true;
        }
        if(a==MotionEvent.ACTION_UP) pullEligible=false;
        return super.dispatchTouchEvent(event);
    }
    private void buildUi() {
        palette(); ink=themed.getColor(R.color.ink); soft=themed.getColor(R.color.muted); accent=themed.getColor(R.color.accent); danger=themed.getColor(R.color.danger); surface=themed.getColor(R.color.surface); fill=themed.getColor(R.color.fill);
        int page=themed.getColor(R.color.page); boolean light=(themed.getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK)!=Configuration.UI_MODE_NIGHT_YES;
        getWindow().setStatusBarColor(page); getWindow().setNavigationBarColor(page);
        View decor=getWindow().getDecorView(); int bits=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        decor.setSystemUiVisibility((decor.getSystemUiVisibility()&~bits)|(light?bits:0));
        if(Build.VERSION.SDK_INT>=30 && getWindow().getInsetsController()!=null) {
            int bars=WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
            getWindow().getInsetsController().setSystemBarsAppearance(light?bars:0,bars);
        }
        root=new LinearLayout(themed);root.setOrientation(1); root.setBackgroundColor(page);
        root.setOnApplyWindowInsetsListener((v,i) -> { v.setPadding(i.getSystemWindowInsetLeft(),i.getSystemWindowInsetTop(),i.getSystemWindowInsetRight(),i.getSystemWindowInsetBottom()); return i.consumeSystemWindowInsets(); });
        setContentView(root); root.requestApplyInsets();
        LinearLayout bar=new LinearLayout(themed);bar.setGravity(Gravity.CENTER_VERTICAL);bar.setPaddingRelative(dp(10),0,dp(14),0);root.addView(bar);
        home=icon(R.drawable.ic_home,tr("根目录"),()->{if(!busy&&profile!=null)load(profile.root);}); bar.addView(home,iconSlot());
        location=body("",15,ink);location.setTypeface(null,Typeface.BOLD); location.setSingleLine(true); location.setEllipsize(TextUtils.TruncateAt.MIDDLE);location.setPadding(dp(6),0,0,0);bar.addView(location,new LinearLayout.LayoutParams(0,-2,1));
        tools=icon(R.drawable.ic_menu,tr("菜单"),()->{}); tools.setOnClickListener(this::mainMenu);bar.addView(tools,iconSlot());
        LinearLayout actions=new LinearLayout(themed);actions.setGravity(Gravity.CENTER_VERTICAL);actions.setPaddingRelative(dp(14),0,dp(14),0);
        LinearLayout.LayoutParams actionsRow=new LinearLayout.LayoutParams(-1,dp(46)); actionsRow.topMargin=dp(4);root.addView(actions,actionsRow);
        FrameLayout statusSlot=new FrameLayout(themed);
        actions.addView(statusSlot,new LinearLayout.LayoutParams(0,-1,1));
        status=actionStatus(statusSlot); status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        selectedStatus=actionStatus(statusSlot);
        archiveIcon=icon(R.drawable.ic_download,tr("归档下载"),this::chooseArchive); actions.addView(archiveIcon,iconSlot());
        uploadIcon=icon(R.drawable.ic_upload,tr("上传"),this::chooseFiles); actions.addView(uploadIcon,iconSlot());
        refreshIcon=icon(R.drawable.ic_refresh,tr("刷新"),this::refreshNow); actions.addView(refreshIcon,iconSlot());
        searchIcon=icon(R.drawable.ic_search,tr("搜索"),this::searchDialog); actions.addView(searchIcon,iconSlot());
        batchMoveIcon=icon(R.drawable.ic_move,tr("批量移动"),()->beginBatchMove(selectedItems())); actions.addView(batchMoveIcon,iconSlot());
        batchDeleteIcon=icon(R.drawable.ic_delete,tr("批量删除"),()->beginBatchDelete(selectedItems())); actions.addView(batchDeleteIcon,iconSlot());
        batchDownloadIcon=icon(R.drawable.ic_download,tr("批量下载"),()->beginBatchDownload(selectedItems())); actions.addView(batchDownloadIcon,iconSlot());
        selectAllIcon=icon(R.drawable.ic_check,tr("全选"),this::toggleSelectAll); actions.addView(selectAllIcon,iconSlot());
        closeSelectionIcon=icon(R.drawable.ic_close,tr("取消选择"),()->{clearSelection();repaintRows();}); actions.addView(closeSelectionIcon,iconSlot());

        progress=new ProgressBar(themed,null,android.R.attr.progressBarStyleHorizontal);progress.setIndeterminate(true);root.addView(progress,new LinearLayout.LayoutParams(-1,dp(3)));
        FrameLayout listArea=new FrameLayout(themed); root.addView(listArea,new LinearLayout.LayoutParams(-1,0,1));
        fileScroll=new ScrollView(themed); fileScroll.setFillViewport(true); fileScroll.setVerticalScrollBarEnabled(true);
        fileScroll.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY); fileScroll.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_RIGHT);
        if(Build.VERSION.SDK_INT>=31) fileScroll.setScrollCaptureHint(View.SCROLL_CAPTURE_HINT_INCLUDE|View.SCROLL_CAPTURE_HINT_EXCLUDE_DESCENDANTS);
        listArea.addView(fileScroll,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout content=new LinearLayout(themed); content.setOrientation(1); content.setPadding(dp(14),dp(2),dp(14),dp(12)); fileScroll.addView(content);
        pullHint=body(tr("松手更新"),13,accent); pullHint.setGravity(Gravity.CENTER);
        pullHint.setMinHeight(dp(39)); pullHint.setPadding(dp(14),dp(3),dp(14),dp(3));
        listArea.addView(pullHint,new FrameLayout.LayoutParams(-1,-2,Gravity.TOP)); pullHint.setVisibility(View.INVISIBLE);
        entries=new LinearLayout(themed);entries.setOrientation(1);content.addView(entries);
        topIcon=icon(R.drawable.ic_top,tr("返回顶部"),()->fileScroll.smoothScrollTo(0,0));
        GradientDrawable circle=new GradientDrawable(); circle.setShape(GradientDrawable.OVAL);circle.setColor(fill);
        topIcon.setBackground(new RippleDrawable(ColorStateList.valueOf((accent&0xffffff)|0x33000000),circle,null));
        installFloatingTop(listArea,topIcon);
        fileScroll.setOnScrollChangeListener((v,x,y,oldX,oldY)->{if(v==fileScroll) topIcon.setVisibility(y>dp(140)?View.VISIBLE:View.GONE);});
        updateBrowserChrome();
    }
    private void installFloatingTop(FrameLayout area,View button) {
        int size=dp(46), inset=dp(16), bottomClearance=dp(64);
        button.setTooltipText(null); // Reserve touch long press for dragging.
        area.addView(button,new FrameLayout.LayoutParams(size,size,Gravity.LEFT|Gravity.TOP));
        button.setVisibility(View.INVISIBLE);
        class Position {
            int left() {return Math.min(inset,Math.max(0,(area.getWidth()-size)/2));}
            int top() {return Math.min(inset,Math.max(0,(area.getHeight()-size)/2));}
            int spanX() {return Math.max(0,area.getWidth()-size-2*left());}
            int spanY() {return Math.max(0,area.getHeight()-size-top()-bottomClearance);}
            void show(float x,float y) {
                button.setTranslationX(left()+Math.max(0,Math.min(x-left(),spanX())));
                button.setTranslationY(top()+Math.max(0,Math.min(y-top(),spanY())));
            }
            void restore() {
                show(left()+topX*spanX(),Float.isNaN(topY)?area.getHeight()*0.8f-size/2f:top()+topY*spanY());
            }
        }
        Position position=new Position();
        area.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->position.restore());
        class Drag implements View.OnTouchListener, View.OnLongClickListener {
            float downX,downY,startX,startY;
            boolean held,moved;
            int pointer=-1;
            @Override public boolean onLongClick(View v) {
                if(pointer==-1) return false;
                held=true;
                return true; // Native long-click feedback and click suppression.
            }
            @Override public boolean onTouch(View v,MotionEvent event) {
                int action=event.getActionMasked();
                if(action==MotionEvent.ACTION_DOWN) {
                    pointer=event.getPointerId(0); held=moved=false;
                    downX=event.getRawX(); downY=event.getRawY();
                    startX=v.getTranslationX(); startY=v.getTranslationY();
                }
                if(held && action==MotionEvent.ACTION_MOVE && event.getPointerCount()==1 && event.getPointerId(0)==pointer) {
                    position.show(startX+event.getRawX()-downX,startY+event.getRawY()-downY);
                    moved=true;
                    return true;
                }
                if(action==MotionEvent.ACTION_UP || action==MotionEvent.ACTION_CANCEL) {
                    pointer=-1;
                    if(held && moved) {
                        if(action==MotionEvent.ACTION_UP) {
                            if(position.spanX()>0) topX=(v.getTranslationX()-position.left())/position.spanX();
                            if(position.spanY()>0) topY=(v.getTranslationY()-position.top())/position.spanY();
                            getPreferences(MODE_PRIVATE).edit().putFloat("topButtonX",topX).putFloat("topButtonY",topY).apply();
                        }
                        position.restore();
                    }
                    held=moved=false;
                }
                return false;
            }
        }
        Drag drag=new Drag(); button.setOnLongClickListener(drag); button.setOnTouchListener(drag);
    }
    private TextView actionStatus(FrameLayout slot) {
        ToolbarStatus text=new ToolbarStatus(themed);
        text.setTextColor(accent); text.setTextLocale(Locale.forLanguageTag(language()));
        text.setOnLongClickListener(v->{
            if(!text.isClipped() || active!=null) return false;
            show(new AlertDialog.Builder(themed).setMessage(text.getText()).setPositiveButton(tr("知道了"),null).create());
            return true;
        });
        slot.addView(text,new FrameLayout.LayoutParams(-1,-1)); return text;
    }
    private void updateStatusText() {
        String label=lastStatus.get();
        if(showingConnectedStatus && listing!=null) {
            label=tr("已连接 · ")+listing.items.size()+tr(" 项");
            if(checkingPermissions) label+=tr(" · 核验权限中…");
            else if(unverifiedPermissions>0) label+=" · "+unverifiedPermissions+tr(" 项权限待确认");
        }
        if(!TextUtils.equals(status.getText(),label)) status.setText(label);
    }
    private void setConnectedStatus(boolean checking,int incomplete) {
        busy=false; showingConnectedStatus=true; checkingPermissions=checking;
        unverifiedPermissions=incomplete; updateBrowserChrome();
    }
    private void setBusy(boolean value,java.util.function.Supplier<String> label) {
        busy=value;lastStatus=label;showingConnectedStatus=false;updateBrowserChrome();
    }
    private void updateBrowserChrome() {
        updateStatusText();progress.setVisibility(busy?View.VISIBLE:View.INVISIBLE);tools.setEnabled(!busy);
        boolean online=profile!=null;
        URI current=online?(directory==null?profile.root:directory):null;
        home.setEnabled(!busy && online);
        updateSelectionBar();
        String path=searchResults!=null?tr("搜索结果"):!online ? "Dufs" : (current.equals(profile.root)?rootLabel():current.getPath().substring(profile.root.getPath().length()-1));
        if(!TextUtils.equals(location.getText(),path)) {
            location.setText(path); location.setContentDescription(path); location.setTooltipText(path);
        }
        if (busy) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}

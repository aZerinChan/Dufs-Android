"""Check Activity wiring; Android touches/layout still require a device."""
from pathlib import Path

p=Path(__file__).resolve().parents[1]
s=(p/'src/local/dufs/manage/MainActivity.java').read_text(encoding='utf8')
# Each way of adding selections reaches the same debounced, scoped permission path.
for start,end in [('if(!searching) row.setOnLongClickListener','DufsDirectory.Actions rights='),('private void setSelected','private void startDrag'),('private boolean handleDrag','private List<Item> selectedItems'),('private void toggleSelectAll','private static final class Batch')]:
    assert 'scheduleSelectionPermissionCheck();' in s.split(start,1)[1].split(end,1)[0],start
present=s.split('private void present(',1)[1].split('private void checkSelectedPermissions',1)[0]
assert 'captureSelected' not in present and 'worker.execute' not in present
callback=s.split('captureSelected(data,missing',1)[1].split('private void checkItemMenu',1)[0]
assert 'if(listing!=data || profile!=p) return;' in callback and 'updateSelectionBar();' in callback
assert 'attemptedSelectionRights.addAll(result.started)' in callback and 'attemptedSelectionRights.addAll(missing)' not in s, 'Only probes that actually started may be excluded from the next wave'
assert 'Gravity.CENTER_VERTICAL|Gravity.START' in Path(__file__).resolve().parents[1].joinpath('src/local/dufs/manage/ToolbarStatus.java').read_text(encoding='utf8')
assert 'bar.addView(up,' not in s and 'tr("刷新"),false,this::refreshNow' not in s
for name in ('archiveIcon','uploadIcon','refreshIcon','searchIcon','batchMoveIcon','batchDeleteIcon','batchDownloadIcon'):
    assert 'showCompactAction('+name in s, name
assert 'showCompactAction(refreshIcon,!selecting && profile!=null && !canUpload);' in s
assert 'icon.setVisibility(available?View.VISIBLE:View.GONE)' in s
rows=s.split('private void renderRows()',1)[1].split('private DufsDirectory.Actions rights(',1)[0]
assert 'glyph.setMarginStart(dp(8))' in rows
assert rows.index('container.addView(row,') < rows.index('container.addView(rowAction,')
assert 'rowAction.addView(check,' in rows and 'rowAction.addView(more,' in rows
settings=s.split('private void editSettings()',1)[1].split('private EditText field(',1)[0]
assert '.setNeutralButton(tr("测试连接"),null).setPositiveButton(tr("保存"),null)' in settings
assert 'final boolean persist=v==save;' in settings and 'if(persist) draft.save(this);' in settings
assert settings.index('readItems(draft,draft.root)') < settings.index('if(persist) draft.save(this);')
assert 'if(persist) {profile=draft;' in settings and '连接成功，未保存。' in settings
print('PASS static selection, fixed row geometry and test/save connection wiring')

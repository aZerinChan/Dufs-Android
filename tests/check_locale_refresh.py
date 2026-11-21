"""Activity wiring checks; Android visual behaviour needs a device."""
from pathlib import Path
p=Path(__file__).resolve().parents[1]
s=(p/'src/local/dufs/manage/MainActivity.java').read_text(encoding='utf8')
section=lambda a,b:s.split(a,1)[1].split(b,1)[0]
assert 'lastStatus.get()' in section('private void updateStatusText()', 'private void setConnectedStatus('), 'Status must render from fresh translations after a language change'
assert 'setBusy(false,()->tr("读取目录失败：")+explain(e))' in s
assert 'setBusy(true,()->tr("读取第 ")' in s and 'setBusy(true,()->tr("上传 ")' in s
assert 'setBusy(false,()->tr("系统未提供文件选择器"))' in s
switch=section('private void languageDialog()', 'private void sortDialog()')
assert 'setLayoutDirection(View.LAYOUT_DIRECTION_LTR)' in switch and 'Gravity.LEFT' in switch
assert 'setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG)' in switch
assert 'row.getCheckMarkDrawable()' in switch and 'setCompoundDrawablesWithIntrinsicBounds(mark,null,null,null)' in switch and 'btn_radio' not in switch, 'Move the themed native radio to the left'
assert 'UiGeometry.modifiedLabel(item.modified,java.time.ZoneId.systemDefault(),language())' in s
assert 'statusDetail' not in s and 'new ToolbarStatus(themed)' in s, 'Keep the status in the toolbar'
status_view=(p/'src/local/dufs/manage/ToolbarStatus.java').read_text(encoding='utf8')
assert 'setMaxLines(3)' in status_view and 'UiGeometry.fitStatus(' in status_view and 'StaticLayout.Builder.obtain(' in status_view
assert 'requestLayout()' in status_view.split('onTextChanged(',1)[1].split('onMeasure(',1)[0], 'Status text changes must remeasure even at fixed bounds'
assert 'form.addView(body(tr("账号说明（可留空）")' not in s
print('PASS locale rerender, date layout, chooser alignment and settings wiring')

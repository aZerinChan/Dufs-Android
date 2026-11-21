"""Static wiring checks; Android process recreation still needs device testing."""
from pathlib import Path
s = (Path(__file__).resolve().parents[1]/'src/local/dufs/manage/MainActivity.java').read_text(encoding='utf8')
restore = s.split('public void onCreate(Bundle state)', 1)[1].split('void onSaveInstanceState', 1)[0]
save = s.split('void onSaveInstanceState', 1)[1].split('void onNewIntent', 1)[0]
assert 'state.getString("sharePathDraft")' in restore and 'state.putString("sharePathDraft",sharePathDraft)' in save, 'Unsubmitted text must survive recreation'
assert 'sharePathDraft=s.toString()' in s and 'path.setText(sharePathDraft)' in s
assert 'state.getParcelable("saveDestination")' in restore and 'state.putParcelable("saveDestination",saveDestination)' in save
assert 'if(saveRequest!=null) state.putBundle("saveRequest",saveRequest)' in save, 'Save request must persist after picker closes'
callback = s.split('void onActivityResult', 1)[1].split('private void download(', 1)[0]
assert 'if(req==ARCHIVE_SAVE || req==SAVE)' in callback and 'saveDestination=' in callback and 'download(' not in callback
assert 'if(saveDestination!=null && resumeSave()) return;' in s
assert 'confirmRestoredSave=saveDestination!=null;' in restore and 'if(confirmRestoredSave)' in s
print('PASS static share draft and file/ZIP picker lifecycle wiring')

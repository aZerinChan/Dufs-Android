"""Static guard for upload progress when a share arrives during multiselect."""
from pathlib import Path
import re

source = (Path(__file__).resolve().parents[1] / 'src/local/dufs/manage/MainActivity.java').read_text(encoding='utf8')
send = source.split('private void send(', 1)[1].split('private URI availableName(', 1)[0]
assert re.search(r'if\(selecting\)\s*\{\s*clearSelection\(\);\s*repaintRows\(\);\s*\}\s*setBusy\(true,', send), 'An accepted upload must exit selection before reporting progress'
selection = source.split('private void clearSelection()', 1)[1].split('private void setSelected(', 1)[0]
assert 'selecting=false' in selection
print('PASS static upload toolbar transition')

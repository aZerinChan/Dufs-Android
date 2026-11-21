"""Check Android wiring not exercised by the JVM/Dufs protocol tests."""
from pathlib import Path
import re
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
source = (root / 'src/local/dufs/manage/MainActivity.java').read_text(encoding='utf-8')

def block(start, end):
    return source.split(start, 1)[1].split(end, 1)[0]

restore = block('String plan=state.getString("batchDownloadPlan");', 'setBusy(true, tr("读取保存的连接…"))')
assert 'confirmRestoredDownload=true;' in restore, 'Saved picker plans must require confirmation even when snapshot predates writing'
resume = block('private void resumeBatchDownload()', 'private void beginBatchDelete(')
assert resume.index('if(confirmRestoredDownload)') < resume.index('worker.execute('), 'Restore gate must precede background validation and writing'
gate = resume.split('if(confirmRestoredDownload)', 1)[1].split('setBusy(true', 1)[0]
assert re.search(r'setPositiveButton\([^\n]+confirmRestoredDownload=false;\s*resumeBatchDownload\(\);', gate), 'Only explicit confirmation may proceed'
assert re.search(r'setNegativeButton\([^\n]+pendingDownload=null;\s*pickedTree=null;', gate), 'Cancel must discard both saved selection and tree'
assert re.search(r'\.create\(\)\);\s*return;', gate), 'Showing the confirmation must return without scheduling writes'
assert 'address.setText(profile == null ? "http://host:5000" : profile.root.toString());' in source
assert not any(t in source for t in ('gatewayAddress', 'ConnectivityManager', 'RouteInfo', 'NetworkCapabilities', 'LinkProperties'))
ns = '{http://schemas.android.com/apk/res/android}'
assert all(p.get(ns+'name') != 'android.permission.ACCESS_NETWORK_STATE' for p in ET.parse(root/'AndroidManifest.xml').findall('uses-permission'))
print('PASS static Android replay confirmation and first-run server template wiring')

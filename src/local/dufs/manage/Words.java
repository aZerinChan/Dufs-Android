package local.dufs.manage;

import java.util.Locale;

/** Exact lookup for UI literals, including whitespace in concatenated fragments. */
public final class Words {
    private Words() {}

    private static final String[] CODES={"auto","en","zh-CN","zh-TW","vi","ru","fa","ar","az","bn","bqi-IR"};
    private static final String[] LABELS={"auto","English","中文简体","中文繁體","Tiếng Việt","Русский","فارسی","عربي","Azərbaycanca","বাংলা","لۊری بختیاری"};
    public static String[] codes() {return CODES.clone();}
    public static String[] labels() {return LABELS.clone();}
    public static String preference(String value) {
        if("zh".equalsIgnoreCase(value) || "zh-rCN".equalsIgnoreCase(value)) return "zh-CN";
        if("zh-rTW".equalsIgnoreCase(value)) return "zh-TW";
        if("bqi-rIR".equalsIgnoreCase(value)) return "bqi-IR";
        for(String code:CODES) if(code.equalsIgnoreCase(value)) return code;
        return "auto";
    }
    /** Explicit choice wins; Auto follows the system's primary BCP-47 tag. */
    public static String choose(String requested, String systemLanguage) {
        String selected=preference(requested); if(!"auto".equals(selected)) return selected;
        Locale locale=Locale.forLanguageTag(systemLanguage==null?"":systemLanguage.replace('_','-'));
        String lang=locale.getLanguage(), region=locale.getCountry();
        if("zh".equals(lang)) {
            if("Hans".equals(locale.getScript())) return "zh-CN";
            return "Hant".equals(locale.getScript()) || "TW".equals(region) || "HK".equals(region) || "MO".equals(region)?"zh-TW":"zh-CN";
        }
        if("bqi".equals(lang)) return "bqi-IR";
        selected=preference(lang); return "auto".equals(selected)?"en":selected;
    }

    public static String text(String locale,String key) {
        if(key==null || "zh-CN".equals(locale)) return key;
        String value;
        switch(locale) {
            case "zh-TW": case "vi": case "ru": value=LocaleEast.text(locale,key); break;
            case "fa": case "ar": case "bqi-IR": value=LocaleRtl.text(locale,key); break;
            case "az": case "bn": value=LocaleAzBn.text(locale,key); break;
            default: value=null;
        }
        return value==null?text(false,key):value;
    }

    /** Preserve Chinese, nulls, and unrecognized strings as supplied. */
    public static String text(boolean zh, String s) {
        if (zh || s == null) return s;
        switch (s) {
            // Settings and connection
            case "跟随系统": return "Follow system";
            case "浅色": return "Light";
            case "深色": return "Dark";
            case "外观": return "Appearance";
            case "无法保存外观选择": return "Could not save the appearance setting";
            case "语言": return "Language";
            case "简体中文": return "Simplified Chinese";
            case "连接设置": return "Connection settings";
            case "Dufs 连接设置": return "Dufs connection settings";
            case "服务器地址（IP/域名）": return "Server address (IP or domain)";
            case "账号（无认证可留空）": return "Username (optional without authentication)";
            case "账号（可选）": return "Username (optional)";
            case "服务器地址": return "Server address";
            case "服务器地址示例": return "Server address example";
            case "账号说明（可留空）": return "Leave blank if the server does not require authentication.";
            case " 等 ": return " · ";
            case " 个文件": return " files";
            case "密码": return "Password";
            case "显示密码": return "Show password";
            case "隐藏密码": return "Hide password";
            case "测试连接": return "Test connection";
            case "保存": return "Save";
            case "连接成功，未保存。": return "Connection successful. Not saved.";
            case "连接失败：": return "Connection failed: ";
            case "准备连接": return "Ready to connect";
            case "读取保存的连接…": return "Loading saved connection…";
            case "请填写服务器地址和账号密码": return "Enter the server address and credentials";
            case "无法读取保存的配置，请重新填写": return "Could not load saved settings. Enter them again.";
            case "账号不能包含冒号；填写密码时需要账号。": return "Usernames cannot contain a colon. A password requires a username.";
            case "正在连接…": return "Connecting…";
            case "正在验证连接…": return "Verifying connection…";
            case "连接未保存：": return "Connection not saved: ";
            case "Dufs 文件助手": return "Dufs File Manager";

            // Navigation, directory listing, and selection
            case "菜单": return "Menu";
            case "重试": return "Retry";
            case "文件夹": return "Folder";
            case "上传时由服务器创建目录": return "The server will create this directory on upload.";
            case "请选择其他可写目录": return "Choose another writable directory.";
            case "永久删除所选项目及其中的文件？": return "Permanently delete the selected items and their contents?";
            case "取消": return "Cancel";
            case "确定": return "OK";
            case "关闭": return "Close";
            case "知道了": return "Got it";
            case "名称": return "Name";
            case "根目录": return "Root directory";
            case "目录：": return "Directory: ";
            case "上一级":
            case "上一层": return "Parent directory";
            case "↑ 上一级": return "↑ Parent directory";
            case "返回顶部": return "Back to top";
            case "刷新": return "Refresh";
            case "下拉刷新": return "Pull to refresh";
            case "松手更新": return "Release to refresh";
            case "归档下载": return "Download archive";
            case "排序": return "Sort";
            case "最后修改时间": return "Last modified";
            case "文件大小": return "File size";
            case "升序": return "Ascending";
            case "降序": return "Descending";
            case "选择": return "Select";
            case "全选": return "Select all";
            case "取消全选": return "Deselect all";
            case "取消选择": return "Deselect";
            case "已选择": return "Selected";
            case "批量下载": return "Batch download";
            case "重新下载": return "Download again";
            case "上次的下载可能已完成或只保存了一部分。请先检查保存目录，再决定是否重新下载。": return "The previous download may be complete or partial. Check the destination before downloading again.";
            case "批量下载已中断，保存位置可能有不完整的文件。请检查后重新选择。": return "Batch download was interrupted. Check the destination for incomplete files before selecting again.";
            case "每批最多下载 500 个项目": return "Select up to 500 items per download batch.";
            case "批量删除": return "Batch delete";
            case "批量移动": return "Batch move";
            case "请等当前操作完成": return "Wait for the current operation to finish";
            case "读取目录": return "Load directory";
            case "↻ 重试读取目录": return "↻ Retry loading directory";
            case "读取目录…": return "Loading directory…";
            case "读取失败：": return "Could not load: ";
            case "读取目录失败：": return "Could not load directory: ";
            case "请先读取目录": return "Load the directory first";
            case "已连接 · ": return "Connected · ";
            case " 项": return " item(s)";
            case " · 核验权限中…": return " · Checking permissions…";
            case " 项权限待确认": return " item(s) with unverified permissions";
            case "待确认权限": return "Permissions not yet verified";
            case "请刷新读取当前目录权限": return "Refresh to check permissions for this directory";
            case "文件夹 · 点击打开": return "Folder · Tap to open";
            case " · 点击下载": return " · Tap to download";
            case "的操作": return " — actions";
            case "这个目录没有文件": return "This directory is empty";

            // Search and file operations
            case "搜索": return "Search";
            case "搜索结果": return "Search results";
            case "核验所选权限": return "Check selected permissions";
            case "正在核验所选权限…": return "Checking permissions…";
            case "部分权限未能确认": return "Some permissions could not be confirmed";
            case "无法核验权限：": return "Could not check permissions: ";
            case "此项目没有可用的管理操作": return "No management actions available for this item";
            case "文件列表未更新，可手动刷新": return "File list not updated. You can refresh it manually.";
            case "搜索文件": return "Search files";
            case "搜索当前目录和子目录": return "Search this directory and its subdirectories";
            case "文件名关键词": return "Filename keyword";
            case "请输入关键词": return "Enter a keyword";
            case "请输入有效名称": return "Enter a valid name";
            case "正在搜索…": return "Searching…";
            case "找到 ": return "Found ";
            case "搜索结果 · ": return "Search results · ";
            case "未找到匹配的文件或文件夹": return "No matching files or folders";
            case "搜索失败：": return "Search failed: ";
            case "新建文件夹": return "New folder";
            case "重命名": return "Rename";
            case "移动": return "Move";
            case "删除": return "Delete";
            case "确认删除？": return "Confirm deletion?";
            case "\n将删除该文件夹及其内容。": return "\nThis will delete the folder and its contents.";
            case "\n此操作不能在应用内撤销。": return "\nThis cannot be undone in the app.";
            case "读取目标目录…": return "Loading destination…";
            case "请选择移动目标": return "Choose a move destination";
            case "选择移动目录": return "Choose a destination folder";
            case "移动到：": return "Move to: ";
            case "没有可进入的子文件夹": return "No accessible subfolders";
            case "移到这里": return "Move here";
            case "无法读取目标：": return "Could not load destination: ";
            case "正在": return "In progress: ";
            case "成功": return " succeeded";
            case "失败：": return " failed: ";
            case "（目录未刷新）": return " (directory not refreshed)";

            // Sharing, uploading, and downloading
            case "没有收到可上传的文件（不支持分享文本或网址）": return "No files received to upload. Shared text and links are not supported.";
            case "正在读取文件名…": return "Reading filename…";
            case "共享文件（文件名暂未返回）": return "Shared file (name not yet available)";
            case "共享文件 · 共 ": return "Shared files · ";
            case " 个": return " total";
            case "共享文件": return "Shared file";
            case "共享文件-": return "Shared-file-";
            case "已取消，未上传": return "Canceled. Nothing was uploaded.";
            case "读取上传目录…": return "Loading upload directory…";
            case "选择上传目录": return "Choose an upload directory";
            case "自定义目录": return "Custom directory";
            case "暂无可写目录": return "No writable directories available";
            case "检查目录": return "Check directory";
            case "相对于服务器根目录（/ 表示根目录）": return "Relative to the server root (/ for root)";
            case "此目录不可上传": return "Uploads are not allowed here.";
            case "不能移动到当前目录、所选文件夹或其子目录": return "Choose a different directory, outside the selected folders.";
            case "输入相对路径": return "Enter a relative path";
            case "此目录不可上传，可继续打开子目录": return "Uploads are not allowed here. You can still open subfolders.";
            case "没有子文件夹，可上传到当前目录": return "No subfolders. You can upload to this directory.";
            case "上传到这里": return "Upload here";
            case "选择文件上传": return "Select files to upload";
            case "上传": return "Upload";
            case "上传 ": return "Uploading ";
            case "开始上传…": return "Starting upload…";
            case "读取第 ": return "Reading file ";
            case " 个文件…": return "…";
            case "上传完成 · 成功 ": return "Upload complete · Succeeded: ";
            case "，失败 ": return ", failed: ";
            case " · 目录读取失败：": return " · Could not load directory: ";
            case "上传结果": return "Upload results";
            case "成功 ": return "Uploaded ";
            case "其余失败未展开": return "Additional failures not shown";
            case "系统未提供文件选择器": return "No system file picker available";
            case "系统未提供保存位置选择器": return "No system save picker available";
            case "下载会话已失效，请重新选择文件": return "Download session expired. Select the file again.";
            case "正在下载…": return "Downloading…";
            case "无法打开保存位置": return "Could not open the save location";
            case "下载失败（保存位置可能有不完整文件）：": return "Download failed (a partial file may remain): ";
            case "下载完成": return "Download complete";
            case "未授权文件": return "File access not granted";
            case "无法打开本地文件": return "Could not open local file";
            case "同名文件过多": return "Too many files with the same name";

            // Error messages
            case "账号或密码错误，或服务器未接受 Basic 认证 (401)": return "Invalid credentials, or the server did not accept Basic authentication (401)";
            case "当前账号或服务器不允许此操作 (403)": return "This account or server does not allow the operation (403)";
            case "服务端不支持此操作，或目标已存在 (405)": return "The server does not support this operation, or the destination already exists (405)";
            case "同名目标已存在，未覆盖 (412)": return "A destination with that name exists and was not overwritten (412)";
            case "服务器要求跳转，请填写最终地址 (HTTP ": return "The server redirected the request. Enter the final address (HTTP ";
            case "HTTPS 证书验证失败": return "HTTPS certificate verification failed";
            case "未返回 Dufs 目录，请确认地址、路径和版本": return "No Dufs directory was returned. Check the address, path, and version.";
            case "来源应用未授权读取该文件": return "The source app did not grant access to this file";
            case "无效文件名或地址": return "Invalid filename or address";
            case "连接或文件读写失败，请检查网络、文件和服务状态": return "Connection or file access failed. Check the network, file, and server status.";
            case "操作失败，请检查连接配置": return "Operation failed. Check the connection settings.";
            default: return s;
        }
    }
}

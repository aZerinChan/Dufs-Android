package local.dufs.manage;

/** Exact-key RTL UI dictionary. Whitespace follows the English fragment contract in Words. */
public final class LocaleRtl {
    private LocaleRtl() {}

    public static String text(String locale, String key) {
        if (key == null) return null;
        if ("fa".equalsIgnoreCase(locale)) return fa(key);
        if ("ar".equalsIgnoreCase(locale)) return ar(key);
        if ("bqi-IR".equalsIgnoreCase(locale)) return bqi(key);
        return null;
    }

    private static String fa(String key) {
        switch (key) {
            // Settings and connection
            case "跟随系统": return "پیروی از سیستم";
            case "浅色": return "روشن";
            case "深色": return "تیره";
            case "外观": return "ظاهر";
            case "无法保存外观选择": return "تنظیم ظاهر ذخیره نشد";
            case "语言": return "زبان";
            case "简体中文": return "چینی ساده‌شده";
            case "连接设置": return "تنظیمات اتصال";
            case "Dufs 连接设置": return "تنظیمات اتصال Dufs";
            case "服务器地址（IP/域名）": return "نشانی سرور (IP یا دامنه)";
            case "账号（无认证可留空）": return "نام کاربری (بدون احراز هویت می‌تواند خالی بماند)";
            case "密码": return "گذرواژه";
            case "显示密码": return "نمایش گذرواژه";
            case "隐藏密码": return "پنهان کردن گذرواژه";
            case "测试连接": return "آزمایش اتصال";
            case "保存": return "ذخیره";
            case "连接成功，未保存。": return "اتصال برقرار شد، اما ذخیره نشد.";
            case "连接失败：": return "اتصال برقرار نشد: ";
            case "准备连接": return "آمادهٔ اتصال";
            case "读取保存的连接…": return "در حال بارگیری اتصال ذخیره‌شده…";
            case "请填写服务器地址和账号密码": return "نشانی سرور، نام کاربری و گذرواژه را وارد کنید";
            case "无法读取保存的配置，请重新填写": return "تنظیمات ذخیره‌شده خوانده نشد. دوباره آن‌ها را وارد کنید.";
            case "账号不能包含冒号；填写密码时需要账号。": return "نام کاربری نمی‌تواند دونقطه داشته باشد. برای وارد کردن گذرواژه، نام کاربری لازم است.";
            case "正在连接…": return "در حال اتصال…";
            case "正在验证连接…": return "در حال بررسی اتصال…";
            case "连接未保存：": return "اتصال ذخیره نشد: ";
            case "Dufs 文件助手": return "مدیر فایل Dufs";

            // Navigation and selection
            case "菜单": return "منو";
            case "重试": return "تلاش دوباره";
            case "文件夹": return "پوشه";
            case "上传时由服务器创建目录": return "سرور هنگام بارگذاری این پوشه را می‌سازد.";
            case "请选择其他可写目录": return "پوشهٔ دیگری را انتخاب کنید که بتوان در آن نوشت.";
            case "永久删除所选项目及其中的文件？": return "موارد انتخاب‌شده و محتویاتشان برای همیشه حذف شوند؟";
            case "取消": return "لغو";
            case "确定": return "تأیید";
            case "关闭": return "بستن";
            case "知道了": return "متوجه شدم";
            case "名称": return "نام";
            case "根目录": return "پوشهٔ ریشه";
            case "目录：": return "پوشه: ";
            case "上一级": return "پوشهٔ بالادست";
            case "上一层": return "پوشهٔ بالادست";
            case "↑ 上一级": return "↑ پوشهٔ بالادست";
            case "返回顶部": return "بازگشت به بالا";
            case "刷新": return "تازه‌سازی";
            case "下拉刷新": return "برای تازه‌سازی به پایین بکشید";
            case "松手更新": return "برای تازه‌سازی رها کنید";
            case "归档下载": return "دریافت بایگانی";
            case "排序": return "مرتب‌سازی";
            case "最后修改时间": return "آخرین تغییر";
            case "文件大小": return "اندازهٔ فایل";
            case "升序": return "صعودی";
            case "降序": return "نزولی";
            case "选择": return "انتخاب";
            case "全选": return "انتخاب همه";
            case "取消全选": return "لغو انتخاب همه";
            case "取消选择": return "لغو انتخاب";
            case "已选择": return "انتخاب‌شده";
            case "批量下载": return "دریافت گروهی";
            case "重新下载": return "دریافت دوباره";
            case "上次的下载可能已完成或只保存了一部分。请先检查保存目录，再决定是否重新下载。": return "دریافت قبلی ممکن است کامل شده باشد یا فقط بخشی از آن ذخیره شده باشد. پیش از دریافت دوباره، پوشهٔ مقصد را بررسی کنید.";
            case "批量下载已中断，保存位置可能有不完整的文件。请检查后重新选择。": return "دریافت گروهی قطع شد. ممکن است فایل‌های ناقصی در مقصد مانده باشند. پیش از انتخاب دوباره، مقصد را بررسی کنید.";
            case "每批最多下载 500 个项目": return "در هر نوبت دریافت تا ۵۰۰ مورد انتخاب کنید.";
            case "批量删除": return "حذف گروهی";
            case "批量移动": return "جابه‌جایی گروهی";
            case "请等当前操作完成": return "صبر کنید تا کار فعلی تمام شود";
            case "读取目录": return "بارگیری پوشه";
            case "↻ 重试读取目录": return "↻ تلاش دوباره برای بارگیری پوشه";
            case "读取目录…": return "در حال بارگیری پوشه…";
            case "读取失败：": return "بارگیری ناموفق بود: ";
            case "读取目录失败：": return "پوشه بارگیری نشد: ";
            case "请先读取目录": return "ابتدا پوشه را بارگیری کنید";
            case "已连接 · ": return "متصل شد · ";
            case " 项": return " مورد";
            case " · 核验权限中…": return " · در حال بررسی مجوزها…";
            case " 项权限待确认": return " مورد با مجوز بررسی‌نشده";
            case "待确认权限": return "مجوزها هنوز بررسی نشده‌اند";
            case "请刷新读取当前目录权限": return "برای بررسی مجوزهای این پوشه، آن را تازه‌سازی کنید";
            case "文件夹 · 点击打开": return "پوشه · برای باز کردن بزنید";
            case " · 点击下载": return " · برای دریافت بزنید";
            case "的操作": return " — عملیات";
            case "这个目录没有文件": return "این پوشه خالی است";

            // Search and file operations
            case "搜索": return "جست‌وجو";
            case "搜索结果": return "نتایج جست‌وجو";
            case "核验所选权限": return "بررسی مجوزهای موارد انتخاب‌شده";
            case "正在核验所选权限…": return "در حال بررسی مجوزها…";
            case "部分权限未能确认": return "برخی مجوزها تأیید نشدند";
            case "无法核验权限：": return "بررسی مجوزها ممکن نشد: ";
            case "此项目没有可用的管理操作": return "برای این مورد هیچ عملیات مدیریتی در دسترس نیست";
            case "文件列表未更新，可手动刷新": return "فهرست فایل‌ها به‌روز نشد. می‌توانید آن را دستی تازه کنید.";
            case "搜索文件": return "جست‌وجوی فایل‌ها";
            case "搜索当前目录和子目录": return "جست‌وجو در این پوشه و زیرپوشه‌های آن";
            case "文件名关键词": return "کلیدواژهٔ نام فایل";
            case "请输入关键词": return "یک کلیدواژه وارد کنید";
            case "请输入有效名称": return "نام معتبری وارد کنید";
            case "正在搜索…": return "در حال جست‌وجو…";
            case "找到 ": return "پیدا شد: ";
            case "搜索结果 · ": return "نتایج جست‌وجو · ";
            case "未找到匹配的文件或文件夹": return "هیچ فایل یا پوشهٔ مطابقی پیدا نشد";
            case "搜索失败：": return "جست‌وجو ناموفق بود: ";
            case "新建文件夹": return "پوشهٔ جدید";
            case "重命名": return "تغییر نام";
            case "移动": return "جابه‌جایی";
            case "删除": return "حذف";
            case "确认删除？": return "حذف تأیید می‌شود؟";
            case "\n将删除该文件夹及其内容。": return "\nاین پوشه و محتویاتش حذف می‌شوند.";
            case "\n此操作不能在应用内撤销。": return "\nاین کار در برنامه قابل بازگشت نیست.";
            case "读取目标目录…": return "در حال بارگیری مقصد…";
            case "请选择移动目标": return "مقصد جابه‌جایی را انتخاب کنید";
            case "选择移动目录": return "پوشهٔ مقصد را انتخاب کنید";
            case "移动到：": return "انتقال به: ";
            case "没有可进入的子文件夹": return "هیچ زیرپوشه‌ای قابل دسترسی نیست";
            case "移到这里": return "انتقال به اینجا";
            case "无法读取目标：": return "مقصد بارگیری نشد: ";
            case "正在": return "در حال انجام: ";
            case "成功": return " انجام شد";
            case "失败：": return " ناموفق بود: ";
            case "（目录未刷新）": return " (پوشه تازه‌سازی نشد)";

            // Sharing and transfers
            case "没有收到可上传的文件（不支持分享文本或网址）": return "فایلی برای بارگذاری دریافت نشد. اشتراک‌گذاری متن یا نشانی وب پشتیبانی نمی‌شود.";
            case "正在读取文件名…": return "در حال خواندن نام فایل…";
            case "共享文件（文件名暂未返回）": return "فایل هم‌رسانی‌شده (نام هنوز در دسترس نیست)";
            case "共享文件 · 共 ": return "فایل‌های هم‌رسانی‌شده · ";
            case " 个": return " در مجموع";
            case "共享文件": return "فایل هم‌رسانی‌شده";
            case " 等 ": return " و بقیه · ";
            case " 个文件": return " فایل";
            case "共享文件-": return "فایل-هم‌رسانی-شده-";
            case "已取消，未上传": return "لغو شد. هیچ فایلی بارگذاری نشد.";
            case "读取上传目录…": return "در حال بارگیری پوشهٔ بارگذاری…";
            case "选择上传目录": return "پوشهٔ بارگذاری را انتخاب کنید";
            case "自定义目录": return "پوشهٔ سفارشی";
            case "暂无可写目录": return "فعلاً پوشه‌ای برای نوشتن در دسترس نیست";
            case "检查目录": return "بررسی پوشه";
            case "相对于服务器根目录（/ 表示根目录）": return "نسبت به ریشهٔ سرور (/ یعنی پوشهٔ ریشه)";
            case "此目录不可上传": return "بارگذاری در این پوشه مجاز نیست.";
            case "不能移动到当前目录、所选文件夹或其子目录": return "انتقال به پوشهٔ فعلی، پوشه‌های انتخاب‌شده یا زیرپوشه‌هایشان ممکن نیست.";
            case "输入相对路径": return "مسیر نسبی را وارد کنید";
            case "此目录不可上传，可继续打开子目录": return "بارگذاری در این پوشه مجاز نیست، اما می‌توانید زیرپوشه‌ها را باز کنید.";
            case "没有子文件夹，可上传到当前目录": return "زیرپوشه‌ای نیست. می‌توانید در همین پوشه بارگذاری کنید.";
            case "上传到这里": return "بارگذاری در اینجا";
            case "选择文件上传": return "فایل‌ها را برای بارگذاری انتخاب کنید";
            case "上传": return "بارگذاری";
            case "上传 ": return "در حال بارگذاری ";
            case "开始上传…": return "در حال شروع بارگذاری…";
            case "读取第 ": return "در حال خواندن فایل ";
            case " 个文件…": return "…";
            case "上传完成 · 成功 ": return "بارگذاری پایان یافت · موفق: ";
            case "，失败 ": return "، ناموفق: ";
            case " · 目录读取失败：": return " · پوشه بارگیری نشد: ";
            case "上传结果": return "نتایج بارگذاری";
            case "成功 ": return "بارگذاری شد: ";
            case "其余失败未展开": return "جزئیات خطاهای دیگر نمایش داده نشد";
            case "系统未提供文件选择器": return "انتخاب‌گر فایل در سیستم در دسترس نیست";
            case "系统未提供保存位置选择器": return "انتخاب‌گر محل ذخیره در سیستم در دسترس نیست";
            case "下载会话已失效，请重新选择文件": return "نشست دریافت منقضی شد. فایل را دوباره انتخاب کنید.";
            case "正在下载…": return "در حال دریافت…";
            case "无法打开保存位置": return "محل ذخیره باز نشد";
            case "下载失败（保存位置可能有不完整文件）：": return "دریافت ناموفق بود (ممکن است فایلی ناقص مانده باشد): ";
            case "下载完成": return "دریافت کامل شد";
            case "未授权文件": return "دسترسی به فایل داده نشده است";
            case "无法打开本地文件": return "فایل محلی باز نشد";
            case "同名文件过多": return "تعداد فایل‌های همنام بیش از حد است";

            // Errors
            case "账号或密码错误，或服务器未接受 Basic 认证 (401)": return "نام کاربری یا گذرواژه نادرست است، یا سرور احراز هویت Basic را نپذیرفت (401)";
            case "当前账号或服务器不允许此操作 (403)": return "این حساب یا سرور اجازهٔ این کار را نمی‌دهد (403)";
            case "服务端不支持此操作，或目标已存在 (405)": return "سرور از این عملیات پشتیبانی نمی‌کند یا مقصد از قبل وجود دارد (405)";
            case "同名目标已存在，未覆盖 (412)": return "مقصدی با همین نام وجود دارد و بازنویسی نشد (412)";
            case "服务器要求跳转，请填写最终地址 (HTTP ": return "سرور درخواست را به نشانی دیگری هدایت کرد. نشانی نهایی را وارد کنید (HTTP ";
            case "HTTPS 证书验证失败": return "اعتبارسنجی گواهی HTTPS ناموفق بود";
            case "未返回 Dufs 目录，请确认地址、路径和版本": return "پوشهٔ Dufs برگردانده نشد. نشانی، مسیر و نسخه را بررسی کنید.";
            case "来源应用未授权读取该文件": return "برنامهٔ مبدأ اجازهٔ خواندن این فایل را نداده است";
            case "无效文件名或地址": return "نام فایل یا نشانی نامعتبر است";
            case "连接或文件读写失败，请检查网络、文件和服务状态": return "اتصال یا خواندن و نوشتن فایل ناموفق بود. شبکه، فایل و وضعیت سرور را بررسی کنید.";
            case "操作失败，请检查连接配置": return "عملیات ناموفق بود. تنظیمات اتصال را بررسی کنید.";

            // Additional connection labels
            case "账号（可选）": return "نام کاربری (اختیاری)";
            case "服务器地址": return "نشانی سرور";
            case "服务器地址示例": return "نمونهٔ نشانی سرور";
            case "账号说明（可留空）": return "می‌توانید نام کاربری را خالی بگذارید";

            default: return null;
        }
    }

    private static String ar(String key) {
        switch (key) {
            // Settings and connection
            case "跟随系统": return "اتباع إعداد النظام";
            case "浅色": return "فاتح";
            case "深色": return "داكن";
            case "外观": return "المظهر";
            case "无法保存外观选择": return "تعذّر حفظ إعداد المظهر";
            case "语言": return "اللغة";
            case "简体中文": return "الصينية المبسّطة";
            case "连接设置": return "إعدادات الاتصال";
            case "Dufs 连接设置": return "إعدادات اتصال Dufs";
            case "服务器地址（IP/域名）": return "عنوان الخادم (IP أو اسم النطاق)";
            case "账号（无认证可留空）": return "اسم المستخدم (يمكن تركه فارغًا إذا لم تُطلب المصادقة)";
            case "密码": return "كلمة المرور";
            case "显示密码": return "إظهار كلمة المرور";
            case "隐藏密码": return "إخفاء كلمة المرور";
            case "测试连接": return "اختبار الاتصال";
            case "保存": return "حفظ";
            case "连接成功，未保存。": return "تم الاتصال، لكن لم يُحفظ.";
            case "连接失败：": return "فشل الاتصال: ";
            case "准备连接": return "جاهز للاتصال";
            case "读取保存的连接…": return "جارٍ تحميل الاتصال المحفوظ…";
            case "请填写服务器地址和账号密码": return "أدخل عنوان الخادم وبيانات تسجيل الدخول";
            case "无法读取保存的配置，请重新填写": return "تعذّر تحميل الإعدادات المحفوظة. أدخلها من جديد.";
            case "账号不能包含冒号；填写密码时需要账号。": return "لا يجوز أن يحتوي اسم المستخدم على النقطتين الرأسيتين. إدخال كلمة المرور يتطلب اسم مستخدم.";
            case "正在连接…": return "جارٍ الاتصال…";
            case "正在验证连接…": return "جارٍ التحقق من الاتصال…";
            case "连接未保存：": return "لم يُحفظ الاتصال: ";
            case "Dufs 文件助手": return "مدير ملفات Dufs";

            // Navigation and selection
            case "菜单": return "القائمة";
            case "重试": return "إعادة المحاولة";
            case "文件夹": return "مجلد";
            case "上传时由服务器创建目录": return "سينشئ الخادم هذا المجلد عند الرفع.";
            case "请选择其他可写目录": return "اختر مجلدًا آخر يمكن الكتابة فيه.";
            case "永久删除所选项目及其中的文件？": return "هل تريد حذف العناصر المحددة ومحتوياتها نهائيًا؟";
            case "取消": return "إلغاء";
            case "确定": return "موافق";
            case "关闭": return "إغلاق";
            case "知道了": return "فهمت";
            case "名称": return "الاسم";
            case "根目录": return "المجلد الجذر";
            case "目录：": return "المجلد: ";
            case "上一级": return "المجلد الأعلى";
            case "上一层": return "المجلد الأعلى";
            case "↑ 上一级": return "↑ المجلد الأعلى";
            case "返回顶部": return "العودة إلى الأعلى";
            case "刷新": return "تحديث";
            case "下拉刷新": return "اسحب للأسفل للتحديث";
            case "松手更新": return "أفلت للتحديث";
            case "归档下载": return "تنزيل الأرشيف";
            case "排序": return "فرز";
            case "最后修改时间": return "آخر تعديل";
            case "文件大小": return "حجم الملف";
            case "升序": return "تصاعدي";
            case "降序": return "تنازلي";
            case "选择": return "تحديد";
            case "全选": return "تحديد الكل";
            case "取消全选": return "إلغاء تحديد الكل";
            case "取消选择": return "إلغاء التحديد";
            case "已选择": return "محدد";
            case "批量下载": return "تنزيل دفعة واحدة";
            case "重新下载": return "تنزيل مرة أخرى";
            case "上次的下载可能已完成或只保存了一部分。请先检查保存目录，再决定是否重新下载。": return "ربما اكتمل التنزيل السابق أو حُفظ جزء منه فقط. تحقّق من مجلد الحفظ قبل إعادة التنزيل.";
            case "批量下载已中断，保存位置可能有不完整的文件。请检查后重新选择。": return "انقطع تنزيل الدفعة. تحقّق من مكان الحفظ بحثًا عن ملفات غير مكتملة قبل إعادة التحديد.";
            case "每批最多下载 500 个项目": return "يمكن تحديد 500 عنصر كحد أقصى في كل دفعة تنزيل.";
            case "批量删除": return "حذف دفعة واحدة";
            case "批量移动": return "نقل دفعة واحدة";
            case "请等当前操作完成": return "انتظر حتى تنتهي العملية الحالية";
            case "读取目录": return "تحميل المجلد";
            case "↻ 重试读取目录": return "↻ إعادة محاولة تحميل المجلد";
            case "读取目录…": return "جارٍ تحميل المجلد…";
            case "读取失败：": return "تعذّر التحميل: ";
            case "读取目录失败：": return "تعذّر تحميل المجلد: ";
            case "请先读取目录": return "حمّل المجلد أولًا";
            case "已连接 · ": return "تم الاتصال · ";
            case " 项": return " عنصر";
            case " · 核验权限中…": return " · جارٍ التحقق من الأذونات…";
            case " 项权限待确认": return " عنصر لم تُتحقق أذوناته";
            case "待确认权限": return "لم تُتحقق الأذونات بعد";
            case "请刷新读取当前目录权限": return "حدّث المجلد للتحقق من أذوناته";
            case "文件夹 · 点击打开": return "مجلد · اضغط للفتح";
            case " · 点击下载": return " · اضغط للتنزيل";
            case "的操作": return " — الإجراءات";
            case "这个目录没有文件": return "هذا المجلد فارغ";

            // Search and file operations
            case "搜索": return "بحث";
            case "搜索结果": return "نتائج البحث";
            case "核验所选权限": return "التحقق من أذونات العناصر المحددة";
            case "正在核验所选权限…": return "جارٍ التحقق من الأذونات…";
            case "部分权限未能确认": return "تعذّر تأكيد بعض الأذونات";
            case "无法核验权限：": return "تعذّر التحقق من الأذونات: ";
            case "此项目没有可用的管理操作": return "لا تتوفر إجراءات لإدارة هذا العنصر";
            case "文件列表未更新，可手动刷新": return "لم تُحدّث قائمة الملفات. يمكنك تحديثها يدويًا.";
            case "搜索文件": return "البحث عن الملفات";
            case "搜索当前目录和子目录": return "البحث في هذا المجلد ومجلداته الفرعية";
            case "文件名关键词": return "كلمة مفتاحية لاسم الملف";
            case "请输入关键词": return "أدخل كلمة مفتاحية";
            case "请输入有效名称": return "أدخل اسمًا صالحًا";
            case "正在搜索…": return "جارٍ البحث…";
            case "找到 ": return "عُثر على ";
            case "搜索结果 · ": return "نتائج البحث · ";
            case "未找到匹配的文件或文件夹": return "لم يُعثر على ملفات أو مجلدات مطابقة";
            case "搜索失败：": return "فشل البحث: ";
            case "新建文件夹": return "مجلد جديد";
            case "重命名": return "إعادة تسمية";
            case "移动": return "نقل";
            case "删除": return "حذف";
            case "确认删除？": return "هل تؤكّد الحذف؟";
            case "\n将删除该文件夹及其内容。": return "\nسيُحذف هذا المجلد ومحتوياته.";
            case "\n此操作不能在应用内撤销。": return "\nلا يمكن التراجع عن هذا الإجراء داخل التطبيق.";
            case "读取目标目录…": return "جارٍ تحميل الوجهة…";
            case "请选择移动目标": return "اختر وجهة النقل";
            case "选择移动目录": return "اختر مجلد الوجهة";
            case "移动到：": return "نقل إلى: ";
            case "没有可进入的子文件夹": return "لا توجد مجلدات فرعية يمكن فتحها";
            case "移到这里": return "انقل إلى هنا";
            case "无法读取目标：": return "تعذّر تحميل الوجهة: ";
            case "正在": return "جارٍ التنفيذ: ";
            case "成功": return " نجح";
            case "失败：": return " فشل: ";
            case "（目录未刷新）": return " (لم يُحدّث المجلد)";

            // Sharing and transfers
            case "没有收到可上传的文件（不支持分享文本或网址）": return "لم تُستلم ملفات للرفع. لا تُدعم مشاركة النصوص أو الروابط.";
            case "正在读取文件名…": return "جارٍ قراءة اسم الملف…";
            case "共享文件（文件名暂未返回）": return "ملف تمت مشاركته (الاسم غير متاح بعد)";
            case "共享文件 · 共 ": return "الملفات المشارَكة · ";
            case " 个": return " إجمالًا";
            case "共享文件": return "ملف تمت مشاركته";
            case " 等 ": return " وغيرها · ";
            case " 个文件": return " ملف";
            case "共享文件-": return "ملف-مشارك-";
            case "已取消，未上传": return "أُلغي الرفع. لم يُرفع أي ملف.";
            case "读取上传目录…": return "جارٍ تحميل مجلد الرفع…";
            case "选择上传目录": return "اختر مجلد الرفع";
            case "自定义目录": return "مجلد مخصّص";
            case "暂无可写目录": return "لا توجد حاليًا مجلدات يمكن الكتابة فيها";
            case "检查目录": return "التحقق من المجلد";
            case "相对于服务器根目录（/ 表示根目录）": return "نسبةً إلى المجلد الجذر للخادم (/ تعني المجلد الجذر)";
            case "此目录不可上传": return "لا يُسمح بالرفع في هذا المجلد.";
            case "不能移动到当前目录、所选文件夹或其子目录": return "لا يمكن النقل إلى المجلد الحالي أو المجلدات المحددة أو مجلداتها الفرعية.";
            case "输入相对路径": return "أدخل مسارًا نسبيًا";
            case "此目录不可上传，可继续打开子目录": return "لا يُسمح بالرفع هنا، لكن يمكنك فتح المجلدات الفرعية.";
            case "没有子文件夹，可上传到当前目录": return "لا توجد مجلدات فرعية. يمكنك الرفع في هذا المجلد.";
            case "上传到这里": return "ارفع هنا";
            case "选择文件上传": return "حدّد الملفات المراد رفعها";
            case "上传": return "رفع";
            case "上传 ": return "جارٍ رفع ";
            case "开始上传…": return "جارٍ بدء الرفع…";
            case "读取第 ": return "جارٍ قراءة الملف ";
            case " 个文件…": return "…";
            case "上传完成 · 成功 ": return "اكتمل الرفع · نجح: ";
            case "，失败 ": return "، فشل: ";
            case " · 目录读取失败：": return " · تعذّر تحميل المجلد: ";
            case "上传结果": return "نتائج الرفع";
            case "成功 ": return "تم رفع ";
            case "其余失败未展开": return "لم تُعرض تفاصيل حالات الفشل الأخرى";
            case "系统未提供文件选择器": return "لا تتوفر أداة اختيار الملفات في النظام";
            case "系统未提供保存位置选择器": return "لا تتوفر أداة اختيار مكان الحفظ في النظام";
            case "下载会话已失效，请重新选择文件": return "انتهت جلسة التنزيل. حدّد الملف من جديد.";
            case "正在下载…": return "جارٍ التنزيل…";
            case "无法打开保存位置": return "تعذّر فتح مكان الحفظ";
            case "下载失败（保存位置可能有不完整文件）：": return "فشل التنزيل (قد يبقى ملف غير مكتمل): ";
            case "下载完成": return "اكتمل التنزيل";
            case "未授权文件": return "لم يُمنح إذن الوصول إلى الملف";
            case "无法打开本地文件": return "تعذّر فتح الملف المحلي";
            case "同名文件过多": return "عدد الملفات التي تحمل الاسم نفسه كبير جدًا";

            // Errors
            case "账号或密码错误，或服务器未接受 Basic 认证 (401)": return "اسم المستخدم أو كلمة المرور غير صحيحين، أو أن الخادم لم يقبل مصادقة Basic ‏(401)";
            case "当前账号或服务器不允许此操作 (403)": return "هذا الحساب أو الخادم لا يسمح بهذا الإجراء (403)";
            case "服务端不支持此操作，或目标已存在 (405)": return "الخادم لا يدعم هذا الإجراء، أو أن الوجهة موجودة بالفعل (405)";
            case "同名目标已存在，未覆盖 (412)": return "توجد وجهة بالاسم نفسه ولم يُكتب فوقها (412)";
            case "服务器要求跳转，请填写最终地址 (HTTP ": return "أعاد الخادم توجيه الطلب. أدخل العنوان النهائي (HTTP ";
            case "HTTPS 证书验证失败": return "فشل التحقق من شهادة HTTPS";
            case "未返回 Dufs 目录，请确认地址、路径和版本": return "لم يُرجع الخادم مجلد Dufs. تحقّق من العنوان والمسار والإصدار.";
            case "来源应用未授权读取该文件": return "لم يمنح التطبيق المصدر إذن قراءة هذا الملف";
            case "无效文件名或地址": return "اسم الملف أو العنوان غير صالح";
            case "连接或文件读写失败，请检查网络、文件和服务状态": return "فشل الاتصال أو الوصول إلى الملف. تحقّق من الشبكة والملف وحالة الخادم.";
            case "操作失败，请检查连接配置": return "فشل الإجراء. تحقّق من إعدادات الاتصال.";

            // Additional connection labels
            case "账号（可选）": return "اسم المستخدم (اختياري)";
            case "服务器地址": return "عنوان الخادم";
            case "服务器地址示例": return "مثال على عنوان الخادم";
            case "账号说明（可留空）": return "يمكنك ترك اسم المستخدم فارغًا";

            default: return null;
        }
    }

    // Unreviewed Bakhtiari draft: colloquial spellings, idioms, and technical terms
    // need native review. Explicit Persian fallbacks are marked on their cases.
    private static String bqi(String key) {
        switch (key) {
            // Settings and connection
            case "跟随系统": return "دنبالِ سیستم";
            case "浅色": return "روشن";
            case "深色": return "تاریک";
            case "外观": return "قیافه";
            case "无法保存外观选择": return "تنظیمِ قیافه ذخیره نَبو";
            case "语言": return "زوون";
            case "简体中文": return "چینیِ ساده‌وابی";
            case "连接设置": return "تنظیماتِ وصل";
            case "Dufs 连接设置": return "تنظیماتِ وصلِ Dufs";
            case "服务器地址（IP/域名）": return fa(key); // Persian fallback
            case "账号（无认证可留空）": return fa(key); // Persian fallback
            case "密码": return "رمز";
            case "显示密码": return "رمزِ نشون بده";
            case "隐藏密码": return "رمزِ قایم کن";
            case "测试连接": return "وصلِ امتحون کن";
            case "保存": return "ذخیره کن";
            case "连接成功，未保存。": return "وصل بو، ولی ذخیره نَبو.";
            case "连接失败：": return "وصل نَبو: ";
            case "准备连接": return "آمادهٔ وصل";
            case "读取保存的连接…": return fa(key); // Persian fallback
            case "请填写服务器地址和账号密码": return "آدرسِ سرور و نومِ کاربری و رمزِ بنویس";
            case "无法读取保存的配置，请重新填写": return fa(key); // Persian fallback
            case "账号不能包含冒号；填写密码时需要账号。": return fa(key); // Persian fallback
            case "正在连接…": return "داره وصل ایکنه…";
            case "正在验证连接…": return "داره وصلِ امتحون ایکنه…";
            case "连接未保存：": return "وصل ذخیره نَبو: ";
            case "Dufs 文件助手": return "مدیرِ فایلِ Dufs";

            // Navigation and selection
            case "菜单": return "منو";
            case "重试": return "دوواره امتحون کن";
            case "文件夹": return "پوشه";
            case "上传时由服务器创建目录": return "سرور ای پوشه‌نِ وقتِ آپلود ایسازه.";
            case "请选择其他可写目录": return "یه پوشهٔ دیگه بردار که بشه تیِش نوشت.";
            case "永久删除所选项目及其中的文件？": return "ای چیزایی که برداشتی و هرچی توشونه، بَرِ همیشه پاک بون؟";
            case "取消": return "بی‌خیال";
            case "确定": return "باشه";
            case "关闭": return "بَوَند";
            case "知道了": return "فهمیدُم";
            case "名称": return "نوم";
            case "根目录": return "پوشهٔ ریشه";
            case "目录：": return "پوشه: ";
            case "上一级": return "پوشهٔ بالایی";
            case "上一层": return "پوشهٔ بالایی";
            case "↑ 上一级": return "↑ پوشهٔ بالایی";
            case "返回顶部": return "برگرد بالا";
            case "刷新": return "نو کن";
            case "下拉刷新": return "بِکِش پایین که نو بوه";
            case "松手更新": return "وِل کن که نو بوه";
            case "归档下载": return fa(key); // Persian fallback
            case "排序": return "ردیف کن";
            case "最后修改时间": return "وقتِ آخرین تغییر";
            case "文件大小": return "اندازهٔ فایل";
            case "升序": return "اَ کم به زیاد";
            case "降序": return "اَ زیاد به کم";
            case "选择": return "بردار";
            case "全选": return "همه‌نِ بردار";
            case "取消全选": return "انتخابِ همه‌نِ بردار";
            case "取消选择": return "اَ انتخاب دربیار";
            case "已选择": return "برداشته‌وابی";
            case "批量下载": return "با هم دانلود کن";
            case "重新下载": return "دوواره دانلود کن";
            case "上次的下载可能已完成或只保存了一部分。请先检查保存目录，再决定是否重新下载。": return fa(key); // Persian fallback
            case "批量下载已中断，保存位置可能有不完整的文件。请检查后重新选择。": return fa(key); // Persian fallback
            case "每批最多下载 500 个项目": return "هر دفه تا ۵۰۰ تا بردار بَرِ دانلود.";
            case "批量删除": return "با هم پاک کن";
            case "批量移动": return "با هم جابه‌جا کن";
            case "请等当前操作完成": return "صبر کن تا ای کار تموم بوه";
            case "读取目录": return "پوشه‌نِ بخون";
            case "↻ 重试读取目录": return "↻ پوشه‌نِ دوواره بخون";
            case "读取目录…": return "داره پوشه‌نِ ایخونه…";
            case "读取失败：": return "خونده نَبو: ";
            case "读取目录失败：": return "پوشه خونده نَبو: ";
            case "请先读取目录": return "اول پوشه‌نِ بخون";
            case "已连接 · ": return "وصل بو · ";
            case " 项": return " تا";
            case " · 核验权限中…": return " · داره اجازه‌یا‌نِ وارسی ایکنه…";
            case " 项权限待确认": return fa(key); // Persian fallback
            case "待确认权限": return "اجازه‌یا هنوز وارسی نَوابیدن";
            case "请刷新读取当前目录权限": return "پوشه‌نِ نو کن تا اجازه‌یاش وارسی بون";
            case "文件夹 · 点击打开": return "پوشه · بزن تا وا بوه";
            case " · 点击下载": return " · بزن بَرِ دانلود";
            case "的操作": return " — کاریا";
            case "这个目录没有文件": return "ای پوشه خالیه";

            // Search and file operations
            case "搜索": return "بگرد";
            case "搜索结果": return "نتیجه‌های گشتن";
            case "核验所选权限": return fa(key); // Persian fallback
            case "正在核验所选权限…": return "داره اجازه‌یا‌نِ وارسی ایکنه…";
            case "部分权限未能确认": return "یه چَن تا اجازه تأیید نَبو";
            case "无法核验权限：": return "اجازه‌یا وارسی نَوابیدن: ";
            case "此项目没有可用的管理操作": return fa(key); // Persian fallback
            case "文件列表未更新，可手动刷新": return "لیستِ فایل‌یا نو نَبو؛ خوت نُوش کن.";
            case "搜索文件": return "بگرد دُنبالِ فایل‌یا";
            case "搜索当前目录和子目录": return "تی ای پوشه و پوشه‌های توش بگرد";
            case "文件名关键词": return fa(key); // Persian fallback
            case "请输入关键词": return "یه کلمه بَرِ گشتن بنویس";
            case "请输入有效名称": return "یه نومِ درست بنویس";
            case "正在搜索…": return "داره ایگرده…";
            case "找到 ": return "پیدا بو: ";
            case "搜索结果 · ": return "نتیجه‌های گشتن · ";
            case "未找到匹配的文件或文件夹": return "فایل یا پوشه‌ای که بخوره به ای گشتن پیدا نَبو";
            case "搜索失败：": return "گشتن جور نَبو: ";
            case "新建文件夹": return "پوشهٔ نو";
            case "重命名": return "نومِ عوض کن";
            case "移动": return "جابه‌جا کن";
            case "删除": return "پاک کن";
            case "确认删除？": return "پاکش کنم؟";
            case "\n将删除该文件夹及其内容。": return "\nای پوشه و هرچی توشه پاک ایبوه.";
            case "\n此操作不能在应用内撤销。": return "\nای کارِ تی برنامه نِشه برگردوند.";
            case "读取目标目录…": return "داره پوشهٔ مقصدِ ایخونه…";
            case "请选择移动目标": return "جا بَرِ جابه‌جایی بردار";
            case "选择移动目录": return "پوشهٔ مقصدِ بردار";
            case "移动到：": return "جابه‌جا کن به: ";
            case "没有可进入的子文件夹": return "پوشه‌ای توش نی که بشه واش کرد";
            case "移到这里": return "بیارش ایجا";
            case "无法读取目标：": return "مقصد خونده نَبو: ";
            case "正在": return "داره انجام ایبوه: ";
            case "成功": return " انجام بو";
            case "失败：": return " جور نَبو: ";
            case "（目录未刷新）": return " (پوشه نو نَبو)";

            // Sharing and transfers
            case "没有收到可上传的文件（不支持分享文本或网址）": return fa(key); // Persian fallback
            case "正在读取文件名…": return "داره نومِ فایلِ ایخونه…";
            case "共享文件（文件名暂未返回）": return fa(key); // Persian fallback
            case "共享文件 · 共 ": return "فایل‌های همرسانی‌وابی · ";
            case " 个": return " رویِ هم";
            case "共享文件": return "فایلِ همرسانی‌وابی";
            case " 等 ": return " و بقیه · ";
            case " 个文件": return " تا فایل";
            case "共享文件-": return "فایل-همرسانی‌وابی-";
            case "已取消，未上传": return "لغو بو؛ فایلی آپلود نَبو.";
            case "读取上传目录…": return "داره پوشهٔ آپلودِ ایخونه…";
            case "选择上传目录": return "پوشهٔ آپلودِ بردار";
            case "自定义目录": return "پوشهٔ دلخواه";
            case "暂无可写目录": return "هَنی پوشه‌ای نی که بشه تیِش نوشت";
            case "检查目录": return "پوشه‌نِ وارسی کن";
            case "相对于服务器根目录（/ 表示根目录）": return fa(key); // Persian fallback
            case "此目录不可上传": return "نِشه تی ای پوشه آپلود کرد.";
            case "不能移动到当前目录、所选文件夹或其子目录": return fa(key); // Persian fallback
            case "输入相对路径": return fa(key); // Persian fallback
            case "此目录不可上传，可继续打开子目录": return "نِشه ایجا آپلود کرد؛ ولی ایتونی پوشه‌های توشِ وا کنی.";
            case "没有子文件夹，可上传到当前目录": return "پوشه‌ای توش نی؛ ایتونی تی هَمی پوشه آپلود کنی.";
            case "上传到这里": return "ایجا آپلود کن";
            case "选择文件上传": return "فایل‌یا‌نِ بَرِ آپلود بردار";
            case "上传": return "آپلود";
            case "上传 ": return "داره آپلود ایکنه: ";
            case "开始上传…": return "داره آپلودِ شروع ایکنه…";
            case "读取第 ": return "داره فایلِ ایخونه: ";
            case " 个文件…": return "…";
            case "上传完成 · 成功 ": return "آپلود تموم بو · انجام بو: ";
            case "，失败 ": return "، جور نَبو: ";
            case " · 目录读取失败：": return " · پوشه خونده نَبو: ";
            case "上传结果": return "نتیجه‌های آپلود";
            case "成功 ": return "آپلود بو: ";
            case "其余失败未展开": return fa(key); // Persian fallback
            case "系统未提供文件选择器": return fa(key); // Persian fallback
            case "系统未提供保存位置选择器": return fa(key); // Persian fallback
            case "下载会话已失效，请重新选择文件": return fa(key); // Persian fallback
            case "正在下载…": return "داره دانلود ایکنه…";
            case "无法打开保存位置": return "جایِ ذخیره وا نَبو";
            case "下载失败（保存位置可能有不完整文件）：": return "دانلود جور نَبو (شاید یه فایلِ نیمه‌کاره مونده): ";
            case "下载完成": return "دانلود تموم بو";
            case "未授权文件": return "اجازهٔ دسترسی به فایل نَدادن";
            case "无法打开本地文件": return "فایلِ رو دستگاه وا نَبو";
            case "同名文件过多": return "فایلِ همنوم خیلی زیاده";

            // Errors
            case "账号或密码错误，或服务器未接受 Basic 认证 (401)": return fa(key); // Persian fallback
            case "当前账号或服务器不允许此操作 (403)": return "ای حساب یا سرور اجازهٔ ای کارِ نیده (403)";
            case "服务端不支持此操作，或目标已存在 (405)": return fa(key); // Persian fallback
            case "同名目标已存在，未覆盖 (412)": return fa(key); // Persian fallback
            case "服务器要求跳转，请填写最终地址 (HTTP ": return fa(key); // Persian fallback
            case "HTTPS 证书验证失败": return fa(key); // Persian fallback
            case "未返回 Dufs 目录，请确认地址、路径和版本": return fa(key); // Persian fallback
            case "来源应用未授权读取该文件": return "برنامه‌ای که فایلِ فرستاد اجازه نَداد بخونیمِش";
            case "无效文件名或地址": return "نومِ فایل یا آدرس درست نی";
            case "连接或文件读写失败，请检查网络、文件和服务状态": return fa(key); // Persian fallback
            case "操作失败，请检查连接配置": return "کار جور نَبو؛ تنظیماتِ وصلِ وارسی کن.";

            // Additional connection labels
            case "账号（可选）": return "نومِ کاربری (اگه خواستی)";
            case "服务器地址": return "آدرسِ سرور";
            case "服务器地址示例": return "نمونهٔ آدرسِ سرور";
            case "账号说明（可留空）": return "ایتونی نومِ کاربری‌نِ خالی بَهلی";

            default: return null;
        }
    }
}

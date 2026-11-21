package local.dufs.manage;

/** Exact UI translations for Traditional Chinese, Vietnamese, and Russian. */
final class LocaleEast {
    private LocaleEast() {}

    static String text(String locale, String key) {
        if (locale == null || key == null) return null;
        int language;
        switch (locale) {
            case "zh-TW": language = 0; break;
            case "vi": language = 1; break;
            case "ru": language = 2; break;
            default: return null;
        }
        switch (key) {
            // Settings and connection
            case "跟随系统": return pick(language, "跟隨系統", "Theo hệ thống", "Как в системе");
            case "浅色": return pick(language, "淺色", "Sáng", "Светлая");
            case "深色": return pick(language, "深色", "Tối", "Тёмная");
            case "外观": return pick(language, "外觀", "Giao diện", "Оформление");
            case "无法保存外观选择": return pick(language, "無法儲存外觀設定", "Không lưu được tùy chọn giao diện", "Не удалось сохранить настройки оформления");
            case "语言": return pick(language, "語言", "Ngôn ngữ", "Язык");
            case "简体中文": return pick(language, "簡體中文", "Tiếng Trung giản thể", "Китайский (упрощённый)");
            case "连接设置": return pick(language, "連線設定", "Cài đặt kết nối", "Настройки подключения");
            case "Dufs 连接设置": return pick(language, "Dufs 連線設定", "Cài đặt kết nối Dufs", "Настройки подключения к Dufs");
            case "服务器地址（IP/域名）": return pick(language, "伺服器位址（IP／網域）", "Địa chỉ máy chủ (IP hoặc tên miền)", "Адрес сервера (IP или домен)");
            case "账号（无认证可留空）": return pick(language, "帳號（無須驗證時可留空）", "Tên đăng nhập (có thể để trống nếu không cần xác thực)", "Имя пользователя (без аутентификации можно оставить пустым)");
            case "账号（可选）": return pick(language, "帳號（選填）", "Tên đăng nhập (không bắt buộc)", "Имя пользователя (необязательно)");
            case "服务器地址": return pick(language, "伺服器位址", "Địa chỉ máy chủ", "Адрес сервера");
            case "服务器地址示例": return pick(language, "伺服器位址範例", "Ví dụ về địa chỉ máy chủ", "Пример адреса сервера");
            case "账号说明（可留空）": return pick(language, "若伺服器不需要驗證，帳號可留空。", "Để trống nếu máy chủ không yêu cầu xác thực.", "Оставьте поле пустым, если сервер не требует аутентификации.");
            case " 等 ": return pick(language, " · ", " · ", " · ");
            case " 个文件": return pick(language, " 個檔案", " tệp", " файл(ов)");
            case "密码": return pick(language, "密碼", "Mật khẩu", "Пароль");
            case "显示密码": return pick(language, "顯示密碼", "Hiện mật khẩu", "Показать пароль");
            case "隐藏密码": return pick(language, "隱藏密碼", "Ẩn mật khẩu", "Скрыть пароль");
            case "测试连接": return pick(language, "測試連線", "Kiểm tra kết nối", "Проверить подключение");
            case "保存": return pick(language, "儲存", "Lưu", "Сохранить");
            case "连接成功，未保存。": return pick(language, "連線成功，尚未儲存。", "Kết nối thành công. Chưa lưu.", "Подключение успешно. Настройки не сохранены.");
            case "连接失败：": return pick(language, "連線失敗： ", "Kết nối thất bại: ", "Не удалось подключиться: ");
            case "准备连接": return pick(language, "準備連線", "Sẵn sàng kết nối", "Готово к подключению");
            case "读取保存的连接…": return pick(language, "正在載入已儲存的連線設定…", "Đang tải cài đặt kết nối đã lưu…", "Загрузка сохранённых настроек подключения…");
            case "请填写服务器地址和账号密码": return pick(language, "請輸入伺服器位址、帳號及密碼", "Nhập địa chỉ máy chủ và thông tin đăng nhập", "Введите адрес сервера и данные для входа");
            case "无法读取保存的配置，请重新填写": return pick(language, "無法載入已儲存的設定，請重新輸入。", "Không tải được cài đặt đã lưu. Hãy nhập lại.", "Не удалось загрузить сохранённые настройки. Введите их заново.");
            case "账号不能包含冒号；填写密码时需要账号。": return pick(language, "帳號不能包含冒號；輸入密碼時必須填寫帳號。", "Tên đăng nhập không được chứa dấu hai chấm. Nếu nhập mật khẩu, phải nhập tên đăng nhập.", "В имени пользователя не должно быть двоеточия. Если указан пароль, необходимо указать имя пользователя.");
            case "正在连接…": return pick(language, "正在連線…", "Đang kết nối…", "Подключение…");
            case "正在验证连接…": return pick(language, "正在驗證連線…", "Đang xác minh kết nối…", "Проверка подключения…");
            case "连接未保存：": return pick(language, "未儲存連線設定： ", "Chưa lưu cài đặt kết nối: ", "Настройки подключения не сохранены: ");
            case "Dufs 文件助手": return pick(language, "Dufs 檔案管理器", "Trình quản lý tệp Dufs", "Файловый менеджер Dufs");
            // Navigation, directory listing, and selection
            case "菜单": return pick(language, "選單", "Trình đơn", "Меню");
            case "重试": return pick(language, "重試", "Thử lại", "Повторить");
            case "文件夹": return pick(language, "資料夾", "Thư mục", "Папка");
            case "上传时由服务器创建目录": return pick(language, "上傳時，伺服器會建立此目錄。", "Máy chủ sẽ tạo thư mục này khi tải lên.", "Сервер создаст этот каталог при загрузке файлов.");
            case "请选择其他可写目录": return pick(language, "請選擇其他可寫入的目錄。", "Hãy chọn thư mục khác có quyền ghi.", "Выберите другой каталог с правом записи.");
            case "永久删除所选项目及其中的文件？": return pick(language, "要永久刪除所選項目及其內容嗎？", "Xóa vĩnh viễn các mục đã chọn cùng nội dung bên trong?", "Удалить выбранные элементы и их содержимое без возможности восстановления?");
            case "取消": return pick(language, "取消", "Hủy", "Отмена");
            case "确定": return pick(language, "確定", "Đồng ý", "ОК");
            case "关闭": return pick(language, "關閉", "Đóng", "Закрыть");
            case "知道了": return pick(language, "知道了", "Đã hiểu", "Понятно");
            case "名称": return pick(language, "名稱", "Tên", "Имя");
            case "根目录": return pick(language, "根目錄", "Thư mục gốc", "Корневой каталог");
            case "目录：": return pick(language, "目錄： ", "Thư mục: ", "Каталог: ");
            case "上一级": return pick(language, "上一層", "Thư mục cha", "Родительский каталог");
            case "上一层": return pick(language, "上一層", "Thư mục cha", "Родительский каталог");
            case "↑ 上一级": return pick(language, "↑ 上一層", "↑ Thư mục cha", "↑ Родительский каталог");
            case "返回顶部": return pick(language, "回到頂端", "Về đầu trang", "К началу списка");
            case "刷新": return pick(language, "重新整理", "Làm mới", "Обновить");
            case "下拉刷新": return pick(language, "下拉以重新整理", "Kéo xuống để làm mới", "Потяните вниз, чтобы обновить");
            case "松手更新": return pick(language, "放開以重新整理", "Thả tay để làm mới", "Отпустите, чтобы обновить");
            case "归档下载": return pick(language, "下載封存檔", "Tải xuống bản nén", "Скачать архив");
            case "排序": return pick(language, "排序", "Sắp xếp", "Сортировка");
            case "最后修改时间": return pick(language, "最後修改時間", "Lần sửa cuối", "Дата изменения");
            case "文件大小": return pick(language, "檔案大小", "Kích thước tệp", "Размер файла");
            case "升序": return pick(language, "遞增", "Tăng dần", "По возрастанию");
            case "降序": return pick(language, "遞減", "Giảm dần", "По убыванию");
            case "选择": return pick(language, "選取", "Chọn", "Выбрать");
            case "全选": return pick(language, "全選", "Chọn tất cả", "Выбрать всё");
            case "取消全选": return pick(language, "取消全選", "Bỏ chọn tất cả", "Снять выделение со всех");
            case "取消选择": return pick(language, "取消選取", "Bỏ chọn", "Снять выделение");
            case "已选择": return pick(language, "已選取", "Đã chọn", "Выбрано");
            case "批量下载": return pick(language, "批次下載", "Tải xuống hàng loạt", "Пакетное скачивание");
            case "重新下载": return pick(language, "重新下載", "Tải xuống lại", "Скачать заново");
            case "上次的下载可能已完成或只保存了一部分。请先检查保存目录，再决定是否重新下载。": return pick(language, "上次下載可能已完成，也可能只儲存了部分內容。請先檢查儲存位置，再決定是否重新下載。", "Lần tải xuống trước có thể đã hoàn tất hoặc chỉ lưu được một phần. Hãy kiểm tra nơi lưu trước khi quyết định tải lại.", "Предыдущее скачивание могло завершиться полностью или частично. Проверьте папку сохранения, прежде чем скачивать заново.");
            case "批量下载已中断，保存位置可能有不完整的文件。请检查后重新选择。": return pick(language, "批次下載已中斷，儲存位置可能有不完整的檔案。請檢查後重新選取。", "Quá trình tải xuống hàng loạt bị gián đoạn. Hãy kiểm tra xem có tệp chưa hoàn chỉnh ở nơi lưu không, rồi chọn lại.", "Пакетное скачивание прервано. Проверьте, нет ли неполных файлов в папке сохранения, затем выберите элементы заново.");
            case "每批最多下载 500 个项目": return pick(language, "每批最多可下載 500 個項目。", "Mỗi lượt chỉ tải xuống tối đa 500 mục.", "За один раз можно скачать не более 500 элементов.");
            case "批量删除": return pick(language, "批次刪除", "Xóa hàng loạt", "Пакетное удаление");
            case "批量移动": return pick(language, "批次移動", "Di chuyển hàng loạt", "Пакетное перемещение");
            case "请等当前操作完成": return pick(language, "請等候目前的操作完成", "Chờ thao tác hiện tại hoàn tất", "Дождитесь завершения текущей операции");
            case "读取目录": return pick(language, "載入目錄", "Tải danh sách thư mục", "Загрузить содержимое каталога");
            case "↻ 重试读取目录": return pick(language, "↻ 重試載入目錄", "↻ Thử tải lại danh sách thư mục", "↻ Повторить загрузку содержимого каталога");
            case "读取目录…": return pick(language, "正在載入目錄…", "Đang tải danh sách thư mục…", "Загрузка содержимого каталога…");
            case "读取失败：": return pick(language, "載入失敗： ", "Không tải được: ", "Не удалось загрузить: ");
            case "读取目录失败：": return pick(language, "無法載入目錄： ", "Không tải được danh sách thư mục: ", "Не удалось загрузить содержимое каталога: ");
            case "请先读取目录": return pick(language, "請先載入目錄", "Hãy tải danh sách thư mục trước", "Сначала загрузите содержимое каталога");
            case "已连接 · ": return pick(language, "已連線 · ", "Đã kết nối · ", "Подключено · ");
            case " 项": return pick(language, " 個項目", " mục", " эл.");
            case " · 核验权限中…": return pick(language, " · 正在檢查權限…", " · Đang kiểm tra quyền…", " · Проверка прав доступа…");
            case " 项权限待确认": return pick(language, " 個項目的權限尚未確認", " mục chưa được xác minh quyền", " эл. с непроверенными правами");
            case "待确认权限": return pick(language, "權限尚未確認", "Chưa xác minh quyền", "Права доступа не проверены");
            case "请刷新读取当前目录权限": return pick(language, "請重新整理以檢查目前目錄的權限", "Làm mới để kiểm tra quyền của thư mục này", "Обновите список, чтобы проверить права доступа к этому каталогу");
            case "文件夹 · 点击打开": return pick(language, "資料夾 · 輕觸以開啟", "Thư mục · Nhấn để mở", "Папка · Нажмите, чтобы открыть");
            case " · 点击下载": return pick(language, " · 輕觸以下載", " · Nhấn để tải xuống", " · Нажмите, чтобы скачать");
            case "的操作": return pick(language, "的操作", " — thao tác", " — действия");
            case "这个目录没有文件": return pick(language, "這個目錄沒有檔案", "Thư mục này trống", "Этот каталог пуст");
            // Search and file operations
            case "搜索": return pick(language, "搜尋", "Tìm kiếm", "Поиск");
            case "搜索结果": return pick(language, "搜尋結果", "Kết quả tìm kiếm", "Результаты поиска");
            case "核验所选权限": return pick(language, "檢查所選項目的權限", "Kiểm tra quyền của các mục đã chọn", "Проверить права для выбранных элементов");
            case "正在核验所选权限…": return pick(language, "正在檢查所選項目的權限…", "Đang kiểm tra quyền…", "Проверка прав доступа…");
            case "部分权限未能确认": return pick(language, "無法確認部分權限", "Không xác minh được một số quyền", "Не удалось подтвердить некоторые права доступа");
            case "无法核验权限：": return pick(language, "無法檢查權限： ", "Không kiểm tra được quyền: ", "Не удалось проверить права доступа: ");
            case "此项目没有可用的管理操作": return pick(language, "此項目沒有可用的管理操作", "Không có thao tác quản lý nào cho mục này", "Для этого элемента нет доступных действий управления");
            case "文件列表未更新，可手动刷新": return pick(language, "檔案清單尚未更新，可手動重新整理。", "Danh sách tệp chưa được cập nhật. Bạn có thể làm mới thủ công.", "Список файлов не обновился. Его можно обновить вручную.");
            case "搜索文件": return pick(language, "搜尋檔案", "Tìm tệp", "Поиск файлов");
            case "搜索当前目录和子目录": return pick(language, "搜尋目前目錄及其子目錄", "Tìm trong thư mục hiện tại và các thư mục con", "Искать в текущем каталоге и его подкаталогах");
            case "文件名关键词": return pick(language, "檔名關鍵字", "Từ khóa trong tên tệp", "Ключевое слово в имени файла");
            case "请输入关键词": return pick(language, "請輸入關鍵字", "Nhập từ khóa", "Введите ключевое слово");
            case "请输入有效名称": return pick(language, "請輸入有效名稱", "Nhập tên hợp lệ", "Введите допустимое имя");
            case "正在搜索…": return pick(language, "正在搜尋…", "Đang tìm kiếm…", "Поиск…");
            case "找到 ": return pick(language, "找到 ", "Tìm thấy ", "Найдено ");
            case "搜索结果 · ": return pick(language, "搜尋結果 · ", "Kết quả tìm kiếm · ", "Результаты поиска · ");
            case "未找到匹配的文件或文件夹": return pick(language, "找不到符合的檔案或資料夾", "Không tìm thấy tệp hoặc thư mục phù hợp", "Подходящие файлы или папки не найдены");
            case "搜索失败：": return pick(language, "搜尋失敗： ", "Tìm kiếm thất bại: ", "Ошибка поиска: ");
            case "新建文件夹": return pick(language, "新增資料夾", "Tạo thư mục", "Новая папка");
            case "重命名": return pick(language, "重新命名", "Đổi tên", "Переименовать");
            case "移动": return pick(language, "移動", "Di chuyển", "Переместить");
            case "删除": return pick(language, "刪除", "Xóa", "Удалить");
            case "确认删除？": return pick(language, "確定要刪除嗎？", "Xác nhận xóa?", "Подтвердить удаление?");
            case "\n将删除该文件夹及其内容。": return pick(language, "\n將刪除此資料夾及其內容。", "\nThư mục này và nội dung bên trong sẽ bị xóa.", "\nПапка и её содержимое будут удалены.");
            case "\n此操作不能在应用内撤销。": return pick(language, "\n無法在應用程式中復原此操作。", "\nKhông thể hoàn tác thao tác này trong ứng dụng.", "\nЭто действие нельзя отменить в приложении.");
            case "读取目标目录…": return pick(language, "正在載入目的目錄…", "Đang tải thư mục đích…", "Загрузка каталога назначения…");
            case "请选择移动目标": return pick(language, "請選擇移動目的地", "Chọn nơi muốn chuyển đến", "Выберите место назначения");
            case "选择移动目录": return pick(language, "選擇目的資料夾", "Chọn thư mục đích", "Выберите папку назначения");
            case "移动到：": return pick(language, "移動到： ", "Di chuyển đến: ", "Переместить в: ");
            case "没有可进入的子文件夹": return pick(language, "沒有可開啟的子資料夾", "Không có thư mục con nào có thể mở", "Нет доступных вложенных папок");
            case "移到这里": return pick(language, "移到這裡", "Di chuyển vào đây", "Переместить сюда");
            case "无法读取目标：": return pick(language, "無法載入目的地： ", "Không tải được thư mục đích: ", "Не удалось загрузить каталог назначения: ");
            case "正在": return pick(language, "正在執行： ", "Đang thực hiện: ", "Выполняется: ");
            case "成功": return pick(language, " 成功", " thành công", " — выполнено");
            case "失败：": return pick(language, " 失敗： ", " thất bại: ", " — ошибка: ");
            case "（目录未刷新）": return pick(language, "（目錄尚未重新整理）", " (chưa làm mới thư mục)", " (каталог не обновлён)");
            // Sharing, uploading, and downloading
            case "没有收到可上传的文件（不支持分享文本或网址）": return pick(language, "未收到可上傳的檔案，不支援分享文字或網址。", "Không nhận được tệp nào để tải lên. Không hỗ trợ chia sẻ văn bản hoặc liên kết.", "Нет файлов для загрузки. Отправка текста и ссылок не поддерживается.");
            case "正在读取文件名…": return pick(language, "正在讀取檔名…", "Đang đọc tên tệp…", "Чтение имени файла…");
            case "共享文件（文件名暂未返回）": return pick(language, "分享的檔案（尚未取得檔名）", "Tệp được chia sẻ (chưa có tên)", "Отправленный файл (имя пока неизвестно)");
            case "共享文件 · 共 ": return pick(language, "分享的檔案 · 共 ", "Tệp được chia sẻ · Tổng cộng ", "Отправленные файлы · Всего: ");
            case " 个": return pick(language, " 個", " mục", " шт.");
            case "共享文件": return pick(language, "分享的檔案", "Tệp được chia sẻ", "Отправленный файл");
            case "共享文件-": return pick(language, "分享的檔案-", "Tep-chia-se-", "Отправленный-файл-");
            case "已取消，未上传": return pick(language, "已取消，未上傳任何檔案。", "Đã hủy. Không có tệp nào được tải lên.", "Отменено. Файлы не загружены.");
            case "读取上传目录…": return pick(language, "正在載入上傳目錄…", "Đang tải thư mục tải lên…", "Загрузка каталога для отправки файлов…");
            case "选择上传目录": return pick(language, "選擇上傳目錄", "Chọn thư mục tải lên", "Выберите каталог для загрузки файлов");
            case "自定义目录": return pick(language, "自訂目錄", "Thư mục tùy chỉnh", "Указать каталог");
            case "暂无可写目录": return pick(language, "目前沒有可寫入的目錄", "Không có thư mục nào có quyền ghi", "Нет каталогов с правом записи");
            case "检查目录": return pick(language, "檢查目錄", "Kiểm tra thư mục", "Проверить каталог");
            case "相对于服务器根目录（/ 表示根目录）": return pick(language, "相對於伺服器根目錄（/ 表示根目錄）", "Đường dẫn tương đối so với thư mục gốc của máy chủ (/ là thư mục gốc)", "Относительно корневого каталога сервера (/ — корневой каталог)");
            case "此目录不可上传": return pick(language, "無法上傳至此目錄。", "Không được phép tải lên thư mục này.", "Загрузка файлов в этот каталог запрещена.");
            case "不能移动到当前目录、所选文件夹或其子目录": return pick(language, "無法移至目前目錄、所選資料夾或其子目錄。", "Không thể chuyển vào thư mục hiện tại, thư mục đã chọn hoặc thư mục con của chúng.", "Нельзя переместить в текущий каталог, выбранные папки или их подкаталоги.");
            case "输入相对路径": return pick(language, "輸入相對路徑", "Nhập đường dẫn tương đối", "Введите относительный путь");
            case "此目录不可上传，可继续打开子目录": return pick(language, "無法上傳至此目錄，但仍可開啟子目錄。", "Không được phép tải lên đây, nhưng vẫn có thể mở các thư mục con.", "Сюда нельзя загружать файлы, но можно открывать вложенные папки.");
            case "没有子文件夹，可上传到当前目录": return pick(language, "沒有子資料夾，可上傳至目前目錄。", "Không có thư mục con. Bạn có thể tải lên thư mục hiện tại.", "Вложенных папок нет. Можно загрузить файлы в текущий каталог.");
            case "上传到这里": return pick(language, "上傳到這裡", "Tải lên đây", "Загрузить сюда");
            case "选择文件上传": return pick(language, "選擇要上傳的檔案", "Chọn tệp để tải lên", "Выберите файлы для загрузки");
            case "上传": return pick(language, "上傳", "Tải lên", "Загрузить");
            case "上传 ": return pick(language, "正在上傳 ", "Đang tải lên ", "Загрузка: ");
            case "开始上传…": return pick(language, "正在開始上傳…", "Đang bắt đầu tải lên…", "Начало загрузки…");
            case "读取第 ": return pick(language, "正在讀取第 ", "Đang đọc tệp số ", "Чтение файла № ");
            case " 个文件…": return pick(language, " 個檔案…", " tệp…", " …");
            case "上传完成 · 成功 ": return pick(language, "上傳完成 · 成功 ", "Đã tải lên xong · Thành công: ", "Загрузка завершена · Успешно: ");
            case "，失败 ": return pick(language, "，失敗 ", ", thất bại: ", ", с ошибками: ");
            case " · 目录读取失败：": return pick(language, " · 無法載入目錄： ", " · Không tải được danh sách thư mục: ", " · Не удалось загрузить содержимое каталога: ");
            case "上传结果": return pick(language, "上傳結果", "Kết quả tải lên", "Результаты загрузки");
            case "成功 ": return pick(language, "已上傳 ", "Đã tải lên ", "Загружено: ");
            case "其余失败未展开": return pick(language, "未顯示其餘失敗項目", "Không hiển thị các mục thất bại còn lại", "Остальные ошибки не показаны");
            case "系统未提供文件选择器": return pick(language, "系統未提供檔案選擇器", "Không có trình chọn tệp của hệ thống", "Системное окно выбора файлов недоступно");
            case "系统未提供保存位置选择器": return pick(language, "系統未提供儲存位置選擇器", "Không có trình chọn nơi lưu của hệ thống", "Системное окно выбора места сохранения недоступно");
            case "下载会话已失效，请重新选择文件": return pick(language, "下載工作階段已失效，請重新選擇檔案。", "Phiên tải xuống đã hết hiệu lực. Hãy chọn lại tệp.", "Сеанс скачивания истёк. Выберите файл заново.");
            case "正在下载…": return pick(language, "正在下載…", "Đang tải xuống…", "Скачивание…");
            case "无法打开保存位置": return pick(language, "無法開啟儲存位置", "Không mở được nơi lưu", "Не удалось открыть папку сохранения");
            case "下载失败（保存位置可能有不完整文件）：": return pick(language, "下載失敗（儲存位置可能有不完整的檔案）： ", "Tải xuống thất bại (nơi lưu có thể còn tệp chưa hoàn chỉnh): ", "Не удалось скачать (мог остаться неполный файл): ");
            case "下载完成": return pick(language, "下載完成", "Đã tải xuống xong", "Скачивание завершено");
            case "未授权文件": return pick(language, "未取得檔案存取權限", "Chưa được cấp quyền truy cập tệp", "Нет разрешения на доступ к файлу");
            case "无法打开本地文件": return pick(language, "無法開啟本機檔案", "Không mở được tệp cục bộ", "Не удалось открыть локальный файл");
            case "同名文件过多": return pick(language, "同名檔案過多", "Có quá nhiều tệp trùng tên", "Слишком много файлов с одинаковым именем");
            // Error messages
            case "账号或密码错误，或服务器未接受 Basic 认证 (401)": return pick(language, "帳號或密碼錯誤，或伺服器未接受 Basic 驗證 (401)", "Sai tên đăng nhập hoặc mật khẩu, hoặc máy chủ không chấp nhận xác thực Basic (401)", "Неверное имя пользователя или пароль либо сервер не принял аутентификацию Basic (401)");
            case "当前账号或服务器不允许此操作 (403)": return pick(language, "目前的帳號或伺服器不允許此操作 (403)", "Tài khoản hiện tại hoặc máy chủ không cho phép thao tác này (403)", "Эта операция запрещена для текущей учётной записи или на сервере (403)");
            case "服务端不支持此操作，或目标已存在 (405)": return pick(language, "伺服器不支援此操作，或目的地已存在 (405)", "Máy chủ không hỗ trợ thao tác này hoặc đích đã tồn tại (405)", "Сервер не поддерживает эту операцию или целевой объект уже существует (405)");
            case "同名目标已存在，未覆盖 (412)": return pick(language, "同名項目已存在，未覆寫 (412)", "Đã có mục trùng tên tại đích; không ghi đè (412)", "Одноимённый объект уже существует и не был перезаписан (412)");
            case "服务器要求跳转，请填写最终地址 (HTTP ": return pick(language, "伺服器要求重新導向，請輸入最終位址 (HTTP ", "Máy chủ đã chuyển hướng yêu cầu. Hãy nhập địa chỉ cuối cùng (HTTP ", "Сервер перенаправил запрос. Введите конечный адрес (HTTP ");
            case "HTTPS 证书验证失败": return pick(language, "HTTPS 憑證驗證失敗", "Không xác minh được chứng chỉ HTTPS", "Не удалось проверить сертификат HTTPS");
            case "未返回 Dufs 目录，请确认地址、路径和版本": return pick(language, "伺服器未傳回 Dufs 目錄，請確認位址、路徑及版本。", "Máy chủ không trả về danh sách thư mục Dufs. Hãy kiểm tra địa chỉ, đường dẫn và phiên bản.", "Сервер не вернул список файлов Dufs. Проверьте адрес, путь и версию.");
            case "来源应用未授权读取该文件": return pick(language, "來源應用程式未授權讀取此檔案", "Ứng dụng nguồn không cấp quyền đọc tệp này", "Исходное приложение не предоставило разрешение на чтение файла");
            case "无效文件名或地址": return pick(language, "檔名或位址無效", "Tên tệp hoặc địa chỉ không hợp lệ", "Недопустимое имя файла или адрес");
            case "连接或文件读写失败，请检查网络、文件和服务状态": return pick(language, "連線或檔案讀寫失敗，請檢查網路、檔案及伺服器狀態。", "Kết nối hoặc truy cập tệp thất bại. Hãy kiểm tra mạng, tệp và trạng thái máy chủ.", "Не удалось подключиться или получить доступ к файлу. Проверьте сеть, файл и состояние сервера.");
            case "操作失败，请检查连接配置": return pick(language, "操作失敗，請檢查連線設定。", "Thao tác thất bại. Hãy kiểm tra cài đặt kết nối.", "Операция не выполнена. Проверьте настройки подключения.");
            default: return null;
        }
    }

    private static String pick(int language, String zhTw, String vi, String ru) {
        return language == 0 ? zhTw : language == 1 ? vi : ru;
    }
}

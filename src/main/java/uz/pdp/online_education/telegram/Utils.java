package uz.pdp.online_education.telegram;

public interface Utils {

    String ADMIN = "/admin";
    String INSTRUCTOR = "/instructor";
    String STUDENT = "/student";

    String START = "/start";
    String DASHBOARD = "/dashboard";

    interface ReplyButtons {
        // --- Admin Menu ---
        String ADMIN_USERS = "👤 Foydalanuvchilar";
        String ADMIN_COURSES = "📚 Kurslar";
        String ADMIN_SEND_MESSAGE = "📤 Xabar Yuborish";
        String ADMIN_STATISTICS = "📊 Statistika";
        String ADMIN_SUPPORT_REQUESTS = "🆘 Yordam So'rovlari";

        // --- Instructor Menu ---
        String INSTRUCTOR_MY_COURSES = "📚 Mening Kurslarim";
        String INSTRUCTOR_MY_STUDENTS = "🎓 O'quvchilarim";
        String INSTRUCTOR_REVIEWS = "💬 Sharhlar va Fikrlar";
        String INSTRUCTOR_MY_REVENUE = "💰 Daromadlarim";

        // --- Student Menu ---
        String STUDENT_MY_COURSES = "📚 Mening Kurslarim";
        String STUDENT_ALL_COURSES = "🎓 Barcha Kurslar";
        String STUDENT_BALANCE = "💰 Balans va To'lovlar";
        String STUDENT_HELP = "🆘 Yordam";
    }

    // --- INLINE KEYBOARD BUTTONS & CALLBACK DATA ---
    interface InlineButtons {
        String USER_LIST_TEXT = "📋 Foydalanuvchilar ro'yxati";
        String USER_SEARCH_TEXT = "🔎 Foydalanuvchi qidirish";
        String USER_STATS_TEXT = "📊 Foydalanuvchi statistikasi";
        String BACK_TO_MAIN_MENU_TEXT = "⬅️ Bosh menyuga";

        String LOGOUT_TEXT = "🚪 Akkauntdan Chiqish (Botdan Uzish)";
        String LOGOUT_CONFIRM_YES_TEXT = "✅ Ha, uzish";
        String LOGOUT_CONFIRM_NO_TEXT = "❌ Yo'q, qolish";

        // --- NEW: Course menu texts ---
        String COURSE_LIST_TEXT = "📋 Barcha kurslar ro'yxati";
        String COURSE_ADD_TEXT = "➕ Yangi kurs qo'shish";
        String COURSE_SEARCH_TEXT = "🔎 Kurs qidirish";
    }

    // --- INLINE KEYBOARD CALLBACK DATA PREFIXES ---
    // Example: "users:list:page:0"
    interface CallbackData {
        // Format: "module:action:value1:value2"
        String USER_LIST_CALLBACK = "users:list:page:0";
        String USER_SEARCH_CALLBACK = "users:search:init";
        String USER_STATS_CALLBACK = "users:stats";
        String BACK_TO_ADMIN_MENU_CALLBACK = "admin:main_menu";

        String AUTH_LOGOUT_INIT_CALLBACK = "auth:logout:init";
        String AUTH_LOGOUT_CONFIRM_CALLBACK = "auth:logout:confirm";
        String AUTH_LOGOUT_CANCEL_CALLBACK = "auth:logout:cancel";

        // --- NEW: Course menu callbacks ---
        String COURSE_LIST_CALLBACK = "courses:list:page:0";
        String COURSE_ADD_CALLBACK = "courses:add:init";
        String COURSE_SEARCH_CALLBACK = "courses:search:init";

        String USERS_PREFIX = "users";
        String COURSES_PREFIX = "courses";
        String ADMIN_PREFIX = "admin";

        String ACTION_LIST = "list";
        String ACTION_SEARCH = "search";
        String ACTION_STATS = "stats";
        String ACTION_MAIN_MENU = "main_menu";
        String ACTION_PAGE = "page";
        String ACTION_BACK = "back";
    }


}

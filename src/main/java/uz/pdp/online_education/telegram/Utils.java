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

        String BACK_TO_MY_COURSES_TEXT = "⬅️ Mening Kurslarimga qaytish";
    }

    // --- INLINE KEYBOARD CALLBACK DATA PREFIXES ---
    // Example: "users:list:page:0"
    interface CallbackData {

        // --- STUDENT "MY COURSES" FLOW PREFIXES ---
        // Format: "prefix:action:value1:value2:..."
        // myc -> my_course, mod -> module, les -> lesson, con -> content
        String AUTH_PREFIX = "auth";
        String MY_COURSE_PREFIX = "myc";    // Kurslar ro'yxati uchun
        String ALL_COURSES_PREFIX = "allc"; // Barcha kurslar oqimi uchun
        String MODULE_PREFIX = "mod";       // Modullar ro'yxati uchun
        String LESSON_PREFIX = "les";       // Darslar ro'yxati uchun
        String CONTENT_PREFIX = "con";      // Kontentni ko'rish uchun
        String STUDENT_PREFIX = "std";      // Talabaga oid umumiy harakatlar

        // --- ACTIONS ---
        String ACTION_VIEW = "v";           // Ko'rish
        String ACTION_LIST = "l";           // Ro'yxatni ochish (odatda navigatsiya uchun)
        String ACTION_PAGE = "p";           // Sahifa
        String ACTION_BACK = "b";           // Orqaga
        String ACTION_BUY = "buy";          // Sotib olish

        // --- BACK TARGETS ---
        String BACK_TO_MAIN_MENU = "main";
        String BACK_TO_COURSES = "courses";
        String BACK_TO_MODULES = "modules";
        String BACK_TO_LESSONS = "lessons";


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
//
//        // --- TAYYOR CALLBACK SATRLARI (to'liq ko'rinishda) ---
//        String AUTH_LOGOUT_INIT_CALLBACK = AUTH_PREFIX + ":logout:init";       // "auth:logout:init"
//        String AUTH_LOGOUT_CONFIRM_CALLBACK = AUTH_PREFIX + ":logout:confirm"; // "auth:logout:confirm"
//        String AUTH_LOGOUT_CANCEL_CALLBACK = AUTH_PREFIX + ":logout:cancel";   // "auth:logout:cancel"

        String MY_COURSE_VIEW_CALLBACK = "mycourse:view:"; // Prefix
        String MY_COURSE_LIST_PAGE_CALLBACK = "mycourse:list:page:"; // Prefix

        String BACK_TO_MY_COURSES_CALLBACK = "mycourse:list:page:0";

        String USERS_PREFIX = "users";
        String COURSES_PREFIX = "courses";
        String ADMIN_PREFIX = "admin";

        //        String ACTION_LIST = "list";
        String ACTION_SEARCH = "search";
        String ACTION_STATS = "stats";
        String ACTION_MAIN_MENU = "main_menu";

        String ACTION_LOGOUT = "logout"; // Yangi
        String ACTION_INIT = "init";     // Yangi
        String ACTION_CONFIRM = "confirm"; // Yangi
        String ACTION_CANCEL = "cancel";   // Yangi
//        String ACTION_PAGE = "page";
//        String ACTION_BACK = "back";
    }

    /**
     * Defines constants and helper methods for constructing external website URLs.
     * This centralizes all web routing logic, making it easy to update paths.
     */
    interface WebRoutes {
        // --- URL Paths ---
        String CHECKOUT_MODULE_PATH = "/checkout/module/";
        String QUIZ_PATH = "/quiz/";

        /**
         * Generates a full URL for purchasing a specific module.
         *
         * @param baseUrl  The base URL of the website (e.g., "https://example.com").
         * @param moduleId The ID of the module to be purchased.
         * @return A complete URL string for the module checkout page.
         */
        static String generateModuleCheckoutUrl(String baseUrl, Long moduleId) {
            return baseUrl + CHECKOUT_MODULE_PATH + moduleId;
        }

        /**
         * Generates a full URL for taking a specific quiz.
         *
         * @param baseUrl The base URL of the website.
         * @param quizId  The ID of the quiz.
         * @return A complete URL string for the quiz page.
         */
        static String generateQuizUrl(String baseUrl, Long quizId) {
            return baseUrl + QUIZ_PATH + quizId;
        }
    }


}

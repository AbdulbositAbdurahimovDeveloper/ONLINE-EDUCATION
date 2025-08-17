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
        String PAGINATION_PREVIOUS_TEXT = "⬅️ Oldingi";
        String PAGINATION_NEXT_TEXT = "Keyingi ➡️";
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
        String ACTION_SUBSCRIPTION = "subs";// obuna bolish
        String DELETED = "deleted";

        String CATEGORY = "cat";            // Category
        String INSTRUCTOR = "ins";          // instructor

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


        String BALANCE_PENDING_PAYMENTS = "balance_pending";
        String BALANCE_PAYMENT_HISTORY = "balance_history";

        String BALANCED = "balanced";

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
     * Contains static final String constants for number-related emojis.
     * These are ready-to-use in message texts for lists, steps, or highlighting numbers.
     */
    interface NumberEmojis {
        String ONE = "1️⃣";
        String TWO = "2️⃣";
        String THREE = "3️⃣";
        String FOUR = "4️⃣";
        String FIVE = "5️⃣";
        String SIX = "6️⃣";
        String SEVEN = "7️⃣";
        String EIGHT = "8️⃣";
        String NINE = "9️⃣";
        String ZERO = "0️⃣";
        String TEN = "🔟";

        // Aylana shaklidagi raqamlar ham foydali bo'lishi mumkin
        String CIRCLED_ONE = "①";
        String CIRCLED_TWO = "②";
        String CIRCLED_THREE = "③";
        String CIRCLED_FOUR = "④";
        String CIRCLED_FIVE = "⑤";
        String CIRCLED_SIX = "⑥";
        String CIRCLED_SEVEN = "⑦";
        String CIRCLED_EIGHT = "⑧";
        String CIRCLED_NINE = "⑨";
        String CIRCLED_TEN = "⑩";
    }

    /**
     * Provides utility methods for converting numbers into decorative emoji strings.
     * Useful for creating visually appealing lists, steps, or counters.
     */
    interface Numbering {
        // Asosiy emoji raqamlar (0-9)
        String[] DIGIT_EMOJIS = {"0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣"};

        // Aylana shaklidagi raqamlar (1-20)
        String[] CIRCLED_NUMBERS = {
                "", "①", "②", "③", "④", "⑤", "⑥", "⑦", "⑧", "⑨", "⑩",
                "⑪", "⑫", "⑬", "⑭", "⑮", "⑯", "⑰", "⑱", "⑲", "⑳"
        };

        /**
         * Converts any integer into a string of square number emojis.
         * For example, 123 becomes "1️⃣2️⃣3️⃣".
         *
         * @param number The integer to convert.
         * @return A string representation of the number using emojis.
         */
        static String toEmoji(int number) {
            if (number < 0) {
                return String.valueOf(number); // Manfiy sonlarni o'zini qaytaramiz
            }

            StringBuilder emojiString = new StringBuilder();
            String numberStr = String.valueOf(number);

            for (char digit : numberStr.toCharArray()) {
                int digitValue = Character.getNumericValue(digit);
                emojiString.append(DIGIT_EMOJIS[digitValue]);
            }
//            return emojiString.toString();
            return String.valueOf(number);
        }

        /**
         * Converts an integer (from 1 to 20) into a circled number character.
         * If the number is outside this range, it returns the number followed by a dot (e.g., "21.").
         *
         * @param number The integer to convert (ideally between 1 and 20).
         * @return A circled number string, or a formatted string for numbers outside the range.
         */
        static String toCircled(int number) {
            if (number >= 1 && number <= 20) {
                return CIRCLED_NUMBERS[number];
//                return String.valueOf(number);
            }
            // Agar raqam 1-20 oralig'ida bo'lmasa, oddiy formatda qaytaramiz
            return number + ".";
        }
    }


}

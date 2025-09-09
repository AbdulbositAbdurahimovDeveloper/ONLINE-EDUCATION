package uz.pdp.online_education.telegram;

import java.util.List;
import java.util.Random;

/**
 * A central repository for application-wide constants.
 * This interface consolidates constants related to bot commands, UI text, callback data structures,
 * and other utilities to ensure consistency and prevent "magic strings" throughout the codebase.
 * <p>
 * This version preserves the original constant names to ensure backward compatibility with existing code.
 */
public interface Utils {


    /**
     * Defines the primary commands that users can type to interact with the bot.
     */
    String ADMIN = "/admin";
    String INSTRUCTOR = "/instructor";
    String STUDENT = "/student";

    String START = "/start";
    String DASHBOARD = "/dashboard";
    String CANCEL = "/cancel";




    /**
     * Contains user-facing text for Reply Keyboard Buttons (the persistent menu at the bottom).
     */
    interface ReplyButtons {

        String ADMIN_USERS = "👤 Foydalanuvchilar";
        String ADMIN_COURSES = "📚 Kurslar";
        String ADMIN_SEND_MESSAGE = "📤 Xabar Yuborish";
        String ADMIN_STATISTICS = "📊 Statistika";
        String ADMIN_SUPPORT_REQUESTS = "🆘 Yordam So'rovlari";


        String INSTRUCTOR_MY_COURSES = "📚 Mening Kurslarim";
        String INSTRUCTOR_MY_STUDENTS = "🎓 O'quvchilarim";
        String INSTRUCTOR_REVIEWS = "💬 Sharhlar va Fikrlar";
        String INSTRUCTOR_MY_REVENUE = "💰 Daromadlarim";


        String STUDENT_MY_COURSES = "📚 Mening Kurslarim";
        String STUDENT_ALL_COURSES = "🎓 Barcha Kurslar";
        String STUDENT_BALANCE = "💰 Balans va To'lovlar";
        String STUDENT_HELP = "🆘 Yordam";
    }




    /**
     * Contains user-facing text for Inline Keyboard Buttons (buttons attached directly to messages).
     */
    interface InlineButtons {
        String USER_LIST_TEXT = "📋 Foydalanuvchilar ro'yxati";
        String USER_SEARCH_TEXT = "🔎 Foydalanuvchi qidirish";
        String USER_STATS_TEXT = "📊 Foydalanuvchi statistikasi";
        String BACK_TO_MAIN_MENU_TEXT = "⬅️ Bosh menyuga";

        String LOGOUT_TEXT = "🚪 Akkauntdan Chiqish (Botdan Uzish)";
        String LOGOUT_CONFIRM_YES_TEXT = "✅ Ha, uzish";
        String LOGOUT_CONFIRM_NO_TEXT = "❌ Yo'q, qolish";


        String COURSE_LIST_TEXT = "📋 Barcha kurslar ro'yxati";
        String COURSE_ADD_TEXT = "➕ Yangi kurs qo'shish";
        String COURSE_SEARCH_TEXT = "🔎 Kurs qidirish";

        String BACK_TO_MY_COURSES_TEXT = "⬅️ Mening Kurslarimga qaytish";
        String PAGINATION_PREVIOUS_TEXT = "⬅️ Oldingi";
        String PAGINATION_NEXT_TEXT = "Keyingi ➡️";
    }




    /**
     * Defines the structure and components for building and parsing callback query data.
     * <p>
     * The recommended format is a colon-separated string: {@code "prefix:action:param1:param2:..."}
     * <ul>
     *     <li><b>prefix:</b> Identifies the main context (e.g., "myc" for My Courses).</li>
     *     <li><b>action:</b> Specifies the operation (e.g., "p" for page).</li>
     *     <li><b>params:</b> Optional data, like an entity ID or page number.</li>
     * </ul>
     * <b>Example:</b> {@code "myc:success:p:0"} -> MyCourses, show Successful, go to Page 0.
     */
    interface CallbackData {


        /**
         * Defines the main functional area the callback belongs to. Used for routing.
         */
        String AUTH_PREFIX = "auth";
        String MY_COURSE_PREFIX = "myc";
        String ALL_COURSES_PREFIX = "allc";
        String MODULE_PREFIX = "mod";
        String LESSON_PREFIX = "les";
        String CONTENT_PREFIX = "con";
        String STUDENT_PREFIX = "std";
        String CATEGORY = "cat";
        String INSTRUCTOR = "ins";
        String BALANCED = "balanced";

        // --- GENERAL ACTIONS ---
        /**
         * Defines the operation to be performed within a context.
         */
        String ACTION_VIEW = "v";
        String ACTION_LIST = "l";
        String ACTION_PAGE = "p";
        String ACTION_BACK = "b";
        String ACTION_BUY = "buy";
        String ACTION_SUBSCRIPTION = "subs";
        String DELETED = "deleted";
        String ACTION_ADD = "add";
        String ACTION_EDIT = "edit";
        String ACTION_DELETE = "del";
        String ACTION_STUDENT = "std";
        String ACTION_STUDENT_ID = "std_id";


        /**
         * Defines more specific actions or states, often used as parameters.
         */
        String ACTION_LOGOUT = "logout";
        String ACTION_INIT = "init";
        String ACTION_CONFIRM = "confirm";
        String ACTION_CANCEL = "cancel";
        String ACTION_SUCCESS = "success";
        String ACTION_DRAFT = "draft";
        String ACTION_REVIEWS = "review";
        String ACTION_REVENUE = "revenue";
        String ACTION_COURSE = "course";
        String ACTION_MODULE = "mod";

        String ACTION_CHOICE = "choice";


        String CURRENT_STEP = "CURRENT_STEP";

        String COURSE_ID = "course_id";
        String THUMBNAIL_ID = "thumbnail_id";
        String CATEGORY_ID = "category_id";
        String MODULE_ID = "module_id";
        String LESSON_ID = "lesson_id";

        String TITLE = "title";
        String DESCRIPTION = "description";
        String PRICE = "price";
//        String THUMBNAIL_ID = "thumbnail_id";

//        String CATEGORY_ID = "category_id";

        String COURSE_LIST_CALLBACK = "admin:courses:list:init";

        String IS_PREE = "is_pree";
        String TRUE = "true";
        String FALSE = "false";

        String PHOTO = "photo";
        String VIDEO = "video";

        String TEXT_CONTENT = "text";
        String ATTACHMENT_CONTENT = "video";
        String QUIZ_CONTENT = "quiz";


        /**
         * @deprecated This hardcoded value is brittle. Prefer dynamic construction: {@code String.join(":", AUTH_PREFIX, ACTION_LOGOUT, ACTION_INIT)}.
         */
        @Deprecated
        String AUTH_LOGOUT_INIT_CALLBACK = "auth:logout:init";
        /**
         * @deprecated This hardcoded value is brittle. Prefer dynamic construction: {@code String.join(":", AUTH_PREFIX, ACTION_LOGOUT, ACTION_CONFIRM)}.
         */
        @Deprecated
        String AUTH_LOGOUT_CONFIRM_CALLBACK = "auth:logout:confirm";
        /**
         * @deprecated This hardcoded value is brittle. Prefer dynamic construction: {@code String.join(":", AUTH_PREFIX, ACTION_LOGOUT, ACTION_CANCEL)}.
         */
        @Deprecated
        String AUTH_LOGOUT_CANCEL_CALLBACK = "auth:logout:cancel";


        String BACK_TO_MAIN_MENU = "main";
        String BALANCE_PENDING_PAYMENTS = "balance_pending";
        String BALANCE_PAYMENT_HISTORY = "balance_history";
    }


    /**
     * Contains static final String constants for number-related emojis.
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
     */
    interface Numbering {
        String[] DIGIT_EMOJIS = {"0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣"};
        String[] CIRCLED_NUMBERS = {
                "", "①", "②", "③", "④", "⑤", "⑥", "⑦", "⑧", "⑨", "⑩",
                "⑪", "⑫", "⑬", "⑭", "⑮", "⑯", "⑰", "⑱", "⑲", "⑳"
        };

        /**
         * Converts any non-negative integer into a string of square number emojis.
         * For example, 123 becomes "1️⃣2️⃣3️⃣".
         *
         * @param number The integer to convert.
         * @return A string representation of the number using emojis, or the original number as a string if negative.
         */
        static String toEmoji(int number) {
            if (number < 0 || true) {
                return String.valueOf(number); // Return negative numbers as is
            }
            if (number == 0) {
                return DIGIT_EMOJIS[0];
            }
            StringBuilder emojiString = new StringBuilder();
            String numberStr = String.valueOf(number);
            for (char digit : numberStr.toCharArray()) {
                int digitValue = Character.getNumericValue(digit);
                emojiString.append(DIGIT_EMOJIS[digitValue]);
            }
            return emojiString.toString();
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
            }
            return number + ".";
        }

        List<String> BOOK_EMOJIS = List.of(
                "📘", "📗", "📙", "📒", "📕", "📔", "📓"
//                , "📚", "📖"
        );

        static String randomBookEmoji() {
            Random random = new Random();
            return BOOK_EMOJIS.get(random.nextInt(BOOK_EMOJIS.size()));
        }
    }
}
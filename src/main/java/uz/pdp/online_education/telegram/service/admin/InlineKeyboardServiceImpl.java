package uz.pdp.online_education.telegram.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import uz.pdp.online_education.model.Category;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.service.admin.template.InlineKeyboardService;

import java.util.ArrayList;
import java.util.List;

import static uz.pdp.online_education.telegram.Utils.CallbackData.*;

@Service
@RequiredArgsConstructor
public class InlineKeyboardServiceImpl implements InlineKeyboardService {

    @Value("${telegram.bot.webhook-path}")
    private String appDomain;

    // --- YORDAMCHI METODLAR ---
    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton(text);
        button.setCallbackData(callbackData);
        return button;
    }

    @Override
    public InlineKeyboardMarkup logoutConfirmation() {
        // "Ha" va "Yo'q" tugmalarini yaratamiz.
        InlineKeyboardButton yesButton = createButton(
                Utils.InlineButtons.LOGOUT_CONFIRM_YES_TEXT,
                String.join(":", AUTH_PREFIX, ACTION_LOGOUT, ACTION_CONFIRM)
        );

        InlineKeyboardButton noButton = createButton(
                Utils.InlineButtons.LOGOUT_CONFIRM_NO_TEXT,
                String.join(":", AUTH_PREFIX, ACTION_LOGOUT, ACTION_CANCEL)
        );

        // Tugmalarni bitta qatorga joylab, klaviaturani qaytaramiz.
        return new InlineKeyboardMarkup(List.of(List.of(yesButton, noButton)));
    }

    @Override
    public InlineKeyboardMarkup createSingleButtonKeyboard(String text, String callbackData) {
        return new InlineKeyboardMarkup(List.of(List.of(createButton(text, callbackData))));
    }

    // --- ASOSIY MENYU TUGMALARI ---
    @Override
    public InlineKeyboardMarkup dashboardMenu() {
        return createSingleButtonKeyboard(
                Utils.InlineButtons.LOGOUT_TEXT,
                String.join(":",
                        Utils.CallbackData.AUTH_PREFIX,
                        Utils.CallbackData.ACTION_LOGOUT,
                        Utils.CallbackData.ACTION_INIT
                )
        );
    }

    @Override
    public InlineKeyboardMarkup welcomeFirstTime(Long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton button = new InlineKeyboardButton("🚀 Kirish / Ro'yxatdan o'tish");
        button.setUrl(appDomain + "/auth.html?chat_id=" + chatId);
        inlineKeyboardMarkup.setKeyboard(List.of(List.of(button)));
        return inlineKeyboardMarkup;
    }

    // --- FOYDALANUVCHILARNI BOSHQARISH UCHUN KLAAVIATURALAR ---
    @Override
    public InlineKeyboardMarkup usersMainMenu() {
        InlineKeyboardButton listBtn = createButton(Utils.InlineButtons.USER_LIST_TEXT, "admin:users:page:0");
        InlineKeyboardButton searchBtn = createButton(Utils.InlineButtons.USER_SEARCH_TEXT, "admin:users:search_init");
        InlineKeyboardButton statsBtn = createButton(Utils.InlineButtons.USER_STATS_TEXT, "admin:users:stats");
        InlineKeyboardButton backBtn = createButton(Utils.InlineButtons.BACK_TO_MAIN_MENU_TEXT, "admin:main_menu");

        return new InlineKeyboardMarkup(List.of(
                List.of(listBtn), List.of(searchBtn), List.of(statsBtn), List.of(backBtn)
        ));
    }

    @Override
    public InlineKeyboardMarkup usersPageMenu(Page<User> userPage, String searchTerm) {
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> numberButtonsRow = new ArrayList<>();
        List<User> usersOnPage = userPage.getContent();

        // 1-QATOR: Raqamli tugmalar (1, 2, 3...)
        for (int i = 0; i < usersOnPage.size(); i++) {
            User user = usersOnPage.get(i);
            String buttonText = String.valueOf(i + 1);
            String callbackData = "admin:users:view:" + user.getId();
            numberButtonsRow.add(createButton(buttonText, callbackData));

            if (i == 4) {
                keyboardRows.add(numberButtonsRow);
                numberButtonsRow = new ArrayList<>();
            }

        }
        if (!numberButtonsRow.isEmpty()) keyboardRows.add(numberButtonsRow);

        // 2-QATOR: Navigatsiya tugmalari (Oldingi/Keyingi)
        List<InlineKeyboardButton> navigationButtonsRow = new ArrayList<>();
        String baseCallback = (searchTerm == null || searchTerm.isBlank())
                ? "admin:users:page:"
                : "admin:users:search_page:" + searchTerm + ":";

        if (userPage.hasPrevious()) {
            navigationButtonsRow.add(createButton("⬅️ Oldingi", baseCallback + (userPage.getNumber() - 1)));
        }
        if (userPage.hasNext()) {
            navigationButtonsRow.add(createButton("Keyingi ➡️", baseCallback + (userPage.getNumber() + 1)));
        }
        if (!navigationButtonsRow.isEmpty()) keyboardRows.add(navigationButtonsRow);

        // 3-QATOR: Orqaga qaytish tugmasi
        keyboardRows.add(List.of(createButton("⬅️ Orqaga", "admin:users:main_menu")));
        return new InlineKeyboardMarkup(keyboardRows);
    }


    @Override
    public InlineKeyboardMarkup userDetailMenu(Long userId, String backCallbackData) {
        // ... "Bloklash", "Rolni o'zgartirish" kabi tugmalarni kelajakda qo'shasiz ...
        InlineKeyboardButton backButton = createButton("⬅️ Orqaga", backCallbackData);
        // Hozircha faqat bitta "Orqaga" tugmasi bor
        return new InlineKeyboardMarkup(List.of(List.of(backButton)));
    }


    // --- KURSLARNI BOSHQARISH UCHUN KLAAVIATURALAR ---

    @Override
    public InlineKeyboardMarkup coursesMainMenu() {
        InlineKeyboardButton listBtn = createButton("📖 Barcha kurslar", "admin:courses:browse:init");
//        InlineKeyboardButton listBtn = createButton("👥 Barcha foydalanuvchilar", "admin:users:page:0");
        InlineKeyboardButton searchBtn = createButton("🔍 Kurs qidirish", "admin:courses:search_init");
        InlineKeyboardButton statsBtn = createButton("📊 Statistika", "admin:courses:stats");
        InlineKeyboardButton backBtn = createButton("⬅️ Bosh menyu", "admin:main_menu");
        return new InlineKeyboardMarkup(List.of(List.of(listBtn), List.of(searchBtn), List.of(statsBtn), List.of(backBtn)));
    }


    @Override
    public InlineKeyboardMarkup coursesPageMenu(Page<Course> coursePage, String searchTerm, Long mentorId, Long categoryId) { // Yangi parametr: categoryId
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> numberButtonsRow = new ArrayList<>();
        List<Course> coursesOnPage = coursePage.getContent();

        for (int i = 0; i < coursesOnPage.size(); i++) {
            Course course = coursesOnPage.get(i);
            String buttonText = String.valueOf(i + 1);

            // --- KONTEKSTNI ANIQLASH ---
            String fromContext;
            if (searchTerm != null) {
                fromContext = "search:" + searchTerm;
            } else if (mentorId != null) {
                fromContext = "mentor:" + mentorId;
            } else if (categoryId != null) {
                fromContext = "category:" + categoryId; // <-- YANGI KONTEKST
            } else {
                fromContext = "all";
            }

            String callbackData = "admin:courses:view:" + course.getId() + ":" + fromContext + ":" + coursePage.getNumber();
            numberButtonsRow.add(createButton(buttonText, callbackData));
        }
        if (!numberButtonsRow.isEmpty()) keyboardRows.add(numberButtonsRow);

        // ... Navigatsiya va Orqaga tugmalari (ularni ham 'categoryId' bilan boyitish kerak) ...
        String backCallback;
        if (categoryId != null) {
            backCallback = "admin:courses:by_category:page:0"; // Kategoriyalar ro'yxatiga
        } else if (mentorId != null) {
            backCallback = "admin:courses:list_by_mentor:page:0"; // Mentorlar ro'yxatiga
        } else {
            backCallback = "admin:courses:browse:init"; // Tanlash menyusiga
        }
        keyboardRows.add(List.of(createButton("⬅️ Orqaga", backCallback)));

        return new InlineKeyboardMarkup(keyboardRows);
    }


    // InlineKeyboardServiceImpl.java
//    @Override
//    public InlineKeyboardMarkup coursesPageMenu(Page<Course> coursePage, String searchTerm, Long mentorId) {
//        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
//        List<InlineKeyboardButton> numberButtonsRow = new ArrayList<>();
//        List<Course> coursesOnPage = coursePage.getContent();
//
//        // --- 1-QADAM: Raqamli Tugmalarni To'g'ri Kontekst Bilan Yasash ---
//        for (int i = 0; i < coursesOnPage.size(); i++) {
//            Course course = coursesOnPage.get(i);
//            String buttonText = String.valueOf(i + 1);
//
//            // Kontekstni aniqlaymiz. Bu "Orqaga" tugmasi to'g'ri ishlashi uchun kerak.
//            String fromContext;
//            if (searchTerm != null && !searchTerm.isBlank()) {
//                fromContext = "search:" + searchTerm;
//            } else if (mentorId != null) {
//                fromContext = "mentor:" + mentorId;
//            } else {
//                fromContext = "all";
//            }
//
//            // YANGI, BOYITILGAN CALLBACK_DATA:
//            // Format: admin:courses:view:COURSE_ID:fromContext:PAGE_NUMBER
//            String callbackData = "admin:courses:view:" + course.getId() + ":" + fromContext + ":" + coursePage.getNumber();
//            numberButtonsRow.add(createButton(buttonText, callbackData));
//        }
//        if (!numberButtonsRow.isEmpty()) {
//            keyboardRows.add(numberButtonsRow);
//        }
//
//        // --- 2-QADAM: Navigatsiya Tugmalarini To'g'ri Kontekst Bilan Yasash ---
//        List<InlineKeyboardButton> navigationButtonsRow = new ArrayList<>();
//        String baseCallback;
//        if (searchTerm != null && !searchTerm.isBlank()) {
//            baseCallback = "admin:courses:search_page:" + searchTerm + ":";
//        } else if (mentorId != null) {
//            baseCallback = "admin:courses:list_by_mentor_courses:" + mentorId + ":page:";
//        } else {
//            baseCallback = "admin:courses:page:";
//        }
//
//        if (coursePage.hasPrevious()) {
//            navigationButtonsRow.add(createButton("⬅️ Oldingi", baseCallback + (coursePage.getNumber() - 1)));
//        }
//        if (coursePage.hasNext()) {
//            navigationButtonsRow.add(createButton("Keyingi ➡️", baseCallback + (coursePage.getNumber() + 1)));
//        }
//        if (!navigationButtonsRow.isEmpty()) {
//            keyboardRows.add(navigationButtonsRow);
//        }
//
//        // --- 3-QADAM: Orqaga Qaytish Tugmasini To'g'ri Kontekst Bilan Yasash ---
//        String backCallback;
//        if (mentorId != null) {
//            // Agar mentor kurslari bo'lsa, mentorlar ro'yxatiga qaytamiz
//            backCallback = "admin:courses:list_by_mentor:page:0";
//        } else {
//            // Agar umumiy ro'yxat yoki qidiruv bo'lsa, kurslarning asosiy menyusiga qaytamiz
//            backCallback = "admin:courses:main_menu";
//        }
//        keyboardRows.add(List.of(createButton("⬅️ Orqaga", backCallback)));
//
//        return new InlineKeyboardMarkup(keyboardRows);
//    }

    @Override
    public InlineKeyboardMarkup courseDetailMenu(Long courseId, String backCallbackData) {
        InlineKeyboardButton backButton = createButton("⬅️ Ro'yxatga qaytish", backCallbackData);
        return new InlineKeyboardMarkup(List.of(List.of(backButton)));
    }

    @Override
    public InlineKeyboardMarkup courseBrowseMethodMenu(String backCallback) {
        InlineKeyboardButton allCourses = createButton("📚 Barcha Kurslar", "admin:courses:page:0");
        InlineKeyboardButton byCategory = createButton("🗂 Kategoriyalar bo'yicha", "admin:courses:list:by_category:page:0");
        InlineKeyboardButton byMentor = createButton("👨‍🏫 Mentorlar bo'yicha", "admin:courses:list_by_mentor:page:0");
        InlineKeyboardButton back = createButton("⬅️ Orqaga", backCallback);

        return new InlineKeyboardMarkup( List.of(List.of(allCourses), List.of(byCategory), List.of(byMentor), List.of(back)));
    }


    // InlineKeyboardServiceImpl.java

    @Override
    public InlineKeyboardMarkup mentorsPageMenu(Page<User> mentorPage, String backCallback) {
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        mentorPage.getContent().forEach(mentor -> {
            String name = mentor.getProfile().getFirstName() + " " + mentor.getProfile().getLastName();
            String callbackData = "admin:courses:list_by_mentor_courses:" + mentor.getId() + ":page:0";
//            String s = String.join(":", ACTION_ADMIN, ACTION_COURSE, ACTION_LIST, INSTRUCTOR, mentor.getId().toString(), ACTION_PAGE, "0")
            keyboardRows.add(List.of(createButton(name, callbackData)));
        });

        List<InlineKeyboardButton> navRow = new ArrayList<>();
        if (mentorPage.hasPrevious()) {
            navRow.add(createButton("⬅️ Oldingi", "admin:courses:list_by_mentor:page:" + (mentorPage.getNumber() - 1)));
        }
        if (mentorPage.hasNext()) {
            navRow.add(createButton("Keyingi ➡️", "admin:courses:list_by_mentor:page:" + (mentorPage.getNumber() + 1)));
        }
        if (!navRow.isEmpty()) keyboardRows.add(navRow);

        keyboardRows.add(List.of(createButton("⬅️ Orqaga", backCallback)));
        return new InlineKeyboardMarkup(keyboardRows);
    }


    @Override
    public InlineKeyboardMarkup categoriesPageMenu(Page<Category> categoryPage, String backCallback) {
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        // Har bir kategoriya uchun tugma
        categoryPage.getContent().forEach(category -> {
            // Callback: admin:courses:by_category_courses:CATEGORY_ID:page:0
            String callback = "admin:courses:by_category_courses:" + category.getId() + ":page:0";
            keyboardRows.add(List.of(createButton(category.getName(), callback)));
        });

        // Pagination
        List<InlineKeyboardButton> navRow = new ArrayList<>();
        if (categoryPage.hasPrevious()) {
            navRow.add(createButton("⬅️", "admin:courses:by_category:page:" + (categoryPage.getNumber() - 1)));
        }
        if (categoryPage.hasNext()) {
            navRow.add(createButton("➡️", "admin:courses:by_category:page:" + (categoryPage.getNumber() + 1)));
        }
        if (!navRow.isEmpty()) keyboardRows.add(navRow);

        // Orqaga qaytish
        keyboardRows.add(List.of(createButton("⬅️ Orqaga", backCallback)));

        return new InlineKeyboardMarkup(keyboardRows);
    }

    @Override
    public InlineKeyboardMarkup allCoursesPageMenu(Page<Course> coursePage) {
        return buildCoursesPageKeyboard(coursePage, "admin:courses:page:", "admin:courses:browse:init");
    }

    private InlineKeyboardMarkup buildCoursesPageKeyboard(Page<Course> coursePage, String baseNavCallback, String backCallback) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> numberButtonsRow = new ArrayList<>();

        for (int i = 0; i < coursePage.getContent().size(); i++) {
            Course course = coursePage.getContent().get(i);
            // Callback'ga faqat kerakli ma'lumotlarni qo'shamiz: 'view', ID va qaysi sahifadan kelgani
            String viewCallback = "admin:courses:view:" + course.getId() + ":" + coursePage.getNumber();
            numberButtonsRow.add(createButton(String.valueOf(i + 1), viewCallback));
        }
        if (!numberButtonsRow.isEmpty()) rows.add(numberButtonsRow);

        List<InlineKeyboardButton> navRow = new ArrayList<>();
        if (coursePage.hasPrevious()) { navRow.add(createButton("⬅️", baseNavCallback + (coursePage.getNumber() - 1))); }
        if (coursePage.hasNext()) { navRow.add(createButton("➡️", baseNavCallback + (coursePage.getNumber() + 1))); }
        if (!navRow.isEmpty()) rows.add(navRow);

        rows.add(List.of(createButton("⬅️ Orqaga", backCallback)));
        return new InlineKeyboardMarkup(rows);
    }

    @Override
    public InlineKeyboardMarkup coursesByMentorPageMenu(Page<Course> coursePage, Long mentorId) {
        String baseCallback = "admin:courses:mentor_courses:" + mentorId + ":page:";
        String backCallback = "admin:courses:by_mentor:page:0";
        return buildCoursesPageKeyboard(coursePage, baseCallback, backCallback);
    }

    @Override
    public InlineKeyboardMarkup coursesByCategoryPageMenu(Page<Course> coursePage, Long categoryId) {
        String baseCallback = "admin:courses:by_category_courses:" + categoryId + ":page:";
        String backCallback = "admin:courses:by_category:page:0";
        return buildCoursesPageKeyboard(coursePage, baseCallback, backCallback);
    }


}
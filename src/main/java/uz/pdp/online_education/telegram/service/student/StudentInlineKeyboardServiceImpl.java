package uz.pdp.online_education.telegram.service.student;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.ModuleEnrollment;
import uz.pdp.online_education.model.lesson.*;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.service.student.template.StudentInlineKeyboardService;

import java.util.ArrayList;
import java.util.List;


@Service
public class StudentInlineKeyboardServiceImpl implements StudentInlineKeyboardService {

    /**
     * {@inheritDoc}
     */
    @Override
    public InlineKeyboardMarkup dashboardMenu() {
        return createSingleButtonKeyboard(
                Utils.InlineButtons.LOGOUT_TEXT,
                Utils.CallbackData.AUTH_LOGOUT_INIT_CALLBACK
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InlineKeyboardMarkup logoutConfirmation() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        // Create the "Yes" and "No" buttons
        InlineKeyboardButton yesButton = createButton(
                Utils.InlineButtons.LOGOUT_CONFIRM_YES_TEXT,
                Utils.CallbackData.AUTH_LOGOUT_CONFIRM_CALLBACK
        );

        InlineKeyboardButton noButton = createButton(
                Utils.InlineButtons.LOGOUT_CONFIRM_NO_TEXT,
                Utils.CallbackData.AUTH_LOGOUT_CANCEL_CALLBACK
        );

        // Place them side-by-side in a single row
        markup.setKeyboard(List.of(List.of(yesButton, noButton)));

        return markup;
    }

    @Override
    public InlineKeyboardMarkup myCoursesListPage(Page<Course> coursesPage) {
        int currentPage = coursesPage.getNumber();
        List<Course> courses = coursesPage.getContent();

        // Create the numbered buttons [1][2][3]...
        List<InlineKeyboardButton> numberButtons = new ArrayList<>();
        for (int i = 0; i < courses.size(); i++) {
            int courseNumber = currentPage * coursesPage.getSize() + i + 1;
            Course course = courses.get(i);
            numberButtons.add(createButton(
                    String.valueOf(courseNumber),
                    Utils.CallbackData.MY_COURSE_VIEW_CALLBACK + course.getId()
            ));
        }

        // Use the generic pagination helper method
        return createPaginationKeyboardWithNumbers(
                coursesPage,
                numberButtons, // The numbered buttons
                Utils.CallbackData.MY_COURSE_LIST_PAGE_CALLBACK, // "mycourse:list:page:"
                "student:main_menu" // Back button callback
        );
    }


    @Override
    public InlineKeyboardMarkup courseModulesPage(Long courseId, List<ModuleEnrollment> enrollments) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // 1. Har bir modul uchun raqamli tugmalarni yaratish
        List<InlineKeyboardButton> numberButtons = new ArrayList<>();
        for (int i = 0; i < enrollments.size(); i++) {
            Module module = enrollments.get(i).getModule();
            int moduleNumber = i + 1;
            numberButtons.add(createButton(
                    String.valueOf(moduleNumber),
                    "mymodule:view:" + module.getId() // Modulni ko'rish uchun callback
            ));
        }
        if (!numberButtons.isEmpty()) {
            keyboard.add(numberButtons);
        }

        // 2. Orqaga qaytish tugmasi
        keyboard.add(List.of(createButton(
                "⬅️ Mening Kurslarimga qaytish",
                "mycourse:list:page:0" // "Mening Kurslarim"ning birinchi sahifasiga qaytish
        )));

        markup.setKeyboard(keyboard);
        return markup;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InlineKeyboardMarkup backToMyCourses() {
        return createSingleButtonKeyboard(
                Utils.InlineButtons.BACK_TO_MY_COURSES_TEXT,
                Utils.CallbackData.BACK_TO_MY_COURSES_CALLBACK
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InlineKeyboardMarkup lessonListPage(Module module, List<Lesson> lessons, boolean isEnrolled) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // 1. Agar foydalanuvchi a'zo bo'lmagan bo'lsa, "Sotib olish" tugmasini qo'shamiz.
        if (!isEnrolled && module.getPrice() > 0) {
            String buttonText = String.format("🚀 Modulni sotib olish (%d UZS)", module.getPrice());
            keyboard.add(List.of(createButton(
                    buttonText,
                    "module:buy:" + module.getId()
            )));
        }

        // 2. Darslar uchun raqamli navigatsiya tugmalarini yaratamiz.
        List<InlineKeyboardButton> numberButtons = new ArrayList<>();
        for (Lesson lesson : lessons) {
            boolean canAccess = isEnrolled || lesson.isFree();

            // Tugmaga emoji qo'shish orqali holatini bildiramiz.
            String buttonText = (canAccess ? "▶️ " : "🔒 ") + lesson.getOrderIndex();

            numberButtons.add(createButton(
                    buttonText,
                    canAccess ? "mylesson:view:" + lesson.getId() : "lesson:locked"
            ));
        }
        if (!numberButtons.isEmpty()) {
            keyboard.add(numberButtons);
        }

        // 3. "Orqaga" tugmasini to'g'ri manzil bilan yaratamiz.
        keyboard.add(List.of(createButton(
                "⬅️ Modullar ro'yxatiga qaytish",
                "mycourse:view:" + module.getCourse().getId() // Module'dan courseId'ni olamiz.
        )));

        markup.setKeyboard(keyboard);
        return markup;
    }

    @Override
    public InlineKeyboardMarkup lessonViewKeyboard(Long moduleId, String lessonUrlOnSite) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // Button to view the full lesson on the website
        InlineKeyboardButton viewOnSiteButton = createButton("🌐 Darsni to'liq ko'rish (Saytda)", null);
        viewOnSiteButton.setUrl(lessonUrlOnSite); // The URL is passed as a parameter
        keyboard.add(List.of(viewOnSiteButton));

        // Button to go back to the list of lessons for the module
        keyboard.add(List.of(createButton(
                "⬅️ Darslar ro'yxatiga qaytish",
                "mymodule:view:" + moduleId
        )));

        markup.setKeyboard(keyboard);
        return markup;
    }

    @Override
    @Transactional(readOnly = true) // Lesson'dan lazy bog'liqliklarni o'qish uchun
    public InlineKeyboardMarkup lessonContentMenu(Lesson lesson) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // Lesson'ga bog'langan barcha Content'larni aylanib chiqamiz
        for (Content content : lesson.getContents()) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            String buttonText = "❓ Noma'lum kontent";

            if (content instanceof AttachmentContent) {
                buttonText = "▶️ Asosiy Video Darslik";
            } else if (content instanceof TextContent) {
                buttonText = "📝 Darsning To'liq Matni";
            } else if (content instanceof QuizContent) {
                buttonText = "❓ Amaliy Mashg'ulot (Quiz)";
            }

            button.setText(buttonText);
            button.setCallbackData("content:view:" + content.getId());
            keyboard.add(List.of(button));
        }

        // "Orqaga" tugmasini qo'shish
        keyboard.add(List.of(createButton(
                "⬅️ Darslar ro'yxatiga qaytish",
                "mymodule:view:" + lesson.getModule().getId()
        )));

        markup.setKeyboard(keyboard);
        return markup;
    }

    // Quiz uchun alohida klaviatura
    @Override
    public InlineKeyboardMarkup quizButton(Long quizId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        InlineKeyboardButton button = new InlineKeyboardButton("✍️ Testni Boshlash (Saytda)");
        // TODO: Sayt manzilini application.yml'dan olish kerak
        button.setUrl("http://your-site.com/quiz/" + quizId);
        markup.setKeyboard(List.of(List.of(button)));
        return markup;
    }

    /**
     * @param module 
     * @return
     */
    @Override
    public InlineKeyboardMarkup buyOnlyKeyboard(Module module) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        String buttonText = String.format("🚀 Modulni sotib olish (%d UZS)", module.getPrice());
        keyboard.add(List.of(createButton(
                buttonText,
                "module:buy:" + module.getId()
        )));

        keyboard.add(List.of(createButton(
                "⬅️ Modullar ro'yxatiga qaytish",
                "mycourse:view:" + module.getCourse().getId()
        )));

        markup.setKeyboard(keyboard);
        return markup;
    }

    // Add this new generic helper to your InlineKeyboardServiceImpl
    private InlineKeyboardMarkup createPaginationKeyboardWithNumbers(Page<?> page, List<InlineKeyboardButton> numberButtons, String pageCallbackPrefix, String backCallback) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // Row for numbered buttons
        if (!numberButtons.isEmpty()) {
            keyboard.add(numberButtons);
        }

        // Row for prev/next buttons
        List<InlineKeyboardButton> navButtons = new ArrayList<>();
        if (page.hasPrevious()) {
            navButtons.add(createButton("⬅️ Oldingisi", pageCallbackPrefix + (page.getNumber() - 1)));
        }
        navButtons.add(createButton((page.getNumber() + 1) + "/" + page.getTotalPages(), "noop"));
        if (page.hasNext()) {
            navButtons.add(createButton("Keyingisi ➡️", pageCallbackPrefix + (page.getNumber() + 1)));
        }
        if (!navButtons.isEmpty()) {
            keyboard.add(navButtons);
        }

        // Row for back button
        keyboard.add(List.of(createButton("⬅️ Bosh menyuga", backCallback)));

        markup.setKeyboard(keyboard);
        return markup;
    }

    /**
     * A private helper method to create a single {@link InlineKeyboardButton}.
     * This centralizes button creation.
     *
     * @param text         The text to be displayed on the button.
     * @param callbackData The data to be sent when the button is pressed.
     * @return A configured {@link InlineKeyboardButton} object.
     */
    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton(text);
        if (callbackData != null) {
            button.setCallbackData(callbackData);
        }
        return button;
    }

    /**
     * A private helper to create a keyboard with a single, centered button.
     * Useful for simple actions like "Back".
     *
     * @param text         The button's text.
     * @param callbackData The button's callback data.
     * @return An {@link InlineKeyboardMarkup} with one button.
     */
    private InlineKeyboardMarkup createSingleButtonKeyboard(String text, String callbackData) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        InlineKeyboardButton button = createButton(text, callbackData);
        markup.setKeyboard(List.of(List.of(button)));
        return markup;
    }
}

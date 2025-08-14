package uz.pdp.online_education.telegram.service.student;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.lesson.*;
import uz.pdp.online_education.repository.LessonRepository;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.enums.BotMessage;
import uz.pdp.online_education.telegram.service.message.MessageService;
import uz.pdp.online_education.telegram.service.student.template.StudentInlineKeyboardService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentInlineKeyboardServiceImpl implements StudentInlineKeyboardService {

    private final LessonRepository lessonRepository;
    private final MessageService messageService;

    /**
     * Creates the keyboard for the dashboard message, including a "Logout" button.
     */
    @Override
    public InlineKeyboardMarkup dashboardMenu() {
        // Tizimdan chiqish tugmasini yasash uchun yordamchi metodni chaqiramiz.
        return createSingleButtonKeyboard(
                Utils.InlineButtons.LOGOUT_TEXT,
                Utils.CallbackData.AUTH_LOGOUT_INIT_CALLBACK
        );
    }

    /**
     * Creates a confirmation keyboard for the logout action.
     */
    @Override
    public InlineKeyboardMarkup logoutConfirmation() {
        // "Ha" va "Yo'q" tugmalarini yaratamiz.
        InlineKeyboardButton yesButton = createButton(
                Utils.InlineButtons.LOGOUT_CONFIRM_YES_TEXT,
                Utils.CallbackData.AUTH_LOGOUT_CONFIRM_CALLBACK
        );
        InlineKeyboardButton noButton = createButton(
                Utils.InlineButtons.LOGOUT_CONFIRM_NO_TEXT,
                Utils.CallbackData.AUTH_LOGOUT_CANCEL_CALLBACK
        );

        // Tugmalarni bitta qatorga joylab, klaviaturani qaytaramiz.
        return new InlineKeyboardMarkup(List.of(List.of(yesButton, noButton)));
    }

    /**
     * Builds a paginated menu for the student's enrolled courses.
     */
    @Override
    @Transactional(readOnly = true)
    public InlineKeyboardMarkup myCoursesMenu(Page<Course> coursePage) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // 1. Har bir kurs uchun alohida tugma yaratamiz.
        coursePage.getContent().forEach(course -> {
            String buttonText = "🎓 " + course.getTitle();
            String callbackData = String.join(":",
                    Utils.CallbackData.MY_COURSE_PREFIX, Utils.CallbackData.ACTION_VIEW, course.getId().toString());
            keyboard.add(List.of(createButton(buttonText, callbackData)));
        });

        // 2. Navigatsiya (sahifalash) tugmalarini qo'shamiz.
        addPaginationButtons(keyboard, coursePage, Utils.CallbackData.MY_COURSE_PREFIX);

        // 3. "Bosh menyuga qaytish" tugmasini qo'shamiz.
        String backCallback = String.join(":",
                Utils.CallbackData.STUDENT_PREFIX, Utils.CallbackData.ACTION_BACK, Utils.CallbackData.BACK_TO_MAIN_MENU);
        keyboard.add(List.of(createButton("⬅️ " + Utils.InlineButtons.BACK_TO_MAIN_MENU_TEXT, backCallback)));

        return new InlineKeyboardMarkup(keyboard);
    }

    /**
     * Builds a menu for the modules within a specific course.
     */
    @Override
    @Transactional(readOnly = true)
    public InlineKeyboardMarkup modulesMenu(Page<Module> modulePage, Long courseId, List<Long> enrolledModuleIds, boolean isEnrolledToFullCourse) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // Har bir modul uchun foydalanuvchining statusiga qarab tugma yaratamiz.
        modulePage.getContent().forEach(module -> {
            String buttonText;
            String callbackData;

            // Agar foydalanuvchi butun kursni sotib olgan bo'lsa, hamma modul ochiq.
            if (isEnrolledToFullCourse || enrolledModuleIds.contains(module.getId())) {
                buttonText = "✅ " + module.getTitle(); // To'liq kursga yoki shu modulga a'zo
                callbackData = String.join(":", Utils.CallbackData.MODULE_PREFIX, Utils.CallbackData.ACTION_VIEW, module.getId().toString());
            } else if (lessonRepository.existsByModuleAndIsFreeTrue(module)) {
                buttonText = "🆓 " + module.getTitle(); // Modulda bepul dars bor
                callbackData = String.join(":", Utils.CallbackData.MODULE_PREFIX, Utils.CallbackData.ACTION_VIEW, module.getId().toString());
            } else {
                buttonText = "🔒 " + module.getTitle(); // Yopiq modul
                callbackData = String.join(":", Utils.CallbackData.MODULE_PREFIX, Utils.CallbackData.ACTION_BUY, module.getId().toString());
            }
            keyboard.add(List.of(createButton(buttonText, callbackData)));
        });

        // Navigatsiya va "orqaga" tugmalarini qo'shamiz.
        addPaginationButtons(keyboard, modulePage, Utils.CallbackData.MODULE_PREFIX + ":" + courseId);
        String backCallback = String.join(":", Utils.CallbackData.MY_COURSE_PREFIX, Utils.CallbackData.ACTION_LIST, Utils.CallbackData.ACTION_PAGE, "0");
        keyboard.add(List.of(createButton("⬅️ Kurslar ro'yxatiga", backCallback)));

        return new InlineKeyboardMarkup(keyboard);
    }

    /**
     * Generates a menu for the lessons within a specific module.
     */
    @Override
    @Transactional(readOnly = true)
    public InlineKeyboardMarkup lessonsMenu(Page<Lesson> lessonPage, Long moduleId, Long courseId, boolean isModuleEnrolled) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // Har bir dars uchun uning ochiq yoki yopiqligiga qarab tugma yaratamiz.
        lessonPage.getContent().forEach(lesson -> {
            String buttonText;
            String callbackData;

            if (isModuleEnrolled || lesson.isFree()) {
                buttonText = "📖 " + lesson.getTitle();
                callbackData = String.join(":",
                        Utils.CallbackData.LESSON_PREFIX, Utils.CallbackData.ACTION_VIEW, lesson.getId().toString());
            } else {
                buttonText = "🔒 " + lesson.getTitle();
                callbackData = String.join(":",
                        Utils.CallbackData.LESSON_PREFIX, Utils.CallbackData.ACTION_BUY, moduleId.toString());
            }
            keyboard.add(List.of(createButton(buttonText, callbackData)));
        });

        // Navigatsiya va "orqaga" tugmalarini qo'shamiz.
        String paginationBaseCallback = String.join(":", Utils.CallbackData.LESSON_PREFIX, moduleId.toString());
        addPaginationButtons(keyboard, lessonPage, paginationBaseCallback);
        String backCallback = String.join(":", Utils.CallbackData.MY_COURSE_PREFIX, Utils.CallbackData.ACTION_VIEW, courseId.toString());
        keyboard.add(List.of(createButton("⬅️ Modullar ro'yxatiga", backCallback)));

        return new InlineKeyboardMarkup(keyboard);
    }

    /**
     * Creates a menu that lists the content blocks of a specific lesson.
     */
    @Override
    @Transactional(readOnly = true)
    public InlineKeyboardMarkup lessonContentsMenu(Lesson lesson, Long moduleId) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // Darsning har bir kontent bloki uchun tugma yaratamiz.
        lesson.getContents().forEach(content -> {
            String icon = switch (content.getClass().getSimpleName()) {
                case "TextContent" -> "📄";
                case "AttachmentContent" -> "▶️";
                case "QuizContent" -> "❓";
                default -> "▫️";
            };
            String typeText = switch (content.getClass().getSimpleName()) {
                case "TextContent" -> "Matn";
                case "AttachmentContent" -> "Video/Fayl";
                case "QuizContent" -> "Test";
                default -> "Kontent";
            };

            String buttonText = messageService.getMessage(BotMessage.LESSON_CONTENT_BUTTON_TEXT, icon, content.getBlockOrder(), typeText);
            String callbackData = String.join(":", Utils.CallbackData.CONTENT_PREFIX, Utils.CallbackData.ACTION_VIEW, content.getId().toString());
            keyboard.add(List.of(createButton(buttonText, callbackData)));
        });

        // "Darslar ro'yxatiga qaytish" tugmasini qo'shamiz.
        String backCallback = String.join(":", Utils.CallbackData.MODULE_PREFIX, Utils.CallbackData.ACTION_VIEW, moduleId.toString());
        keyboard.add(List.of(createButton("⬅️ Darslar ro'yxatiga", backCallback)));

        return new InlineKeyboardMarkup(keyboard);
    }

    /**
     * Creates a simple inline keyboard with a single button.
     */
    @Override
    public InlineKeyboardMarkup createSingleButtonKeyboard(String text, String callbackData) {
        // Yangi klaviatura obyekti yaratib, unga bitta tugma joylaymiz.
        InlineKeyboardButton button = createButton(text, callbackData);
        return new InlineKeyboardMarkup(List.of(List.of(button)));
    }

    /**
     * Creates an inline keyboard with a single button that links to an external URL.
     */
    @Override
    public InlineKeyboardMarkup createUrlButton(String text, String url) {
        // URL uchun maxsus tugma yaratamiz.
        InlineKeyboardButton button = new InlineKeyboardButton(text);
        button.setUrl(url);
        return new InlineKeyboardMarkup(List.of(List.of(button)));
    }


    // --- PRIVATE HELPER METHODS ---

    /**
     * A private helper method to add pagination buttons (Next, Previous) to a keyboard.
     */
    private void addPaginationButtons(List<List<InlineKeyboardButton>> keyboard, Page<?> page, String baseCallback) {
        // Agar sahifalar soni 1 tadan ko'p bo'lsa, navigatsiya tugmalarini qo'shamiz.
        if (page.getTotalPages() > 1) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            int currentPage = page.getNumber();

            // "Oldingi" tugmasi
            if (page.hasPrevious()) {
                String prevCallback = String.join(":",
                        baseCallback, Utils.CallbackData.ACTION_LIST, Utils.CallbackData.ACTION_PAGE, String.valueOf(currentPage - 1));
                row.add(createButton("⬅️ Oldingi", prevCallback));
            }

            // Sahifa raqami ko'rsatkich
            row.add(createButton(String.format("%d / %d", currentPage + 1, page.getTotalPages()), "do_nothing"));

            // "Keyingi" tugmasi
            if (page.hasNext()) {
                String nextCallback = String.join(":",
                        baseCallback, Utils.CallbackData.ACTION_LIST, Utils.CallbackData.ACTION_PAGE, String.valueOf(currentPage + 1));
                row.add(createButton("Keyingi ➡️", nextCallback));
            }
            keyboard.add(row);
        }
    }

    /**
     * A private helper method to create a single InlineKeyboardButton.
     */
    private InlineKeyboardButton createButton(String text, String callbackData) {
        // Tugma yaratish uchun markazlashtirilgan metod.
        InlineKeyboardButton button = new InlineKeyboardButton(text);
        button.setCallbackData(callbackData);
        return button;
    }
}
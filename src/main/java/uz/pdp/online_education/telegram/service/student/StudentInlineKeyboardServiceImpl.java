package uz.pdp.online_education.telegram.service.student;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.ModuleEnrollment;
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
    @Transactional(readOnly = true)
    public InlineKeyboardMarkup myCoursesMenu(Page<Course> coursePage) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // 1. Kurslar ro'yxatini tugmalarga aylantiramiz
        coursePage.getContent().forEach(course -> {
            String buttonText = "🎓 " + course.getTitle();
            // Callback: myc:v:{courseId}
            String callbackData = String.join(":",
                    Utils.CallbackData.MY_COURSE_PREFIX,
                    Utils.CallbackData.ACTION_VIEW,
                    course.getId().toString());
            keyboard.add(List.of(createButton(buttonText, callbackData)));
        });

        // 2. Navigatsiya tugmalarini qo'shamiz
        addPaginationButtons(keyboard, coursePage, Utils.CallbackData.MY_COURSE_PREFIX);

        // 3. "Bosh menyuga qaytish" tugmasini qo'shamiz
        // Callback: std:b:main (student:back:main_menu)
        String backCallback = String.join(":",
                Utils.CallbackData.STUDENT_PREFIX,
                Utils.CallbackData.ACTION_BACK,
                Utils.CallbackData.BACK_TO_MAIN_MENU);
        keyboard.add(List.of(createButton("⬅️ " + Utils.InlineButtons.BACK_TO_MAIN_MENU_TEXT, backCallback)));

        return new InlineKeyboardMarkup(keyboard);
    }

    @Override
    @Transactional(readOnly = true)
    public InlineKeyboardMarkup modulesMenu(Page<Module> modulePage, Long courseId, List<Long> enrolledModuleIds, boolean isEnrolledToFullCourse) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        modulePage.getContent().forEach(module -> {
            String buttonText;
            String callbackData;

            // YANGI SHART: Agar foydalanuvchi butun kursni sotib olgan bo'lsa, hamma modul ochiq.
            if (isEnrolledToFullCourse) {
                buttonText = "✅ " + module.getTitle();
                callbackData = String.join(":", Utils.CallbackData.MODULE_PREFIX, Utils.CallbackData.ACTION_VIEW, module.getId().toString());

                // Aks holda, eski logika bo'yicha har bir modulni alohida tekshiramiz.
            } else if (enrolledModuleIds.contains(module.getId())) {
                buttonText = "✅ " + module.getTitle(); // Bu ham alohida sotib olingan modul
                callbackData = String.join(":", Utils.CallbackData.MODULE_PREFIX, Utils.CallbackData.ACTION_VIEW, module.getId().toString());
            } else if (lessonRepository.existsByModuleAndIsFreeTrue(module)) {
                buttonText = "🆓 " + module.getTitle();
                callbackData = String.join(":", Utils.CallbackData.MODULE_PREFIX, Utils.CallbackData.ACTION_VIEW, module.getId().toString());
            } else {
                buttonText = "🔒 " + module.getTitle();
                callbackData = String.join(":", Utils.CallbackData.MODULE_PREFIX, Utils.CallbackData.ACTION_BUY, module.getId().toString());
            }
            keyboard.add(List.of(createButton(buttonText, callbackData)));
        });
        addPaginationButtons(keyboard, modulePage, Utils.CallbackData.MODULE_PREFIX + ":" + courseId);
        String backCallback = String.join(":", Utils.CallbackData.MY_COURSE_PREFIX, Utils.CallbackData.ACTION_LIST, Utils.CallbackData.ACTION_PAGE, "0");
        keyboard.add(List.of(createButton("⬅️ Kurslar ro'yxatiga", backCallback)));

        return new InlineKeyboardMarkup(keyboard);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public InlineKeyboardMarkup createSingleButtonKeyboard(String text, String callbackData) {
        // 1. Yangi klaviatura obyekti yaratamiz
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        // 2. Yagona tugmani yaratish uchun helper metodimizni chaqiramiz
        InlineKeyboardButton button = createButton(text, callbackData);

        // 3. Tugmani bitta qator (List) ichiga,
        //    va o'sha qatorni klaviatura ro'yxati (List of List) ichiga joylaymiz.
        markup.setKeyboard(List.of(List.of(button)));

        // 4. Tayyor klaviaturani qaytaramiz
        return markup;
    }


    @Override
    @Transactional(readOnly = true)
    public InlineKeyboardMarkup lessonsMenu(Page<Lesson> lessonPage, Long moduleId, Long courseId, boolean isModuleEnrolled) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // 1. Darslar ro'yxatini tugmalarga aylantiramiz
        lessonPage.getContent().forEach(lesson -> {
            String buttonText;
            String callbackData;

            // Agar modul sotib olingan bo'lsa yoki darsning o'zi bepul bo'lsa, dars ochiq
            if (isModuleEnrolled || lesson.isFree()) {
                buttonText = "📖 " + lesson.getTitle();
                // Callback: les:v:{lessonId}
                callbackData = String.join(":",
                        Utils.CallbackData.LESSON_PREFIX,
                        Utils.CallbackData.ACTION_VIEW,
                        lesson.getId().toString());
            } else {
                buttonText = "🔒 " + lesson.getTitle();
                // Yopiq dars uchun ham callback qo'yamiz, bosganda "sotib oling" degan xabar chiqishi uchun
                // Callback: les:buy:{moduleId}
                callbackData = String.join(":",
                        Utils.CallbackData.LESSON_PREFIX,
                        Utils.CallbackData.ACTION_BUY,
                        moduleId.toString());
            }
            keyboard.add(List.of(createButton(buttonText, callbackData)));
        });

        // 2. Navigatsiya tugmalarini qo'shamiz
        // Base callback: les:{moduleId}:l:p:{pageNum}
        String paginationBaseCallback = String.join(":",
                Utils.CallbackData.LESSON_PREFIX,
                moduleId.toString());
        addPaginationButtons(keyboard, lessonPage, paginationBaseCallback);

        // 3. "Modullar ro'yxatiga qaytish" tugmasini qo'shamiz
        // Callback: myc:v:{courseId}
        String backCallback = String.join(":",
                Utils.CallbackData.MY_COURSE_PREFIX,
                Utils.CallbackData.ACTION_VIEW,
                courseId.toString());
        keyboard.add(List.of(createButton("⬅️ Modullar ro'yxatiga", backCallback)));

        return new InlineKeyboardMarkup(keyboard);
    }

    @Override
    @Transactional(readOnly = true)
    public InlineKeyboardMarkup lessonContentsMenu(Lesson lesson, Long moduleId) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // 1. Kontent bloklari uchun tugmalar
        lesson.getContents().forEach(content -> {
            String icon = "";
            String typeText = "";
            if (content instanceof TextContent) {
                icon = "📄";
                typeText = "Matn";
            } else if (content instanceof AttachmentContent) {
                icon = "▶️";
                typeText = "Video/Fayl";
            } else if (content instanceof QuizContent) {
                icon = "❓";
                typeText = "Test";
            }

            String buttonText = messageService.getMessage(
                    BotMessage.LESSON_CONTENT_BUTTON_TEXT,
                    icon,
                    content.getBlockOrder(), // Dars ichidagi tartibi
                    typeText
            );

            // Callback: con:v:{contentId}
            String callbackData = String.join(":",
                    Utils.CallbackData.CONTENT_PREFIX,
                    Utils.CallbackData.ACTION_VIEW,
                    content.getId().toString()
            );
            keyboard.add(List.of(createButton(buttonText, callbackData)));
        });

        // 2. "Darslar ro'yxatiga qaytish" tugmasi
        // Callback: mod:v:{moduleId}
        String backCallback = String.join(":",
                Utils.CallbackData.MODULE_PREFIX,
                Utils.CallbackData.ACTION_VIEW,
                moduleId.toString());
        keyboard.add(List.of(createButton("⬅️ Darslar ro'yxatiga", backCallback)));

        return new InlineKeyboardMarkup(keyboard);
    }

    /**
     * Veb-saytga olib boruvchi URL tugmasidan iborat klaviatura yaratadi.
     *
     * @param text
     * @param url
     */
    @Override
    public InlineKeyboardMarkup createUrlButton(String text, String url) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        InlineKeyboardButton button = new InlineKeyboardButton(text);
        button.setUrl(url); // setCallbackData o'rniga setUrl ishlatiladi

        markup.setKeyboard(List.of(List.of(button)));
        return markup;
    }

    /**
     * Universal sahifalash tugmalarini yasaydigan yordamchi metod.
     *
     * @param keyboard     Klaviatura ro'yxati
     * @param page         Sahifalangan ma'lumot obyekti
     * @param baseCallback Navigatsiya uchun asosiy callback (masalan, "myc" yoki "mod:123")
     */
    private void addPaginationButtons(List<List<InlineKeyboardButton>> keyboard, Page<?> page, String baseCallback) {
        if (page.getTotalPages() > 1) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            int currentPage = page.getNumber();

            // "Oldingi" tugmasi
            if (page.hasPrevious()) {
                // Callback: {baseCallback}:l:p:{prev_page_num}
                String prevCallback = String.join(":",
                        baseCallback,
                        Utils.CallbackData.ACTION_LIST,
                        Utils.CallbackData.ACTION_PAGE,
                        String.valueOf(currentPage - 1));
                row.add(createButton("⬅️ Oldingi", prevCallback));
            }

            // Sahifa raqami
            row.add(createButton(String.format("%d / %d", currentPage + 1, page.getTotalPages()), "do_nothing"));

            // "Keyingi" tugmasi
            if (page.hasNext()) {
                // Callback: {baseCallback}:l:p:{next_page_num}
                String nextCallback = String.join(":",
                        baseCallback,
                        Utils.CallbackData.ACTION_LIST,
                        Utils.CallbackData.ACTION_PAGE,
                        String.valueOf(currentPage + 1));
                row.add(createButton("Keyingi ➡️", nextCallback));
            }
            keyboard.add(row);
        }
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
//    private InlineKeyboardMarkup createSingleButtonKeyboard(String text, String callbackData) {
//        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
//        InlineKeyboardButton button = createButton(text, callbackData);
//        markup.setKeyboard(List.of(List.of(button)));
//        return markup;
//    }
}

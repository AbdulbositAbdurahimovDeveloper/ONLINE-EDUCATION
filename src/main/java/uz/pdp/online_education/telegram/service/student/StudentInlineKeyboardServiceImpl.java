package uz.pdp.online_education.telegram.service.student;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.Faq;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.Payment;
import uz.pdp.online_education.model.lesson.*;
import uz.pdp.online_education.payload.CategoryInfo;
import uz.pdp.online_education.payload.PageDTO;
import uz.pdp.online_education.payload.UserInfo;
import uz.pdp.online_education.payload.course.CourseDetailDTO;
import uz.pdp.online_education.payload.lesson.LessonResponseDTO;
import uz.pdp.online_education.payload.module.ModuleDetailDTO;
import uz.pdp.online_education.repository.LessonRepository;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.enums.BotMessage;
import uz.pdp.online_education.telegram.service.UrlBuilderService;
import uz.pdp.online_education.telegram.service.message.MessageService;
import uz.pdp.online_education.telegram.service.student.template.StudentInlineKeyboardService;

import java.util.ArrayList;
import java.util.List;

import static uz.pdp.online_education.telegram.Utils.CallbackData.*;

@Service
@RequiredArgsConstructor
public class StudentInlineKeyboardServiceImpl implements StudentInlineKeyboardService {

    private final LessonRepository lessonRepository;
    private final MessageService messageService;
    private final UrlBuilderService urlBuilderService;

    /**
     * Creates the keyboard for the dashboard message, including a "Logout" button.
     */
    @Override
    public InlineKeyboardMarkup dashboardMenu() {
        // Static import tufayli kod qisqaroq va o'qish uchun osonroq
        // Tizimdan chiqish tugmasini yasash uchun yordamchi metodni chaqiramiz.
        return createSingleButtonKeyboard(
                Utils.InlineButtons.LOGOUT_TEXT,
                String.join(":", AUTH_PREFIX, ACTION_LOGOUT, ACTION_INIT)
        );
    }

    /**
     * Creates a confirmation keyboard for the logout action.
     */
    @Override
    public InlineKeyboardMarkup logoutConfirmation() {
        InlineKeyboardButton yesButton = createButton(
                Utils.InlineButtons.LOGOUT_CONFIRM_YES_TEXT,
                String.join(":", AUTH_PREFIX, ACTION_LOGOUT, ACTION_CONFIRM)
        );

        InlineKeyboardButton noButton = createButton(
                Utils.InlineButtons.LOGOUT_CONFIRM_NO_TEXT,
                String.join(":", AUTH_PREFIX, ACTION_LOGOUT, ACTION_CANCEL)
        );

        return new InlineKeyboardMarkup(List.of(List.of(yesButton, noButton)));
    }

    /**
     * Builds a paginated menu for the student's enrolled courses.
     */
    @Override
    @Transactional(readOnly = true)
    public InlineKeyboardMarkup myCoursesMenu(Page<Course> coursePage) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        coursePage.getContent().forEach(course -> {
            String buttonText = "🎓 " + course.getTitle();
            String callbackData = String.join(":",
                    MY_COURSE_PREFIX, ACTION_VIEW, course.getId().toString());
            keyboard.add(List.of(createButton(buttonText, callbackData)));
        });

        addPaginationButtons(keyboard, coursePage, MY_COURSE_PREFIX);

        // 3. "Bosh menyuga qaytish" tugmasini qo'shamiz.
        String backCallback = String.join(":",
                STUDENT_PREFIX, ACTION_BACK, BACK_TO_MAIN_MENU);
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

        modulePage.getContent().forEach(module -> {
            String buttonText;
            String callbackData;

            if (isEnrolledToFullCourse || enrolledModuleIds.contains(module.getId())) {
                buttonText = "✅ " + module.getTitle(); // To'liq kursga yoki shu modulga a'zo
                callbackData = String.join(":", MODULE_PREFIX, ACTION_VIEW, module.getId().toString());
            } else if (lessonRepository.existsByModuleAndIsFreeTrue(module)) {
                buttonText = "🆓 " + module.getTitle(); // Modulda bepul dars bor
                callbackData = String.join(":", MODULE_PREFIX, ACTION_VIEW, module.getId().toString());
            } else {
                buttonText = "🔒 " + module.getTitle(); // Yopiq modul
                callbackData = String.join(":", MODULE_PREFIX, ACTION_BUY, module.getId().toString());
            }
            keyboard.add(List.of(createButton(buttonText, callbackData)));
        });

        addPaginationButtons(keyboard, modulePage, MODULE_PREFIX + ":" + courseId);
        String backCallback = String.join(":", MY_COURSE_PREFIX, ACTION_LIST, ACTION_PAGE, "0");
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

        lessonPage.getContent().forEach(lesson -> {
            String buttonText;
            String callbackData;

            if (isModuleEnrolled || lesson.isFree()) {
                buttonText = "📖 " + lesson.getTitle();
                callbackData = String.join(":",
                        LESSON_PREFIX, ACTION_VIEW, lesson.getId().toString());
            } else {
                buttonText = "🔒 " + lesson.getTitle();
                callbackData = String.join(":",
                        LESSON_PREFIX, ACTION_BUY, moduleId.toString());
            }
            keyboard.add(List.of(createButton(buttonText, callbackData)));
        });

        String paginationBaseCallback = String.join(":", LESSON_PREFIX, moduleId.toString());
        addPaginationButtons(keyboard, lessonPage, paginationBaseCallback);
        String backCallback = String.join(":", MY_COURSE_PREFIX, ACTION_VIEW, courseId.toString());
        keyboard.add(List.of(createButton("⬅️ Modullar ro'yxatiga", backCallback)));

        return new InlineKeyboardMarkup(keyboard);
    }

    /**
     * Creates a menu that lists the content blocks of a specific lesson.
     */
    @Override
    @Transactional(readOnly = true)
    public InlineKeyboardMarkup lessonContentsMenu(Lesson lesson, Long moduleId, String backCallback) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

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
            String callbackData = String.join(":", CONTENT_PREFIX, ACTION_VIEW, content.getId().toString());
            keyboard.add(List.of(createButton(buttonText, callbackData)));
        });

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
        InlineKeyboardButton button = new InlineKeyboardButton(text);
        button.setUrl(url);
        return new InlineKeyboardMarkup(List.of(List.of(button)));
    }

    /**
     * @return
     */
    @Override
    public InlineKeyboardMarkup selectCategoryAndInstructor() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        InlineKeyboardButton buttonInstructor = new InlineKeyboardButton();
        buttonInstructor.setCallbackData(String.join(":", ALL_COURSES_PREFIX, INSTRUCTOR, ACTION_PAGE, "0"));
        buttonInstructor.setText(Utils.NumberEmojis.ONE);
        buttons.add(buttonInstructor);

        InlineKeyboardButton buttonCategory = new InlineKeyboardButton();
        buttonCategory.setCallbackData(String.join(":", ALL_COURSES_PREFIX, CATEGORY, ACTION_PAGE, "0"));
        buttonCategory.setText(Utils.NumberEmojis.TWO);
        buttons.add(buttonCategory);

        keyboard.add(buttons);

        List<InlineKeyboardButton> backButtons = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton = createButton(Utils.InlineButtons.BACK_TO_MAIN_MENU_TEXT, String.join(":", STUDENT_PREFIX, ACTION_BACK, BACK_TO_MAIN_MENU));
        backButtons.add(inlineKeyboardButton);
        keyboard.add(backButtons);

        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }


    /**
     * Creates a menu for browsing categories using buttons that contain ONLY numbers.
     * The layout is a wide grid (5 buttons per row).
     */
    @Override
    public InlineKeyboardMarkup allCourses_categoriesMenu(Page<CategoryInfo> categoryPage) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        int pageSize = categoryPage.getSize();
        int currentPageNumber = categoryPage.getNumber();
        int startingNumber = currentPageNumber * pageSize + 1;

        final int buttonsPerRow = 5;
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        int itemIndex = 0;

        for (CategoryInfo categoryInfo : categoryPage.getContent()) {
            int currentItemNumber = startingNumber + itemIndex;
            String buttonText = Utils.Numbering.toEmoji(currentItemNumber);

            String callbackData = String.join(":",
                    ALL_COURSES_PREFIX,
                    ACTION_LIST,
                    CATEGORY,
                    categoryInfo.getId().toString(),
                    ACTION_PAGE,
                    "0"
            );

            currentRow.add(createButton(buttonText, callbackData));
            itemIndex++;

            if (currentRow.size() == buttonsPerRow) {
                keyboard.add(currentRow);
                currentRow = new ArrayList<>(); // va yangi qator ochamiz
            }
        }

        if (!currentRow.isEmpty()) {
            keyboard.add(currentRow);
        }
        String paginationBaseCallback = String.join(":",
                ALL_COURSES_PREFIX,
                ACTION_LIST,
                CATEGORY
        );
        List<InlineKeyboardButton> paginationRow = createPaginationRow(categoryPage, paginationBaseCallback);
        if (!paginationRow.isEmpty()) {
            keyboard.add(paginationRow);
        }

        String backCallback = String.join(":", ALL_COURSES_PREFIX, ACTION_BACK, BACK_TO_MAIN_MENU);
        keyboard.add(List.of(createButton("⬅️ Orqaga", backCallback)));

        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    /**
     * Creates a universal pagination row for any paginated data.
     * This helper method is designed to be reusable across different menus.
     *
     * @param page         The Page object containing pagination info (e.g., hasNext, getNumber).
     *                     Using Page<?> makes it generic for any type of content.
     * @param baseCallback The base string for the callback, which defines the context.
     *                     For example: "allc:l:cat" or "myc:l".
     * @return A list of pagination buttons (a row), or an empty list if not needed.
     */
    private List<InlineKeyboardButton> createPaginationRow(Page<?> page, String baseCallback) {
        List<InlineKeyboardButton> paginationRow = new ArrayList<>();

        if (page.getTotalPages() <= 1) {
            return paginationRow;
        }

        int currentPage = page.getNumber(); // Joriy sahifa raqami (0 dan boshlanadi)

        if (page.hasPrevious()) {
            String prevCallback = String.join(":",
                    baseCallback,
                    ACTION_PAGE,
                    String.valueOf(currentPage - 1)
            );
            paginationRow.add(createButton(Utils.InlineButtons.PAGINATION_PREVIOUS_TEXT, prevCallback));
        }

        String pageIndicator = String.format("%d / %d", currentPage + 1, page.getTotalPages());
        paginationRow.add(createButton(pageIndicator, "do_nothing")); // Bosilganda hech nima qilmaydi

        if (page.hasNext()) {
            String nextCallback = String.join(":",
                    baseCallback,
                    ACTION_PAGE,
                    String.valueOf(currentPage + 1)
            );
            paginationRow.add(createButton(Utils.InlineButtons.PAGINATION_NEXT_TEXT, nextCallback));
        }
        return paginationRow;
    }

    /**
     * @param instructorPage
     * @return
     */
    @Override
    public InlineKeyboardMarkup allCourses_instructorsMenu(Page<UserInfo> instructorPage) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        int pageSize = instructorPage.getSize();
        int currentPageNumber = instructorPage.getNumber();
        int startingNumber = currentPageNumber * pageSize + 1;

        final int buttonsPerRow = 5;
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        int itemIndex = 0;

        for (UserInfo userInfo : instructorPage.getContent()) {
            int currentItemNumber = startingNumber + itemIndex;
            String buttonText = Utils.Numbering.toEmoji(currentItemNumber);

            String callbackData = String.join(":",
                    ALL_COURSES_PREFIX,
                    ACTION_LIST,
                    INSTRUCTOR,
                    userInfo.getId().toString(),
                    ACTION_PAGE,
                    "0"
            );

            currentRow.add(createButton(buttonText, callbackData));
            itemIndex++;

            if (currentRow.size() == buttonsPerRow) {
                keyboard.add(currentRow);
                currentRow = new ArrayList<>();
            }
        }

        if (!currentRow.isEmpty()) {
            keyboard.add(currentRow);
        }
        String paginationBaseCallback = String.join(":",
                ALL_COURSES_PREFIX,
                ACTION_LIST,
                INSTRUCTOR
        );
        List<InlineKeyboardButton> paginationRow = createPaginationRow(instructorPage, paginationBaseCallback);
        if (!paginationRow.isEmpty()) {
            keyboard.add(paginationRow);
        }

        String backCallback = String.join(":", ALL_COURSES_PREFIX, ACTION_BACK, BACK_TO_MAIN_MENU);
        keyboard.add(List.of(createButton("⬅️ Orqaga", backCallback)));

        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    /**
     * @param categoryPageDTO
     * @param backButton
     * @param type
     * @param id
     * @return
     */
    @Override
    public InlineKeyboardMarkup allCoursesMenu(PageDTO<CourseDetailDTO> categoryPageDTO, String backButton, String type, Long id) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        final int buttonsPerRow = 5;
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        int itemIndex = 1;

        for (CourseDetailDTO courseDetailDTO : categoryPageDTO.getContent()) {
            String buttonText = Utils.Numbering.toEmoji(itemIndex);

            String callbackData = String.join(":",
                    ALL_COURSES_PREFIX,
                    MODULE_PREFIX,
                    type + "." + id.toString(),
                    courseDetailDTO.getId().toString(),
                    ACTION_PAGE,
                    "0"
            );

            currentRow.add(createButton(buttonText, callbackData));
            itemIndex++;

            if (currentRow.size() == buttonsPerRow) {
                keyboard.add(currentRow);
                currentRow = new ArrayList<>();
            }
        }

        if (!currentRow.isEmpty()) {
            keyboard.add(currentRow);
        }
        String paginationBaseCallback = String.join(":",
                ALL_COURSES_PREFIX,
                ACTION_LIST,
                type,
                id.toString()
        );
        List<InlineKeyboardButton> paginationRow = createPaginationRow(categoryPageDTO, paginationBaseCallback);
        if (!paginationRow.isEmpty()) {
            keyboard.add(paginationRow);
        }

        keyboard.add(List.of(createButton("⬅️ Orqaga", backButton)));
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    /**
     * @param modulePageDTO
     * @param backButton
     * @param id
     * @param datum
     * @return
     */
    @Override
    public InlineKeyboardMarkup allCourseModules(PageDTO<ModuleDetailDTO> modulePageDTO, String backButton, Long id, String datum) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        int pageSize = modulePageDTO.getPageSize();
        int currentPageNumber = modulePageDTO.getPageNumber();
        int startingNumber = currentPageNumber * pageSize + 1;

        final int buttonsPerRow = 5;
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        int itemIndex = 1;

        for (ModuleDetailDTO courseDetailDTO : modulePageDTO.getContent()) {
            int currentItemNumber = startingNumber + itemIndex;
            String buttonText = Utils.Numbering.toEmoji(itemIndex);

            String callbackData = String.join(":",
                    ALL_COURSES_PREFIX,
                    LESSON_PREFIX,
                    datum,
                    courseDetailDTO.getId().toString(),
                    ACTION_PAGE,
                    "0"
            );

            currentRow.add(createButton(buttonText, callbackData));
            itemIndex++;

            if (currentRow.size() == buttonsPerRow) {
                keyboard.add(currentRow);
                currentRow = new ArrayList<>();
            }
        }

        if (!currentRow.isEmpty()) {
            keyboard.add(currentRow);
        }
        String paginationBaseCallback = String.join(":",
                ALL_COURSES_PREFIX,
                MODULE_PREFIX,
                ACTION_VIEW,
                id.toString()

        );
        List<InlineKeyboardButton> paginationRow = createPaginationRow(modulePageDTO, paginationBaseCallback);
        if (!paginationRow.isEmpty()) {
            keyboard.add(paginationRow);
        }

        keyboard.add(List.of(createButton("⬅️ Orqaga", backButton)));

        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    /**
     * @param lessonResponseDTOPageDTO
     * @param backButton
     * @param id
     * @param datum
     * @param purchased
     * @param hasSubscription
     * @return
     */
    @Override
    public InlineKeyboardMarkup allCourseLessons(PageDTO<LessonResponseDTO> lessonResponseDTOPageDTO,
                                                 String backButton, Long id, String datum,
                                                 boolean purchased,
                                                 boolean hasSubscription
    ) {


        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> button = new ArrayList<>();
        if (!hasSubscription) {
            InlineKeyboardButton subscription = createButton("Obuna bo`lish", String.join(":",
                    ALL_COURSES_PREFIX,
                    ACTION_SUBSCRIPTION,
                    ACTION_VIEW,
                    datum,
                    id.toString()
            ));
            button.add(subscription);
        }
        if (!purchased) {
            InlineKeyboardButton buy = createButton("Sotib olish",
                    String.join(":",
                            ALL_COURSES_PREFIX,
                            ACTION_BUY,
                            ACTION_VIEW,
                            datum,
                            id.toString()
                    ));
            button.add(buy);
        }
        keyboard.add(button);


        final int buttonsPerRow = 5;
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        int itemIndex = 1;

        for (LessonResponseDTO lessonResponseDTO : lessonResponseDTOPageDTO.getContent()) {
            String buttonText = Utils.Numbering.toEmoji(itemIndex);

            String callbackData = String.join(":",
                    ALL_COURSES_PREFIX,
                    CONTENT_PREFIX,
                    datum,
                    lessonResponseDTO.getId().toString(),
                    ACTION_PAGE,
                    "0"
            );

            currentRow.add(createButton(buttonText, callbackData));
            itemIndex++;

            if (currentRow.size() == buttonsPerRow) {
                keyboard.add(currentRow);
                currentRow = new ArrayList<>();
            }
        }

        if (!currentRow.isEmpty()) {
            keyboard.add(currentRow);
        }
        String paginationBaseCallback = String.join(":",
                ALL_COURSES_PREFIX,
                MODULE_PREFIX,
                ACTION_VIEW,
                id.toString()

        );
        List<InlineKeyboardButton> paginationRow = createPaginationRow(lessonResponseDTOPageDTO, paginationBaseCallback);
        if (!paginationRow.isEmpty()) {
            keyboard.add(paginationRow);
        }

        keyboard.add(List.of(createButton("⬅️ Orqaga", backButton)));

        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    /**
     * @param moduleId
     * @param datum
     * @return
     */
    @Override
    public InlineKeyboardMarkup buildYesNoKeyboard(Long moduleId, String datum) {
        InlineKeyboardButton yesBtn = InlineKeyboardButton.builder()
                .text("✅ Ha")
                .callbackData(String.join(":",
                        ALL_COURSES_PREFIX,
                        ACTION_SUBSCRIPTION,
                        ACTION_CONFIRM,
                        datum,
                        moduleId.toString()
                ))
                .build();

        InlineKeyboardButton noBtn = InlineKeyboardButton.builder()
                .text("❌ Yo‘q")
                .callbackData(String.join(":",
                        ALL_COURSES_PREFIX,
                        ACTION_SUBSCRIPTION,
                        ACTION_CANCEL,
                        datum,
                        moduleId.toString()
                ))
                .build();

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(List.of(yesBtn, noBtn)))
                .build();
    }

    /**
     * @param moduleId
     * @param datum
     * @return
     */
    @Override
    public InlineKeyboardMarkup buildPurchaseButton(Long moduleId, String datum) {

        InlineKeyboardButton buyBtn = InlineKeyboardButton.builder()
                .text("💳 Sotib olish")
                .url(urlBuilderService.generateModuleCheckoutUrl(moduleId))
                .build();

        InlineKeyboardButton cancelBtn = InlineKeyboardButton.builder()
                .text("❌ Bekor qilish")
                .callbackData(
                        String.join(":",
                                ALL_COURSES_PREFIX,
                                ACTION_BUY,
                                ACTION_CANCEL,
                                datum,
                                moduleId.toString()
                        )
                )
                .build();

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(List.of(buyBtn), List.of(cancelBtn)))
                .build();
    }

    /**
     * @param s
     * @param url
     * @return
     */
    @Override
    public InlineKeyboardMarkup createQuizContent(String s, String url) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> inlineKeyboardButtons = new ArrayList<>();

        InlineKeyboardButton urlButton = new InlineKeyboardButton();
        urlButton.setText(s);
        urlButton.setUrl(url);
        inlineKeyboardButtons.add(urlButton);

        InlineKeyboardButton deletedBtn = new InlineKeyboardButton();
        deletedBtn.setText("❌ o`chirish");
        deletedBtn.setCallbackData(DELETED);
        inlineKeyboardButtons.add(deletedBtn);

        buttons.add(inlineKeyboardButtons);
        inlineKeyboardMarkup.setKeyboard(buttons);
        return inlineKeyboardMarkup;
    }

    /**
     * "Balans/To'lovlar" bo'limi uchun klaviaturani (tugmalarni) yaratadi.
     *
     * @param hasPendingPayments Foydalanuvchida to'lanmagan modullar bor-yo'qligi.
     * @param pendingCount       To'lanmagan modullar soni.
     * @return Tayyor InlineKeyboardMarkup obyekti.
     */
    @Override
    public InlineKeyboardMarkup createBalanceMenuKeyboard(boolean hasPendingPayments, int pendingCount) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> firstRow = new ArrayList<>();

        // 1-qatorni yig'amiz
        if (hasPendingPayments) {
            String buttonText = messageService.getMessage(BotMessage.BALANCE_BUTTON_PENDING, pendingCount);
            firstRow.add(createButton(buttonText, String.join(":", BALANCED, BALANCE_PENDING_PAYMENTS, ACTION_PAGE, "0")));
        }

        // "To'lovlar tarixi" tugmasini har doim qo'shamiz
        String historyButtonText = messageService.getMessage(BotMessage.BALANCE_BUTTON_HISTORY);
        firstRow.add(createButton(historyButtonText, String.join(":", BALANCED, BALANCE_PAYMENT_HISTORY, ACTION_PAGE, "0")));

        rows.add(firstRow);

        // 2-qator (Orqaga tugmasi)
        List<InlineKeyboardButton> secondRow = new ArrayList<>();
        String backButtonText = messageService.getMessage(BotMessage.BALANCE_BUTTON_BACK);
        secondRow.add(createButton(backButtonText, String.join(":", STUDENT_PREFIX, ACTION_BACK, BACK_TO_MAIN_MENU)));

        rows.add(secondRow);

        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }

    /**
     * @param payments
     * @return
     */
    @Override
    public InlineKeyboardMarkup userPaymentsHistory(Page<Payment> payments) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        String paginationBaseCallback = String.join(":",
                BALANCED,
                BALANCE_PAYMENT_HISTORY
        );

        List<InlineKeyboardButton> paginationRow = createPaginationRow(payments, paginationBaseCallback);
        if (!paginationRow.isEmpty()) {
            keyboard.add(paginationRow);
        }

        // Orqaga tugmasi
        keyboard.add(List.of(
                createButton("⬅️ Orqaga", String.join(":", BALANCED, ACTION_BACK))
        ));

        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup userPendingPaymentsKeyboard(Page<Module> modules) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        int index = 1;

        for (Module module : modules.getContent()) {
            String buttonText = String.valueOf(index++);

            String callbackData = String.join(":",
                    BALANCED,
                    ACTION_VIEW,
                    module.getId().toString());

            row.add(createButton(buttonText, callbackData));
        }

        // Barcha tugmalar bitta qatorda
        if (!row.isEmpty()) {
            keyboard.add(row);
        }

        String paginationBaseCallback = String.join(":",
                BALANCED,
                BALANCE_PENDING_PAYMENTS
        );

        List<InlineKeyboardButton> paginationRow = createPaginationRow(modules, paginationBaseCallback);
        if (!paginationRow.isEmpty()) {
            keyboard.add(paginationRow);
        }

        keyboard.add(List.of(
                createButton("⬅️ Orqaga", String.join(":", BALANCED, ACTION_BACK))
        ));

        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    @Override
    public InlineKeyboardMarkup buildModuleButtons(Module module) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // ✅ Sotib olish tugmasi (URL tugma)
        InlineKeyboardButton buyButton = new InlineKeyboardButton("✅ Sotib olish");
        buyButton.setUrl(urlBuilderService.generateModuleCheckoutUrl(module.getId()));
        rows.add(List.of(buyButton));

        // ⬅️ Orqaga tugmasi
        rows.add(List.of(
                createButton("⬅️ Orqaga", String.join(":",
                        BALANCED,
                        BALANCE_PENDING_PAYMENTS,
                        ACTION_PAGE,
                        "0"
                ))
        ));

        return new InlineKeyboardMarkup(rows);
    }

    /**
     * @param faqs
     * @param backButton
     * @return
     */
    @Override
    public InlineKeyboardMarkup studentSupportMessage(Page<Faq> faqs, String backButton, int pageNumber) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        int index = 1;

        for (Faq faq : faqs.getContent()) {
            String buttonText = String.valueOf(index++);

            String callbackData = String.join(":",
                    ACTION_SUPPORT,
                    ACTION_VIEW,
                    faq.getId().toString(),
                    String.valueOf(pageNumber));

            row.add(createButton(buttonText, callbackData));
        }
        if (!row.isEmpty()) {
            keyboard.add(row);
        }

        String paginationBaseCallback = String.join(":",
                ACTION_SUPPORT
        );

        List<InlineKeyboardButton> paginationRow = createPaginationRow(faqs, paginationBaseCallback);
        if (!paginationRow.isEmpty()) {
            keyboard.add(paginationRow);
        }

        keyboard.add(List.of(
                createButton("⬅️ Orqaga", backButton)
        ));

        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;

    }

    // --- PRIVATE HELPER METHODS ---

    private List<InlineKeyboardButton> createPaginationRow(PageDTO<?> page, String baseCallback) {
        List<InlineKeyboardButton> paginationRow = new ArrayList<>();

        if (page.getTotalPages() <= 1) {
            return paginationRow;
        }

        int currentPage = page.getPageNumber();

        if (!page.isFirst()) {
            String prevCallback = String.join(":",
                    baseCallback,
                    ACTION_PAGE,
                    String.valueOf(currentPage - 1)
            );
            paginationRow.add(createButton(Utils.InlineButtons.PAGINATION_PREVIOUS_TEXT, prevCallback));
        }

        String pageIndicator = String.format("%d / %d", currentPage + 1, page.getTotalPages());
        paginationRow.add(createButton(pageIndicator, "do_nothing")); // Bosilganda hech nima qilmaydi

        if (!page.isLast()) {
            String nextCallback = String.join(":",
                    baseCallback,
                    ACTION_PAGE,
                    String.valueOf(currentPage + 1)
            );
            paginationRow.add(createButton(Utils.InlineButtons.PAGINATION_NEXT_TEXT, nextCallback));
        }

        return paginationRow;
    }

    /**
     * A private helper method to add pagination buttons (Next, Previous) to a keyboard.
     */
    private void addPaginationButtons(List<List<InlineKeyboardButton>> keyboard, Page<?> page, String baseCallback) {
        if (page.getTotalPages() > 1) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            int currentPage = page.getNumber();

            if (page.hasPrevious()) {
                String prevCallback = String.join(":",
                        baseCallback, ACTION_LIST, ACTION_PAGE, String.valueOf(currentPage - 1));
                row.add(createButton("⬅️ Oldingi", prevCallback));
            }

            row.add(createButton(String.format("%d / %d", currentPage + 1, page.getTotalPages()), "do_nothing"));

            if (page.hasNext()) {
                String nextCallback = String.join(":",
                        baseCallback, ACTION_LIST, ACTION_PAGE, String.valueOf(currentPage + 1));
                row.add(createButton("Keyingi ➡️", nextCallback));
            }
            keyboard.add(row);
        }
    }

    /**
     * A private helper method to create a single InlineKeyboardButton.
     */
    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton(text);
        button.setCallbackData(callbackData);
        return button;
    }
}
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
    public InlineKeyboardMarkup lessonContentsMenu(Lesson lesson, Long moduleId, String backCallback) {
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

    /**
     * @return
     */
    @Override
    public InlineKeyboardMarkup selectCategoryAndInstructor() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        InlineKeyboardButton buttonInstructor = new InlineKeyboardButton();
        buttonInstructor.setCallbackData(String.join(":", Utils.CallbackData.ALL_COURSES_PREFIX, Utils.CallbackData.INSTRUCTOR, Utils.CallbackData.ACTION_PAGE, "0"));
        buttonInstructor.setText(Utils.NumberEmojis.ONE);
        buttons.add(buttonInstructor);

        InlineKeyboardButton buttonCategory = new InlineKeyboardButton();
        buttonCategory.setCallbackData(String.join(":", Utils.CallbackData.ALL_COURSES_PREFIX, Utils.CallbackData.CATEGORY, Utils.CallbackData.ACTION_PAGE, "0"));
        buttonCategory.setText(Utils.NumberEmojis.TWO);
        buttons.add(buttonCategory);

        keyboard.add(buttons);

        List<InlineKeyboardButton> backButtons = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton = createButton(Utils.InlineButtons.BACK_TO_MAIN_MENU_TEXT, String.join(":", Utils.CallbackData.STUDENT_PREFIX, Utils.CallbackData.ACTION_BACK, Utils.CallbackData.BACK_TO_MAIN_MENU));
        backButtons.add(inlineKeyboardButton);
        keyboard.add(backButtons);

        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }


    // StudentInlineKeyboardServiceImpl.java ichiga qo'shing yoki mavjudini almashtiring

    /**
     * Creates a menu for browsing categories using buttons that contain ONLY numbers.
     * The layout is a wide grid (5 buttons per row).
     */
    @Override
    public InlineKeyboardMarkup allCourses_categoriesMenu(Page<CategoryInfo> categoryPage) {
        // 1. Asosiy klaviatura va tugmalar qatorlari uchun ro'yxatlarni yaratamiz
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // 2. Sahifaga mos ravishda boshlang'ich raqamni hisoblaymiz
        int pageSize = categoryPage.getSize();
        int currentPageNumber = categoryPage.getNumber();
        int startingNumber = currentPageNumber * pageSize + 1;

        // 3. Tugmalarni panjara shaklida joylash uchun sozlamalar
        final int buttonsPerRow = 5;
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        int itemIndex = 0;

        // 4. Har bir kategoriya uchun FAQAAT RAQAMDAN iborat tugma yasaymiz
        for (CategoryInfo categoryInfo : categoryPage.getContent()) {
            int currentItemNumber = startingNumber + itemIndex;
            String buttonText = Utils.Numbering.toEmoji(currentItemNumber);

            String callbackData = String.join(":",
                    Utils.CallbackData.ALL_COURSES_PREFIX,    // Oqim: Barcha kurslar
                    Utils.CallbackData.ACTION_LIST,          // Amal: Ro'yxatlash
                    Utils.CallbackData.CATEGORY,     // Obyekt: Kategoriya bo'yicha
                    categoryInfo.getId().toString(),         // Qiymat: Kategoriya ID'si
                    Utils.CallbackData.ACTION_PAGE,           // Parametr: Sahifa
                    "0"                                      // Qiymat: 0-sahifa
            );

            // Tugmani yaratib, joriy qatorga qo'shamiz
            currentRow.add(createButton(buttonText, callbackData));
            itemIndex++;

            // Agar joriy qator to'lsa (5 ta tugma bo'lsa), uni klaviaturaga qo'shamiz
            if (currentRow.size() == buttonsPerRow) {
                keyboard.add(currentRow);
                currentRow = new ArrayList<>(); // va yangi qator ochamiz
            }
        }

        // 5. Sikl tugagandan so'ng, oxirgi qator to'liq bo'lmasa ham uni qo'shib qo'yamiz
        if (!currentRow.isEmpty()) {
            keyboard.add(currentRow);
        }
        String paginationBaseCallback = String.join(":",
                Utils.CallbackData.ALL_COURSES_PREFIX, // "allc"
                Utils.CallbackData.ACTION_LIST,      // "l"
                Utils.CallbackData.CATEGORY  // "cat"
        );
        // 6. Sahifalash (pagination) va "Orqaga" tugmalarini qo'shamiz
        List<InlineKeyboardButton> paginationRow = createPaginationRow(categoryPage, paginationBaseCallback);
        if (!paginationRow.isEmpty()) {
            keyboard.add(paginationRow);
        }

        String backCallback = String.join(":", Utils.CallbackData.ALL_COURSES_PREFIX, Utils.CallbackData.ACTION_BACK, Utils.CallbackData.BACK_TO_MAIN_MENU);
        keyboard.add(List.of(createButton("⬅️ Orqaga", backCallback)));

        // 7. Tayyor klaviaturani qaytaramiz
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    // `createPaginationRow` metodi o'zgarishsiz qoladi.
    // StudentInlineKeyboardServiceImpl.java ichida mavjud metodni shu bilan almashtiring

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

        // 1. Agar sahifalar soni 1 ta yoki undan kam bo'lsa, sahifalash kerak emas.
        if (page.getTotalPages() <= 1) {
            return paginationRow;
        }

        int currentPage = page.getNumber(); // Joriy sahifa raqami (0 dan boshlanadi)

        // 2. "Oldingi" sahifa tugmasini yasaymiz (agar oldingi sahifa mavjud bo'lsa)
        if (page.hasPrevious()) {
            // Callback'ni yig'amiz: {baseCallback}:p:{sahifa_raqami}
            // Masalan: "allc:l:cat:p:0"
            String prevCallback = String.join(":",
                    baseCallback,
                    Utils.CallbackData.ACTION_PAGE,
                    String.valueOf(currentPage - 1)
            );
            paginationRow.add(createButton(Utils.InlineButtons.PAGINATION_PREVIOUS_TEXT, prevCallback));
        }

        // 3. Joriy sahifa ko'rsatkichini qo'shamiz (masalan, "1 / 5")
        String pageIndicator = String.format("%d / %d", currentPage + 1, page.getTotalPages());
        paginationRow.add(createButton(pageIndicator, "do_nothing")); // Bosilganda hech nima qilmaydi

        // 4. "Keyingi" sahifa tugmasini yasaymiz (agar keyingi sahifa mavjud bo'lsa)
        if (page.hasNext()) {
            // Callback'ni yig'amiz: {baseCallback}:p:{sahifa_raqami}
            // Masalan: "allc:l:cat:p:2"
            String nextCallback = String.join(":",
                    baseCallback,
                    Utils.CallbackData.ACTION_PAGE,
                    String.valueOf(currentPage + 1)
            );
            paginationRow.add(createButton(Utils.InlineButtons.PAGINATION_NEXT_TEXT, nextCallback));
        }

        // 5. Tayyor bo'lgan qatorni qaytaramiz
        return paginationRow;
    }

    /**
     * @param instructorPage
     * @return
     */
    @Override
    public InlineKeyboardMarkup allCourses_instructorsMenu(Page<UserInfo> instructorPage) {
        // 1. Asosiy klaviatura va tugmalar qatorlari uchun ro'yxatlarni yaratamiz
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // 2. Sahifaga mos ravishda boshlang'ich raqamni hisoblaymiz
        int pageSize = instructorPage.getSize();
        int currentPageNumber = instructorPage.getNumber();
        int startingNumber = currentPageNumber * pageSize + 1;

        // 3. Tugmalarni panjara shaklida joylash uchun sozlamalar
        final int buttonsPerRow = 5;
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        int itemIndex = 0;

        // 4. Har bir kategoriya uchun FAQAAT RAQAMDAN iborat tugma yasaymiz
        for (UserInfo userInfo : instructorPage.getContent()) {
            int currentItemNumber = startingNumber + itemIndex;
            String buttonText = Utils.Numbering.toEmoji(currentItemNumber);

            String callbackData = String.join(":",
                    Utils.CallbackData.ALL_COURSES_PREFIX,    // Oqim: Barcha kurslar
                    Utils.CallbackData.ACTION_LIST,          // Amal: Ro'yxatlash
                    Utils.CallbackData.INSTRUCTOR,     // Obyekt: Instructor bo'yicha
                    userInfo.getId().toString(),         // Qiymat: Kategoriya ID'si
                    Utils.CallbackData.ACTION_PAGE,           // Parametr: Sahifa
                    "0"                                      // Qiymat: 0-sahifa
            );

            // Tugmani yaratib, joriy qatorga qo'shamiz
            currentRow.add(createButton(buttonText, callbackData));
            itemIndex++;

            // Agar joriy qator to'lsa (5 ta tugma bo'lsa), uni klaviaturaga qo'shamiz
            if (currentRow.size() == buttonsPerRow) {
                keyboard.add(currentRow);
                currentRow = new ArrayList<>(); // va yangi qator ochamiz
            }
        }

        // 5. Sikl tugagandan so'ng, oxirgi qator to'liq bo'lmasa ham uni qo'shib qo'yamiz
        if (!currentRow.isEmpty()) {
            keyboard.add(currentRow);
        }
        String paginationBaseCallback = String.join(":",
                Utils.CallbackData.ALL_COURSES_PREFIX, // "allc"
                Utils.CallbackData.ACTION_LIST,      // "l"
                Utils.CallbackData.INSTRUCTOR  // "cat"
        );
        // 6. Sahifalash (pagination) va "Orqaga" tugmalarini qo'shamiz
        List<InlineKeyboardButton> paginationRow = createPaginationRow(instructorPage, paginationBaseCallback);
        if (!paginationRow.isEmpty()) {
            keyboard.add(paginationRow);
        }

        String backCallback = String.join(":", Utils.CallbackData.ALL_COURSES_PREFIX, Utils.CallbackData.ACTION_BACK, Utils.CallbackData.BACK_TO_MAIN_MENU);
        keyboard.add(List.of(createButton("⬅️ Orqaga", backCallback)));

        // 7. Tayyor klaviaturani qaytaramiz
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
        // 1. Asosiy klaviatura va tugmalar qatorlari uchun ro'yxatlarni yaratamiz
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // 2. Sahifaga mos ravishda boshlang'ich raqamni hisoblaymiz
        int pageSize = categoryPageDTO.getPageSize();
        int currentPageNumber = categoryPageDTO.getPageNumber();
        int startingNumber = currentPageNumber * pageSize + 1;

        // 3. Tugmalarni panjara shaklida joylash uchun sozlamalar
        final int buttonsPerRow = 5;
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        int itemIndex = 1;

        // 4. Har bir kategoriya uchun FAQAAT RAQAMDAN iborat tugma yasaymiz
        for (CourseDetailDTO courseDetailDTO : categoryPageDTO.getContent()) {
            int currentItemNumber = startingNumber + itemIndex;
            String buttonText = Utils.Numbering.toEmoji(itemIndex);

            String callbackData = String.join(":",
                    Utils.CallbackData.ALL_COURSES_PREFIX,    // Oqim: Barcha kurslar
                    Utils.CallbackData.MODULE_PREFIX,          // Amal: Ro'yxatlash
                    type + "." + id.toString(),
                    courseDetailDTO.getId().toString(),         // Qiymat: Kategoriya ID'si
                    Utils.CallbackData.ACTION_PAGE,           // Parametr: Sahifa
                    "0"                                      // Qiymat: 0-sahifa
            );

            // Tugmani yaratib, joriy qatorga qo'shamiz
            currentRow.add(createButton(buttonText, callbackData));
            itemIndex++;

            // Agar joriy qator to'lsa (5 ta tugma bo'lsa), uni klaviaturaga qo'shamiz
            if (currentRow.size() == buttonsPerRow) {
                keyboard.add(currentRow);
                currentRow = new ArrayList<>(); // va yangi qator ochamiz
            }
        }

        // 5. Sikl tugagandan so'ng, oxirgi qator to'liq bo'lmasa ham uni qo'shib qo'yamiz
        if (!currentRow.isEmpty()) {
            keyboard.add(currentRow);
        }
        String paginationBaseCallback = String.join(":",
                Utils.CallbackData.ALL_COURSES_PREFIX, // "allc"
                Utils.CallbackData.ACTION_LIST,        // "l"
                type,            // "cat"
                id.toString()
        );
        // 6. Sahifalash (pagination) va "Orqaga" tugmalarini qo'shamiz
        List<InlineKeyboardButton> paginationRow = createPaginationRow(categoryPageDTO, paginationBaseCallback);
        if (!paginationRow.isEmpty()) {
            keyboard.add(paginationRow);
        }

        keyboard.add(List.of(createButton("⬅️ Orqaga", backButton)));

        // 7. Tayyor klaviaturani qaytaramiz
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
        // 1. Asosiy klaviatura va tugmalar qatorlari uchun ro'yxatlarni yaratamiz
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // 2. Sahifaga mos ravishda boshlang'ich raqamni hisoblaymiz
        int pageSize = modulePageDTO.getPageSize();
        int currentPageNumber = modulePageDTO.getPageNumber();
        int startingNumber = currentPageNumber * pageSize + 1;

        // 3. Tugmalarni panjara shaklida joylash uchun sozlamalar
        final int buttonsPerRow = 5;
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        int itemIndex = 1;

        // 4. Har bir kategoriya uchun FAQAAT RAQAMDAN iborat tugma yasaymiz
        for (ModuleDetailDTO courseDetailDTO : modulePageDTO.getContent()) {
            int currentItemNumber = startingNumber + itemIndex;
            String buttonText = Utils.Numbering.toEmoji(itemIndex);

            String callbackData = String.join(":",
                    Utils.CallbackData.ALL_COURSES_PREFIX,    // Oqim: Barcha kurslar
                    Utils.CallbackData.LESSON_PREFIX,          // Amal: Ro'yxatlash
                    datum,
                    courseDetailDTO.getId().toString(),         // Qiymat: Kategoriya ID'si
                    Utils.CallbackData.ACTION_PAGE,           // Parametr: Sahifa
                    "0"                                      // Qiymat: 0-sahifa
            );

            // Tugmani yaratib, joriy qatorga qo'shamiz
            currentRow.add(createButton(buttonText, callbackData));
            itemIndex++;

            // Agar joriy qator to'lsa (5 ta tugma bo'lsa), uni klaviaturaga qo'shamiz
            if (currentRow.size() == buttonsPerRow) {
                keyboard.add(currentRow);
                currentRow = new ArrayList<>(); // va yangi qator ochamiz
            }
        }

        // 5. Sikl tugagandan so'ng, oxirgi qator to'liq bo'lmasa ham uni qo'shib qo'yamiz
        if (!currentRow.isEmpty()) {
            keyboard.add(currentRow);
        }
        String paginationBaseCallback = String.join(":",
                Utils.CallbackData.ALL_COURSES_PREFIX, // "allc"
                Utils.CallbackData.MODULE_PREFIX,      // "l"
                Utils.CallbackData.ACTION_VIEW,       // "l"
                id.toString()

        );
        // 6. Sahifalash (pagination) va "Orqaga" tugmalarini qo'shamiz
        List<InlineKeyboardButton> paginationRow = createPaginationRow(modulePageDTO, paginationBaseCallback);
        if (!paginationRow.isEmpty()) {
            keyboard.add(paginationRow);
        }

        keyboard.add(List.of(createButton("⬅️ Orqaga", backButton)));

        // 7. Tayyor klaviaturani qaytaramiz
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


        // 1. Asosiy klaviatura va tugmalar qatorlari uchun ro'yxatlarni yaratamiz
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> button = new ArrayList<>();
        if (!hasSubscription) {
            InlineKeyboardButton subscription = createButton("Obuna bo`lish", String.join(":",
                    Utils.CallbackData.ALL_COURSES_PREFIX,
                    Utils.CallbackData.ACTION_SUBSCRIPTION,
                    Utils.CallbackData.ACTION_VIEW,
                    datum,
                    id.toString()
            ));
            button.add(subscription);
        }
        if (!purchased) {
            InlineKeyboardButton buy = createButton("Sotib olish",
                    String.join(":",
                            Utils.CallbackData.ALL_COURSES_PREFIX,
                            Utils.CallbackData.ACTION_BUY,
                            Utils.CallbackData.ACTION_VIEW,
                            datum,
                            id.toString()
                    ));
            button.add(buy);
        }
        keyboard.add(button);


        // 3. Tugmalarni panjara shaklida joylash uchun sozlamalar
        final int buttonsPerRow = 5;
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        int itemIndex = 1;

        // 4. Har bir kategoriya uchun FAQAAT RAQAMDAN iborat tugma yasaymiz
        for (LessonResponseDTO lessonResponseDTO : lessonResponseDTOPageDTO.getContent()) {
            String buttonText = Utils.Numbering.toEmoji(itemIndex);

            String callbackData = String.join(":",
                    Utils.CallbackData.ALL_COURSES_PREFIX,    // Oqim: Barcha kurslar
                    Utils.CallbackData.CONTENT_PREFIX,          // Amal: Ro'yxatlash
                    datum,
                    lessonResponseDTO.getId().toString(),         // Qiymat: Kategoriya ID'si
                    Utils.CallbackData.ACTION_PAGE,           // Parametr: Sahifa
                    "0"                                      // Qiymat: 0-sahifa
            );

            // Tugmani yaratib, joriy qatorga qo'shamiz
            currentRow.add(createButton(buttonText, callbackData));
            itemIndex++;

            // Agar joriy qator to'lsa (5 ta tugma bo'lsa), uni klaviaturaga qo'shamiz
            if (currentRow.size() == buttonsPerRow) {
                keyboard.add(currentRow);
                currentRow = new ArrayList<>(); // va yangi qator ochamiz
            }
        }

        // 5. Sikl tugagandan so'ng, oxirgi qator to'liq bo'lmasa ham uni qo'shib qo'yamiz
        if (!currentRow.isEmpty()) {
            keyboard.add(currentRow);
        }
        String paginationBaseCallback = String.join(":",
                Utils.CallbackData.ALL_COURSES_PREFIX, // "allc"
                Utils.CallbackData.MODULE_PREFIX,      // "l"
                Utils.CallbackData.ACTION_VIEW,       // "l"
                id.toString()

        );
        // 6. Sahifalash (pagination) va "Orqaga" tugmalarini qo'shamiz
        List<InlineKeyboardButton> paginationRow = createPaginationRow(lessonResponseDTOPageDTO, paginationBaseCallback);
        if (!paginationRow.isEmpty()) {
            keyboard.add(paginationRow);
        }

        keyboard.add(List.of(createButton("⬅️ Orqaga", backButton)));

        // 7. Tayyor klaviaturani qaytaramiz
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
//                .callbackData("SUBSCRIBE_MODULE_" + moduleId)
                .callbackData(String.join(":",
                        Utils.CallbackData.ALL_COURSES_PREFIX,
                        Utils.CallbackData.ACTION_SUBSCRIPTION,
                        Utils.CallbackData.ACTION_CONFIRM,
                        datum,
                        moduleId.toString()
                ))
                .build();

        InlineKeyboardButton noBtn = InlineKeyboardButton.builder()
                .text("❌ Yo‘q")
                .callbackData(String.join(":",
                        Utils.CallbackData.ALL_COURSES_PREFIX,
                        Utils.CallbackData.ACTION_SUBSCRIPTION,
                        Utils.CallbackData.ACTION_CANCEL,
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
                .url(urlBuilderService.generateModuleCheckoutUrl(moduleId)) // Sizning saytingizdagi purchase link
                .build();

        InlineKeyboardButton cancelBtn = InlineKeyboardButton.builder()
                .text("❌ Bekor qilish")
                .callbackData(
                        String.join(":",
                                Utils.CallbackData.ALL_COURSES_PREFIX,
                                Utils.CallbackData.ACTION_BUY,
                                Utils.CallbackData.ACTION_CANCEL,
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
        deletedBtn.setCallbackData(Utils.CallbackData.DELETED);
        inlineKeyboardButtons.add(deletedBtn);

        buttons.add(inlineKeyboardButtons);
        inlineKeyboardMarkup.setKeyboard(buttons);
        return inlineKeyboardMarkup;
    }

    // --- PRIVATE HELPER METHODS ---

    private List<InlineKeyboardButton> createPaginationRow(PageDTO<?> page, String baseCallback) {
        List<InlineKeyboardButton> paginationRow = new ArrayList<>();

        // 1. Agar sahifalar soni 1 ta yoki undan kam bo'lsa, sahifalash kerak emas.
        if (page.getTotalPages() <= 1) {
            return paginationRow;
        }

        int currentPage = page.getPageNumber(); // Joriy sahifa raqami (0 dan boshlanadi)

        // 2. "Oldingi" sahifa tugmasini yasaymiz (agar oldingi sahifa mavjud bo'lsa)
        if (!page.isFirst()) {
            // Callback'ni yig'amiz: {baseCallback}:p:{sahifa_raqami}
            // Masalan: "allc:l:cat:p:0"
            String prevCallback = String.join(":",
                    baseCallback,
                    Utils.CallbackData.ACTION_PAGE,
                    String.valueOf(currentPage - 1)
            );
            paginationRow.add(createButton(Utils.InlineButtons.PAGINATION_PREVIOUS_TEXT, prevCallback));
        }

        // 3. Joriy sahifa ko'rsatkichini qo'shamiz (masalan, "1 / 5")
        String pageIndicator = String.format("%d / %d", currentPage + 1, page.getTotalPages());
        paginationRow.add(createButton(pageIndicator, "do_nothing")); // Bosilganda hech nima qilmaydi

        // 4. "Keyingi" sahifa tugmasini yasaymiz (agar keyingi sahifa mavjud bo'lsa)
        if (!page.isLast()) {
            // Callback'ni yig'amiz: {baseCallback}:p:{sahifa_raqami}
            // Masalan: "allc:l:cat:p:2"
            String nextCallback = String.join(":",
                    baseCallback,
                    Utils.CallbackData.ACTION_PAGE,
                    String.valueOf(currentPage + 1)
            );
            paginationRow.add(createButton(Utils.InlineButtons.PAGINATION_NEXT_TEXT, nextCallback));
        }

        // 5. Tayyor bo'lgan qatorni qaytaramiz
        return paginationRow;
    }

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
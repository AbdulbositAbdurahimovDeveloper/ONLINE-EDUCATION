package uz.pdp.online_education.telegram.service.instructor;


import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import uz.pdp.online_education.model.Category;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.payload.PageDTO;
import uz.pdp.online_education.payload.content.ContentDTO;
import uz.pdp.online_education.payload.course.CourseDetailDTO;
import uz.pdp.online_education.payload.lesson.LessonResponseDTO;
import uz.pdp.online_education.payload.module.ModuleDetailDTO;
import uz.pdp.online_education.service.interfaces.CourseService;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.service.instructor.template.InstructorInlineKeyboardService;

import java.util.ArrayList;
import java.util.List;

import static uz.pdp.online_education.telegram.Utils.CallbackData.*;

@Service
public class InstructorInlineKeyboardServiceImpl implements InstructorInlineKeyboardService {

    private final CourseService courseService;

    public InstructorInlineKeyboardServiceImpl(CourseService courseService) {
        this.courseService = courseService;
    }

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
     * Creates a simple inline keyboard with a single button.
     */
    @Override
    public InlineKeyboardMarkup createSingleButtonKeyboard(String text, String callbackData) {
        // Yangi klaviatura obyekti yaratib, unga bitta tugma joylaymiz.
        InlineKeyboardButton button = createButton(text, callbackData);
        return new InlineKeyboardMarkup(List.of(List.of(button)));
    }

    /**
     * Creates a confirmation keyboard for the logout action.
     */
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
     * @return {@return }
     */
    @Override
    public InlineKeyboardMarkup instructorNoDraftCourse() {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> addButtons = new ArrayList<>();
        InlineKeyboardButton button = createButton("⬅️ Orqaga",
                String.join(":",
                        Utils.CallbackData.MY_COURSE_PREFIX,
                        Utils.CallbackData.ACTION_BACK
                )
        );

        InlineKeyboardButton keyboard = createButton("yangi kurs yaratish",
                String.join(":",
                        ACTION_ADD,
                        ACTION_COURSE,
                        ACTION_PAGE,
                        "0"
                )
        );


        addButtons.add(button);
        addButtons.add(keyboard);
        rows.add(addButtons);
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    /**
     * @return i
     */
    @Override
    public InlineKeyboardMarkup myFullOrDraftCourses() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> addButtons = new ArrayList<>();
        InlineKeyboardButton keyboard = createButton("yangi kurs yaratish",
                String.join(":",
                        ACTION_ADD,
                        ACTION_COURSE,
                        ACTION_PAGE,
                        "0"
                )
        );
        addButtons.add(keyboard);

        List<InlineKeyboardButton> buttons = new ArrayList<>();

        InlineKeyboardButton yesButton = createButton("1",
                String.join(":",
                        Utils.CallbackData.MY_COURSE_PREFIX,
                        Utils.CallbackData.ACTION_SUCCESS,
                        Utils.CallbackData.ACTION_PAGE,
                        "0"
                )
        );
        InlineKeyboardButton noButton = createButton("2",
                String.join(":",
                        Utils.CallbackData.MY_COURSE_PREFIX,
                        Utils.CallbackData.ACTION_DRAFT,
                        Utils.CallbackData.ACTION_PAGE,
                        "0"
                )
        );

        buttons.add(yesButton);
        buttons.add(noButton);
        rows.add(buttons);
        rows.add(addButtons);
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    /**
     * @return i
     */
    @Override
    public InlineKeyboardMarkup myViewCourses(Page<Course> coursePage, String backButton, boolean success) {

        // 1. Asosiy klaviatura va tugmalar qatorlari uchun ro'yxatlarni yaratamiz
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        final int buttonsPerRow = 5;
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        int itemIndex = 1;

        // 4. Har bir kategoriya uchun FAQAAT RAQAMDAN iborat tugma yasaymiz
        for (Course course : coursePage.getContent()) {
            String buttonText = Utils.Numbering.toEmoji(itemIndex);

            String callbackData = String.join(":",
                    Utils.CallbackData.MY_COURSE_PREFIX,    // Oqim: Barcha kurslar
                    Utils.CallbackData.ACTION_COURSE,
                    Utils.CallbackData.ACTION_VIEW,          // Amal: Ro'yxatlash
                    course.getId().toString(),         // Qiymat: Kategoriya ID'si
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
                Utils.CallbackData.MY_COURSE_PREFIX,
                success ? Utils.CallbackData.ACTION_SUCCESS : Utils.CallbackData.ACTION_DRAFT
        );
        // 6. Sahifalash (pagination) va "Orqaga" tugmalarini qo'shamiz
        List<InlineKeyboardButton> paginationRow = createPaginationRow(coursePage, paginationBaseCallback);
        if (!paginationRow.isEmpty()) {
            keyboard.add(paginationRow);
        }

        keyboard.add(List.of(createButton("⬅️ Orqaga", backButton)));

        // 7. Tayyor klaviaturani qaytaramiz
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;


    }


    /**
     * @param courseId     .
     * @param backButton   .
     * @param modulesCount
     * @return .
     */
    @Override
    public InlineKeyboardMarkup instructorViewCourses(Long courseId, String backButton, Long modulesCount) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        CourseDetailDTO courseDetailDTO = courseService.read(courseId);

        if (!courseDetailDTO.isSuccess()) {
            List<InlineKeyboardButton> button = List.of(
                    createButton("status",
                            String.join(":",
                                    ACTION_EDIT,
                                    ACTION_COURSE,
                                    courseId.toString()
                            )
                    ),
                    createButton("👀 Modullarni korish",
                            String.join(":",
                                    ACTION_VIEW,
                                    MODULE_PREFIX,
                                    courseId.toString(),
                                    ACTION_PAGE,
                                    "0"
                            )
                    )
            );
            if (modulesCount > 0)
                rows.add(button);
        } else {
            List<InlineKeyboardButton> button = List.of(
                    createButton("👀 Modullarni korish",
                            String.join(":",
                                    ACTION_VIEW,
                                    MODULE_PREFIX,
                                    courseId.toString(),
                                    ACTION_PAGE,
                                    "0"
                            )
                    )
            );
            if (modulesCount > 0)
                rows.add(button);
        }


        List<InlineKeyboardButton> button1 = List.of(
                createButton("✏️ Kursni tahrirlash",
                        String.join(":",
                                Utils.CallbackData.ACTION_EDIT,
                                Utils.CallbackData.ACTION_COURSE,
                                courseId.toString()
                        )
                ),
                createButton("➕ Modul qo‘shish",
                        String.join(":",
                                ACTION_ADD,
                                MODULE_PREFIX,
                                courseId.toString()
                        )
                )
        );

        List<InlineKeyboardButton> button2 = List.of(
                createButton("⬅️ Orqaga",
                        String.join(":",
                                backButton
                        )
                ),
                createButton("❌ O‘chirish",
                        String.join(":",
                                Utils.CallbackData.ACTION_DELETE,
                                Utils.CallbackData.ACTION_COURSE,
                                courseId.toString()

                        )
                )
        );


        rows.add(button1);
        rows.add(button2);
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    /**
     * @param processKey
     * @return
     */
    @Override
    public InlineKeyboardMarkup succesOrDraftBtn(String processKey, String action) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> button1 = List.of(
                createButton("❌ Bekor qilish",
                        String.join(":",
                                ACTION_ADD,
                                action,
                                ACTION_CHOICE,
                                ACTION_DRAFT,
                                processKey
                        )
                ),
                createButton("✅ Tasdiqlash va Saqlash",
                        String.join(":",
                                ACTION_ADD,
                                action,
                                ACTION_CHOICE,
                                ACTION_CONFIRM,
                                processKey
                        )
                )
        );


        rows.add(button1);
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }


    /**
     * @param categories
     * @return
     */
    @Override
    public InlineKeyboardMarkup categorySelect(Page<Category> categories) {

        // 1. Asosiy klaviatura va tugmalar qatorlari uchun ro'yxatlarni yaratamiz
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        final int buttonsPerRow = 5;
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        int itemIndex = 1;

        // 4. Har bir kategoriya uchun FAQAAT RAQAMDAN iborat tugma yasaymiz
        for (Category category : categories.getContent()) {
            String buttonText = Utils.Numbering.toEmoji(itemIndex);

            String callbackData = String.join(":",
                    ACTION_ADD,
                    CATEGORY,
                    category.getId().toString()
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
                ACTION_ADD,
                ACTION_COURSE // todo bu yer teskshirish kerak xatolik bolishi mumkin

        );
        // 6. Sahifalash (pagination) va "Orqaga" tugmalarini qo'shamiz
        List<InlineKeyboardButton> paginationRow = createPaginationRow(categories, paginationBaseCallback);
        if (!paginationRow.isEmpty()) {
            keyboard.add(paginationRow);
        }

//        keyboard.add(List.of(createButton("⬅️ Orqaga", backButton)));

        // 7. Tayyor klaviaturani qaytaramiz
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;


    }

    /**
     * @param modulePage
     * @param backButton
     * @return
     */
    @Override
    public InlineKeyboardMarkup readModule(PageDTO<ModuleDetailDTO> modulePage, String backButton) {

        // 1. Asosiy klaviatura va tugmalar qatorlari uchun ro'yxatlarni yaratamiz
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        final int buttonsPerRow = 5;
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        int itemIndex = 1;

        // 4. Har bir kategoriya uchun FAQAAT RAQAMDAN iborat tugma yasaymiz
        for (ModuleDetailDTO moduleDetailDTO : modulePage.getContent()) {
            String buttonText = Utils.Numbering.toEmoji(itemIndex);

            String callbackData = String.join(":",
                    ACTION_VIEW,
                    MODULE_ID,
                    moduleDetailDTO.getId().toString()
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
                ACTION_ADD,
                ACTION_COURSE

        );
        // 6. Sahifalash (pagination) va "Orqaga" tugmalarini qo'shamiz
        List<InlineKeyboardButton> paginationRow = createPaginationRow(modulePage, paginationBaseCallback);
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
     * @param backButton
     * @param lessonCount
     * @return
     */
    @Override
    public InlineKeyboardMarkup instructorViewModule(Long moduleId, String backButton, Integer lessonCount) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> button = List.of(
                createButton("👀 Darslarni korish",
                        String.join(":",
                                ACTION_VIEW,
                                LESSON_PREFIX,
                                moduleId.toString(),
                                ACTION_PAGE,
                                "0"
                        )
                )
        );

        if (lessonCount > 0)
            rows.add(button);

        List<InlineKeyboardButton> button1 = List.of(
                createButton("✏️ Modulni tahrirlash",
                        String.join(":",
                                ACTION_EDIT,
                                ACTION_MODULE,
                                moduleId.toString()
                        )
                ),
                createButton("➕ Dars qo‘shish",
                        String.join(":",
                                ACTION_ADD,
                                LESSON_PREFIX,
                                moduleId.toString()
                        )
                )
        );

        List<InlineKeyboardButton> button2 = List.of(
                createButton("⬅️ Orqaga",
                        String.join(":",
                                backButton
                        )
                ),
                createButton("❌ O‘chirish",
                        String.join(":",
                                ACTION_DELETE,
                                MODULE_PREFIX,
                                moduleId.toString()

                        )
                )
        );


        rows.add(button1);
        rows.add(button2);
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }


    /**
     * @return
     */
    @Override
    public InlineKeyboardMarkup isFree() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();


        List<InlineKeyboardButton> button1 = List.of(
                createButton("Tekin",
                        String.join(":",
                                ACTION_ADD,
                                LESSON_PREFIX,
                                ACTION_CHOICE,
                                IS_PREE,
                                TRUE
                        )
                ),
                createButton("Pullik",
                        String.join(":",
                                ACTION_ADD,
                                LESSON_PREFIX,
                                ACTION_CHOICE,
                                IS_PREE,
                                FALSE
                        )
                )
        );


        rows.add(button1);
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    /**
     * @param lessonId
     * @param backButton
     * @param contentSize
     * @return
     */
    @Override
    public InlineKeyboardMarkup instructorViewLesson(Long lessonId, String backButton, Integer contentSize) {


        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> button = List.of(
                createButton("👀 Kontentlarni korish",
                        String.join(":",
                                ACTION_VIEW,
                                CONTENT_PREFIX,
                                lessonId.toString(),
                                ACTION_PAGE,
                                "0"
                        )
                )
        );

        if (contentSize > 0)
            rows.add(button);

        List<InlineKeyboardButton> button1 = List.of(
                createButton("✏️ Darsni tahrirlash",
                        String.join(":",
                                ACTION_EDIT,
                                LESSON_PREFIX,
                                lessonId.toString()
                        )
                ),
                createButton("➕ Kontent qo‘shish",
                        String.join(":",
                                ACTION_ADD,
                                CONTENT_PREFIX,
                                lessonId.toString()
                        )
                )
        );

        List<InlineKeyboardButton> button2 = List.of(
                createButton("⬅️ Orqaga",
                        String.join(":",
                                backButton
                        )
                ),
                createButton("❌ O‘chirish",
                        String.join(":",
                                ACTION_DELETE,
                                LESSON_PREFIX,
                                lessonId.toString()

                        )
                )
        );


        rows.add(button1);
        rows.add(button2);
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;


    }


    /**
     * @param lessonResponseDTOPageDTO
     * @param backButton
     * @return
     */
    @Override
    public InlineKeyboardMarkup instructorViewLesson(PageDTO<LessonResponseDTO> lessonResponseDTOPageDTO, String backButton, Long id) {

        // 1. Asosiy klaviatura va tugmalar qatorlari uchun ro'yxatlarni yaratamiz
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        final int buttonsPerRow = 5;
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        int itemIndex = 1;

        // 4. Har bir kategoriya uchun FAQAAT RAQAMDAN iborat tugma yasaymiz
        for (LessonResponseDTO lessonResponseDTO : lessonResponseDTOPageDTO.getContent()) {
            String buttonText = Utils.Numbering.toEmoji(itemIndex);

            String callbackData = String.join(":",
                    ACTION_VIEW,
                    LESSON_ID,
                    lessonResponseDTO.getId().toString()
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
                ACTION_VIEW,
                LESSON_ID,
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
     * @return
     */
    @Override
    public InlineKeyboardMarkup chooseContent(long lessonId) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> button = List.of(
                createButton(" Text kontent qoshish",
                        String.join(":",
                                ACTION_ADD,
                                CONTENT_PREFIX,
                                String.valueOf(lessonId),
                                TEXT_CONTENT
                        )
                )
        );


        List<InlineKeyboardButton> button1 = List.of(
                createButton("Video kontent qoshish",
                        String.join(":",
                                ACTION_ADD,
                                CONTENT_PREFIX,
                                String.valueOf(lessonId),
                                ATTACHMENT_CONTENT
                        )
                )
        );

        List<InlineKeyboardButton> button2 = List.of(
                createButton("Quiz kontent qoshish",
                        String.join(":",
                                ACTION_ADD,
                                CONTENT_PREFIX,
                                String.valueOf(lessonId),
                                QUIZ_CONTENT
                        )
                )

        );

        rows.add(button);
        rows.add(button1);
        rows.add(button2);
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;


    }

    /**
     * @param lessonResponseDTO
     * @param backButton
     * @return
     */
    @Override
    public InlineKeyboardMarkup instructorContent(LessonResponseDTO lessonResponseDTO, String backButton) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();


        for (ContentDTO content : lessonResponseDTO.getContents()) {

            if (content.getContentType().equals("QUIZ")) {

                InlineKeyboardButton keyboardButton = new InlineKeyboardButton(content.getContentType());
                keyboardButton.setUrl("url bolishi kerak");
                List<InlineKeyboardButton> button = List.of(keyboardButton);
                rows.add(button);

            } else {

                List<InlineKeyboardButton> button = List.of(
                        createButton(content.getContentType(),
                                String.join(":",
                                        ACTION_VIEW,
                                        content.getContentType(),
                                        String.valueOf(content.getId())
                                )
                        )
                );
                rows.add(button);
            }
        }

        List<InlineKeyboardButton> button2 = List.of(
                createButton("⬅️ Orqaga",
                        String.join(":",
                                backButton
                        )
                )
        );
        rows.add(button2);


        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    /**
     * @param courseDetailDTO
     * @param backButton
     * @return
     */
    @Override
    public InlineKeyboardMarkup instructorEditCourses(CourseDetailDTO courseDetailDTO, String backButton) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        String startAction = String.join(":",
                ACTION_EDIT,
                ACTION_COURSE,
                courseDetailDTO.getId().toString()
        );

        List<InlineKeyboardButton> button1 = List.of(
                createButton("✏️ Sarlavhani tahrirlash",
                        String.join(":",
                                startAction,
                                TITLE
                        )
                )
        );
        rows.add(button1);

        List<InlineKeyboardButton> button2 = List.of(
                createButton("✏️ Tavsifni tahrirlash",
                        String.join(":",
                                startAction,
                                DESCRIPTION
                        )
                )
        );
        rows.add(button2);

        if (courseDetailDTO.getThumbnailUrl() == null) {

            List<InlineKeyboardButton> button3 = List.of(
                    createButton("✏️ Rasm qo`shish",
                            String.join(":",
                                    startAction,
                                    PHOTO
                            )
                    )
            );
            rows.add(button3);

        } else {
            List<InlineKeyboardButton> button3 = List.of(
                    createButton("✏️ Rasmni yangilash",
                            String.join(":",
                                    startAction,
                                    PHOTO
                            )
                    )
            );
            rows.add(button3);
        }

        List<InlineKeyboardButton> button5 = List.of(
                createButton("✏️ Kategoriyani o`zgartirish",
                        String.join(":",
                                startAction,
                                CATEGORY
                        )
                )
        );
        rows.add(button5);

        List<InlineKeyboardButton> button4 = List.of(
                createButton("⬅️ Orqaga",
                        String.join(":",
                                backButton
                        )
                )
        );
        rows.add(button4);


        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;


    }

    /**
     * @return
     */
    @Override
    public InlineKeyboardMarkup deleteCourse(Long id) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();


        List<InlineKeyboardButton> button1 = List.of(
                createButton("✅ O‘chirish",
                        String.join(":",
                                ACTION_DELETE,
                                ACTION_COURSE,
                                id.toString(),
                                TRUE
                        )
                ),
                createButton("❌ bekor qilish",
                        String.join(":",
                                MY_COURSE_PREFIX,
                                ACTION_COURSE,
                                ACTION_VIEW,
                                id.toString(),
                                ACTION_PAGE,
                                "0"
                        )
                )
        );
        rows.add(button1);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;

    }

    /**
     * @param categories
     * @param cancelBtn
     * @return
     */
    @Override
    public InlineKeyboardMarkup categorySelect(Page<Category> categories, String cancelBtn) {

        // 1. Asosiy klaviatura va tugmalar qatorlari uchun ro'yxatlarni yaratamiz
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        final int buttonsPerRow = 5;
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        int itemIndex = 1;

        // 4. Har bir kategoriya uchun FAQAAT RAQAMDAN iborat tugma yasaymiz
        for (Category category : categories.getContent()) {
            String buttonText = Utils.Numbering.toEmoji(itemIndex);

            String callbackData = String.join(":",
                    ACTION_EDIT,
                    CATEGORY,
                    category.getId().toString()
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
                ACTION_EDIT,
                CATEGORY,
                "-1"

        );
        // 6. Sahifalash (pagination) va "Orqaga" tugmalarini qo'shamiz
        List<InlineKeyboardButton> paginationRow = createPaginationRow(categories, paginationBaseCallback);
        if (!paginationRow.isEmpty()) {
            keyboard.add(paginationRow);
        }

        keyboard.add(List.of(createButton("❌ Bekor qilish", cancelBtn)));

        // 7. Tayyor klaviaturani qaytaramiz
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;


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

}

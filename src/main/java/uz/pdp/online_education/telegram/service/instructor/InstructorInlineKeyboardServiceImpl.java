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
import uz.pdp.online_education.payload.projection.CourseStudentStatsProjection;
import uz.pdp.online_education.payload.lesson.LessonResponseDTO;
import uz.pdp.online_education.payload.module.ModuleDetailDTO;
import uz.pdp.online_education.payload.projection.CourseReviewProjection;
import uz.pdp.online_education.payload.projection.CourseReviewStatsProjection;
import uz.pdp.online_education.payload.projection.UserProjection;
import uz.pdp.online_education.service.interfaces.CourseService;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.service.UrlBuilderService;
import uz.pdp.online_education.telegram.service.instructor.template.InstructorInlineKeyboardService;

import java.util.ArrayList;
import java.util.List;

import static uz.pdp.online_education.telegram.Utils.CallbackData.*;

@Service
public class InstructorInlineKeyboardServiceImpl implements InstructorInlineKeyboardService {

    private final CourseService courseService;
    private final UrlBuilderService urlBuilderService;

    public InstructorInlineKeyboardServiceImpl(CourseService courseService, UrlBuilderService urlBuilderService) {
        this.courseService = courseService;
        this.urlBuilderService = urlBuilderService;
    }

    /**
     * Creates the keyboard for the dashboard message, including a "Logout" button.
     */
    @Override
    public InlineKeyboardMarkup dashboardMenu() {

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


        return new InlineKeyboardMarkup(List.of(List.of(yesButton, noButton)));
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


        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        final int buttonsPerRow = 5;
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        int itemIndex = 1;


        for (Course course : coursePage.getContent()) {
            String buttonText = Utils.Numbering.toEmoji(itemIndex);

            String callbackData = String.join(":",
                    Utils.CallbackData.MY_COURSE_PREFIX,
                    Utils.CallbackData.ACTION_COURSE,
                    Utils.CallbackData.ACTION_VIEW,
                    course.getId().toString(),
                    Utils.CallbackData.ACTION_PAGE,
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
                Utils.CallbackData.MY_COURSE_PREFIX,
                success ? Utils.CallbackData.ACTION_SUCCESS : Utils.CallbackData.ACTION_DRAFT
        );

        List<InlineKeyboardButton> paginationRow = createPaginationRow(coursePage, paginationBaseCallback);
        if (!paginationRow.isEmpty()) {
            keyboard.add(paginationRow);
        }

        keyboard.add(List.of(createButton("⬅️ Orqaga", backButton)));

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
                                    MY_COURSE_PREFIX,
                                    ACTION_STATUS,
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


        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        final int buttonsPerRow = 5;
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        int itemIndex = 1;


        for (Category category : categories.getContent()) {
            String buttonText = Utils.Numbering.toEmoji(itemIndex);

            String callbackData = String.join(":",
                    ACTION_ADD,
                    CATEGORY,
                    category.getId().toString()
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
                ACTION_ADD,
                ACTION_COURSE // todo bu yer teskshirish kerak xatolik bolishi mumkin

        );

        List<InlineKeyboardButton> paginationRow = createPaginationRow(categories, paginationBaseCallback);
        if (!paginationRow.isEmpty()) {
            keyboard.add(paginationRow);
        }

//        keyboard.add(List.of(createButton("⬅️ Orqaga", backButton)));


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


        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        final int buttonsPerRow = 5;
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        int itemIndex = 1;


        for (ModuleDetailDTO moduleDetailDTO : modulePage.getContent()) {
            String buttonText = Utils.Numbering.toEmoji(itemIndex);

            String callbackData = String.join(":",
                    ACTION_VIEW,
                    MODULE_ID,
                    moduleDetailDTO.getId().toString()
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
                ACTION_ADD,
                ACTION_COURSE

        );

        List<InlineKeyboardButton> paginationRow = createPaginationRow(modulePage, paginationBaseCallback);
        if (!paginationRow.isEmpty()) {
            keyboard.add(paginationRow);
        }

        keyboard.add(List.of(createButton("⬅️ Orqaga", backButton)));


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


        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        final int buttonsPerRow = 5;
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        int itemIndex = 1;


        for (LessonResponseDTO lessonResponseDTO : lessonResponseDTOPageDTO.getContent()) {
            String buttonText = Utils.Numbering.toEmoji(itemIndex);

            String callbackData = String.join(":",
                    ACTION_VIEW,
                    LESSON_ID,
                    lessonResponseDTO.getId().toString()
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
                ACTION_VIEW,
                LESSON_ID,
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
                keyboardButton.setUrl(urlBuilderService.generateQuizCreationUrl("string"));
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

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        final int buttonsPerRow = 5;
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        int itemIndex = 1;


        for (Category category : categories.getContent()) {
            String buttonText = Utils.Numbering.toEmoji(itemIndex);

            String callbackData = String.join(":",
                    ACTION_EDIT,
                    CATEGORY,
                    category.getId().toString()
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
        String[] split = cancelBtn.split(":");
        String paginationBaseCallback = String.join(":",
                ACTION_EDIT,
                CATEGORY,
                split[2]

        );

        List<InlineKeyboardButton> paginationRow = createPaginationRow(categories, paginationBaseCallback);
        if (!paginationRow.isEmpty()) {
            keyboard.add(paginationRow);
        }

        keyboard.add(List.of(createButton("❌ Bekor qilish", cancelBtn)));


        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    /**
     * @param moduleDetailDTO
     * @param backButton
     * @return
     */
    @Override
    public InlineKeyboardMarkup instructorEditModules(ModuleDetailDTO moduleDetailDTO, String backButton) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        String startAction = String.join(":",
                ACTION_EDIT,
                ACTION_MODULE,
                moduleDetailDTO.getId().toString()
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

        List<InlineKeyboardButton> button5 = List.of(
                createButton("✏️ Narxni o`zgartirish",
                        String.join(":",
                                startAction,
                                PRICE
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
     * @param lessonResponseDTO
     * @param backButton
     * @return
     */
    @Override
    public InlineKeyboardMarkup instructorEditLessons(LessonResponseDTO lessonResponseDTO, String backButton) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        String startAction = String.join(":",
                ACTION_EDIT,
                LESSON_PREFIX,
                lessonResponseDTO.getId().toString()
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

        List<InlineKeyboardButton> button5 = List.of(
                createButton("✏️ Dars turini tahrirlash",
                        String.join(":",
                                startAction,
                                IS_PREE
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
    public InlineKeyboardMarkup lessonEditIsFree(Long id) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> button2 = List.of(
                createButton("🆓 Bepul",
                        String.join(":",
                                ACTION_EDIT,
                                IS_PREE,
                                id.toString(),
                                TRUE
                        )
                ),
                createButton("💵 Pullik",
                        String.join(":",
                                ACTION_EDIT,
                                IS_PREE,
                                id.toString(),
                                FALSE
                        )
                )
        );
        rows.add(button2);

        List<InlineKeyboardButton> button1 = List.of(
                createButton("❌ Bekor qilish",
                        String.join(":",
                                ACTION_EDIT,
                                LESSON_PREFIX,
                                id.toString()
                        )
                )
        );
        rows.add(button1);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    /**
     * @param id
     * @return
     */
    @Override
    public InlineKeyboardMarkup deleteModule(Long id) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();


        List<InlineKeyboardButton> button1 = List.of(
                createButton("✅ O‘chirish",
                        String.join(":",
                                ACTION_DELETE,
                                ACTION_MODULE,
                                id.toString(),
                                TRUE
                        )
                ),
                createButton("❌ bekor qilish",
                        String.join(":",
                                ACTION_VIEW, MODULE_ID, id.toString()
//                                MY_COURSE_PREFIX,
//                                ACTION_MODULE,
//                                ACTION_VIEW,
//                                id.toString(),
//                                ACTION_PAGE,
//                                "0"
                        )
                )
        );
        rows.add(button1);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;

    }

    /**
     * @param id
     * @return
     */
    @Override
    public InlineKeyboardMarkup deleteLesson(Long id) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();


        List<InlineKeyboardButton> button1 = List.of(
                createButton("✅ O‘chirish",
                        String.join(":",
                                ACTION_DELETE,
                                LESSON_PREFIX,
                                id.toString(),
                                TRUE
                        )
                ),
                createButton("❌ bekor qilish",
                        String.join(":",
                                ACTION_VIEW, LESSON_ID, id.toString()
//                                MY_COURSE_PREFIX,
//                                ACTION_MODULE,
//                                ACTION_VIEW,
//                                id.toString(),
//                                ACTION_PAGE,
//                                "0"
                        )
                )
        );
        rows.add(button1);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    /**
     * @param stats
     * @param backButton
     * @return {@return }
     */
    @Override
    public InlineKeyboardMarkup instructorMyStudents(Page<CourseStudentStatsProjection> stats, String backButton) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        final int buttonsPerRow = 5;
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        int itemIndex = 1;


        for (CourseStudentStatsProjection CourseStudentStatsProjection : stats.getContent()) {
            String buttonText = Utils.Numbering.toEmoji(itemIndex);

            String callbackData = String.join(":",
                    ACTION_VIEW,
                    ACTION_STUDENT,
                    CourseStudentStatsProjection.getCourse_id().toString()
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
                ACTION_VIEW,
                ACTION_STUDENT

        );

        List<InlineKeyboardButton> paginationRow = createPaginationRow(stats, paginationBaseCallback);
        if (!paginationRow.isEmpty()) {
            keyboard.add(paginationRow);
        }

        keyboard.add(List.of(createButton("⬅️ Orqaga", backButton)));


        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    /**
     * @param courseId
     * @param backButton
     * @return
     */
    @Override
    public InlineKeyboardMarkup instructorCourseViewStudents(Long courseId, String backButton) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();


        List<InlineKeyboardButton> button1 = List.of(
                createButton("👥 O‘quvchilar",
                        String.join(":",
                                ACTION_VIEW,
                                ACTION_STUDENT_ID,
                                courseId.toString(),
                                ACTION_PAGE,
                                "0"
                        )
                )
        );
        rows.add(button1);

        rows.add(List.of(createButton("⬅️ Orqaga", backButton)));


        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    /**
     * @param users
     * @param id
     * @param backButton
     * @return
     */
    @Override
    public InlineKeyboardMarkup instructorStudentCourseById(Page<UserProjection> users, Long id, String backButton) {
        // 1. Asosiy klaviatura va tugmalar qatorlari uchun ro'yxatlarni yaratamiz
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        String paginationBaseCallback = String.join(":",
                ACTION_VIEW,
                ACTION_STUDENT_ID,
                id.toString()

        );

        List<InlineKeyboardButton> paginationRow = createPaginationRow(users, paginationBaseCallback);
        if (!paginationRow.isEmpty()) {
            keyboard.add(paginationRow);
        }

        keyboard.add(List.of(createButton("⬅️ Orqaga", backButton)));


        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    /**
     * @return
     */
    @Override
    public InlineKeyboardMarkup myReview() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();


        List<InlineKeyboardButton> button1 = List.of(
                createButton("📊 Kurslar bo`yicha sharhlarni ko`rish",
                        String.join(":",
                                ACTION_VIEW,
                                ACTION_REVIEWS,
                                ACTION_PAGE,
                                "0"
                        )
                )
        );
        rows.add(button1);

        rows.add(List.of(createButton("⬅️ Orqaga", BACK_TO_MAIN_MENU)));


        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;

    }

    /**
     * @param stats
     * @param backButton
     * @return
     */
    @Override
    public InlineKeyboardMarkup instructorReviewCourses(Page<CourseReviewStatsProjection> stats, String backButton) {
        // 1. Asosiy klaviatura va tugmalar qatorlari uchun ro'yxatlarni yaratamiz
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        final int buttonsPerRow = 5;
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        int itemIndex = 1;


        for (CourseReviewStatsProjection projection : stats.getContent()) {

            String callbackData = String.join(":",
                    ACTION_VIEW,
                    ACTION_REVIEWS,
                    projection.getCourseId().toString(),
                    ACTION_PAGE,
                    "0"
            );


            currentRow.add(createButton(String.valueOf(itemIndex), callbackData));
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
                ACTION_VIEW,
                ACTION_REVIEWS

        );

        List<InlineKeyboardButton> paginationRow = createPaginationRow(stats, paginationBaseCallback);
        if (!paginationRow.isEmpty()) {
            keyboard.add(paginationRow);
        }

        keyboard.add(List.of(createButton("⬅️ Orqaga", backButton)));


        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    /**
     * @param reviews
     * @param id
     * @param backButton
     * @return
     */
    @Override
    public InlineKeyboardMarkup courseReviews(Page<CourseReviewProjection> reviews, Long id, String backButton) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        final int buttonsPerRow = 5;
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        int itemIndex = 1;


        for (CourseReviewProjection projection : reviews.getContent()) {

            String callbackData = String.join(":",
                    ACTION_VIEW,
                    ACTION_REVIEWS,
                    COURSE_ID,
                    projection.getCourseId().toString(),
                    ACTION_STUDENT_ID,
                    projection.getStudentId().toString()
            );


            currentRow.add(createButton(String.valueOf(itemIndex), callbackData));
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
                ACTION_VIEW,
                ACTION_REVIEWS,
                id.toString()

        );

        List<InlineKeyboardButton> paginationRow = createPaginationRow(reviews, paginationBaseCallback);
        if (!paginationRow.isEmpty()) {
            keyboard.add(paginationRow);
        }

        keyboard.add(List.of(createButton("⬅️ Orqaga", backButton)));


        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    /**
     * @return
     */
    @Override
    public InlineKeyboardMarkup mentorRevenue() {
        return null;
    }

    /**
     * @param id
     * @return
     */
    @Override
    public InlineKeyboardMarkup succesOrDraftBtnCourse(String id) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> button1 = List.of(
                createButton("❌ Bekor qilish",
                        String.join(":",
                                MY_COURSE_PREFIX,
                                ACTION_STATUS,
                                id,
                                FALSE
                        )
                ),
                createButton("✅ Tasdiqlash",
                        String.join(":",
                                MY_COURSE_PREFIX,
                                ACTION_STATUS,
                                id,
                                TRUE
                        )
                )
        );


        rows.add(button1);
        inlineKeyboardMarkup.setKeyboard(rows);
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


        if (page.getTotalPages() <= 1) {
            return paginationRow;
        }

        int currentPage = page.getNumber();


        if (page.hasPrevious()) {

            String prevCallback = String.join(":",
                    baseCallback,
                    Utils.CallbackData.ACTION_PAGE,
                    String.valueOf(currentPage - 1)
            );
            paginationRow.add(createButton(Utils.InlineButtons.PAGINATION_PREVIOUS_TEXT, prevCallback));
        }


        String pageIndicator = String.format("%d / %d", currentPage + 1, page.getTotalPages());
        paginationRow.add(createButton(pageIndicator, "do_nothing"));


        if (page.hasNext()) {


            String nextCallback = String.join(":",
                    baseCallback,
                    Utils.CallbackData.ACTION_PAGE,
                    String.valueOf(currentPage + 1)
            );
            paginationRow.add(createButton(Utils.InlineButtons.PAGINATION_NEXT_TEXT, nextCallback));
        }


        return paginationRow;
    }

    private List<InlineKeyboardButton> createPaginationRow(PageDTO<?> page, String baseCallback) {
        List<InlineKeyboardButton> paginationRow = new ArrayList<>();


        if (page.getTotalPages() <= 1) {
            return paginationRow;
        }

        int currentPage = page.getPageNumber();


        if (!page.isFirst()) {

            String prevCallback = String.join(":",
                    baseCallback,
                    Utils.CallbackData.ACTION_PAGE,
                    String.valueOf(currentPage - 1)
            );
            paginationRow.add(createButton(Utils.InlineButtons.PAGINATION_PREVIOUS_TEXT, prevCallback));
        }


        String pageIndicator = String.format("%d / %d", currentPage + 1, page.getTotalPages());
        paginationRow.add(createButton(pageIndicator, "do_nothing"));


        if (!page.isLast()) {

            String nextCallback = String.join(":",
                    baseCallback,
                    Utils.CallbackData.ACTION_PAGE,
                    String.valueOf(currentPage + 1)
            );
            paginationRow.add(createButton(Utils.InlineButtons.PAGINATION_NEXT_TEXT, nextCallback));
        }


        return paginationRow;
    }

}

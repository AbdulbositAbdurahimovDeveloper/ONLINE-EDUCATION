package uz.pdp.online_education.telegram.service.instructor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import uz.pdp.online_education.config.security.JwtService;
import uz.pdp.online_education.enums.TransactionStatus;
import uz.pdp.online_education.model.Abs.AbsDateEntity;
import uz.pdp.online_education.model.Category;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.PageDTO;
import uz.pdp.online_education.payload.category.CategoryDTO;
import uz.pdp.online_education.payload.content.attachmentContent.AttachmentDTO;
import uz.pdp.online_education.payload.course.*;
import uz.pdp.online_education.payload.lesson.LessonCreatDTO;
import uz.pdp.online_education.payload.lesson.LessonResponseDTO;
import uz.pdp.online_education.payload.lesson.LessonUpdateDTO;
import uz.pdp.online_education.payload.module.ModuleCreateDTO;
import uz.pdp.online_education.payload.module.ModuleDetailDTO;
import uz.pdp.online_education.payload.projection.*;
import uz.pdp.online_education.payload.review.ReviewSummaryDTO;
import uz.pdp.online_education.payload.text.TextContentResponseDTO;
import uz.pdp.online_education.payload.user.UserDTO;
import uz.pdp.online_education.repository.*;
import uz.pdp.online_education.service.interfaces.*;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.config.controller.OnlineEducationBot;
import uz.pdp.online_education.telegram.enums.BotMessage;
import uz.pdp.online_education.telegram.enums.UserState;
import uz.pdp.online_education.telegram.mapper.SendMsg;
import uz.pdp.online_education.telegram.service.RedisTemporaryDataService;
import uz.pdp.online_education.telegram.service.RoleService;
import uz.pdp.online_education.telegram.service.TelegramUserService;
import uz.pdp.online_education.telegram.service.UrlBuilderService;
import uz.pdp.online_education.telegram.service.instructor.template.InstructorCallBackQueryService;
import uz.pdp.online_education.telegram.service.instructor.template.InstructorInlineKeyboardService;
import uz.pdp.online_education.telegram.service.instructor.template.InstructorProcessMessageService;
import uz.pdp.online_education.telegram.service.instructor.template.InstructorReplyKeyboardService;
import uz.pdp.online_education.telegram.service.message.MessageService;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uz.pdp.online_education.telegram.Utils.CallbackData.*;
import static uz.pdp.online_education.telegram.Utils.Numbering.randomBookEmoji;


@Slf4j
@Service
@RequiredArgsConstructor
public class InstructorCallBackQueryServiceImpl implements InstructorCallBackQueryService {

    private static final int PAGE_SIZE = 5; // Sahifadagi elementlar soni

    private final TelegramUserRepository telegramUserRepository;
    private final OnlineEducationBot bot;
    private final SendMsg sendMsg;
    private final MessageService messageService;
    private final RoleService roleService;
    private final InstructorReplyKeyboardService replyKeyboardService;
    private final InstructorInlineKeyboardService inlineKeyboardService;
    private final ModuleEnrollmentRepository moduleEnrollmentRepository;
    private final CourseRepository courseRepository;
    private final PaymentRepository paymentRepository;
    private final TelegramUserService telegramUserService;
    private final InstructorProcessMessageService processMessageService;
    private final CourseService courseService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final ModuleService moduleService;
    private final RedisTemporaryDataService redisTemporaryDataService;
    private final CategoryRepository categoryRepository;
    private final AttachmentService attachmentService;
    private final LessonService lessonService;
    private final UrlBuilderService urlBuilderService;
    private final JwtService jwtService;
    private final TextContentService textContentService;
    private final ModuleRepository moduleRepository;
    private final PaymentService paymentService;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;


    @Override
    public void handleCallback(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String queryData = callbackQuery.getData();
        String callbackQueryId = callbackQuery.getId();

        bot.myExecute(new AnswerCallbackQuery(callbackQueryId));

        UserState userState = telegramUserService.getUserState(chatId);
        User user = telegramUserRepository.findByChatId(chatId).orElseThrow(() -> new RuntimeException("User not found for callback. ChatID: " + chatId)).getUser();

        bot.myExecute(new AnswerCallbackQuery(callbackQuery.getId()));

        try {
            String[] data = queryData.split(":");
            String prefix = data[0];

            switch (prefix) {
                case DELETED -> bot.myExecute(sendMsg.deleteMessage(chatId, messageId));
                case BACK_TO_MAIN_MENU -> backToMain(chatId, messageId);
                case AUTH_PREFIX -> handleAuthCallback(user, chatId, messageId, data);
                case MY_COURSE_PREFIX -> instructorMyCourseHandle(chatId, user, messageId, data, queryData);
                case STUDENT_PREFIX -> instructorMyStudentHandle(chatId, user, messageId, data, queryData);
                case ACTION_REVIEWS -> instructorReviewsHandle(chatId, user, messageId, data, queryData);
                case ACTION_REVENUE -> instructorMyRevenueHandle(chatId, user, messageId, data, queryData);
                case ACTION_ADD -> instructorActionAddHandle(chatId, user, messageId, data, queryData, userState);
                case ACTION_EDIT -> instructorActionEditHandle(chatId, user, messageId, data, queryData);
                case ACTION_DELETE -> instructorActionDeleteHandle(chatId, user, messageId, data, queryData);
                case ACTION_VIEW -> instructorActionViewHandle(chatId, user, messageId, data, queryData);
            }
        } catch (Exception e) {
            log.error("Callbackni qayta ishlashda xatolik yuz berdi: Query='{}'", queryData, e);
            InlineKeyboardMarkup keyboard = inlineKeyboardService.createSingleButtonKeyboard("🗑 xabarni ochirish", DELETED);
            bot.myExecute(sendMsg.sendMessage(chatId, messageService.getMessage(BotMessage.ERROR_UNEXPECTED), keyboard));
        }
    }


    private void handleAuthCallback(User user, Long chatId, Integer messageId, String[] data) {
        String action = data[1]; // "logout"
        String step = data[2];  // "init", "confirm", "cancel"

        if (!action.equals("logout")) {
            log.warn("Noma'lum autentifikatsiya amali: {}", action);
            return;
        }

        switch (step) {
            case Utils.CallbackData.ACTION_INIT -> {
                String confirmationText = messageService.getMessage(BotMessage.AUTH_LOGOUT_CONFIRMATION_TEXT);
                InlineKeyboardMarkup confirmationKeyboard = inlineKeyboardService.logoutConfirmation();
                bot.myExecute(sendMsg.editMessage(chatId, messageId, confirmationText, confirmationKeyboard));
            }
            case Utils.CallbackData.ACTION_CONFIRM -> {
                telegramUserService.unregistered(chatId);
                String successText = messageService.getMessage(BotMessage.AUTH_LOGOUT_SUCCESS_TEXT);
                bot.myExecute(sendMsg.editMessage(chatId, messageId, successText, null));
            }
            case Utils.CallbackData.ACTION_CANCEL -> processMessageService.showDashboard(user, chatId, messageId);
            default -> log.warn("Noma'lum chiqish qadami: {}", step);
        }
    }

    private void instructorMyCourseHandle(Long chatId, User user, Integer messageId, String[] data, String queryData) {
        String type = data[1];
        switch (type) {
            case ACTION_BACK -> processMessageService.instructorMyCourseHandle(chatId, user, messageId);
            case ACTION_SUCCESS -> instructorMyCoursesSuccess(chatId, user, messageId, data);
            case ACTION_DRAFT -> instructorMyCourseDraft(chatId, user, messageId, data);
            case ACTION_COURSE -> instructorMyCourseViewId(chatId, messageId, data);
            case ACTION_MODULE -> instructorMyModuleViewId(chatId, messageId, data);
        }
    }

    private void instructorMyModuleViewId(Long chatId, Integer messageId, String[] data) {


    }


    private void instructorMyStudentHandle(Long chatId, User user, Integer messageId, String[] data, String queryData) {
        // ... Logika
    }

    private void instructorReviewsHandle(Long chatId, User user, Integer messageId, String[] data, String queryData) {
        // ... Logika
    }

    private void instructorMyRevenueHandle(Long chatId, User user, Integer messageId, String[] data, String queryData) {
        // ... Logika
    }

    private void instructorActionAddHandle(Long chatId, User user, Integer messageId, String[] data, String queryData, UserState userState) {

        String type = data[1];

        // todo:  ochirish kerek
        System.err.println(queryData);


        switch (type) {
            case CATEGORY -> {

                Long categoryId = Long.valueOf(data[2]);
                CategoryDTO categoryDTO = categoryService.read(categoryId);

                String processKey = String.join(":", ACTION_ADD, ACTION_COURSE, chatId.toString());
                redisTemporaryDataService.addField(processKey, CATEGORY_ID, data[2]);

                telegramUserService.updateUserState(chatId, UserState.AWAITING_COURSE_TITLE);

                String message = messageService.getMessage(BotMessage.INSTRUCTOR_COURSE_CREATE_CATEGORY_CHOSEN, categoryDTO.getName());
                bot.myExecute(sendMsg.editMessage(chatId, messageId, message));
                bot.myExecute(sendMsg.sendMessage(chatId, messageService.getMessage(BotMessage.INSTRUCTOR_COURSE_CREATE_TITLE_PROMPT)));


            }
            case ACTION_COURSE -> {
                if (data.length > 2 && data[2].equals(ACTION_CHOICE)) {
                    handleCourseCreationConfirmation(chatId, messageId, data, user);
                } else {
                    startCourseCreationProcess(chatId, messageId, data, userState);
                }
            }
            case ACTION_MODULE -> {
                if (data.length > 2 && data[2].equals(ACTION_CHOICE)) {
                    handleModuleCreationConfirmation(chatId, messageId, data);
                } else {
                    startModuleCreationProcess(chatId, messageId, data);
                }
            }
            case LESSON_PREFIX -> {
                if (data.length > 2 && data[2].equals(ACTION_CHOICE)) {
                    handleLessonCreationConfirmation(chatId, messageId, data);
                } else {
                    startLessonCreationProcess(chatId, messageId, data);
                }
            }
            case CONTENT_PREFIX -> {
                if (data.length > 2 && data[2].equals(ACTION_CHOICE)) {
                    handleContentCreationConfirmation(chatId, messageId, data);
                } else {
                    startContentCreationProcess(chatId, messageId, data, userState, user);
                }
            }

        }


        // ... Logika
    }

    private void handleContentCreationConfirmation(Long chatId, Integer messageId, String[] data) {

    }

    private void startContentCreationProcess(Long chatId, Integer messageId, String[] data, UserState userState, User user) {

        long lessonId = Long.parseLong(data[2]); // courseId'ni olamiz
        if (userState.equals(UserState.AWAITING_CHOOSE_CONTENT)) {

            String contentType = data[3];

            switch (contentType) {
                case QUIZ_CONTENT -> {
                    String accessToken = jwtService.generateAccessToken(user);
                    String url = urlBuilderService.generateQuizCreationUrl(accessToken);
                    InlineKeyboardMarkup urlButton = inlineKeyboardService.createUrlButton("Quiz yaratish", url);
                    bot.myExecute(sendMsg.editMessage(chatId, messageId, "quiz yaratish uchun kinopkani bosing", urlButton));
                    telegramUserService.updateUserState(chatId, UserState.NONE);

                }
                case ATTACHMENT_CONTENT -> {

                    bot.myExecute(sendMsg.editMessage(chatId, messageId, "Iltimos darslik uchun video yuklang"));
                    telegramUserService.updateUserState(chatId, UserState.AWAITING_ATTACHMENT_CONTENT);

                }
                case TEXT_CONTENT -> {

                    bot.myExecute(sendMsg.editMessage(chatId, messageId, "Iltimos text kontent kiring textni bir martada toliq va xatolik siz kiriting"));
                    telegramUserService.updateUserState(chatId, UserState.AWAITING_TEXT_CONTENT);

                }
            }


        } else {

            // 1. Unikal jarayon kalitini generatsiya qilamiz.
            // Bu metodni shu klassning o'ziga yozib qo'yamiz.
            String processKey = String.join(":", ACTION_ADD, LESSON_PREFIX, chatId.toString());

            // 2. Boshlang'ich ma'lumotlarni tayyorlaymiz.
            Map<String, Object> initialData = new HashMap<>();
            initialData.put(LESSON_ID, String.valueOf(lessonId));
            initialData.put(CURRENT_STEP, UserState.AWAITING_CHOOSE_CONTENT.name());

            // 3. Jarayonni Redis'da boshlaymiz.
            redisTemporaryDataService.startProcess(processKey, initialData);

            // 4. Foydalanuvchining umumiy holatini o'zgartiramiz.
            // Eslatma: Bu yondashuv bir vaqtda faqat bitta jarayon uchun ishlaydi.
            telegramUserService.updateUserState(chatId, UserState.AWAITING_CHOOSE_CONTENT);

            // 5. Foydalanuvchiga birinchi savolni yuboramiz.
            bot.myExecute(sendMsg.editMessage(chatId, messageId,
                    messageService.getMessage(BotMessage.INSTRUCTOR_CREATE_REMINDER)
            ));
            InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.chooseContent(lessonId);
            bot.myExecute(sendMsg.sendMessage(chatId, "Kontent turini tanlang", inlineKeyboardMarkup));
        }
    }

    private void startCourseCreationProcess(Long chatId, Integer messageId, String[] data, UserState userState) {

        if (userState.equals(UserState.AWAITING_COURSE_CATEGORY_CHOICE)) {

            int pageNumber = Integer.parseInt(data[3]);
            Page<Category> categories = categoryRepository.findAll(PageRequest.of(pageNumber, 10));

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Kerakli kategory tanlang \n");
            int number = 1;
            for (Category category : categories.getContent()) {
                stringBuilder.append(number++).append(". ").append(category.getName()).append("\n");
            }

            InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.categorySelect(categories);
            bot.myExecute(sendMsg.editMessage(chatId, messageId, stringBuilder.toString(), inlineKeyboardMarkup));

            telegramUserService.updateUserState(chatId, UserState.AWAITING_COURSE_CATEGORY_CHOICE);

        } else {


            String processKey = String.join(":", ACTION_ADD, ACTION_COURSE, chatId.toString());
            Map<String, Object> initialData = new HashMap<>();
            initialData.put(CURRENT_STEP, UserState.AWAITING_MODULE_TITLE.name());

            redisTemporaryDataService.startProcess(processKey, initialData);

            telegramUserService.updateUserState(chatId, UserState.AWAITING_COURSE_CATEGORY_CHOICE);

            bot.myExecute(sendMsg.editMessage(chatId, messageId,
                    messageService.getMessage(BotMessage.INSTRUCTOR_CREATE_REMINDER)
            ));
            bot.myExecute(sendMsg.sendMessage(chatId, messageService.getMessage(BotMessage.INSTRUCTOR_COURSE_CREATE_CATEGORY_PROMPT)));


            int pageNumber = Integer.parseInt(data[3]);
            Page<Category> categories = categoryRepository.findAll(PageRequest.of(pageNumber, 10));

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Kerakli kategory tanlang \n");
            int number = 1;
            for (Category category : categories.getContent()) {
                stringBuilder.append(number++).append(". ").append(category.getName()).append("\n");
            }

            InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.categorySelect(categories);
            bot.myExecute(sendMsg.sendMessage(chatId, stringBuilder.toString(), inlineKeyboardMarkup));

        }
    }

    private void handleCourseCreationConfirmation(Long chatId, Integer messageId, String[] data, User user) {

        String processKey = String.join(":", data[4], data[5], data[6]);

        Optional<Map<String, Object>> allFieldsOpt = redisTemporaryDataService.getAllFields(processKey);

        if (allFieldsOpt.isPresent()) {
            Map<String, Object> courseData = allFieldsOpt.get();

            Long categoryId = Long.valueOf(String.valueOf(courseData.get(Utils.CallbackData.CATEGORY_ID)));
            String title = String.valueOf(courseData.get(TITLE));
            String description = String.valueOf(courseData.get(DESCRIPTION));
            Long thumbnailId = Long.valueOf(String.valueOf(courseData.get(Utils.CallbackData.THUMBNAIL_ID)));

            CourseDetailDTO course = courseService.create(new CourseCreateDTO(title, description, thumbnailId, categoryId), user);

            UserDTO userDTO = userService.read(course.getInstructorId());
            CategoryDTO categoryDTO = categoryService.read(course.getCategoryId());
            AttachmentDTO attachmentDTO = attachmentService.read(Long.valueOf(course.getThumbnailUrl()));
            ReviewSummaryDTO reviewSummary = course.getReviewSummary();

            String formatted = String.format(
                    """
                            📘 Course Detail
                            ────────────────
                            ID: %d
                            Title: %s
                            Description: %s
                            Slug: %s
                            Thumbnail: %s
                            Instructor name: %s
                            Category name: %s
                            Modules Count: %d
                            Review count: %s
                            Review average: %s
                            Created At: %d
                            Updated At: %d
                            Success: %b
                            """,
                    course.getId(),
                    course.getTitle(),
                    course.getDescription(),
                    course.getSlug(),
                    course.getThumbnailUrl(),
                    userDTO.getFirstName() + " " + userDTO.getLastName(),
                    categoryDTO.getName(),
                    course.getModulesCount(),
                    reviewSummary.getCount(),
                    reviewSummary.getAverageRating(),
                    course.getCreatedAt(),
                    course.getUpdatedAt(),
                    course.isSuccess()
            );


            bot.myExecute(sendMsg.deleteMessage(chatId, messageId));
            bot.myExecute(sendMsg.sendMessage(chatId, "kurs muvofaqiyatli qoshildi"));
            bot.myExecute(sendMsg.sendPhoto(chatId, attachmentDTO.getTelegramFileId(), formatted));
            telegramUserService.updateUserState(chatId, UserState.NONE);
        }
    }

    /**
     * Yangi modul yaratish jarayonini boshlaydi.
     */
    private void startModuleCreationProcess(Long chatId, Integer messageId, String[] data) {
        long courseId = Long.parseLong(data[2]); // courseId'ni olamiz

        // 1. Unikal jarayon kalitini generatsiya qilamiz.
        // Bu metodni shu klassning o'ziga yozib qo'yamiz.
        String processKey = String.join(":", ACTION_ADD, ACTION_MODULE, chatId.toString());

        // 2. Boshlang'ich ma'lumotlarni tayyorlaymiz.
        Map<String, Object> initialData = new HashMap<>();
        initialData.put(COURSE_ID, String.valueOf(courseId));
        initialData.put(CURRENT_STEP, UserState.AWAITING_MODULE_TITLE.name());

        // 3. Jarayonni Redis'da boshlaymiz.
        redisTemporaryDataService.startProcess(processKey, initialData);

        // 4. Foydalanuvchining umumiy holatini o'zgartiramiz.
        // Eslatma: Bu yondashuv bir vaqtda faqat bitta jarayon uchun ishlaydi.
        telegramUserService.updateUserState(chatId, UserState.AWAITING_MODULE_TITLE);

        // 5. Foydalanuvchiga birinchi savolni yuboramiz.
        bot.myExecute(sendMsg.editMessage(chatId, messageId,
                messageService.getMessage(BotMessage.INSTRUCTOR_CREATE_REMINDER)
        ));
        bot.myExecute(sendMsg.sendMessage(chatId, messageService.getMessage(BotMessage.INSTRUCTOR_MODULE_CREATE_TITLE)));
    }

    /**
     * Foydalanuvchi tasdiqlash/bekor qilish tugmasini bosganda ishlaydi.
     */
    private void handleModuleCreationConfirmation(Long chatId, Integer messageId, String[] data) {
        String choice = data[3]; // "confirm" yoki "draft"/"cancel"

        // XATO 1 ni to'g'rilaymiz: Kalit callback'dan to'g'ri uzatilishi kerak.
        // Tasdiqlash tugmasining callback'iga butun processKey'ni yuborish kerak.
        // Masalan: "...:module:choice:confirm:module_create_12345_99"
        // Bu yerda data[4] da to'liq kalit bor deb faraz qilamiz.
        String processKey = String.join(":", data[4], data[5], data[6]);

        if (choice.equals(ACTION_CONFIRM)) {
            // "Tasdiqlash" bosildi
            Optional<Map<String, Object>> allFields = redisTemporaryDataService.getAllFields(processKey);

            if (allFields.isPresent()) {
                Map<String, Object> moduleFields = allFields.get();

                // XATO 2 ni to'g'rilaymiz: ClassCastException
                Long courseId = Long.parseLong(String.valueOf(moduleFields.get(COURSE_ID)));
                String title = String.valueOf(moduleFields.get(TITLE));
                String description = String.valueOf(moduleFields.get(DESCRIPTION));
                Long price = Long.parseLong(String.valueOf(moduleFields.get(PRICE)));

                ModuleCreateDTO moduleCreateDTO = new ModuleCreateDTO(
                        title,
                        description,
                        price * 100, // Pulni tiyinda saqlash
                        courseId
                );

                ModuleDetailDTO moduleDetailDTO = moduleService.create(moduleCreateDTO);

                // Jarayon tugadi, tozalaymiz.
                redisTemporaryDataService.endProcess(processKey);
                telegramUserService.updateUserState(chatId, UserState.NONE);


                bot.myExecute(sendMsg.editMessage(chatId, messageId, "✅ Modul muvaffaqiyatli yaratildi!"));
                ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardService.buildMentorMenu();
                bot.myExecute(sendMsg.sendMessage(chatId, buildModuleDetailText(moduleDetailDTO), replyKeyboardMarkup));

            } else {
                bot.myExecute(sendMsg.editMessage(chatId, messageId, "❌ Xatolik: Jarayon topilmadi yoki muddati o'tgan."));
            }
        } else { // ACTION_DRAFT yoki ACTION_CANCEL
            // "Bekor qilish" bosildi
            redisTemporaryDataService.endProcess(processKey);
            telegramUserService.updateUserState(chatId, UserState.NONE);
            bot.myExecute(sendMsg.editMessage(chatId, messageId, "🚫 Jarayon bekor qilindi."));
        }
    }

    private void handleLessonCreationConfirmation(Long chatId, Integer messageId, String[] data) {
        String choice = data[3]; // "confirm" yoki "draft"/"cancel"


        if (choice.equals(ACTION_CONFIRM)) {
            String processKey = String.join(":", data[4], data[5], data[6]);

            // "Tasdiqlash" bosildi
            Optional<Map<String, Object>> allFields = redisTemporaryDataService.getAllFields(processKey);

            if (allFields.isPresent()) {
                Map<String, Object> moduleFields = allFields.get();

                // XATO 2 ni to'g'rilaymiz: ClassCastException
                Long moduleId = Long.parseLong(String.valueOf(moduleFields.get(MODULE_ID)));
                String title = String.valueOf(moduleFields.get(TITLE));
                String description = String.valueOf(moduleFields.get(DESCRIPTION));
                boolean isFree = Boolean.parseBoolean(String.valueOf(moduleFields.get(Utils.CallbackData.IS_PREE)));

                LessonCreatDTO lessonCreatDTO = new LessonCreatDTO(
                        title,
                        description,
                        isFree,
                        moduleId
                );

                LessonResponseDTO lessonResponseDTO = lessonService.create(lessonCreatDTO);

                // Jarayon tugadi, tozalaymiz.
                redisTemporaryDataService.endProcess(processKey);
                telegramUserService.updateUserState(chatId, UserState.NONE);


                bot.myExecute(sendMsg.editMessage(chatId, messageId, "✅ Darslik muvaffaqiyatli yaratildi!"));
                ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardService.buildMentorMenu();
                bot.myExecute(sendMsg.sendMessage(chatId, buildLessonDetailText(lessonResponseDTO), replyKeyboardMarkup));

            } else {
                bot.myExecute(sendMsg.editMessage(chatId, messageId, "❌ Xatolik: Jarayon topilmadi yoki muddati o'tgan."));
            }
        } else if (choice.equals(IS_PREE)) {

            boolean isFree = Boolean.parseBoolean(data[4]);

            String processKey = String.join(":", data[0], data[1], chatId.toString());
            redisTemporaryDataService.addField(processKey, IS_PREE, isFree);


            Optional<Map<String, Object>> allFields = redisTemporaryDataService.getAllFields(processKey);
            if (allFields.isPresent()) {
                Map<String, Object> moduleFields = allFields.get();

                Long moduleId = Long.valueOf(moduleFields.get(Utils.CallbackData.MODULE_ID).toString());
                String title = moduleFields.get(TITLE).toString();
                String description = moduleFields.get(DESCRIPTION).toString();

                LessonResponseDTO lessonResponseDTO = new LessonResponseDTO();
                lessonResponseDTO.setTitle(title);
                lessonResponseDTO.setContent(description);
                lessonResponseDTO.setModuleId(moduleId);
                lessonResponseDTO.setFree(isFree);

                String built = buildLessonDetailText(lessonResponseDTO);
                InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.succesOrDraftBtn(processKey, LESSON_PREFIX);
                bot.myExecute(sendMsg.editMessage(chatId, messageId, built, inlineKeyboardMarkup));


            }


        } else { // ACTION_DRAFT yoki ACTION_CANCEL
            // "Bekor qilish" bosildi

            String processKey = String.join(":", data[4], data[5], data[6]);
            redisTemporaryDataService.endProcess(processKey);
            telegramUserService.updateUserState(chatId, UserState.NONE);
            bot.myExecute(sendMsg.editMessage(chatId, messageId, "🚫 Jarayon bekor qilindi."));
        }
    }


    private String buildLessonDetailText(LessonResponseDTO l) {
        String title = safe(l.getTitle());
        String desc = safe(l.getContent());
        String free = l.isFree() ? "✅ Ha" : "❌ Yo‘q";
        String order = l.getOrderIndex() == null ? "-" : String.valueOf(l.getOrderIndex());
        String module = l.getModuleId() == null ? "-" : String.valueOf(l.getModuleId());

        return """
                🎓 <b>Dars haqida</b>
                
                🆔 ID: <code>%d</code>
                🏷️ Nomi: <b>%s</b>
                🪜 Tartib: %s
                🆓 Bepul: %s
                🔗 Modul ID: <code>%s</code>
                
                kontent %s
                
                📝 Tavsif:
                %s
                """.formatted(
                l.getId(), title, order, free, module,
                l.getContents() != null ? l.getContents().size() : null,
                (desc.isBlank() ? "-" : desc)
        );
    }


    private void startLessonCreationProcess(Long chatId, Integer messageId, String[] data) {

        Long moduleId = Long.parseLong(data[2]); // courseId'ni olamiz

        String processKey = String.join(":", ACTION_ADD, LESSON_PREFIX, chatId.toString());

        // 2. Boshlang'ich ma'lumotlarni tayyorlaymiz.
        Map<String, Object> initialData = new HashMap<>();
        initialData.put(MODULE_ID, String.valueOf(moduleId));
        initialData.put(CURRENT_STEP, UserState.AWAITING_LESSON_TITLE.name());

        redisTemporaryDataService.startProcess(processKey, initialData);

        telegramUserService.updateUserState(chatId, UserState.AWAITING_LESSON_TITLE);

        bot.myExecute(sendMsg.editMessage(chatId, messageId,
                messageService.getMessage(BotMessage.INSTRUCTOR_CREATE_REMINDER)
        ));
        bot.myExecute(sendMsg.sendMessage(chatId, "Daslik uchun sarlavha kiriting"));
    }

    private void instructorActionEditHandle(Long chatId, User user, Integer messageId, String[] data, String
            queryData) {

        // todo ochiramiz
        System.err.println(queryData);
        String type = data[1];
        Long id = Long.valueOf(data[2]);


        final String joined = String.join(":",
                MY_COURSE_PREFIX,
                ACTION_COURSE,
                ACTION_VIEW,
                id.toString(),
                ACTION_PAGE,
                "0"
        );
        switch (type) {
            case IS_PREE -> {

                boolean isFree = Boolean.parseBoolean(data[3]);
                LessonUpdateDTO lessonUpdateDTO = new LessonUpdateDTO();
                lessonUpdateDTO.setFree(isFree);
                LessonResponseDTO update = lessonService.update(id, lessonUpdateDTO);
                String built = buildLessonDetailText(update);
                String backButton = String.join(":", ACTION_VIEW, LESSON_ID, id.toString());
                InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.instructorEditLessons(update, backButton);
                bot.myExecute(sendMsg.editMessage(chatId, messageId, built, inlineKeyboardMarkup));

            }
            case CATEGORY -> {

                if (data.length > 3) {

                    int pageNumber = Integer.parseInt(data[4]);
                    telegramUserService.updateUserState(chatId, UserState.AWAITING_EDIT_COURSE_CATEGORY_CHOICE);
                    Page<Category> categories = categoryRepository.findAll(PageRequest.of(pageNumber, 10));

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Kerakli kategory tanlang \n");
                    int number = 1;
                    for (Category category : categories.getContent()) {
                        stringBuilder.append(number++).append(". ").append(category.getName()).append("\n");
                    }

//                    String name = categoryDTO.getName();
                    String categoryMessage = """
                            🏷 <b>Kategoriya tahriri</b>
                            
                            📌 <i>Joriy kategoriya:</i>
                            
                            <b>%s</b>
                            
                            ✍️ Yangi kategoriyani tanlang:
                            %s
                            ℹ️ Agar tahrir qilishni xohlamasangiz, pastdagi
                            "❌ Bekor qilish" tugmasini bosing.
                            """.formatted("🔁", stringBuilder.toString());

                    String cancelBtn = String.join(":", ACTION_EDIT, ACTION_COURSE, id.toString());
                    InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.categorySelect(categories, cancelBtn);

                    bot.myExecute(sendMsg.editMessage(chatId, messageId, categoryMessage, inlineKeyboardMarkup));
                } else {
                    String processKey = String.join(":", ACTION_EDIT, ACTION_COURSE, chatId.toString());
                    Optional<Map<String, Object>> allFields = redisTemporaryDataService.getAllFields(processKey);
                    if (allFields.isPresent()) {
                        Map<String, Object> courseFields = allFields.get();
                        Long courseId = Long.valueOf(courseFields.get(COURSE_ID).toString());


                        CourseUpdateDTO courseUpdateDTO = new CourseUpdateDTO();
                        courseUpdateDTO.setCategoryId(id);
                        CourseDetailDTO courseDetailDTO = courseService.update(courseId, courseUpdateDTO, user);

                        CategoryDTO categoryDTO = categoryService.read(courseDetailDTO.getCategoryId());
                        UserDTO userDTO = userService.read(courseDetailDTO.getInstructorId());

                        String text = String.format("""
                                        📘 <b>Kurs Tafsilotlari</b>
                                        
                                        🔹 <b>Nomi:</b> %s \s
                                        🔹 <b>Slug:</b> %s \s
                                        🔹 <b>Kategoriya:</b> %s \s
                                        🔹 <b>Mentor :</b> %s \s
                                        
                                        📖 <b>Tavsif:</b> \s
                                        %s \s
                                        
                                        📊 <b>Statistika:</b> \s
                                        - 📚 <b>Modullar:</b> %s \s
                                        - ⭐ <b>Baholash:</b> %s  \s
                                        - 📝 <b>Sharhlar:</b> %s ta \s
                                        
                                        🕒 <b>Qo‘shilgan sana:</b> %s \s
                                        ♻️ <b>Yangilangan sana:</b> %s \s
                                        
                                        ✅ <b>Status:</b> %s
                                        """,
                                courseDetailDTO.getTitle(),
                                courseDetailDTO.getSlug(),
                                categoryDTO.getName(),
                                userDTO.getFirstName() + " " + userDTO.getLastName(),
                                courseDetailDTO.getDescription(),
                                courseDetailDTO.getModulesCount(),
                                generateStars(courseDetailDTO.getReviewSummary().getAverageRating()),
                                courseDetailDTO.getReviewSummary().getCount(),
                                formatDate(courseDetailDTO.getCreatedAt()),
                                formatDate(courseDetailDTO.getUpdatedAt()),
                                courseDetailDTO.isSuccess() ? "Faol" : "Faol emas"
                        );
                        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.instructorEditCourses(courseDetailDTO, joined);
                        bot.myExecute(sendMsg.editMessage(chatId, messageId, text, inlineKeyboardMarkup));
                    }
                }
            }
            case ACTION_COURSE -> handleEditCourse(chatId, messageId, data, id, joined);
            case ACTION_MODULE -> handleEditModule(chatId, messageId, data, id);
            case LESSON_PREFIX -> {

                LessonResponseDTO lessonResponseDTO = lessonService.read(id);
                if (data.length > 3) {

                    InlineKeyboardMarkup cancelButton = inlineKeyboardService.createSingleButtonKeyboard("❌ Bekor qilish", String.join(":",
                            ACTION_EDIT, LESSON_PREFIX, id.toString()));

                    String editType = data[3];
                    String sendMessage = "null";
                    switch (editType) {
                        case TITLE -> {
                            telegramUserService.updateUserState(chatId, UserState.AWAITING_EDIT_LESSON_TITLE);
                            String title = lessonResponseDTO.getTitle();
                            sendMessage = """
                                    ✏️ <b>Sarlavha tahriri</b>
                                    
                                    
                                    📌 <i>Joriy sarlavha:</i>
                                    
                                    <b>%s</b>
                                    
                                    
                                    ✍️ Yangi sarlavhani kiriting:
                                    
                                    
                                    ℹ️ Agar tahrir qilishni xohlamasangiz, pastdagi
                                    "❌ Bekor qilish" tugmasini bosing.
                                    """.formatted(title);

                        }
                        case DESCRIPTION -> {
                            telegramUserService.updateUserState(chatId, UserState.AWAITING_EDIT_LESSON_DESCRIPTION);
                            String description = lessonResponseDTO.getContent();

                            sendMessage = """
                                    📄 <b>Tavsif tahriri</b>
                                    
                                    
                                    📌 <i>Joriy tavsif:</i>
                                    
                                    %s
                                    
                                    
                                    ✍️ Yangi tavsifni kiriting:
                                    
                                    
                                    ℹ️ Agar tahrir qilishni xohlamasangiz, pastdagi
                                    "❌ Bekor qilish" tugmasini bosing.
                                    """.formatted(description);


                        }
                        case IS_PREE -> {
                            telegramUserService.updateUserState(chatId, UserState.AWAITING_EDIT_LESSON_IS_FREE);

                            sendMessage = """
                                    💰 <b>Dars narxi tahriri</b>
                                    
                                    
                                    📌 <i>Joriy holat:</i> %s
                                    
                                    
                                    ✍️ Yangi holatni tanlang:
                                    
                                    ✅ Bepul  
                                    💵 Pullik
                                    
                                    
                                    ℹ️ Agar tahrir qilishni xohlamasangiz, pastdagi
                                    "❌ Bekor qilish" tugmasini bosing.
                                    """.formatted(lessonResponseDTO.isFree() ? "Bepul" : "Pullik");


                            InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.lessonEditIsFree(id);
                            String processKey = String.join(":", ACTION_EDIT, LESSON_PREFIX, chatId.toString());
                            redisTemporaryDataService.startProcess(processKey, Map.of(LESSON_ID, id.toString(), IS_PREE, lessonResponseDTO.isFree()));
                            bot.myExecute(sendMsg.editMessage(chatId, messageId, sendMessage, inlineKeyboardMarkup));
                            return;
                        }
                    }

                    String processKey = String.join(":", ACTION_EDIT, LESSON_PREFIX, chatId.toString());
                    redisTemporaryDataService.startProcess(processKey, Map.of(LESSON_ID, id.toString(), IS_PREE, lessonResponseDTO.isFree()));
                    bot.myExecute(sendMsg.editMessage(chatId, messageId, sendMessage, cancelButton));

                } else {

                    String built = buildLessonDetailText(lessonResponseDTO);
                    String backButton = String.join(":", ACTION_VIEW, LESSON_ID, id.toString());
                    InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.instructorEditLessons(lessonResponseDTO, backButton);
                    bot.myExecute(sendMsg.editMessage(chatId, messageId, built, inlineKeyboardMarkup));


                }

            }
        }
    }

    private void handleEditModule(Long chatId, Integer messageId, String[] data, Long id) {
        ModuleDetailDTO moduleDetailDTO = moduleService.read(id);

        if (data.length > 3) {

            InlineKeyboardMarkup cancelButton = inlineKeyboardService.createSingleButtonKeyboard("❌ Bekor qilish", String.join(":",
                    ACTION_EDIT, ACTION_MODULE, id.toString()));

            String editType = data[3];
            String sendMessage = "null";
            switch (editType) {
                case TITLE -> {
                    telegramUserService.updateUserState(chatId, UserState.AWAITING_EDIT_MODULE_TITLE);
                    String title = moduleDetailDTO.getTitle();
                    sendMessage = """
                            ✏️ <b>Sarlavha tahriri</b>
                            
                            
                            📌 <i>Joriy sarlavha:</i>
                            
                            <b>%s</b>
                            
                            
                            ✍️ Yangi sarlavhani kiriting:
                            
                            
                            ℹ️ Agar tahrir qilishni xohlamasangiz, pastdagi
                            "❌ Bekor qilish" tugmasini bosing.
                            """.formatted(title);

                }
                case DESCRIPTION -> {
                    telegramUserService.updateUserState(chatId, UserState.AWAITING_EDIT_MODULE_DESCRIPTION);
                    String description = moduleDetailDTO.getDescription();
                    sendMessage = """
                            📄 <b>Tavsif tahriri</b>
                            
                            
                            📌 <i>Joriy tavsif:</i>
                            
                            %s
                            
                            
                            ✍️ Yangi tavsifni kiriting:
                            
                            
                            ℹ️ Agar tahrir qilishni xohlamasangiz, pastdagi
                            "❌ Bekor qilish" tugmasini bosing.
                            """.formatted(description);

                }
                case PRICE -> {
                    telegramUserService.updateUserState(chatId, UserState.AWAITING_EDIT_MODULE_PRICE);
                    Long price = moduleDetailDTO.getPrice();
                    sendMessage = """
                            📄 <b>Narx tahriri</b>
                            
                            
                            📌 <i>Joriy Narx:</i>
                            
                            %s
                            
                            
                            ✍️ Yangi tavsifni kiriting:
                            
                            
                            ℹ️ Agar tahrir qilishni xohlamasangiz, pastdagi
                            "❌ Bekor qilish" tugmasini bosing.
                            """.formatted(formatAmount(price));
                }
            }
            String processKey = String.join(":", ACTION_EDIT, ACTION_MODULE, chatId.toString());
            redisTemporaryDataService.startProcess(processKey, Map.of(MODULE_ID, id.toString()));
            bot.myExecute(sendMsg.editMessage(chatId, messageId, sendMessage, cancelButton));

        } else {

            String built = buildModuleDetailText(moduleDetailDTO);
            String backButton = String.join(":", ACTION_VIEW, MODULE_ID, id.toString());
            InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.instructorEditModules(moduleDetailDTO, backButton);
            bot.myExecute(sendMsg.editMessage(chatId, messageId, built, inlineKeyboardMarkup));


        }
    }

    private void handleEditCourse(Long chatId, Integer messageId, String[] data, Long id, String joined) {
        CourseDetailDTO courseDetailDTO = courseService.read(id);
        CategoryDTO categoryDTO = categoryService.read(courseDetailDTO.getCategoryId());
        UserDTO userDTO = userService.read(courseDetailDTO.getInstructorId());

        if (data.length == 4) {

            InlineKeyboardMarkup cancelButton = inlineKeyboardService.createSingleButtonKeyboard("❌ Bekor qilish", String.join(":",
                    ACTION_EDIT, ACTION_COURSE, id.toString()));

            String editType = data[3];
            String sendMessage = "null";
            switch (editType) {
                case TITLE -> {
                    telegramUserService.updateUserState(chatId, UserState.AWAITING_EDIT_COURSE_TITLE);
                    String title = courseDetailDTO.getTitle();
                    sendMessage = """
                            ✏️ <b>Sarlavha tahriri</b>
                            
                            
                            📌 <i>Joriy sarlavha:</i>
                            
                            <b>%s</b>
                            
                            
                            ✍️ Yangi sarlavhani kiriting:
                            
                            
                            ℹ️ Agar tahrir qilishni xohlamasangiz, pastdagi 
                            "❌ Bekor qilish" tugmasini bosing.
                            """.formatted(title);
                }
                case DESCRIPTION -> {
                    telegramUserService.updateUserState(chatId, UserState.AWAITING_EDIT_COURSE_DESCRIPTION);
                    String description = courseDetailDTO.getDescription();
                    sendMessage = """
                            📄 <b>Tavsif tahriri</b>
                            
                            
                            📌 <i>Joriy tavsif:</i>
                            
                            %s
                            
                            
                            ✍️ Yangi tavsifni kiriting:
                            
                            
                            ℹ️ Agar tahrir qilishni xohlamasangiz, pastdagi 
                            "❌ Bekor qilish" tugmasini bosing.
                            """.formatted(description);
                }
                case PHOTO -> {
                    telegramUserService.updateUserState(chatId, UserState.AWAITING_EDIT_COURSE_THUMBNAIL);
                    if (courseDetailDTO.getThumbnailUrl() == null) {
                        sendMessage = """
                                🖼 <b>Rasm qo‘shilmagan</b>
                                
                                
                                ✍️ Iltimos yangi rasmni yuklang ⬇️
                                
                                
                                ℹ️ Agar tahrir qilishni xohlamasangiz, pastdagi 
                                "❌ Bekor qilish" tugmasini bosing.
                                """;
                    } else {
                        sendMessage = """
                                🖼 <b>Rasm tahriri</b>
                                
                                
                                📌 Joriy rasm mavjud.
                                
                                ✍️ Iltimos yangi rasmni yuklang ⬇️
                                
                                
                                ℹ️ Agar tahrir qilishni xohlamasangiz, pastdagi 
                                "❌ Bekor qilish" tugmasini bosing.
                                """;
                        InlineKeyboardMarkup button = inlineKeyboardService.createSingleButtonKeyboard("❌ Bekor qilish", String.join(":",
                                ACTION_EDIT, ACTION_COURSE, id.toString(), ACTION_DELETE, PHOTO));

                        bot.myExecute(sendMsg.deleteMessage(chatId, messageId));
                        AttachmentDTO attachmentDTO = attachmentService.read(Long.valueOf(courseDetailDTO.getThumbnailUrl()));
                        bot.myExecute(sendMsg.sendPhoto(chatId, attachmentDTO.getTelegramFileId(), sendMessage, button));
                        return;
                    }
                }
                case CATEGORY -> {
                    telegramUserService.updateUserState(chatId, UserState.AWAITING_EDIT_COURSE_CATEGORY_CHOICE);
                    Page<Category> categories = categoryRepository.findAll(PageRequest.of(0, 10));

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Kerakli kategory tanlang \n");
                    int number = 1;
                    for (Category category : categories.getContent()) {
                        stringBuilder.append(number++).append(". ").append(category.getName()).append("\n");
                    }

                    String name = categoryDTO.getName();
                    String categoryMessage = """
                            🏷 <b>Kategoriya tahriri</b>
                            
                            📌 <i>Joriy kategoriya:</i>
                            
                            <b>%s</b>
                            
                            ✍️ Yangi kategoriyani tanlang:
                            %s
                            ℹ️ Agar tahrir qilishni xohlamasangiz, pastdagi
                            "❌ Bekor qilish" tugmasini bosing.
                            """.formatted(name, stringBuilder.toString());
                    String processKey = String.join(":", ACTION_EDIT, ACTION_COURSE, chatId.toString());
                    redisTemporaryDataService.startProcess(processKey, Map.of(COURSE_ID, id.toString(), CATEGORY_ID, categoryDTO.getId().toString()));

                    String cancelBtn = String.join(":", ACTION_EDIT, ACTION_COURSE, id.toString());
                    InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.categorySelect(categories, cancelBtn);

                    bot.myExecute(sendMsg.editMessage(chatId, messageId, categoryMessage, inlineKeyboardMarkup));
                    return;
                }
            }

            String processKey = String.join(":", ACTION_EDIT, ACTION_COURSE, chatId.toString());
            redisTemporaryDataService.startProcess(processKey, Map.of(COURSE_ID, id.toString(), CATEGORY_ID, categoryDTO.getId().toString()));
            bot.myExecute(sendMsg.editMessage(chatId, messageId, sendMessage, cancelButton));
        } else {


            String text = String.format("""
                            📘 <b>Kurs Tafsilotlari</b>
                            
                            🔹 <b>Nomi:</b> %s \s
                            🔹 <b>Slug:</b> %s \s
                            🔹 <b>Kategoriya:</b> %s \s
                            🔹 <b>Mentor :</b> %s \s
                            
                            📖 <b>Tavsif:</b> \s
                            %s \s
                            
                            📊 <b>Statistika:</b> \s
                            - 📚 <b>Modullar:</b> %s \s
                            - ⭐ <b>Baholash:</b> %s  \s
                            - 📝 <b>Sharhlar:</b> %s ta \s
                            
                            🕒 <b>Qo‘shilgan sana:</b> %s \s
                            ♻️ <b>Yangilangan sana:</b> %s \s
                            
                            ✅ <b>Status:</b> %s
                            """,
                    courseDetailDTO.getTitle(),
                    courseDetailDTO.getSlug(),
                    categoryDTO.getName(),
                    userDTO.getFirstName() + " " + userDTO.getLastName(),
                    courseDetailDTO.getDescription(),
                    courseDetailDTO.getModulesCount(),
                    generateStars(courseDetailDTO.getReviewSummary().getAverageRating()),
                    courseDetailDTO.getReviewSummary().getCount(),
                    formatDate(courseDetailDTO.getCreatedAt()),
                    formatDate(courseDetailDTO.getUpdatedAt()),
                    courseDetailDTO.isSuccess() ? "Faol" : "Faol emas"
            );


            InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.instructorEditCourses(courseDetailDTO, joined);

            if (data.length > 4 && data[4].equals(PHOTO)) {
                bot.myExecute(sendMsg.deleteMessage(chatId, messageId));
                bot.myExecute(sendMsg.sendMessage(chatId, text, inlineKeyboardMarkup));
            } else {
                bot.myExecute(sendMsg.editMessage(chatId, messageId, text, inlineKeyboardMarkup));
            }
        }
    }

    private void instructorActionDeleteHandle(Long chatId, User user, Integer messageId, String[] data, String queryData) {

        String type = data[1];
        Long id = Long.valueOf(data[2]);
        switch (type) {
            case ACTION_COURSE -> {
                long count = paymentRepository.countByModule_Course_Id(id);
                if (count != 0) {

                    InlineKeyboardMarkup keyboard = inlineKeyboardService.createSingleButtonKeyboard("Tushunarli",
                            String.join(":", MY_COURSE_PREFIX, ACTION_COURSE, ACTION_VIEW, id.toString(), ACTION_PAGE, "0"));
                    bot.myExecute(sendMsg.editMessage(chatId, messageId,
                            "Siz ushbu kursni ochira olmaysiz\n chunki bu kurs modulini  <b>" + count + "ta</b> oquvchilar sotib olgan !!!",
                            keyboard));
                } else {
                    if (data.length > 3 && Boolean.parseBoolean(data[3])) {
                        InlineKeyboardMarkup keyboard = inlineKeyboardService.createSingleButtonKeyboard("Tushunarli",
                                String.join(":", ACTION_VIEW, ACTION_COURSE, id.toString()));
                        courseService.delete(id);
                        bot.myExecute(sendMsg.editMessage(chatId, messageId, "Kurs ochirildi", keyboard));
                    } else {
                        InlineKeyboardMarkup keyboard = inlineKeyboardService.deleteCourse(id);
                        bot.myExecute(sendMsg.editMessage(chatId, messageId, "Kurs ochirish agar kurs o`chirilsa uni qaytarib tiklab bolmaydi ", keyboard));
                    }
                }
            }
            case ACTION_MODULE -> {
                long count = paymentRepository.countByModule_Id(id);
                if (count != 0) {

                    InlineKeyboardMarkup keyboard = inlineKeyboardService.createSingleButtonKeyboard("Tushunarli",
                            String.join(":", ACTION_VIEW, MODULE_ID, id.toString()));
                    bot.myExecute(sendMsg.editMessage(chatId, messageId,
                            "Siz ushbu Moduleni ochira olmaysiz\n chunki bu moduli  <b>" + count + "ta</b> oquvchilar sotib olgan !!!",
                            keyboard));

                } else {

                    if (data.length > 3 && Boolean.parseBoolean(data[3])) {
                        ModuleDetailDTO moduleDetailDTO = moduleService.read(id);
                        InlineKeyboardMarkup keyboard = inlineKeyboardService.createSingleButtonKeyboard("Tushunarli",
                                String.join(":", MY_COURSE_PREFIX, ACTION_COURSE, ACTION_VIEW, moduleDetailDTO.getCourseId().toString(), ACTION_PAGE, "0"));
                        moduleService.delete(id);
                        bot.myExecute(sendMsg.editMessage(chatId, messageId, "Modulni ochirildi", keyboard));
                    } else {
                        InlineKeyboardMarkup keyboard = inlineKeyboardService.deleteModule(id);
                        bot.myExecute(sendMsg.editMessage(chatId, messageId, "Modulni ochirish agar modul o`chirilsa uni qaytarib tiklab bolmaydi ", keyboard));
                    }
                }
            }
            case LESSON_PREFIX -> {
                LessonResponseDTO lessonResponseDTO = lessonService.read(id);
                long count = paymentRepository.countByModule_Id(lessonResponseDTO.getModuleId());
                if (count != 0) {

                    InlineKeyboardMarkup keyboard = inlineKeyboardService.createSingleButtonKeyboard("Tushunarli",
                            String.join(":", ACTION_VIEW, LESSON_ID, id.toString()));
                    bot.myExecute(sendMsg.editMessage(chatId, messageId,
                            "Siz ushbu Darsni ochira olmaysiz\n chunki bu darsni  <b>" + count + "ta</b> oquvchilar sotib olgan !!!",
                            keyboard));

                } else {

                    if (data.length > 3 && Boolean.parseBoolean(data[3])) {
                        InlineKeyboardMarkup keyboard = inlineKeyboardService.createSingleButtonKeyboard("Tushunarli",
                                String.join(":", ACTION_VIEW, MODULE_ID, lessonResponseDTO.getModuleId().toString()));
                        lessonService.delete(id);
                        bot.myExecute(sendMsg.editMessage(chatId, messageId, "Dars ochirildi", keyboard));
                    } else {
                        InlineKeyboardMarkup keyboard = inlineKeyboardService.deleteLesson(id);
                        bot.myExecute(sendMsg.editMessage(chatId, messageId, "Darsni ochirish agar dars o`chirilsa uni qaytarib tiklab bolmaydi ", keyboard));
                    }
                }
            }
        }


    }

    private void instructorActionViewHandle(Long chatId, User user, Integer messageId, String[] data, String queryData) {
        String type = data[1];
        switch (type) {
            case ACTION_REVIEWS -> {
                if (data.length == 6) {
                    Long studentId = Long.valueOf(data[5]);
                    Long courseId = Long.valueOf(data[3]);

//                    CourseReviewDetailProjection studentDetailForCourse = courseRepository.findStudentDetailForCourse(courseId, studentId);
                    CourseReviewDetailProjection studentDetailForCourse = userRepository.findStudentDetailForCourse(courseId, studentId);

                    String built = buildReviewDetailHtml(studentDetailForCourse);
                    InlineKeyboardMarkup keyboard = inlineKeyboardService.createSingleButtonKeyboard("⬅️ Orqaga", String.join(":", ACTION_VIEW, ACTION_REVIEWS, courseId.toString(), ACTION_PAGE, "0"));
                    bot.myExecute(sendMsg.editMessage(chatId, messageId, built, keyboard));


                } else if (data.length == 3 && data[2].equals(ACTION_BACK)) {
                    ReviewStatsProjection projection = reviewRepository.getOverallReviewStatsByMentorId(user.getId());
                    String built = buildReviewSummary(projection);
                    InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.myReview();
                    bot.myExecute(sendMsg.editMessage(chatId, messageId, built, inlineKeyboardMarkup));
                } else if (data.length == 4) {
                    int pageNumber = Integer.parseInt(data[3]);
                    Page<CourseReviewStatsProjection> stats = courseRepository.findCourseReviewStatsByMentor(user.getId(), PageRequest.of(pageNumber, 5));
                    String backButton = String.join(":", ACTION_VIEW, ACTION_REVIEWS, ACTION_BACK);
                    InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.instructorReviewCourses(stats, backButton);
                    String built = buildCourseReviewStatsText(stats);
                    bot.myExecute(sendMsg.editMessage(chatId, messageId, built, inlineKeyboardMarkup));
                } else {
                    Long id = Long.valueOf(data[2]);
                    int pageNumber = Integer.parseInt(data[4]);

                    Sort sortByRatingDesc = Sort.by(
                            Sort.Order.desc("rating"),      // Asosiy saralash
                            Sort.Order.desc("createdAt")    // Ikkilamchi saralash
                    );
                    Pageable pageable = PageRequest.of(pageNumber, 5, sortByRatingDesc);
                    Page<CourseReviewProjection> reviews = reviewRepository.findReviewsByCourseId(id, pageable);
                    String built = formatCourseReviewsPage(reviews);
                    String backButton = String.join(":", ACTION_VIEW, ACTION_REVIEWS, ACTION_PAGE, "0");
                    InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.courseReviews(reviews, id, backButton);
                    bot.myExecute(sendMsg.editMessage(chatId, messageId, built, inlineKeyboardMarkup));
                }
            }
            case ACTION_STUDENT -> {
                if (data.length > 3) {
                    int pageNumber = Integer.parseInt(data[3]);
                    long count = paymentRepository.countTotalStudentsByMentor(user.getId(), TransactionStatus.SUCCESS);
                    PageRequest pageable = PageRequest.of(pageNumber, 10);
                    Page<CourseStudentStatsProjection> stats = courseRepository.findCourseStatsByInstructor(user.getId(), TransactionStatus.SUCCESS.name(), pageable);

                    String built = buildStudentsDashboardText(count, stats);
                    String backButton = String.join(":", BACK_TO_MAIN_MENU);
                    InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.instructorMyStudents(stats, backButton);
                    bot.myExecute(sendMsg.editMessage(chatId, messageId, built, inlineKeyboardMarkup));
                } else {
                    Long courseId = Long.valueOf(data[2]);

                    String built = buildCourseDetail(
                            courseService.read(courseId),
                            courseRepository.findCourseStatsById(courseId, TransactionStatus.SUCCESS.name())
                    );
                    String backButton = String.join(":", ACTION_VIEW, ACTION_STUDENT, ACTION_PAGE, "0");
                    InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.instructorCourseViewStudents(courseId, backButton);
                    bot.myExecute(sendMsg.editMessage(chatId, messageId, built, inlineKeyboardMarkup));
                }
            }
            case ACTION_STUDENT_ID -> {
                Long id = Long.valueOf(data[2]);
                int pageNumber = Integer.parseInt(data[4]);
                Page<UserProjection> users = userRepository.findEnrolledStudentProfilesByCourseId(id, TransactionStatus.SUCCESS.name(), PageRequest.of(pageNumber, 5));

                String built = formatUserPage(users);

                String backButton = String.join(":", ACTION_VIEW, ACTION_STUDENT, id.toString());
                InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.instructorStudentCourseById(users, id, backButton);
                bot.myExecute(sendMsg.editMessage(chatId, messageId, built, inlineKeyboardMarkup));
            }
            case ACTION_COURSE -> {
                int countTrue = courseRepository.countByInstructorIdAndSuccess(user.getId(), true);
                int countFalse = courseRepository.countByInstructorIdAndSuccess(user.getId(), false);
                String text = String.format(
                        """
                                📚 <b>Mening kurslarim</b>
                                
                                📊 <u>Statistika</u>
                                ━━━━━━━━━━━━━━━
                                1  <b>To‘liq yaratilgan kurslar:</b> <code>%d</code> ✅
                                2  <b>Tugallanmagan kurslar:</b> <code>%d</code> 🛠
                                ━━━━━━━━━━━━━━━━
                                
                                👇 <i>Quyidagi tugmalardan birini tanlang:</i>""",
                        countTrue,
                        countFalse
                );

                InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.myFullOrDraftCourses();
                bot.myExecute(sendMsg.editMessage(chatId, messageId, text, inlineKeyboardMarkup));

            }
            case ACTION_MODULE -> {

                Long courseId = Long.valueOf(data[2]);
                int pageNumber = Integer.parseInt(data[4]);


                PageDTO<ModuleDetailDTO> modulePage = moduleService.read(courseId, pageNumber, 10);

                String built = formatModulesPage(modulePage);
                String backButton = String.join(":", MY_COURSE_PREFIX, ACTION_COURSE, ACTION_VIEW, courseId.toString(), ACTION_PAGE, "0");
                InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.readModule(modulePage, backButton);

                bot.myExecute(sendMsg.editMessage(chatId, messageId, built, inlineKeyboardMarkup));


            }
            case MODULE_ID -> {

                Long moduleId = Long.valueOf(data[2]);

                ModuleDetailDTO moduleDetailDTO = moduleService.read(moduleId);
                String built = buildModuleDetailText(moduleDetailDTO);
                String backButton = String.join(":", ACTION_VIEW, ACTION_MODULE, moduleDetailDTO.getCourseId().toString(), ACTION_PAGE, "0");
                InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.instructorViewModule(
                        moduleId,
                        backButton,
                        moduleDetailDTO.getLessonCount()
                );
                bot.myExecute(sendMsg.editMessage(chatId, messageId, built, inlineKeyboardMarkup));
            }
            case LESSON_PREFIX -> {

                Long id = Long.valueOf(data[2]);
                Integer pageNumber = Integer.parseInt(data[4]);

                PageDTO<LessonResponseDTO> lessonResponseDTOPageDTO = moduleService.readLessons(id, pageNumber, 10);

                String built = buildLessonsPage(lessonResponseDTOPageDTO);
                String backButton = String.join(":", ACTION_VIEW, MODULE_ID, id.toString(), ACTION_PAGE, "0");
                InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.instructorViewLesson(lessonResponseDTOPageDTO, backButton, id);
                bot.myExecute(sendMsg.editMessage(chatId, messageId, built, inlineKeyboardMarkup));


            }
            case LESSON_ID -> {
                Long lessonId = Long.valueOf(data[2]);

                LessonResponseDTO lessonResponseDTO = lessonService.read(lessonId);
                String lessonDetailText = buildLessonDetailText(lessonResponseDTO);
                String backButton = String.join(":", ACTION_VIEW, LESSON_PREFIX, lessonResponseDTO.getModuleId().toString(), ACTION_PAGE, "0");
                InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.instructorViewLesson(lessonId, backButton, lessonResponseDTO.getContents().size());
                bot.myExecute(sendMsg.editMessage(chatId, messageId, lessonDetailText, inlineKeyboardMarkup));

            }
            case CONTENT_PREFIX -> {
                Long lessonId = Long.valueOf(data[2]);

                LessonResponseDTO lessonResponseDTO = lessonService.read(lessonId);
                String backButton = String.join(":", ACTION_VIEW, LESSON_PREFIX, lessonId.toString(), ACTION_PAGE, "0");
                InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.instructorContent(lessonResponseDTO, backButton);
                bot.myExecute(sendMsg.editMessage(chatId, messageId, "Dars nomi " + lessonResponseDTO.getTitle(), inlineKeyboardMarkup));
            }
            case "ATTACHMENT" -> {

                Long id = Long.valueOf(data[2]);

                AttachmentDTO attachmentDTO = attachmentService.read(id);

                if (attachmentDTO.getTelegramFileId() != null && !attachmentDTO.getContentType().equals("image/jpeg")) {

                    InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.createSingleButtonKeyboard("delete", DELETED);
                    bot.myExecute(sendMsg.sendVideo(chatId, attachmentDTO.getTelegramFileId(), inlineKeyboardMarkup));
                }


            }
            case "TEXT" -> {

                Long id = Long.valueOf(data[2]);
                TextContentResponseDTO responseDTO = textContentService.getById(id);

                InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.createSingleButtonKeyboard("🗑 Xabarni ochirish", DELETED);
                bot.myExecute(sendMsg.sendMessage(chatId, responseDTO.getText(), inlineKeyboardMarkup));
            }
        }
    }

    private void backToMain(Long chatId, Integer messageId) {

        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardService.buildMentorMenu();
        bot.myExecute(sendMsg.deleteMessage(chatId, messageId));
        bot.myExecute(sendMsg.sendMessage(chatId, "Bosh menyu!", replyKeyboardMarkup));

    }

    public String buildReviewDetailHtml(CourseReviewDetailProjection p) {
        StringBuilder sb = new StringBuilder();

        // Sotib olish va sharh sanalari
        sb.append("📌 <b>Kurs sotib olingan:</b> ")
                .append(p.getPurchasedAt() != null ? p.getPurchasedAt().toLocalDateTime().toLocalDate() : "❌ Ma'lumot yo‘q")
                .append("\n");
        sb.append("📝 <b>Sharh yozilgan:</b> ")
                .append(p.getReviewDate() != null ? p.getReviewDate().toLocalDateTime().toLocalDate() : "❌ Ma'lumot yo‘q")
                .append("\n\n");

        // Foydalanuvchi ma'lumotlari
        sb.append("👤 <b>Talaba:</b> ")
                .append(p.getStudentName() != null && !p.getStudentName().isBlank() ? escapeHtml(p.getStudentName()) : "Anonim foydalanuvchi")
                .append("\n");
        if (p.getBio() != null && !p.getBio().isBlank()) {
            sb.append("ℹ️ <b>Bio:</b> ").append(escapeHtml(p.getBio())).append("\n");
        }
        sb.append("📅 <b>Platformaga qo‘shilgan:</b> ")
                .append(p.getJoinedAt() != null ? p.getJoinedAt().toLocalDateTime().toLocalDate() : "❌ Ma'lumot yo‘q")
                .append("\n\n");

        // Statistika
        sb.append("📚 <b>Umumiy kurslar:</b> ").append(p.getTotalCourses() != null ? p.getTotalCourses() : 0).append("\n");
        sb.append("⭐️ <b>O‘rtacha baho:</b> ")
                .append(p.getAverageRating() != null ? String.format("%.1f", p.getAverageRating()) : "❌ Ma'lumot yo‘q")
                .append("\n");
        sb.append("🎯 <b>Ushbu kursdagi bahosi:</b> ")
                .append(p.getRating() != null ? p.getRating() + " ⭐️" : "❌ Ma'lumot yo‘q")
                .append("\n\n");

        // Sharh matni
        if (p.getComment() != null && !p.getComment().isBlank()) {
            sb.append("💬 <b>Fikr:</b> ").append(escapeHtml(p.getComment())).append("\n");
        } else {
            sb.append("💬 <b>Fikr:</b> ❌ Sharh qoldirilmagan\n");
        }

        return sb.toString();
    }

    public String formatCourseReviewsPage(Page<CourseReviewProjection> page) {
        if (page.isEmpty()) {
            return "❌ Bu kurs uchun sharhlar topilmadi.";
        }

        StringBuilder sb = new StringBuilder("💬 <b>Kurs sharhlari</b>\n\n");

        for (CourseReviewProjection review : page.getContent()) {
            String name = review.getStudentName() != null ? review.getStudentName() : "Anonim foydalanuvchi";
            String stars = "⭐️".repeat(review.getRating());
            String comment = review.getComment() != null && !review.getComment().isBlank()
                    ? "✍️ " + review.getComment()
                    : "✍️ Fikr qoldirilmagan";

            String date = review.getCreatedAt()
                    .toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));


            sb.append("👤 ").append(name).append("\n")
                    .append(stars).append(" ").append(review.getRating()).append("\n")
                    .append(comment).append("\n")
                    .append("⏰ ").append(date).append("\n\n");
        }

        sb.append("📄 Sahifa: ").append(page.getNumber() + 1)
                .append(" / ").append(page.getTotalPages());

        return sb.toString();
    }


    public static String buildReviewSummary(ReviewStatsProjection stats) {
        DecimalFormat df = new DecimalFormat("#.##");
        String avgRating = stats.getAverageRating() == null ? "0.0" : df.format(stats.getAverageRating());

        return """
                💬 <b>Kurs sharhlari va fikrlar</b>
                
                📊 Umumiy baho: ⭐️ %s (%d ta sharh)
                📝 Jami fikrlar soni: %d
                👥 Faol ishtirokchilar: %d
                
                Quyidagi tugmalar orqali sharhlarni ko‘rishingiz yoki filtr qilishingiz mumkin 👇
                """.formatted(
                avgRating,
                stats.getTotalReviews(),
                stats.getTotalComments(),
                stats.getActiveStudents()
        );
    }

    public String buildCourseReviewStatsText(Page<CourseReviewStatsProjection> stats) {
        if (stats == null || stats.isEmpty()) {
            return """
                    📚 <b>Sizning kurslaringiz bo‘yicha sharhlar</b>
                    
                    ❌ Hozircha sharhlar mavjud emas.
                    """;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📚 <b>Sizning kurslaringiz bo‘yicha sharhlar</b>\n\n");

        int startIndex = 0;

        for (int i = 0; i < stats.getContent().size(); i++) {
            CourseReviewStatsProjection course = stats.getContent().get(i);

            int number = startIndex + i + 1;

            sb.append("%d️⃣ <b>%s</b>\n".formatted(number, course.getCourseTitle()));

            if (course.getTotalReviews() == null || course.getTotalReviews() == 0) {
                sb.append("⭐️ O‘rtacha baho: - \n 💬 Sharhlar mavjud emas\n\n");
            } else {
                String avgRating = String.format("%.2f", course.getAverageRating());
                sb.append("⭐️ O‘rtacha baho: %s \n 💬 %d ta sharh \n 📝 %d ta fikr\n\n"
                        .formatted(
                                avgRating,
                                course.getTotalReviews(),
                                course.getTotalComments()
                        )
                );
            }
        }

        return sb.toString();
    }


    public String formatUserPage(Page<UserProjection> usersPage) {
        StringBuilder sb = new StringBuilder();
        sb.append("👥 <b>O'quvchilar ro'yxati</b>\n");
        sb.append("Sahifa: ").append(usersPage.getNumber() + 1)
                .append(" / ").append(usersPage.getTotalPages()).append("\n\n");

        int index = usersPage.getNumber() * usersPage.getSize();

        for (UserProjection user : usersPage.getContent()) {
            index++;
            sb.append(index).append(". ");

            // Agar FirstName + LastName bo‘lsa
            if (user.getFirstName() != null || user.getLastName() != null) {
                sb.append("👤 <b>")
                        .append(user.getFirstName() != null ? user.getFirstName() : "")
                        .append(" ")
                        .append(user.getLastName() != null ? user.getLastName() : "")
                        .append("</b>\n");
            } else {
                sb.append("👤 <b>").append(user.getUsername()).append("</b>\n");
            }

            // Email
            if (user.getEmail() != null) {
                sb.append("📧 ").append(user.getEmail()).append("\n");
            }

            // Telefon
            if (user.getPhoneNumber() != null) {
                sb.append("📱 ").append(user.getPhoneNumber()).append("\n");
            }

            // Bio
            if (user.getBio() != null) {
                sb.append("📝 ").append(user.getBio()).append("\n");
            }

            // Reyting
            if (user.getRating() != null) {
                sb.append("⭐ Reyting: ").append(generateStars(user.getRating())).append("\n");
            }

            sb.append("〰️〰️〰️〰️〰️\n");
        }

        return sb.toString();
    }


    public String buildCourseDetail(CourseDetailDTO detail,
                                    CourseStudentStatsProjection stats) {
        StringBuilder sb = new StringBuilder();

        // 📘 Kitob emoji — random qilmoqchi bo‘lsangiz, o‘sha oldingi Utils.getRandomBookEmoji() dan foydalaning
        sb.append("📘 <b>").append(detail.getTitle()).append("</b>\n\n");

        if (detail.getDescription() != null) {
            sb.append("📝 ").append(detail.getDescription()).append("\n\n");
        }

        UserDTO userDTO = userService.read(detail.getId());
        CategoryDTO categoryDTO = categoryService.read(detail.getCategoryId());

        sb.append("👨‍🏫 <b>Mentor ID:</b> ").append(userDTO.getLastName()).append(" ").append(userDTO.getFirstName()).append("\n");
        sb.append("📂 <b>Kategoriya ID:</b> ").append(categoryDTO.getName()).append("\n");
        sb.append("📚 <b>Modullar soni:</b> ").append(detail.getModulesCount()).append("\n\n");

        // Statistikalar
        if (stats != null) {
            sb.append("📊 <b>Statistika:</b>\n");
            sb.append("👥 Unikal o‘quvchilar: <b>").append(stats.getUnique_student_count()).append("</b>\n");
            sb.append("💳 Jami sotuvlar: <b>").append(stats.getTotal_sales_count()).append("</b>\n\n");
        }

        // Review summary
        if (detail.getReviewSummary() != null) {
            sb.append("⭐️ Reyting: ").append(generateStars(detail.getReviewSummary().getAverageRating()))
                    .append(" (").append(detail.getReviewSummary().getCount()).append(" ta sharh)\n\n");
        }

        // Vaqt (agar kerak bo‘lsa human-readable formatga aylantirishingiz mumkin)
        sb.append("📅 Yaratilgan: ").append(formatDate(detail.getCreatedAt())).append("\n");

        return sb.toString();
    }

    private String buildStudentsDashboardText(long totalUniqueStudents, Page<CourseStudentStatsProjection> courseStatsPage) {
        // --- Kurslar ro'yxatini formatlaymiz ---
        List<CourseStudentStatsProjection> courseStats = courseStatsPage.getContent();
        StringBuilder coursesFormattedText = new StringBuilder();
        if (courseStats.isEmpty()) {
            coursesFormattedText.append("<i>Hozircha birorta ham kursingizga o'quvchilar yozilmagan.</i>");
        } else {

            for (CourseStudentStatsProjection stat : courseStats) {
                coursesFormattedText.append(String.format(
                        "%s <b>%s:</b> %d ta o'quvchi\n\n",
                        randomBookEmoji(),
                        escapeHtml(stat.getCourse_title()),
                        stat.getUnique_student_count() != null ? stat.getUnique_student_count() : 0 // null'dan himoya
                ));
            }
        }

        // --- Asosiy shablonni to'ldiramiz ---
        String dashboardTemplate = """
                🎓 <b>O'quvchilarim Sahifasi</b>
                
                Sizda jami <b>%d ta</b> unikal o'quvchi mavjud.
                
                〰️〰️〰️〰️〰️〰️〰️〰️〰️〰️〰️
                
                📊 <b>Kurslar bo'yicha statistika:</b>
                
                %s
                
                <i>Quyidagi amallardan birini tanlashingiz mumkin:</i>""";

        return String.format(
                dashboardTemplate,
                totalUniqueStudents,
                coursesFormattedText.toString().trim()
        );
    }


    private String buildLessonsPage(PageDTO<LessonResponseDTO> page) {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>📚 Modul darslari</b>\n\n");

        if (page.getContent().isEmpty()) {
            sb.append("❌ Hozircha dars mavjud emas.");
            return sb.toString();
        }

        for (LessonResponseDTO lesson : page.getContent()) {
            sb.append("🔹 <b>")
                    .append(lesson.getOrderIndex() != null ? lesson.getOrderIndex() : "-")
                    .append(")</b> ")
                    .append(lesson.getTitle())
                    .append("\n");
        }

        sb.append("\nSahifa: ").append(page.getPageNumber() + 1).append("/").append(page.getTotalPages());
        return sb.toString();
    }


    // --- Matn qurish ---
    private String buildModuleDetailText(ModuleDetailDTO m) {
        String title = safe(m.getTitle());
        String desc = safe(m.getDescription());
        String price = formatAmount(m.getPrice()); // "50 000 so'm"
        String lessons = m.getLessonCount() == null ? "0" : String.valueOf(m.getLessonCount());
        String enr = m.getModuleEnrollmentsCount() == null ? "-" : String.valueOf(m.getModuleEnrollmentsCount());
        String orderIx = m.getOrderIndex() == null ? "-" : String.valueOf(m.getOrderIndex() + 1);
        String created = formatDate(m.getCreatedAt());
        String updated = formatDate(m.getUpdatedAt());
        Long course = m.getCourseId() == null ? null : m.getCourseId();
        CourseDetailDTO courseDetailDTO = courseService.read(course);

        return """
                🔎 <b>Modul batafsil</b>
                
                🔗 Kurs nomi: <b>%s</b>
                
                🆔 ID: <code>%d</code>
                🏷️ Nomi: <b>%s</b>
                📖 Darslar soni: %s ta
                💵 Narx: %s
                👥 O‘quvchilar: %s
                🔢 Tartib: %s
                
                📝 Tavsif:
                %s
                
                🗓️ Yaratilgan: %s
                ♻️ Yangilangan: %s
                """.formatted(
                courseDetailDTO.getTitle(),
                m.getId(), title, lessons, price, enr, orderIx, desc,
                created, updated
        );
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String formatModulesPage(PageDTO<ModuleDetailDTO> modulePage) {
        StringBuilder sb = new StringBuilder();
        sb.append("📚 <b>Modullar ro‘yxati</b>\n\n");

        int number = 1;
        for (ModuleDetailDTO module : modulePage.getContent()) {
            sb.append("<b>").append(number++).append("</b>");
            sb.append(" <b> Nomi: ")
                    .append(module.getTitle())
                    .append("</b>\n")
                    .append("   📖 Darslar soni: ")
                    .append(module.getLessonCount() == null ? 0 : module.getLessonCount())
                    .append(" ta\n")
                    .append("\n\n");
        }

        sb.append("Sahifa: ")
                .append(modulePage.getPageNumber() + 1)
                .append(" / ")
                .append(modulePage.getTotalPages());

        return sb.toString();
    }


    private void instructorMyCourseViewId(Long chatId, Integer messageId, String[] data) {
        Long courseId = Long.valueOf(data[3]);
        CourseDetailDTO courseDetailDTO = courseService.read(courseId);
        CategoryDTO categoryDTO = categoryService.read(courseDetailDTO.getCategoryId());
        UserDTO userDTO = userService.read(courseDetailDTO.getInstructorId());

        String text = String.format("""
                        📘 <b>Kurs Tafsilotlari</b>
                        
                        🔹 <b>Nomi:</b> %s \s
                        🔹 <b>Slug:</b> %s \s
                        🔹 <b>Kategoriya:</b> %s \s
                        🔹 <b>Mentor :</b> %s \s
                        
                        📖 <b>Tavsif:</b> \s
                        %s \s
                        
                        📊 <b>Statistika:</b> \s
                        - 📚 <b>Modullar:</b> %s \s
                        - ⭐ <b>Baholash:</b> %s  \s
                        - 📝 <b>Sharhlar:</b> %s ta \s
                        
                        🕒 <b>Qo‘shilgan sana:</b> %s \s
                        ♻️ <b>Yangilangan sana:</b> %s \s
                        
                        ✅ <b>Status:</b> %s
                        """,
                courseDetailDTO.getTitle(),
                courseDetailDTO.getSlug(),
                categoryDTO.getName(),
                userDTO.getFirstName() + " " + userDTO.getLastName(),
                courseDetailDTO.getDescription(),
                courseDetailDTO.getModulesCount(),
                generateStars(courseDetailDTO.getReviewSummary().getAverageRating()),
                courseDetailDTO.getReviewSummary().getCount(),
                formatDate(courseDetailDTO.getCreatedAt()),
                formatDate(courseDetailDTO.getUpdatedAt()),
                courseDetailDTO.isSuccess() ? "Faol" : "Faol emas"
        );

        String backButton = String.join(":",
                Utils.CallbackData.MY_COURSE_PREFIX,
                courseDetailDTO.isSuccess() ? Utils.CallbackData.ACTION_SUCCESS : Utils.CallbackData.ACTION_DRAFT,
                Utils.CallbackData.ACTION_PAGE,
                "0"
        );
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.instructorViewCourses(courseDetailDTO.getId(), backButton, courseDetailDTO.getModulesCount());
        bot.myExecute(sendMsg.editMessage(chatId, messageId, text, inlineKeyboardMarkup));
    }

    private void instructorMyCoursesSuccess(Long chatId, User user, Integer messageId, String[] data) {
        int courseCount = courseRepository.countByInstructorIdAndSuccess(user.getId(), true);
        if (courseCount == 0) {
            String fullCourseText = """
                    📚 <b>Sizda hali tugatilgan kurslar mavjud emas!</b>
                    
                    ✍️ <i>Iltimos, avval kurs yarating va uni to‘liq tugating.</i>
                    
                    ➕ <b>Yangi kurs yaratish orqali o‘quvchilaringizga foyda ulashing!</b>
                    """;

            InlineKeyboardMarkup keyboard = inlineKeyboardService.instructorNoDraftCourse();
            bot.myExecute(sendMsg.editMessage(chatId, messageId, fullCourseText, keyboard));
            return;
        }
        int pageNumber = Integer.parseInt(data[3]);

        Sort sort = Sort.by(AbsDateEntity.Fields.createdAt);
        PageRequest pageRequest = PageRequest.of(pageNumber, PAGE_SIZE, sort);
        Page<Course> coursePage = courseRepository.findAllByInstructorIdAndSuccess(user.getId(), true, pageRequest);

        instructorMyCoursesViewBtn(chatId, messageId, coursePage, true);
    }

    private void instructorMyCourseDraft(Long chatId, User user, Integer messageId, String[] data) {
        int courseCount = courseRepository.countByInstructorIdAndSuccess(user.getId(), false);
        if (courseCount == 0) {
            String draftCourseText = """
                    🛠 <b>Sizda tugallanmagan kurslar mavjud emas!</b>
                    
                    📌 <i>Boshlangan kurslarni tugatish yoki yangi kurs yaratish mumkin.</i>
                    """;

            InlineKeyboardMarkup keyboard = inlineKeyboardService.instructorNoDraftCourse();
            bot.myExecute(sendMsg.editMessage(chatId, messageId, draftCourseText, keyboard));
            return;
        }

        int pageNumber = Integer.parseInt(data[3]);

        PageRequest pageRequest = PageRequest.of(pageNumber, PAGE_SIZE);
        Page<Course> coursePage = courseRepository.findAllByInstructorIdAndSuccessNative(user.getId(), false, pageRequest);

        instructorMyCoursesViewBtn(chatId, messageId, coursePage, false);
    }

    private void instructorMyCoursesViewBtn(Long chatId, Integer messageId, Page<Course> coursePage,
                                            boolean successOrDraft) {
        List<Course> courses = coursePage.getContent();

        StringBuilder text = new StringBuilder("🎓 Sizning kurslaringiz\n\n");

        int index = 1;
        for (Course course : courses) {
            int purchasedCount = courseRepository.countPurchasedUsers(course.getId());
            int subscribedCount = courseRepository.countSubscribedUsers(course.getId());

            text.append(String.format("""
                            %d) 📘 %s
                            📅 Yaratilgan sana: %s
                            📦 Modullar soni: %d
                            👥 Sotib olganlar: %d
                            🔔 Obunachilar: %d
                            
                            """,
                    index++,
                    course.getTitle(),
                    formatDate(course.getCreatedAt()),
                    course.getModules().size(),
                    purchasedCount,
                    subscribedCount
            ));
        }

        text.append(String.format("📄 Sahifa: %d / %d", coursePage.getPageable().getPageNumber() + 1, coursePage.getTotalPages()));
        String backButton = String.join(":", Utils.CallbackData.MY_COURSE_PREFIX, Utils.CallbackData.ACTION_BACK);
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.myViewCourses(coursePage, backButton, successOrDraft);
        try {
            bot.myExecute(sendMsg.editMessage(chatId, messageId, text.toString(), inlineKeyboardMarkup));
        } catch (Exception e) {
            bot.myExecute(sendMsg.deleteMessage(chatId, messageId));
            bot.myExecute(sendMsg.sendMessage(chatId, text.toString(), inlineKeyboardMarkup));
        }
    }


    private String formatDate(Timestamp timestamp) {
        if (timestamp == null) {
            return "-";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return timestamp.toLocalDateTime().format(formatter);
    }

    private String formatDate(Long millis) {
        if (millis == null) {
            return "-";
        }
        Instant instant = Instant.ofEpochMilli(millis);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                .withZone(ZoneId.systemDefault());
        return formatter.format(instant);
    }


    private String formatAmount(Long amount) {
        if (amount == null || amount == 0) {
            return "0 so'm";
        }
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount / 100) + " so'm";
    }

    private String generateStars(int count) {
        if (count < 1) return "☆☆☆☆☆"; // Reyting yo'q bo'lsa
        if (count > 5) count = 5; // 5 dan oshmasligi kerak

        String filledStar = "⭐";
        String emptyStar = "☆";

        return filledStar.repeat(count) + emptyStar.repeat(5 - count);
    }

    private String generateStars(double rating) {
        if (rating <= 0) return "☆☆☆☆☆";
        if (rating > 5) rating = 5;

        int filled = (int) rating; // to‘liq yulduzlar
        boolean half = (rating - filled) >= 0.5; // yarim yulduz bormi?
        int empty = 5 - filled - (half ? 1 : 0);

        String filledStar = "⭐";
        String halfStar = "✩"; // yoki "⭐️" / "✨"
        String emptyStar = "☆";

        return filledStar.repeat(filled)
                + (half ? halfStar : "")
                + emptyStar.repeat(empty);
    }

    /**
     * Matndagi maxsus HTML belgilarini xavfsiz holatga keltiradi.
     */
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
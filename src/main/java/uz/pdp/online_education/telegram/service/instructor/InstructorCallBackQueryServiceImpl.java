package uz.pdp.online_education.telegram.service.instructor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import uz.pdp.online_education.model.Abs.AbsDateEntity;
import uz.pdp.online_education.model.Category;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.PageDTO;
import uz.pdp.online_education.payload.category.CategoryDTO;
import uz.pdp.online_education.payload.content.attachmentContent.AttachmentDTO;
import uz.pdp.online_education.payload.course.CourseCreateDTO;
import uz.pdp.online_education.payload.course.CourseDetailDTO;
import uz.pdp.online_education.payload.lesson.LessonCreatDTO;
import uz.pdp.online_education.payload.lesson.LessonResponseDTO;
import uz.pdp.online_education.payload.module.ModuleCreateDTO;
import uz.pdp.online_education.payload.module.ModuleDetailDTO;
import uz.pdp.online_education.payload.review.ReviewSummaryDTO;
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


    @Override
    public void handleCallback(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String queryData = callbackQuery.getData();
        String callbackQueryId = callbackQuery.getId();

        UserState userState = telegramUserService.getUserState(chatId);
        User user = telegramUserRepository.findByChatId(chatId).orElseThrow(() -> new RuntimeException("User not found for callback. ChatID: " + chatId)).getUser();

        bot.myExecute(new AnswerCallbackQuery(callbackQuery.getId()));

        try {
            String[] data = queryData.split(":");
            String prefix = data[0];

            switch (prefix) {
                case DELETED -> bot.myExecute(sendMsg.deleteMessage(chatId, messageId));
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
            bot.myExecute(sendMsg.sendMessage(chatId, messageService.getMessage(BotMessage.ERROR_UNEXPECTED)));
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
            case Utils.CallbackData.ACTION_BACK ->
                    processMessageService.instructorMyCourseHandle(chatId, user, messageId);
            case Utils.CallbackData.ACTION_SUCCESS -> instructorMyCoursesSuccess(chatId, user, messageId, data);
            case Utils.CallbackData.ACTION_DRAFT -> instructorMyCourseDraft(chatId, user, messageId, data);
            case Utils.CallbackData.ACTION_COURSE -> instructorMyCourseViewId(chatId, messageId, data);
        }
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

        }


        // ... Logika
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
                
                📝 Tavsif:
                %s
                """.formatted(
                l.getId(), title, order, free, module,
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

    private void instructorActionEditHandle(Long chatId, User user, Integer messageId, String[] data, String queryData) {
        // ... Logika
    }

    private void instructorActionDeleteHandle(Long chatId, User user, Integer messageId, String[] data, String queryData) {
        // ... Logika
    }

    private void instructorActionViewHandle(Long chatId, User user, Integer messageId, String[] data, String queryData) {


        String type = data[1];

        switch (type) {
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
                String backButton = String.join(":", ACTION_VIEW, MODULE_PREFIX, moduleDetailDTO.getCourseId().toString(), ACTION_PAGE, "0");
                InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.instructorViewModule(
                        moduleId,
                        backButton,
                        moduleDetailDTO.getLessonCount()
                );
                bot.myExecute(sendMsg.editMessage(chatId, messageId, built, inlineKeyboardMarkup));


            }
        }


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

    public String formatModulesPage(PageDTO<ModuleDetailDTO> modulePage) {
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

    private void instructorMyCoursesViewBtn(Long chatId, Integer messageId, Page<Course> coursePage, boolean successOrDraft) {
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
        bot.myExecute(sendMsg.editMessage(chatId, messageId, text.toString(), inlineKeyboardMarkup));
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
}
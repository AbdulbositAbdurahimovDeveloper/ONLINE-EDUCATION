package uz.pdp.online_education.telegram.service.instructor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import uz.pdp.online_education.enums.Role;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.model.UserProfile;
import uz.pdp.online_education.payload.category.CategoryDTO;
import uz.pdp.online_education.payload.content.attachmentContent.AttachmentDTO;
import uz.pdp.online_education.payload.course.CourseDetailDTO;
import uz.pdp.online_education.payload.course.CourseUpdateDTO;
import uz.pdp.online_education.payload.text.TextContentCreateDTO;
import uz.pdp.online_education.payload.user.UserDTO;
import uz.pdp.online_education.repository.*;
import uz.pdp.online_education.service.interfaces.*;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.config.controller.OnlineEducationBot;
import uz.pdp.online_education.telegram.enums.BotMessage;
import uz.pdp.online_education.telegram.enums.UserState;
import uz.pdp.online_education.telegram.mapper.SendMsg;
import uz.pdp.online_education.telegram.model.TelegramUser;
import uz.pdp.online_education.telegram.service.RedisTemporaryDataService;
import uz.pdp.online_education.telegram.service.RoleService;
import uz.pdp.online_education.telegram.service.TelegramUserService;
import uz.pdp.online_education.telegram.service.instructor.template.InstructorInlineKeyboardService;
import uz.pdp.online_education.telegram.service.instructor.template.InstructorProcessMessageService;
import uz.pdp.online_education.telegram.service.instructor.template.InstructorReplyKeyboardService;
import uz.pdp.online_education.telegram.service.message.MessageService;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

import static uz.pdp.online_education.telegram.Utils.CallbackData.*;
import static uz.pdp.online_education.telegram.Utils.ReplyButtons.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstructorProcessMessageServiceImpl implements InstructorProcessMessageService {

    private final CategoryService categoryService;
    private final AttachmentService attachmentService;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final TextContentService textContentService;
    private final CourseService courseService;
    private final UserService userService;
    @Value("${telegram.bot.channel-id}")
    private String CHANNEL_ID;

    private final RoleService roleService;
    private final SendMsg sendMsg;
    private final MessageService messageService;
    private final OnlineEducationBot bot;
    private final InstructorReplyKeyboardService replyKeyboardService;
    private final InstructorInlineKeyboardService inlineKeyboardService;
    private final TelegramUserRepository telegramUserRepository;
    private final ModuleEnrollmentRepository moduleEnrollmentRepository;
    private final CourseRepository courseRepository;
    private final PaymentRepository paymentRepository;
    private final TelegramUserService telegramUserService;
    private final RedisTemporaryDataService redisTemporaryDataService;

    /**
     * @param message
     * @return
     */
    @Override
    public void handleMessage(Message message) {
        Long chatId = message.getChatId();

        String text = message.getText();

        TelegramUser telegramUser = getOrCreateTelegramUser(chatId);
        if (telegramUser.getUser() == null) {
            bot.myExecute(sendMsg.sendMessage(chatId, "Please authenticate first."));
            return;
        }

        User user = telegramUser.getUser();
        UserProfile profile = user.getProfile();

        Role currentRole = roleService.getUserRole(chatId);

        UserState userState = telegramUserService.getUserState(chatId);

        if (message.hasText() && text.equals(Utils.CANCEL)) {
            telegramUserService.updateUserState(chatId, UserState.NONE);
            bot.myExecute(sendMsg.sendMessage(chatId, "Can't cancel the instructor."));
            return;
        }

        switch (userState) {
            case AWAITING_MODULE_TITLE:
            case AWAITING_MODULE_DESCRIPTION:
            case AWAITING_MODULE_PRICE:
                handleModuleCreationStep(chatId, text, userState);
                break;
            case AWAITING_COURSE_TITLE:
            case AWAITING_COURSE_DESCRIPTION:
            case AWAITING_COURSE_THUMBNAIL:
            case AWAITING_COURSE_CATEGORY_CHOICE:
                handleCourseCreationStep(chatId, message, userState);
                break;
            case AWAITING_LESSON_TITLE:
            case AWAITING_LESSON_DESCRIPTION:
            case AWAITING_LESSON_IS_FREE:
            case AWAITING_LESSON_CONFIRMATION:
                handleLessonCreationStep(chatId, message, userState);
                break;
            case AWAITING_ATTACHMENT_CONTENT:
            case AWAITING_TEXT_CONTENT:
                handleInstructorContent(chatId, message, userState);
            case AWAITING_EDIT_COURSE_TITLE:
            case AWAITING_EDIT_COURSE_DESCRIPTION:
            case AWAITING_EDIT_COURSE_THUMBNAIL:
            case AWAITING_EDIT_COURSE_CONFIRMATION:
                handleCourseEditionStep(chatId, message, userState, user);


        }

        if (!message.hasText()) {
            return;
        }
        switch (text) {
            case Utils.START -> startMessage(chatId);
            case Utils.DASHBOARD -> dashboardMessage(user, profile, chatId);
            case INSTRUCTOR_MY_COURSES -> instructorMyCourseHandle(chatId, user, null);
            case INSTRUCTOR_MY_STUDENTS -> instructorMyStudentHandle(chatId, user);
            case INSTRUCTOR_REVIEWS -> instructorReviewsHandle(chatId, user);
            case INSTRUCTOR_MY_REVENUE -> instructorMyRevenueHandle(chatId, user);
        }

    }

    private void handleCourseEditionStep(Long chatId, Message message, UserState userState, User user) {

        String processKey = String.join(":", ACTION_EDIT, ACTION_COURSE, chatId.toString());
        Optional<Map<String, Object>> allFields = redisTemporaryDataService.getAllFields(processKey);
        if (allFields.isPresent()) {

            Map<String, Object> courseFields = allFields.get();
            Long id = Long.valueOf(courseFields.get(COURSE_ID).toString());


            switch (userState) {
                case AWAITING_EDIT_COURSE_TITLE -> {
                    if (message.hasText()) {
                        String title = message.getText();

                        if (title.length() < 5 || title.length() > 150) {
                            bot.myExecute(sendMsg.sendMessage(chatId, "Sarlavha uzunligi 5 tadan kop 150 tadan kam bo`lsin"));
                        } else {
                            CourseUpdateDTO courseUpdateDTO = new CourseUpdateDTO();
                            courseUpdateDTO.setTitle(title);

                            CourseDetailDTO courseDetailDTO = courseService.update(id, courseUpdateDTO, user);

                            CategoryDTO categoryDTO = categoryService.read(courseDetailDTO.getCategoryId());
                            UserDTO userDTO = userService.read(courseDetailDTO.getInstructorId());

                            telegramUserService.updateUserState(chatId, UserState.NONE);
                            bot.myExecute(sendMsg.sendMessage(chatId, "Kurs nomi ozgartirildi :<b>" + title + "</b>"));
                            buildCourseDetails(chatId, courseDetailDTO, categoryDTO, userDTO, id);
                        }
                    } else {
                        bot.myExecute(sendMsg.sendMessage(chatId, "Iltimos sarlavha text korinishida kirgizing!!!"));
                    }
                }
                case AWAITING_EDIT_COURSE_DESCRIPTION -> {

                    if (message.hasText()) {
                        String description = message.getText();

                        if (description.length() > 1000) {
                            bot.myExecute(sendMsg.sendMessage(chatId, "Tavsif uzunligi 1000 ta belgidan kop bolmasun"));
                        } else {
                            CourseUpdateDTO courseUpdateDTO = new CourseUpdateDTO();
                            courseUpdateDTO.setDescription(description);

                            CourseDetailDTO courseDetailDTO = courseService.update(id, courseUpdateDTO, user);

                            CategoryDTO categoryDTO = categoryService.read(courseDetailDTO.getCategoryId());
                            UserDTO userDTO = userService.read(courseDetailDTO.getInstructorId());

                            telegramUserService.updateUserState(chatId, UserState.NONE);
                            bot.myExecute(sendMsg.sendMessage(chatId, "Kurs tavsifi ozgartirildi :<b>" + description + "</b>"));
                            buildCourseDetails(chatId, courseDetailDTO, categoryDTO, userDTO, id);
                        }
                    } else {
                        bot.myExecute(sendMsg.sendMessage(chatId, "Iltimos sarlavha text korinishida kirgizing!!!"));
                    }
                }
                case AWAITING_EDIT_COURSE_THUMBNAIL -> {
                    if (message.hasPhoto()) {

                        AttachmentDTO attachmentDTO = attachmentService.saveTg(message.getPhoto());
                        CourseUpdateDTO courseUpdateDTO = new CourseUpdateDTO();
                        courseUpdateDTO.setThumbnailId(attachmentDTO.getId());

                        CourseDetailDTO courseDetailDTO = courseService.update(id, courseUpdateDTO, user);

                        CategoryDTO categoryDTO = categoryService.read(courseDetailDTO.getCategoryId());
                        UserDTO userDTO = userService.read(courseDetailDTO.getInstructorId());

                        telegramUserService.updateUserState(chatId, UserState.NONE);
                        bot.myExecute(sendMsg.sendMessage(chatId, "Kurs uchun yangi :<b> Rasm yuklandi </b>"));
                        buildCourseDetails(chatId, courseDetailDTO, categoryDTO, userDTO, id);

                    } else {

                        bot.myExecute(sendMsg.sendMessage(chatId, "Kurs uchun faqat rasm yuklashingiz mumkin!!!"));

                    }
                }
            }
        }

    }

    private void buildCourseDetails(Long chatId, CourseDetailDTO courseDetailDTO, CategoryDTO categoryDTO, UserDTO userDTO, Long id) {
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
                ACTION_COURSE,
                ACTION_VIEW,
                id.toString(),
                Utils.CallbackData.ACTION_PAGE,
                "0"
        );
        String tgFile = courseDetailDTO.getThumbnailUrl();
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.instructorEditCourses(courseDetailDTO, backButton);

        if (tgFile == null) {
            bot.myExecute(sendMsg.sendMessage(chatId, text, inlineKeyboardMarkup));
        } else {
            AttachmentDTO attachmentDTO = attachmentService.read(Long.valueOf(tgFile));
            bot.myExecute(sendMsg.sendPhoto(chatId, attachmentDTO.getTelegramFileId(), text, inlineKeyboardMarkup));
            telegramUserService.updateUserState(chatId, UserState.AWAITING_COURSE_CONFIRMATION);
        }
    }

    private void handleInstructorContent(Long chatId, Message message, UserState userState) {

        String processKey = String.join(":", ACTION_ADD, LESSON_PREFIX, chatId.toString());

        Optional<Map<String, Object>> allFields = redisTemporaryDataService.getAllFields(processKey);
        if (allFields.isPresent()) {

            Map<String, Object> stringObjectMap = allFields.get();

            Long lessonId = Long.valueOf(stringObjectMap.get(Utils.CallbackData.LESSON_ID).toString());

            switch (userState) {
                case AWAITING_ATTACHMENT_CONTENT -> {
                    if (!message.hasVideo()) {
                        bot.myExecute(sendMsg.sendMessage(chatId, "Please enter the video URL."));
                        return;
                    }

                    Video video = message.getVideo();
                    attachmentService.saveTgVideoAsync(video, chatId, lessonId);


                }
                case AWAITING_TEXT_CONTENT -> {

                    if (!message.hasText()) {
                        bot.myExecute(sendMsg.sendMessage(chatId, "Please enter the text URL."));
                        return;
                    }

                    String text = message.getText();

                    if (text == null) {
                        bot.myExecute(sendMsg.sendMessage(chatId, "Please enter the text URL."));
                        return;
                    }

                    textContentService.create(new TextContentCreateDTO(lessonId, text));

                }
            }

        }
    }

    private void handleLessonCreationStep(Long chatId, Message message, UserState userState) {

        String processKey = String.join(":", ACTION_ADD, LESSON_PREFIX, chatId.toString());

        String text = message.getText();

        switch (userState) {
            case AWAITING_LESSON_TITLE -> {

                Optional<Map<String, Object>> allFields = redisTemporaryDataService.getAllFields(processKey);

                if (allFields.isPresent()) {
                    Map<String, Object> stringObjectMap = allFields.get();
                    Long moduleId = Long.valueOf(stringObjectMap.get(Utils.CallbackData.MODULE_ID).toString());

                    if (lessonRepository.existsByTitleAndModuleId(text, moduleId)) {
                        bot.myExecute(sendMsg.sendMessage(chatId, "Lesson already exists."));
                        return;
                    }
                    redisTemporaryDataService.addField(processKey, TITLE, text);
                    telegramUserService.updateUserState(chatId, UserState.AWAITING_LESSON_DESCRIPTION);
                    bot.myExecute(sendMsg.sendMessage(chatId, "Dars uchun tavsif kiriting"));
                }

            }
            case AWAITING_LESSON_DESCRIPTION -> {
                if (text != null && text.equals("/skip")) {
                    redisTemporaryDataService.addField(processKey, DESCRIPTION, "");
                    String skippedText = messageService.getMessage(BotMessage.INSTRUCTOR_COURSE_CREATE_DESCRIPTION_SKIPPED);
                    bot.myExecute(sendMsg.sendMessage(chatId, skippedText));
                } else {
                    // Validatsiya
                    if (text == null || text.length() > 1000) {
                        String errorText = messageService.getMessage(BotMessage.INSTRUCTOR_COURSE_CREATE_VALIDATION_DESC_ERROR);
                        bot.myExecute(sendMsg.sendMessage(chatId, errorText));
                        return;
                    }
                    // Ma'lumotni Redis'ga saqlaymiz
                    redisTemporaryDataService.addField(processKey, DESCRIPTION, text);
                    String acceptedText = messageService.getMessage(BotMessage.INSTRUCTOR_COURSE_CREATE_DESCRIPTION_ACCEPTED);
                    bot.myExecute(sendMsg.sendMessage(chatId, acceptedText));
                }

                // Keyingi bosqichga o'tkazamiz
                telegramUserService.updateUserState(chatId, UserState.AWAITING_COURSE_THUMBNAIL);
                String nextPrompt = messageService.getMessage(BotMessage.INSTRUCTOR_COURSE_CREATE_THUMBNAIL_PROMPT);
                InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.isFree();
                bot.myExecute(sendMsg.sendMessage(chatId, "darslik turi qanday boladi pullik yoki tekinmi", inlineKeyboardMarkup));
            }
        }

    }

    /**
     * Kurs yaratish jarayonining bosqichlarini boshqaradigan markaziy metod.
     * Bu metod MessageHandler ichida, foydalanuvchining UserState'iga qarab chaqiriladi.
     *
     * @param chatId    Foydalanuvchining chat ID'si.
     * @param message   Telegramdan kelgan to'liq xabar obyekti.
     * @param userState Foydalanuvchining joriy holati.
     */
    private void handleCourseCreationStep(Long chatId, Message message, UserState userState) {


        String processKey = String.join(":", ACTION_ADD, ACTION_COURSE, chatId.toString());

        // 2. Joriy bosqichga qarab kerakli amalni bajaramiz.
        switch (userState) {
            case AWAITING_COURSE_TITLE -> {
                String title = message.getText();

                // Validatsiya
                if (title == null || title.length() < 3 || title.length() > 150) {
                    String errorText = messageService.getMessage(BotMessage.INSTRUCTOR_COURSE_CREATE_VALIDATION_TITLE_ERROR);
                    bot.myExecute(sendMsg.sendMessage(chatId, errorText));
                    return; // Holatni o'zgartirmaymiz, foydalanuvchi qayta urinadi.
                }

                if (courseRepository.existsByTitle(title)) {
                    bot.myExecute(sendMsg.sendMessage(chatId, "Title already exists"));
                    return;
                }

                // Ma'lumotni Redis'ga saqlaymiz
                redisTemporaryDataService.addField(processKey, TITLE, title);

                // Keyingi bosqichga o'tkazamiz
                telegramUserService.updateUserState(chatId, UserState.AWAITING_COURSE_DESCRIPTION);
                String nextPrompt = messageService.getMessage(BotMessage.INSTRUCTOR_COURSE_CREATE_DESCRIPTION_PROMPT);
                bot.myExecute(sendMsg.sendMessage(chatId, nextPrompt));
            }

            case AWAITING_COURSE_DESCRIPTION -> {
                String description = message.getText();

                // Foydalanuvchi bu qadamni o'tkazib yuborishi mumkin
                if (description != null && description.equals("/skip")) {
                    redisTemporaryDataService.addField(processKey, DESCRIPTION, "");
                    String skippedText = messageService.getMessage(BotMessage.INSTRUCTOR_COURSE_CREATE_DESCRIPTION_SKIPPED);
                    bot.myExecute(sendMsg.sendMessage(chatId, skippedText));
                } else {
                    // Validatsiya
                    if (description == null || description.length() > 1000) {
                        String errorText = messageService.getMessage(BotMessage.INSTRUCTOR_COURSE_CREATE_VALIDATION_DESC_ERROR);
                        bot.myExecute(sendMsg.sendMessage(chatId, errorText));
                        return;
                    }
                    // Ma'lumotni Redis'ga saqlaymiz
                    redisTemporaryDataService.addField(processKey, DESCRIPTION, description);
                    String acceptedText = messageService.getMessage(BotMessage.INSTRUCTOR_COURSE_CREATE_DESCRIPTION_ACCEPTED);
                    bot.myExecute(sendMsg.sendMessage(chatId, acceptedText));
                }

                // Keyingi bosqichga o'tkazamiz
                telegramUserService.updateUserState(chatId, UserState.AWAITING_COURSE_THUMBNAIL);
                String nextPrompt = messageService.getMessage(BotMessage.INSTRUCTOR_COURSE_CREATE_THUMBNAIL_PROMPT);
                bot.myExecute(sendMsg.sendMessage(chatId, nextPrompt));
            }

            case AWAITING_COURSE_THUMBNAIL -> {
                // Rasm yuborilganini tekshiramiz
                if (!message.hasPhoto()) {
                    String errorText = messageService.getMessage(BotMessage.INSTRUCTOR_COURSE_CREATE_VALIDATION_PHOTO_ERROR);
                    bot.myExecute(sendMsg.sendMessage(chatId, errorText));
                    return; // Holat o'zgarmaydi.
                }

                // Eng katta o'lchamdagi rasmni olamiz (eng yaxshi sifat)
                PhotoSize photo = message.getPhoto().stream()
                        .max(Comparator.comparing(PhotoSize::getFileSize))
                        .orElse(null);

                if (photo == null) {
                    // Bu holat deyarli yuz bermaydi, lekin himoya uchun kerak
                    return;
                }

                String fileId = photo.getFileId();

//                bot.myExecute(sendMsg.sendPhoto(CHANNEL_ID, new InputFile(fileId)));

                AttachmentDTO attachmentDTO = attachmentService.saveTg(message.getPhoto());


                // Ma'lumotni Redis'ga saqlaymiz
                redisTemporaryDataService.addField(processKey, THUMBNAIL_ID, attachmentDTO.getId());

                // Keyingi bosqichga o'tamiz
                telegramUserService.updateUserState(chatId, UserState.AWAITING_COURSE_CONFIRMATION);

                // Barcha ma'lumotlarni yig'ib, tasdiqlash xabarini yuboramiz
                sendCourseConfirmationMessage(chatId, processKey);
            }
        }
    }

    /**
     * Kurs ma'lumotlarini tasdiqlash uchun xabar yuboradi.
     */
    private void sendCourseConfirmationMessage(Long chatId, String processKey) {
        Optional<Map<String, Object>> allFieldsOpt = redisTemporaryDataService.getAllFields(processKey);

        if (allFieldsOpt.isPresent()) {
            Map<String, Object> courseData = allFieldsOpt.get();

            String categoryId = String.valueOf(courseData.get(CATEGORY_ID));
            // Bu yerda categoryRepository.findById(categoryId) orqali kategoriya nomini olish kerak.
            String categoryName = categoryService.read(Long.valueOf(categoryId)).getName();
            String title = String.valueOf(courseData.get(TITLE));
            String description = String.valueOf(courseData.get(DESCRIPTION));
            Long thumbnailId = Long.valueOf(String.valueOf(courseData.get(Utils.CallbackData.THUMBNAIL_ID)));

            AttachmentDTO attachmentDTO = attachmentService.read(thumbnailId);

            String confirmationText = messageService.getMessage(
                    BotMessage.INSTRUCTOR_COURSE_CREATE_CONFIRMATION_PROMPT,
                    categoryName,
                    title,
                    description
            );

            InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.succesOrDraftBtn(processKey, ACTION_COURSE);
            bot.myExecute(sendMsg.sendPhoto(chatId, attachmentDTO.getTelegramFileId(), confirmationText, inlineKeyboardMarkup));
            telegramUserService.updateUserState(chatId, UserState.AWAITING_COURSE_CONFIRMATION);
        }
    }

    /**
     * Modul yaratish jarayonining bosqichlarini boshqaradigan markaziy metod.
     */
    private void handleModuleCreationStep(Long chatId, String text, UserState currentState) {

        String processKey = String.join(":", ACTION_ADD, ACTION_MODULE, chatId.toString());

        switch (currentState) {
            case AWAITING_MODULE_TITLE -> {
                if (text.length() < 3 || text.length() > 200) {
                    bot.myExecute(sendMsg.sendMessage(chatId, "Sarlavha uzunligi 3 dan 200 gacha bo'lishi kerak."));
                    return;
                }
                if (moduleRepository.existsByTitle(text)) {
                    bot.myExecute(sendMsg.sendMessage(chatId, "Bunday sarlavha band qilingan"));
                    return;
                }

                redisTemporaryDataService.addField(processKey, TITLE, text);

                telegramUserService.updateUserState(chatId, UserState.AWAITING_MODULE_DESCRIPTION);
                bot.myExecute(sendMsg.sendMessage(chatId, messageService.getMessage(BotMessage.INSTRUCTOR_MODULE_CREATE_DESCRIPTION)));
            }
            case AWAITING_MODULE_DESCRIPTION -> {
                if (text.length() > 1000) {
                    bot.myExecute(sendMsg.sendMessage(chatId, "Tavsif 1000 ta belgidan oshmasligi kerak."));
                    return;
                }

                redisTemporaryDataService.addField(processKey, DESCRIPTION, text);

                telegramUserService.updateUserState(chatId, UserState.AWAITING_MODULE_PRICE);
                bot.myExecute(sendMsg.sendMessage(chatId, messageService.getMessage(BotMessage.INSTRUCTOR_MODULE_CREATE_PRICE)));
            }
            case AWAITING_MODULE_PRICE -> {
                Long priceValue;
                try {
                    priceValue = Long.parseLong(text);
                    if (priceValue < 0) {
                        bot.myExecute(sendMsg.sendMessage(chatId, "Narx manfiy bo'lishi mumkin emas. Qaytadan kiriting."));
                        return;
                    }
                } catch (NumberFormatException e) {
                    bot.myExecute(sendMsg.sendMessage(chatId, "Narxni raqam bilan to'g'ri kiriting. Masalan: 500000"));
                    return;
                }

                redisTemporaryDataService.addField(processKey, PRICE, String.valueOf(priceValue));

                sendConfirmationMessage(chatId, processKey);

                telegramUserService.updateUserState(chatId, UserState.AWAITING_MODULE_CONFIRMATION);
            }
        }
    }

    /**
     * Yig'ilgan ma'lumotlar asosida tasdiqlash xabarini yasaydi va yuboradi.
     */
    private void sendConfirmationMessage(Long chatId, String processKey) {
        Optional<Map<String, Object>> allFieldsOptional = redisTemporaryDataService.getAllFields(processKey);

        if (allFieldsOptional.isPresent()) {
            Map<String, Object> moduleFields = allFieldsOptional.get();

            Long courseId = Long.parseLong(String.valueOf(moduleFields.get(COURSE_ID)));
            Long price = Long.parseLong(String.valueOf(moduleFields.get(PRICE)));
            String title = String.valueOf(moduleFields.get(TITLE));
            String description = String.valueOf(moduleFields.get(DESCRIPTION));

            String confirmationText = String.format("""
                    Iltimos, ma'lumotlarni tasdiqlang:
                    
                    🔹 Kurs ID: %d
                    📘 Sarlavha: %s
                    📖 Tavsif: %s
                    💰 Narx: %,d so'm
                    """, courseId, title, description, price);

            InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.succesOrDraftBtn(processKey, ACTION_MODULE);
            bot.myExecute(
                    sendMsg.sendMessage(
                            chatId,
                            confirmationText,
                            inlineKeyboardMarkup
                    )
            );
        } else {
            bot.myExecute(sendMsg.sendMessage(chatId, "Xatolik yuz berdi. Jarayon ma'lumotlari topilmadi."));
            telegramUserService.updateUserState(chatId, UserState.NONE);
        }
    }

    @Override
    public void instructorMyCourseHandle(Long chatId, User user, Integer messageId) {


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
        if (messageId != null)
            bot.myExecute(sendMsg.editMessage(chatId, messageId, text, inlineKeyboardMarkup));
        else
            bot.myExecute(sendMsg.sendMessage(chatId, text, inlineKeyboardMarkup));


    }

    private void instructorMyStudentHandle(Long chatId, User user) {

    }

    private void instructorReviewsHandle(Long chatId, User user) {

    }

    private void instructorMyRevenueHandle(Long chatId, User user) {

    }

    /**
     * @param user      The authenticated User object.
     * @param chatId    The chat ID where the dashboard should be shown.
     * @param messageId The ID of the message to be edited.
     */
    @Override
    @Transactional(readOnly = true)
    public void showDashboard(User user, Long chatId, Integer messageId) {
        UserProfile profile = user.getProfile();
        String dashboardText = prepareInstructorDashboardText(user, profile);
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.dashboardMenu();
        bot.myExecute(sendMsg.editMessage(chatId, messageId, dashboardText, inlineKeyboardMarkup));
    }


    /**
     * Sends a new message containing the student's dashboard.
     */
    private void dashboardMessage(User user, UserProfile profile, Long chatId) {
        String dashboardText = prepareInstructorDashboardText(user, profile);
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.dashboardMenu();
        bot.myExecute(sendMsg.sendMessage(chatId, dashboardText, inlineKeyboardMarkup));
    }

    private void startMessage(Long chatId) {
        String mentorStartText = """
                <b>👋 Assalomu alaykum, hurmatli mentor!</b>
                
                <b>Online Education</b> platformasiga xush kelibsiz. 
                Quyidagi menyu orqali o‘z kurslaringizni boshqarishingiz mumkin.
                """;

        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardService.buildMentorMenu();
        SendMessage sendMessage = sendMsg.sendMessage(
                chatId,
                mentorStartText,
                replyKeyboardMarkup
        );
        bot.myExecute(sendMessage);
    }

    /**
     * Prepares the formatted text for the student's dashboard.
     */
    @Transactional(readOnly = true)
    public String prepareInstructorDashboardText(User user, UserProfile profile) {

        String fullName = profile.getFirstName() + " " + profile.getLastName();
        String email = profile.getEmail();
        String username = user.getUsername();
        Role role = user.getRole();
        int activeCoursesCount = courseRepository.countByInstructorIdAndSuccess(user.getId(), true);
        int inactiveCoursesCount = courseRepository.countByInstructorIdAndSuccess(user.getId(), false);

        int purchasedStudentsCount = paymentRepository.countDistinctPurchasedUsersByInstructorId(user.getId());

        // 3. Daromad statistikasini olish
        Long totalIncome = paymentRepository.calculateTotalIncomeByInstructorId(user.getId());


        return String.format(
                """
                        <b>👨‍🏫 Mentor Profili</b>
                        
                        👤 <b>Ism Familiya:</b> %s
                        📧 <b>Email:</b> %s
                        🔑 <b>Username:</b> @%s
                        🛡 <b>Role:</b> %s
                        
                        ────────────────────────────
                        <b>📊 Umumiy ma’lumotlar:</b>
                        ✅ Aktiv kurslar: %d ta
                        ⏸️ Aktiv emas kurslar: %d ta
                        🎓 O‘quvchilar (sotib olgan): %d ta
                        💰 Umumiy daromad: $%s""",
                fullName,
                email,
                username,
                role,
                activeCoursesCount,
                inactiveCoursesCount,
                purchasedStudentsCount,
                formatAmount(totalIncome)
        );
    }

    /**
     * Creates a simple text-based progress bar.
     */
    private String createProgressBar(int percentage) {
        if (percentage < 0) percentage = 0;
        if (percentage > 100) percentage = 100;
        int filledBlocks = Math.round(percentage / 10.0f);
        int emptyBlocks = 10 - filledBlocks;
        return "█".repeat(filledBlocks) + "░".repeat(emptyBlocks);
    }

    private String formatAmount(Long amount) {
        if (amount == null || amount == 0) {
            return "0 so'm";
        }
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount / 100) + " so'm";
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
     * Retrieves an existing TelegramUser or creates a new one if not found.
     */
    private TelegramUser getOrCreateTelegramUser(Long chatId) {
        return telegramUserRepository.findByChatId(chatId).orElseGet(() -> {
            log.info("Creating a new TelegramUser for chatId: {}", chatId);
            TelegramUser newTelegramUser = new TelegramUser();
            newTelegramUser.setChatId(chatId);
            newTelegramUser.setUserState(UserState.UNREGISTERED);
            return telegramUserRepository.save(newTelegramUser);
        });
    }
}

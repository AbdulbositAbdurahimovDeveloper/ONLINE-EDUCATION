package uz.pdp.online_education.telegram.service.student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import uz.pdp.online_education.model.Attachment;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.model.lesson.*;
import uz.pdp.online_education.payload.CategoryInfo;
import uz.pdp.online_education.payload.UserInfo;
import uz.pdp.online_education.repository.*;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.config.controller.OnlineEducationBot;
import uz.pdp.online_education.telegram.enums.BotMessage;
import uz.pdp.online_education.telegram.mapper.SendMsg;
import uz.pdp.online_education.telegram.service.TelegramUserService;
import uz.pdp.online_education.telegram.service.message.MessageService;
import uz.pdp.online_education.telegram.service.student.template.StudentCallBackQueryService;
import uz.pdp.online_education.telegram.service.student.template.StudentInlineKeyboardService;
import uz.pdp.online_education.telegram.service.student.template.StudentProcessMessageService;

import java.util.List;

// StudentCallBackQueryServiceImpl.java

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentCallBackQueryServiceImpl implements StudentCallBackQueryService {

    private final TelegramUserService telegramUserService;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    @Value("${telegram.bot.webhook-path}")
    private String SITE_URL;
    // Asosiy servislar
    private final StudentProcessMessageService studentProcessMessageService;
    private final OnlineEducationBot bot;
    private final SendMsg sendMsg;
    private final StudentInlineKeyboardService studentInlineKeyboardService;
    private final MessageService messageService;

    // Repositories
    private final TelegramUserRepository telegramUserRepository;
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final ModuleEnrollmentRepository moduleEnrollmentRepository;
    private final LessonRepository lessonRepository;
    private final ContentRepository contentRepository; // YANGI BOG'LIQLIK

    private static final int PAGE_SIZE = 5;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional// readOnly=true, chunki asosan o'qish operatsiyalari
    public void handleCallback(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String queryData = callbackQuery.getData();

        User user = telegramUserRepository.findByChatId(chatId)
                .orElseThrow(() -> new RuntimeException("User not found for callback"))
                .getUser();

        bot.myExecute(new AnswerCallbackQuery(callbackQuery.getId()));

        try {
            String[] data = queryData.split(":");
            String prefix = data[0];

            switch (prefix) {
                case Utils.CallbackData.AUTH_PREFIX -> handleAuthCallback(user, chatId, messageId, data);
                case Utils.CallbackData.MY_COURSE_PREFIX -> handleMyCourseCallback(user, chatId, messageId, data);
                case Utils.CallbackData.MODULE_PREFIX ->
                        handleModuleCallback(user, chatId, messageId, data, callbackQuery);
                case Utils.CallbackData.LESSON_PREFIX ->
                        handleLessonCallback(user, chatId, messageId, data, callbackQuery);
                case Utils.CallbackData.CONTENT_PREFIX -> handleContentCallback(user, chatId, data, callbackQuery);
                case Utils.CallbackData.STUDENT_PREFIX -> handleStudentGeneralCallback(user, chatId, messageId, data);
                case Utils.CallbackData.ALL_COURSES_PREFIX -> handleAllCoursesCallback(user, chatId, messageId, data);
            }
        } catch (Exception e) {
            log.error("Error processing callback query: {}", queryData, e);
            bot.myExecute(sendMsg.sendMessage(chatId, messageService.getMessage(BotMessage.ERROR_UNEXPECTED)));
        }
    }

    /**
     * Barcha autentifikatsiya bilan bog'liq callback'larni boshqaradi.
     */

    private void handleAuthCallback(User user, Long chatId, Integer messageId, String[] data) {
        // Callback ma'lumotining to'liq ko'rinishi: auth:logout:init, auth:logout:confirm, ...
        // Biz data[1] (logout) va data[2] (init/confirm/cancel) qismlaridan foydalanamiz.
        String action = data[1]; // "logout"
        String step = data[2];  // "init", "confirm", "cancel"

        if (!action.equals("logout")) {
            log.warn("Unknown auth action: {}", action);
            return;
        }

        switch (step) {
            case "init":
                // "Chiqish" tugmasi bosildi -> Tasdiqlash so'rovini ko'rsatamiz
                String confirmationText = messageService.getMessage(BotMessage.AUTH_LOGOUT_CONFIRMATION_TEXT);
                InlineKeyboardMarkup confirmationKeyboard = studentInlineKeyboardService.logoutConfirmation();
                bot.myExecute(sendMsg.editMessage(chatId, messageId, confirmationText, confirmationKeyboard));
                break;

            case "confirm":

                telegramUserService.unregistered(chatId);

                String successText = messageService.getMessage(BotMessage.AUTH_LOGOUT_SUCCESS_TEXT);
                // Tugmalarni olib tashlash uchun keyboard'ga null beramiz
                bot.myExecute(sendMsg.editMessage(chatId, messageId, successText, null));
                break;

            case "cancel":
                // "Yo'q" tugmasi bosildi -> Dashboardga qaytaramiz
                studentProcessMessageService.showDashboard(user, chatId, messageId);
                break;

            default:
                log.warn("Unknown logout step: {}", step);
                break;
        }
    }

    private void handleMyCourseCallback(User user, Long chatId, Integer messageId, String[] data) {
        String action = data[1];
        if (action.equals(Utils.CallbackData.ACTION_VIEW)) { // myc:v:{courseId}
            Long courseId = Long.parseLong(data[2]);
            showModulesForCourse(user, chatId, messageId, courseId, 0);
        } else if (action.equals(Utils.CallbackData.ACTION_LIST)) { // myc:l:p:{pageNum}
            int pageNum = Integer.parseInt(data[3]);
            showMyCourses(user, chatId, messageId, pageNum);
        }
    }

    private void handleModuleCallback(User user, Long chatId, Integer messageId, String[] data, CallbackQuery callbackQuery) {
        String action = data[1];
        switch (action) {
            case Utils.CallbackData.ACTION_VIEW -> { // mod:v:{moduleId}
                // --- TUZATILDI: Endi darslar ro'yxatini ko'rsatadi ---
                Long moduleId = Long.parseLong(data[2]);
                showLessonsForModule(user, chatId, messageId, moduleId, 0);
            }
            case Utils.CallbackData.ACTION_LIST -> {
                Long courseId = Long.parseLong(data[2]); // TUZATILDI: data[1] o'rniga data[2]

                int pageNum = Integer.parseInt(data[4]);
                showModulesForCourse(user, chatId, messageId, courseId, pageNum);
            }
            case Utils.CallbackData.ACTION_BUY -> { // mod:buy:{moduleId}
                // --- YAKUNIY LOGIKA ---
                long moduleId = Long.parseLong(data[2]);
                String url = SITE_URL + "/checkout/module/" + moduleId;
                InlineKeyboardMarkup keyboard = studentInlineKeyboardService.createUrlButton("🛒 Saytda sotib olish", url);

                bot.myExecute(sendMsg.editMessage(chatId, callbackQuery.getMessage().getMessageId(), "Ushbu modulni sotib olish uchun saytga o'ting:", keyboard));
            }
        }
    }

    private void handleLessonCallback(User user, Long chatId, Integer messageId, String[] data, CallbackQuery callbackQuery) {
        String action = data[1];

        switch (action) {
            case Utils.CallbackData.ACTION_VIEW -> {
                // les:v:{lessonId}
                Long lessonId = Long.parseLong(data[2]);
                showLessonContents(chatId, messageId, lessonId);
            }
            case Utils.CallbackData.ACTION_LIST -> {
                // les:{moduleId}:l:p:{pageNum}
                Long moduleId = Long.parseLong(data[2]);
                int pageNum = Integer.parseInt(data[4]);
                showLessonsForModule(user, chatId, messageId, moduleId, pageNum);

            }
            case Utils.CallbackData.ACTION_BUY -> {
                // --- TO'G'IRLANGAN "SOTIB OLISH" LOGIKASI ---
                // les:buy:{moduleId}
                Long moduleId = Long.parseLong(data[2]);

                // 1. Qaysi modulni sotib olish kerakligini aniqlaymiz
                Module moduleToBuy = moduleRepository.findById(moduleId).orElse(null);
                if (moduleToBuy == null) {
                    // Agar modul topilmasa, shunchaki xatolik haqida ogohlantiramiz
                    AnswerCallbackQuery method = new AnswerCallbackQuery(callbackQuery.getId());
                    method.setText("Xatolik: Bunday modul topilmadi.");
                    method.setShowAlert(true);
                    bot.myExecute(method);
                    return;
                }

                // 2. Foydalanuvchiga yuboriladigan xabar matnini tayyorlaymiz
                String messageText = messageService.getMessage(
                        BotMessage.LESSON_LOCKED_MESSAGE, // "Bu darsni ko'rish uchun '%s' modulini sotib olishingiz kerak."
                        moduleToBuy.getTitle()
                );

                // 3. Veb-saytga olib boruvchi URL'ni yasaymiz
                // BU YERGA O'ZINGIZNING SAYTINGIZ MANZILINI QO'YISHINGIZ KERAK
                String purchaseUrl = SITE_URL + moduleId;

                // 4. "Sotib olish" tugmasini yasaymiz
                InlineKeyboardMarkup keyboard = studentInlineKeyboardService.createUrlButton(
                        messageService.getMessage(BotMessage.BUY_MODULE_BUTTON), // "🛒 Sotib olish"
                        purchaseUrl
                );

                // 5. Eskirgan darslar ro'yxati xabarini o'chirib tashlaymiz
                bot.myExecute(sendMsg.deleteMessage(chatId, messageId));

                // 6. Foydalanuvchiga yangi xabarni tugma bilan birga yuboramiz
                bot.myExecute(sendMsg.sendMessage(chatId, messageText, keyboard));
            }
        }
    }

    private void handleContentCallback(User user, Long chatId, String[] data, CallbackQuery callbackQuery) {
        // --- YAKUNIY LOGIKA: KONTENTNI YUBORISH ---
        if (data[1].equals(Utils.CallbackData.ACTION_VIEW)) { // con:v:{contentId}
            Long contentId = Long.parseLong(data[2]);
            Content content = contentRepository.findById(contentId).orElse(null);
            if (content == null) return;

            if (content instanceof TextContent textContent) {
                bot.myExecute(sendMsg.sendMessage(chatId, textContent.getText()));
            } else if (content instanceof AttachmentContent attachmentContent) {
                Attachment attachment = attachmentContent.getAttachment();
                if (attachment != null && attachment.getTelegramFileId() != null) {
//                    bot.myExecute(sendMsg.sendVideo(chatId, attachment.getTelegramFileId(), attachment.getOriginalName()));
                }
            } else if (content instanceof QuizContent quizContent) {
                String url = "https://your-website.com/quiz/" + quizContent.getQuiz().getId();
                InlineKeyboardMarkup keyboard = studentInlineKeyboardService.createSingleButtonKeyboard("❓ Testni ishlash", url);
                bot.myExecute(sendMsg.sendMessage(chatId, messageService.getMessage(BotMessage.QUIZ_REDIRECT_MESSAGE), keyboard));
            }
        }
    }

    private void handleStudentGeneralCallback(User user, Long chatId, Integer messageId, String[] data) {
        if (data[1].equals(Utils.CallbackData.ACTION_BACK) && data[2].equals(Utils.CallbackData.BACK_TO_MAIN_MENU)) {
            bot.myExecute(sendMsg.deleteMessage(chatId, messageId));
            studentProcessMessageService.showMainMenu(user, chatId);
        }
    }

    //    private void handleAllCoursesCallback(User user, Long chatId, Integer messageId, String[] data) {
//        String action = data[1];
//
//        switch (action) {
//            case "by_cat": // allc:by_cat:p:{pageNum}
//                showCategories(chatId, messageId, Integer.parseInt(data[3]));
//                break;
//            case "by_ins": // allc:by_ins:p:{pageNum}
//                showInstructors(chatId, messageId, Integer.parseInt(data[3]));
//                break;
//            case "list_cat": // allc:list_cat:{catId}:p:{pageNum}
//                // TODO: Kategoriya bo'yicha kurslar ro'yxatini ko'rsatish
//                break;
//            case "list_ins": // allc:list_ins:{insId}:p:{pageNum}
//                // TODO: Instruktor bo'yicha kurslar ro'yxatini ko'rsatish
//                break;
//            case "back": // allc:back:main
//                showChooseMethod(chatId, messageId);
//                break;
//        }
//    }
    private void handleAllCoursesCallback(User user, Long chatId, Integer messageId, String[] data) {

    }

    // --- ACTION METHODS (SHOWING SCREENS) ---

    private void showMyCourses(User user, Long chatId, Integer messageId, int pageNum) {
        Pageable pageable = PageRequest.of(pageNum, PAGE_SIZE, Sort.by("title"));
        Page<Course> coursePage = courseRepository.findDistinctEnrolledCoursesForUser(user.getId(), pageable);
        String text = messageService.getMessage(BotMessage.MY_COURSES_TITLE, pageNum + 1, coursePage.getTotalPages());
        InlineKeyboardMarkup keyboard = studentInlineKeyboardService.myCoursesMenu(coursePage);
        bot.myExecute(sendMsg.editMessage(chatId, messageId, text, keyboard));
    }

    private void showModulesForCourse(User user, Long chatId, Integer messageId, Long courseId, int pageNum) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) return;
        Pageable pageable = PageRequest.of(pageNum, PAGE_SIZE, Sort.by("orderIndex"));
        Page<Module> modulePage = moduleRepository.findAllByCourse(course, pageable);
        boolean isEnrolledToFullCourse = checkIfEnrolledToFullCourse(user, course);
        List<Long> enrolledModuleIds = moduleEnrollmentRepository.findEnrolledModuleIdsByUser(user.getId(), courseId);
        String text = messageService.getMessage(BotMessage.MODULES_LIST_TITLE, course.getTitle(), pageNum + 1, modulePage.getTotalPages());
        InlineKeyboardMarkup keyboard = studentInlineKeyboardService.modulesMenu(modulePage, courseId, enrolledModuleIds, isEnrolledToFullCourse);

        if (course.getThumbnailUrl() != null && course.getThumbnailUrl().getTelegramFileId() != null) {
            String fileId = course.getThumbnailUrl().getTelegramFileId();
            bot.myExecute(sendMsg.editMessageMedia(chatId, messageId, fileId, text, keyboard));
        } else {
            bot.myExecute(sendMsg.editMessage(chatId, messageId, text, keyboard));
        }
    }

    private void showLessonsForModule(User user, Long chatId, Integer messageId, Long moduleId, int pageNum) {
        Module module = moduleRepository.findById(moduleId).orElse(null);
        if (module == null) return;
        Pageable pageable = PageRequest.of(pageNum, PAGE_SIZE, Sort.by("orderIndex"));
        Page<Lesson> lessonPage = lessonRepository.findAllByModuleOrderByOrderIndexAsc(module, pageable);
//        boolean isModuleEnrolled = moduleEnrollmentRepository.existsByUserAndModuleId(user, moduleId);
        boolean isModuleEnrolled = paymentRepository.existsByUserAndModuleId(user, moduleId);

        String text = messageService.getMessage(BotMessage.LESSONS_LIST_TITLE, module.getTitle(), pageNum + 1, lessonPage.getTotalPages());
        InlineKeyboardMarkup keyboard = studentInlineKeyboardService.lessonsMenu(lessonPage, moduleId, module.getCourse().getId(), isModuleEnrolled);
        bot.myExecute(sendMsg.editMessage(chatId, messageId, text, keyboard));
    }

    private void showLessonContents(Long chatId, Integer messageId, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId).orElse(null);
        if (lesson == null) return;
        String text = messageService.getMessage(BotMessage.LESSON_DETAIL_TITLE, lesson.getTitle(), lesson.getContent() != null ? lesson.getContent() : "");
        InlineKeyboardMarkup keyboard = studentInlineKeyboardService.lessonContentsMenu(lesson, lesson.getModule().getId());
        bot.myExecute(sendMsg.editMessage(chatId, messageId, text, keyboard));
    }

    private boolean checkIfEnrolledToFullCourse(User user, Course course) {
        long totalModulesInCourse = moduleRepository.countByCourse(course);
        if (totalModulesInCourse == 0) return true;
        long enrolledModulesCount = moduleEnrollmentRepository.countByUserAndCourse(user.getId(), course.getId());
        return totalModulesInCourse == enrolledModulesCount;
    }
}

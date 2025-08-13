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
import uz.pdp.online_education.repository.*;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.config.controller.OnlineEducationBot;
import uz.pdp.online_education.telegram.enums.BotMessage;
import uz.pdp.online_education.telegram.mapper.SendMsg;
import uz.pdp.online_education.telegram.service.TelegramUserService;
import uz.pdp.online_education.telegram.service.UrlBuilderService;
import uz.pdp.online_education.telegram.service.message.MessageService;
import uz.pdp.online_education.telegram.service.student.template.StudentCallBackQueryService;
import uz.pdp.online_education.telegram.service.student.template.StudentInlineKeyboardService;
import uz.pdp.online_education.telegram.service.student.template.StudentProcessMessageService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentCallBackQueryServiceImpl implements StudentCallBackQueryService {

    // --- DEPENDENCIES ---
    @Value("${telegram.bot.webhook-path}")
    private String SITE_URL;

    // Services
    private final TelegramUserService telegramUserService;
    private final StudentProcessMessageService studentProcessMessageService;
    private final OnlineEducationBot bot;
    private final SendMsg sendMsg;
    private final StudentInlineKeyboardService studentInlineKeyboardService;
    private final MessageService messageService;
    private final UrlBuilderService urlBuilderService;

    // Repositories
    private final TelegramUserRepository telegramUserRepository;
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final ModuleEnrollmentRepository moduleEnrollmentRepository;
    private final LessonRepository lessonRepository;
    private final ContentRepository contentRepository;
    private final PaymentRepository paymentRepository;

    private static final int PAGE_SIZE = 5;

    @Override
    @Transactional
    public void handleCallback(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String queryData = callbackQuery.getData();

        // Foydalanuvchini topish, topilmasa xatolik berish
        User user = telegramUserRepository.findByChatId(chatId)
                .orElseThrow(() -> new RuntimeException("User not found for callback. ChatID: " + chatId))
                .getUser();

        // Callback so'roviga javob berish (loading animatsiyasini to'xtatish)
        bot.myExecute(new AnswerCallbackQuery(callbackQuery.getId()));

        try {
            String[] data = queryData.split(":");
            String prefix = data[0];

            // Callback prefixiga qarab tegishli handler'ga yo'naltirish
            switch (prefix) {
                case Utils.CallbackData.AUTH_PREFIX -> handleAuthCallback(user, chatId, messageId, data);
                case Utils.CallbackData.MY_COURSE_PREFIX -> handleMyCourseCallback(user, chatId, messageId, data);
                case Utils.CallbackData.MODULE_PREFIX -> handleModuleCallback(user, chatId, messageId, data, callbackQuery);
                case Utils.CallbackData.LESSON_PREFIX -> handleLessonCallback(user, chatId, messageId, data, callbackQuery);
                case Utils.CallbackData.CONTENT_PREFIX -> handleContentCallback(user, chatId, data, callbackQuery);
                case Utils.CallbackData.STUDENT_PREFIX -> handleStudentGeneralCallback(user, chatId, messageId, data);
                case Utils.CallbackData.ALL_COURSES_PREFIX -> handleAllCoursesCallback(user, chatId, messageId, data);
            }
        } catch (Exception e) {
            log.error("Callbackni qayta ishlashda xatolik yuz berdi: Query='{}'", queryData, e);
            bot.myExecute(sendMsg.sendMessage(chatId, messageService.getMessage(BotMessage.ERROR_UNEXPECTED)));
        }
    }

    // --- CALLBACK HANDLERS (Private methods for routing callbacks) ---

    /**
     * Autentifikatsiya (masalan, tizimdan chiqish) bilan bog'liq callback'larni boshqaradi.
     */
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
                InlineKeyboardMarkup confirmationKeyboard = studentInlineKeyboardService.logoutConfirmation();
                bot.myExecute(sendMsg.editMessage(chatId, messageId, confirmationText, confirmationKeyboard));
            }
            case Utils.CallbackData.ACTION_CONFIRM -> {
                telegramUserService.unregistered(chatId);
                String successText = messageService.getMessage(BotMessage.AUTH_LOGOUT_SUCCESS_TEXT);
                bot.myExecute(sendMsg.editMessage(chatId, messageId, successText, null));
            }
            case Utils.CallbackData.ACTION_CANCEL -> studentProcessMessageService.showDashboard(user, chatId, messageId);
            default -> log.warn("Noma'lum chiqish qadami: {}", step);
        }
    }

    /**
     * "Mening kurslarim" bo'limi bilan bog'liq callback'larni boshqaradi.
     */
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

    /**
     * Modullar (ko'rish, sotib olish, ro'yxat) bilan bog'liq callback'larni boshqaradi.
     */
    private void handleModuleCallback(User user, Long chatId, Integer messageId, String[] data, CallbackQuery callbackQuery) {
        String action = data[1];
        switch (action) {
            case Utils.CallbackData.ACTION_VIEW -> { // mod:v:{moduleId}
                Long moduleId = Long.parseLong(data[2]);
                showLessonsForModule(user, chatId, messageId, moduleId, 0);
            }
            case Utils.CallbackData.ACTION_LIST -> { // mod:l:{courseId}:p:{pageNum}
                Long courseId = Long.parseLong(data[2]);
                int pageNum = Integer.parseInt(data[4]);
                showModulesForCourse(user, chatId, messageId, courseId, pageNum);
            }
            case Utils.CallbackData.ACTION_BUY -> { // mod:buy:{moduleId}
                long moduleId = Long.parseLong(data[2]);
//                String url = SITE_URL + "/checkout/module/" + moduleId;
                String url = urlBuilderService.generateModuleCheckoutUrl(moduleId);
                InlineKeyboardMarkup keyboard = studentInlineKeyboardService.createUrlButton("🛒 Saytda sotib olish", url);
                bot.myExecute(sendMsg.editMessage(chatId, callbackQuery.getMessage().getMessageId(), "Ushbu modulni sotib olish uchun saytga o'ting:", keyboard));
            }
        }
    }

    /**
     * Darslar (ko'rish, sotib olish, ro'yxat) bilan bog'liq callback'larni boshqaradi.
     */
    private void handleLessonCallback(User user, Long chatId, Integer messageId, String[] data, CallbackQuery callbackQuery) {
        String action = data[1];
        switch (action) {
            case Utils.CallbackData.ACTION_VIEW -> { // les:v:{lessonId}
                Long lessonId = Long.parseLong(data[2]);
                showLessonContents(chatId, messageId, lessonId);
            }
            case Utils.CallbackData.ACTION_LIST -> { // les:{moduleId}:l:p:{pageNum}
                Long moduleId = Long.parseLong(data[2]);
                int pageNum = Integer.parseInt(data[4]);
                showLessonsForModule(user, chatId, messageId, moduleId, pageNum);
            }
            case Utils.CallbackData.ACTION_BUY -> { // les:buy:{moduleId}
                Long moduleId = Long.parseLong(data[2]);
                Module moduleToBuy = moduleRepository.findById(moduleId).orElse(null);
                if (moduleToBuy == null) {
                    AnswerCallbackQuery method = new AnswerCallbackQuery(callbackQuery.getId());
                    method.setText( "Xatolik: Bunday modul topilmadi.");
                    method.setShowAlert(true);
                    bot.myExecute(method);
                    return;
                }
                String messageText = messageService.getMessage(BotMessage.LESSON_LOCKED_MESSAGE, moduleToBuy.getTitle());
                String purchaseUrl = SITE_URL + "/checkout/module/" + moduleId;
                InlineKeyboardMarkup keyboard = studentInlineKeyboardService.createUrlButton(
                        messageService.getMessage(BotMessage.BUY_MODULE_BUTTON), purchaseUrl
                );
                bot.myExecute(sendMsg.deleteMessage(chatId, messageId));
                bot.myExecute(sendMsg.sendMessage(chatId, messageText, keyboard));
            }
        }
    }

    /**
     * Kontent (matn, video, test) bilan bog'liq callback'larni boshqaradi.
     */
    private void handleContentCallback(User user, Long chatId, String[] data, CallbackQuery callbackQuery) {
        if (data[1].equals(Utils.CallbackData.ACTION_VIEW)) { // con:v:{contentId}
            Long contentId = Long.parseLong(data[2]);
            Content content = contentRepository.findById(contentId).orElse(null);
            if (content == null) return;

            if (content instanceof TextContent textContent) {
                bot.myExecute(sendMsg.sendMessage(chatId, textContent.getText()));
            } else if (content instanceof AttachmentContent attachmentContent) {
                Attachment attachment = attachmentContent.getAttachment();
                if (attachment != null && attachment.getTelegramFileId() != null) {
                    // TODO: sendVideo yoki sendDocument logikasini implementatsiya qilish kerak.
                    log.info("Foydalanuvchiga video yuborish so'rovi keldi. File ID: {}", attachment.getTelegramFileId());
                }
            } else if (content instanceof QuizContent quizContent) {
//                String url = SITE_URL + "/quiz/" + quizContent.getQuiz().getId();
                String url = urlBuilderService.generateQuizUrl(quizContent.getQuiz().getId());
                InlineKeyboardMarkup keyboard = studentInlineKeyboardService.createSingleButtonKeyboard("❓ Testni ishlash", url);
                bot.myExecute(sendMsg.sendMessage(chatId, messageService.getMessage(BotMessage.QUIZ_REDIRECT_MESSAGE), keyboard));
            }
        }
    }

    /**
     * Umumiy callback'larni (masalan, "Orqaga") boshqaradi.
     */
    private void handleStudentGeneralCallback(User user, Long chatId, Integer messageId, String[] data) {
        if (data[1].equals(Utils.CallbackData.ACTION_BACK) && data[2].equals(Utils.CallbackData.BACK_TO_MAIN_MENU)) {
            bot.myExecute(sendMsg.deleteMessage(chatId, messageId));
            studentProcessMessageService.showMainMenu(user, chatId);
        }
    }

    /**
     * "Barcha kurslar" bo'limi bilan bog'liq callback'larni boshqaradi.
     */
    private void handleAllCoursesCallback(User user, Long chatId, Integer messageId, String[] data) {
        // TODO: Bu bo'limning logikasi hali implementatsiya qilinmagan.
        // Kerakli amallar:
        // 1. Kategoriyalar ro'yxatini ko'rsatish
        // 2. Instruktorlar ro'yxatini ko'rsatish
        // 3. Tanlangan kategoriya/instruktor bo'yicha kurslarni chiqarish
        log.warn("Hali tayyor bo'lmagan 'Barcha kurslar' bo'limiga murojaat qilindi. Query: {}", String.join(":", data));
        AnswerCallbackQuery method = new AnswerCallbackQuery(String.valueOf(messageId));
        method.setText( "Bu bo'lim hozircha ishlab chiqilmoqda.");
        method.setShowAlert(true);
        bot.myExecute(method);
    }

    // --- UI/SCREEN METHODS (Private methods for displaying information) ---

    /**
     * Foydalanuvchi a'zo bo'lgan kurslarni sahifalangan holda ko'rsatadi.
     */
    private void showMyCourses(User user, Long chatId, Integer messageId, int pageNum) {
        Pageable pageable = PageRequest.of(pageNum, PAGE_SIZE, Sort.by("title"));
        Page<Course> coursePage = courseRepository.findDistinctEnrolledCoursesForUser(user.getId(), pageable);
        String text = messageService.getMessage(BotMessage.MY_COURSES_TITLE, pageNum + 1, coursePage.getTotalPages());
        InlineKeyboardMarkup keyboard = studentInlineKeyboardService.myCoursesMenu(coursePage);
        bot.myExecute(sendMsg.editMessage(chatId, messageId, text, keyboard));
    }

    /**
     * Tanlangan kursning modullarini sahifalangan holda ko'rsatadi.
     */
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

    /**
     * Tanlangan modulning darslarini sahifalangan holda ko'rsatadi.
     */
    private void showLessonsForModule(User user, Long chatId, Integer messageId, Long moduleId, int pageNum) {
        Module module = moduleRepository.findById(moduleId).orElse(null);
        if (module == null) return;

        Pageable pageable = PageRequest.of(pageNum, PAGE_SIZE, Sort.by("orderIndex"));
        Page<Lesson> lessonPage = lessonRepository.findAllByModuleOrderByOrderIndexAsc(module, pageable);
        boolean isModuleEnrolled = paymentRepository.existsByUserAndModuleId(user, moduleId);

        String text = messageService.getMessage(BotMessage.LESSONS_LIST_TITLE, module.getTitle(), pageNum + 1, lessonPage.getTotalPages());
        InlineKeyboardMarkup keyboard = studentInlineKeyboardService.lessonsMenu(lessonPage, moduleId, module.getCourse().getId(), isModuleEnrolled);
        bot.myExecute(sendMsg.editMessage(chatId, messageId, text, keyboard));
    }

    /**
     * Tanlangan darsning kontentini (vazifa, video, matn) ko'rish uchun menyu ko'rsatadi.
     */
    private void showLessonContents(Long chatId, Integer messageId, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId).orElse(null);
        if (lesson == null) return;

        String text = messageService.getMessage(BotMessage.LESSON_DETAIL_TITLE, lesson.getTitle(), lesson.getContent() != null ? lesson.getContent() : "");
        InlineKeyboardMarkup keyboard = studentInlineKeyboardService.lessonContentsMenu(lesson, lesson.getModule().getId());
        bot.myExecute(sendMsg.editMessage(chatId, messageId, text, keyboard));
    }

    // --- HELPER METHODS ---

    /**
     * Foydalanuvchi kursga to'liq a'zo bo'lganligini tekshiradi.
     */
    private boolean checkIfEnrolledToFullCourse(User user, Course course) {
        long totalModulesInCourse = moduleRepository.countByCourse(course);
        if (totalModulesInCourse == 0) return true; // Modulsiz kursga a'zo hisoblanadi
        long enrolledModulesCount = moduleEnrollmentRepository.countByUserAndCourse(user.getId(), course.getId());
        return totalModulesInCourse == enrolledModulesCount;
    }
}
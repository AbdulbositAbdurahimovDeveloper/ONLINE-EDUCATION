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
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import uz.pdp.online_education.enums.TransactionStatus;
import uz.pdp.online_education.model.Abs.AbsDateEntity;
import uz.pdp.online_education.model.*;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.lesson.*;
import uz.pdp.online_education.payload.CategoryInfo;
import uz.pdp.online_education.payload.FilterDTO;
import uz.pdp.online_education.payload.PageDTO;
import uz.pdp.online_education.payload.UserInfo;
import uz.pdp.online_education.payload.course.CourseDetailDTO;
import uz.pdp.online_education.payload.enrollment.ModuleEnrollmentRequestDTO;
import uz.pdp.online_education.payload.enrollment.ModuleEnrollmentResponseDTO;
import uz.pdp.online_education.payload.faq.FaqDTO;
import uz.pdp.online_education.payload.lesson.LessonResponseDTO;
import uz.pdp.online_education.payload.module.ModuleDetailDTO;
import uz.pdp.online_education.repository.*;
import uz.pdp.online_education.service.interfaces.*;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.config.controller.OnlineEducationBot;
import uz.pdp.online_education.telegram.enums.BotMessage;
import uz.pdp.online_education.telegram.mapper.SendMsg;
import uz.pdp.online_education.telegram.service.RedisTemporaryDataService;
import uz.pdp.online_education.telegram.service.TelegramUserService;
import uz.pdp.online_education.telegram.service.UrlBuilderService;
import uz.pdp.online_education.telegram.service.message.MessageService;
import uz.pdp.online_education.telegram.service.student.template.StudentCallBackQueryService;
import uz.pdp.online_education.telegram.service.student.template.StudentInlineKeyboardService;
import uz.pdp.online_education.telegram.service.student.template.StudentProcessMessageService;
import uz.pdp.online_education.telegram.service.student.template.StudentReplyKeyboardService;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import static uz.pdp.online_education.telegram.Utils.CallbackData.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentCallBackQueryServiceImpl implements StudentCallBackQueryService {

    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final CourseService courseService;
    private final UserRepository userRepository;
    private final ModuleService moduleService;
    private final PaymentService paymentService;
    private final LessonService lessonService;
    private final ModuleEnrollmentService moduleEnrollmentService;
    private final StudentReplyKeyboardService studentReplyKeyboardService;
    private final ReviewService reviewService;
    private final FaqService faqService;
    private final FaqRepository faqRepository;
    private final RedisTemporaryDataService redisTemporaryDataService;
    // --- DEPENDENCIES ---

    @Value("${telegram.bot.webhook-path}")
    private String SITE_URL;


    private final TelegramUserService telegramUserService;
    private final StudentProcessMessageService studentProcessMessageService;
    private final OnlineEducationBot bot;
    private final SendMsg sendMsg;
    private final StudentInlineKeyboardService studentInlineKeyboardService;
    private final MessageService messageService;
    private final UrlBuilderService urlBuilderService;


    private final TelegramUserRepository telegramUserRepository;
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final ModuleEnrollmentRepository moduleEnrollmentRepository;
    private final LessonRepository lessonRepository;
    private final ContentRepository contentRepository;
    private final PaymentRepository paymentRepository;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int PAGE_SIZE = 5;

    @Override
    @Transactional
    public void handleCallback(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String queryData = callbackQuery.getData();
        String callbackQueryId = callbackQuery.getId();


        User user = telegramUserRepository.findByChatId(chatId).orElseThrow(() -> new RuntimeException("User not found for callback. ChatID: " + chatId)).getUser();


        bot.myExecute(new AnswerCallbackQuery(callbackQuery.getId()));

        try {
            String[] data = queryData.split(":");
            String prefix = data[0];


            switch (prefix) {
                case DELETED -> bot.myExecute(sendMsg.deleteMessage(chatId, messageId));
                case AUTH_PREFIX -> handleAuthCallback(user, chatId, messageId, data);
                case MY_COURSE_PREFIX -> handleMyCourseCallback(user, chatId, messageId, data);
                case MODULE_PREFIX -> handleModuleCallback(user, chatId, messageId, data, callbackQuery);
                case LESSON_PREFIX -> handleLessonCallback(user, chatId, messageId, data, callbackQuery);
                case CONTENT_PREFIX -> handleContentCallback(user, chatId, data, callbackQuery);
                case STUDENT_PREFIX -> handleStudentGeneralCallback(user, chatId, messageId, data);
                case ALL_COURSES_PREFIX -> handleAllCoursesCallback(user, chatId, messageId, data, callbackQueryId);
                case BALANCED -> handleBalanced(user, chatId, messageId, data);
                case ACTION_SUPPORT -> handleSupport(user, chatId, messageId, data, queryData, callbackQueryId);
            }
        } catch (Exception e) {
            log.error("Callbackni qayta ishlashda xatolik yuz berdi: Query='{}'", queryData, e);
            bot.myExecute(sendMsg.sendMessage(chatId, messageService.getMessage(BotMessage.ERROR_UNEXPECTED)));
        }
    }

    private void handleSupport(User user, Long chatId, Integer messageId, String[] data, String queryData, String callbackQueryId) {
        String type = data[1];

        switch (type) {
            case BACK_TO_MAIN_MENU -> {
                bot.myExecute(sendMsg.deleteMessage(chatId, messageId));
                bot.myExecute(sendMsg.sendMessage(chatId, "🏠 Bosh menyu!", studentReplyKeyboardService.studentMainMenu()));
            }
            case ACTION_VIEW -> {
                Long id = Long.valueOf(data[2]);
                FaqDTO faqDTO = faqService.getById(id);
                String built = buildFaqAnswerText(faqDTO);
                InlineKeyboardMarkup keyboard = studentInlineKeyboardService.createSingleButtonKeyboard("orqaga", String.join(":", ACTION_SUPPORT, ACTION_PAGE, data[3]));
                bot.myExecute(sendMsg.editMessage(chatId, messageId, built, keyboard));
            }
            case ACTION_PAGE -> {
                int pageNumber = Integer.parseInt(data[2]);
                Page<Faq> faqs = faqRepository.findAll(PageRequest.of(pageNumber, 5));
                String built = buildFaqListText(faqs);
                InlineKeyboardMarkup inlineKeyboardMarkup = studentInlineKeyboardService.studentSupportMessage(faqs, String.join("", ACTION_SUPPORT, BACK_TO_MAIN_MENU), pageNumber);
                bot.myExecute(sendMsg.editMessage(chatId, messageId, built, inlineKeyboardMarkup));
            }
        }
    }

    private void handleBalanced(User user, Long chatId, Integer messageId, String[] data) {
        String type = data[1];

        switch (type) {
            case Utils.CallbackData.ACTION_BACK -> sendBalanceMenu(chatId, user, messageId);
            case Utils.CallbackData.BALANCE_PAYMENT_HISTORY -> userPaymentHistory(user, chatId, messageId, data);
            case Utils.CallbackData.BALANCE_PENDING_PAYMENTS -> userPendingPayments(user, chatId, messageId, data);
            case Utils.CallbackData.ACTION_VIEW -> {
                Long id = Long.valueOf(data[2]);
                Module module = moduleRepository.findById(id).orElseThrow();
                Course course = module.getCourse();

                UserProfile profile = course.getInstructor().getProfile();

                String text = String.format(
                        """
                                <b>📘 Modul:</b> %s
                                🔢 Tartib raqami: %d
                                💵 Narxi: %s\s
                                
                                <b>📚 Kurs:</b> %s
                                📂 Kategoriya: %s
                                👨‍🏫 O‘qituvchi: %s
                                
                                📖 Darslar soni: %d
                                
                                📝 <b>Tavsif:</b>
                                %s""",
                        module.getTitle(),
                        module.getOrderIndex(),
                        formatAmount(module.getPrice()),
                        course.getTitle(),
                        course.getCategory() != null ? course.getCategory().getName() : "Noma’lum",
                        profile.getFirstName() + " " + profile.getLastName(),
                        module.getLessons() != null ? module.getLessons().size() : 0,
                        module.getDescription() != null ? module.getDescription() : "Tavsif mavjud emas"
                );

                InlineKeyboardMarkup inlineKeyboardMarkup = studentInlineKeyboardService.buildModuleButtons(module);
                bot.myExecute(sendMsg.editMessage(chatId, messageId, text, inlineKeyboardMarkup));
            }

        }
    }

    private void userPendingPayments(User user, Long chatId, Integer messageId, String[] data) {
        int pageNumber = Integer.parseInt(data[3]);

        PageRequest pageRequest = PageRequest.of(pageNumber, PAGE_SIZE);
        Page<Module> modules = moduleEnrollmentRepository.findUnpaidModulesByUserId(user.getId(), pageRequest);


        StringBuilder sb = new StringBuilder();
        sb.append("💳 <b>To‘lanmagan modullar ro‘yxati</b>\n\n");

        int counter = 1;
        for (Module module : modules.getContent()) {
            sb.append(String.format(
                    "📘 <b>%d. %s</b>\n" +
                            "📚 Kurs: %s\n" +
                            "💰 Narxi: %s so‘m\n" +
                            "📑 Tartib raqami: %d\n\n",
                    counter++,
//                                module.getTitle(),
                    module.getTitle() != null ? module.getTitle() : "Noma'lum modul",
                    module.getCourse().getTitle(),
                    formatAmount(module.getPrice()),
                    module.getOrderIndex()
            ));
        }

        sb.append("⬇️ Quyidagi ro‘yxatdan modulni tanlang yoki sahifani almashtiring.");


        InlineKeyboardMarkup markup = studentInlineKeyboardService.userPendingPaymentsKeyboard(modules);

        bot.myExecute(sendMsg.editMessage(chatId, messageId, sb.toString(), markup));
    }

    private void userPaymentHistory(User user, Long chatId, Integer messageId, String[] data) {
        int pageNumber = Integer.parseInt(data[3]);


        Sort sort = Sort.by(AbsDateEntity.Fields.createdAt).descending();
        PageRequest pageable = PageRequest.of(pageNumber, PAGE_SIZE, sort);
        Page<Payment> payments = paymentRepository.findByUserAndStatus(user, TransactionStatus.SUCCESS, pageable);


        StringBuilder history = new StringBuilder("🧾 <b>So‘nggi to‘lovlaringiz:</b>\n\n");

        int count = 1;
        for (Payment p : payments.getContent()) {
            if (count > 5) break; // faqat 5 ta ko‘rsatamiz

            String date = p.getCreatedAt() != null
                    ? FORMATTER.format(p.getCreatedAt().toLocalDateTime()) // ✅ Timestamp → LocalDateTime
                    : "Noma’lum sana";

            history.append(String.format(
                    """
                            %d️⃣ %s — %s UZS
                               📚 Modul: %s
                               💳 Karta: %s
                               📅 Sana: %s
                               💵  Summa %s
                               📌 Holat: %s
                            
                            """,

                    count,
                    p.getDescription() != null ? p.getDescription() : "To‘lov",
                    formatAmount(p.getAmount()),
                    p.getModule() != null ? p.getModule().getTitle() : "Noma’lum modul",
                    p.getMaskedCardNumber() != null ? p.getMaskedCardNumber() : "**** **** **** 1234",
                    date,
                    formatAmount(p.getAmount()),
                    p.getStatus() != null ? p.getStatus().name() : "UNKNOWN"
            ));

            count++;
        }

        String paymentHistoryText = history.toString();
        InlineKeyboardMarkup inlineKeyboardMarkup = studentInlineKeyboardService.userPaymentsHistory(payments);
        bot.myExecute(sendMsg.editMessage(chatId, messageId, paymentHistoryText, inlineKeyboardMarkup));
    }



    public void sendBalanceMenu(Long chatId, User user, Integer messageId) {

        List<Module> unpaidModules = moduleEnrollmentRepository.findUnpaidModulesByUserId(user.getId());
        Long totalAmount = paymentRepository.findTotalSuccessfulPaymentsByUserId(user.getId(), TransactionStatus.SUCCESS);
        Payment lastPayment = paymentRepository.findTopByUser_IdAndStatusOrderByCreatedAtDesc(user.getId(), TransactionStatus.SUCCESS).orElse(null);
        long purchasedCoursesCount = paymentRepository.countByUser_IdAndStatus(user.getId(), TransactionStatus.SUCCESS);


        String messageText;

        /**
         * ⏳ Kutilayotgan to'lovlar (1)	📜 To'lovlar tarixi
         * ⬅️ Orqaga"
         */

        if (!unpaidModules.isEmpty()) {

            Module firstUnpaidModule = unpaidModules.get(0);

            messageText = messageService.getMessage(
                    BotMessage.BALANCE_INFO_WITH_PENDING_PAYMENT,

                    formatAmount(totalAmount),
                    purchasedCoursesCount,
                    lastPayment != null ? lastPayment.getModule().getTitle() : "Mavjud emas",
                    lastPayment != null ? formatAmount(lastPayment.getAmount()) : "0 so'm",
                    lastPayment != null ? formatDate(lastPayment.getCreatedAt()) : "-",
                    unpaidModules.size(),
                    firstUnpaidModule.getTitle(),
                    formatAmount(firstUnpaidModule.getPrice())
            );
        } else {

            messageText = messageService.getMessage(
                    BotMessage.BALANCE_INFO_NO_PENDING_PAYMENT,

                    formatAmount(totalAmount),
                    purchasedCoursesCount,
                    lastPayment != null ? lastPayment.getModule().getTitle() : "Mavjud emas",
                    lastPayment != null ? formatAmount(lastPayment.getAmount()) : "0 so'm",
                    lastPayment != null ? formatDate(lastPayment.getCreatedAt()) : "-"
            );
        }
        boolean hasPending = !unpaidModules.isEmpty();
        int pendingCount = unpaidModules.size();
        InlineKeyboardMarkup inlineKeyboardMarkup = studentInlineKeyboardService.createBalanceMenuKeyboard(hasPending, pendingCount);
        bot.myExecute(sendMsg.editMessage(chatId, messageId, messageText, inlineKeyboardMarkup));

    }

    private String formatAmount(Long amount) {
        if (amount == null || amount == 0) {
            return "0 so'm";
        }
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount / 100) + " so'm";
    }


    private String formatDate(Timestamp timestamp) {
        if (timestamp == null) {
            return "-";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return timestamp.toLocalDateTime().format(formatter);
    }


    /**
     * Autentifikatsiya (masalan, tizimdan chiqish) bilan bog'liq callback'larni boshqaradi.
     */
    private void handleAuthCallback(User user, Long chatId, Integer messageId, String[] data) {
        String action = data[1];
        String step = data[2];

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
            case Utils.CallbackData.ACTION_CANCEL ->
                    studentProcessMessageService.showDashboard(user, chatId, messageId);
            default -> log.warn("Noma'lum chiqish qadami: {}", step);
        }
    }

    /**
     * "Mening kurslarim" bo'limi bilan bog'liq callback'larni boshqaradi.
     */
    private void handleMyCourseCallback(User user, Long chatId, Integer messageId, String[] data) {
        String action = data[1];
        if (action.equals(Utils.CallbackData.ACTION_VIEW)) {
            Long courseId = Long.parseLong(data[2]);
            showModulesForCourse(user, chatId, messageId, courseId, 0);
        } else if (action.equals(Utils.CallbackData.ACTION_LIST)) {
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
            case Utils.CallbackData.ACTION_VIEW -> {
                Long moduleId = Long.parseLong(data[2]);
                showLessonsForModule(user, chatId, messageId, moduleId, 0);
            }
            case Utils.CallbackData.ACTION_LIST -> {
                Long courseId = Long.parseLong(data[2]);
                int pageNum = Integer.parseInt(data[4]);
                showModulesForCourse(user, chatId, messageId, courseId, pageNum);
            }
            case Utils.CallbackData.ACTION_BUY -> {
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
            case Utils.CallbackData.ACTION_VIEW -> {
                Long lessonId = Long.parseLong(data[2]);
                showLessonContents(chatId, messageId, lessonId);
            }
            case Utils.CallbackData.ACTION_LIST -> {
                Long moduleId = Long.parseLong(data[2]);
                int pageNum = Integer.parseInt(data[4]);
                showLessonsForModule(user, chatId, messageId, moduleId, pageNum);
            }
            case Utils.CallbackData.ACTION_BUY -> {
                Long moduleId = Long.parseLong(data[2]);
                Module moduleToBuy = moduleRepository.findById(moduleId).orElse(null);
                if (moduleToBuy == null) {
                    AnswerCallbackQuery method = new AnswerCallbackQuery(callbackQuery.getId());
                    method.setText("Xatolik: Bunday modul topilmadi.");
                    method.setShowAlert(true);
                    bot.myExecute(method);
                    return;
                }
                String messageText = messageService.getMessage(BotMessage.LESSON_LOCKED_MESSAGE, moduleToBuy.getTitle());
                String purchaseUrl = SITE_URL + "/checkout/module/" + moduleId;
                InlineKeyboardMarkup keyboard = studentInlineKeyboardService.createUrlButton(messageService.getMessage(BotMessage.BUY_MODULE_BUTTON), purchaseUrl);
                bot.myExecute(sendMsg.deleteMessage(chatId, messageId));
                bot.myExecute(sendMsg.sendMessage(chatId, messageText, keyboard));
            }
        }
    }

    /**
     * Kontent (matn, video, test) bilan bog'liq callback'larni boshqaradi.
     */
    private void handleContentCallback(User user, Long chatId, String[] data, CallbackQuery callbackQuery) {
        if (data[1].equals(Utils.CallbackData.ACTION_VIEW)) {
            Long contentId = Long.parseLong(data[2]);
            Content content = contentRepository.findById(contentId).orElse(null);
            if (content == null) return;

            if (content instanceof TextContent textContent) {
                InlineKeyboardMarkup inlineKeyboardMarkup = studentInlineKeyboardService.createSingleButtonKeyboard("❌ o`chirish", Utils.CallbackData.DELETED);
                bot.myExecute(sendMsg.sendMessage(chatId, textContent.getText(), inlineKeyboardMarkup));
            } else if (content instanceof AttachmentContent attachmentContent) {
                Attachment attachment = attachmentContent.getAttachment();
                if (attachment != null && attachment.getTelegramFileId() != null) {
                    InlineKeyboardMarkup keyboard = studentInlineKeyboardService.createSingleButtonKeyboard(
                            "Xabarni ochirish",
                            DELETED
                    );
                    bot.myExecute(sendMsg.sendVideo(chatId, attachment.getTelegramFileId(), keyboard));
                }
            } else if (content instanceof QuizContent quizContent) {
//                String url = SITE_URL + "/quiz/" + quizContent.getQuiz().getId();
                String url = urlBuilderService.generateQuizUrl(quizContent.getQuiz().getId());
//                InlineKeyboardMarkup keyboard = studentInlineKeyboardService.createSingleButtonKeyboard("❓ Testni ishlash", url);
                InlineKeyboardMarkup keyboard = studentInlineKeyboardService.createQuizContent("❓ Testni ishlash", url);
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
    private void handleAllCoursesCallback(User user, Long chatId, Integer messageId, String[] data, String callbackQueryId) {
        String type = data[1];

        switch (type) {
            case CATEGORY -> showAllCourses_Categories(chatId, messageId, Integer.parseInt(data[3]));
            case INSTRUCTOR -> showAllCourses_Instructors(chatId, messageId, Integer.parseInt(data[3]));
            case ACTION_LIST -> showAllCourse(chatId, messageId, data);
            case MODULE_PREFIX -> showAllCourseModules(user, chatId, messageId, data);
            case LESSON_PREFIX -> showAllCourseModuleLessons(user, chatId, messageId, data);
            case CONTENT_PREFIX -> sendAllCoursesLessonContent(user, chatId, messageId, data, callbackQueryId);
            case ACTION_SUBSCRIPTION -> userModuleSubscription(user, chatId, messageId, data, callbackQueryId);
            case ACTION_BUY -> userModuleBuy(user, chatId, messageId, data);
            case ACTION_BACK -> sendAllCoursesPage(chatId, messageId);
        }
    }

    private void userModuleBuy(User user, Long chatId, Integer messageId, String[] data) {

        String type = data[2];
        Long id = Long.valueOf(data[4]);
        String datum = data[3];

        if (type.equals(Utils.CallbackData.ACTION_VIEW)) {
            ModuleDetailDTO moduleDetailDTO = moduleService.read(id);
            String message = String.format(
                    """
                            📦 <b>%s</b>
                            
                            📝 %s
                            
                            📚 Darslar soni: %d ta
                            👥 O‘quvchilar: %d ta
                            💵 Narxi: %d so‘m
                            
                            👉 Modulni sotib olish uchun quyidagi tugmani bosing:""",
                    moduleDetailDTO.getTitle(),
                    moduleDetailDTO.getDescription(),
                    moduleDetailDTO.getLessonCount(),
                    moduleDetailDTO.getModuleEnrollmentsCount(),
                    moduleDetailDTO.getPrice()
            );

            InlineKeyboardMarkup inlineKeyboardMarkup = studentInlineKeyboardService.buildPurchaseButton(id, datum);
            bot.myExecute(sendMsg.editMessage(chatId, messageId, message, inlineKeyboardMarkup));
        } else {

            String join = String.join(":",
                    Utils.CallbackData.ALL_COURSES_PREFIX,
                    Utils.CallbackData.LESSON_PREFIX,
                    datum,
                    id.toString(),
                    Utils.CallbackData.ACTION_PAGE,
                    "0"
            );
            String[] split = join.split(":");
            showAllCourseModuleLessons(user, chatId, messageId, split);

        }

    }

    private void userModuleSubscription(User user, Long chatId, Integer messageId, String[] data, String callbackQueryId) {
        String type = data[2];
        Long id = Long.valueOf(data[4]);
        String datum = data[3];

        switch (type) {
            case Utils.CallbackData.ACTION_VIEW -> {

                ModuleDetailDTO moduleDetailDTO = moduleService.read(id);

                String msg = String.format(
                        "📦 <b>%s</b>\n━━━━━━━━━━━━━━━━\n📚 %d ta dars\n👥 %d ta o‘quvchi\n💵 %d so‘m\n━━━━━━━━━━━━━━━━\n📖 Tavsif:\n%s\n━━━━━━━━━━━━━━━━\n👉 Obuna bo‘lishni xohlaysizmi?",
                        moduleDetailDTO.getTitle(),
                        moduleDetailDTO.getLessonCount(),
                        moduleDetailDTO.getModuleEnrollmentsCount(),
                        moduleDetailDTO.getPrice() / 100,
                        moduleDetailDTO.getDescription()
                );

                InlineKeyboardMarkup inlineKeyboardMarkup = studentInlineKeyboardService.buildYesNoKeyboard(id, datum);
                bot.myExecute(sendMsg.editMessage(chatId, messageId, msg, inlineKeyboardMarkup));
                return;
            }
            case Utils.CallbackData.ACTION_CANCEL -> {

                AnswerCallbackQuery answerCallbackQuery = sendMsg.answerCallbackQuery(callbackQueryId, "Obuna Bomadingiz");

                bot.myExecute(answerCallbackQuery);


            }
            case Utils.CallbackData.ACTION_CONFIRM -> {

                ModuleEnrollmentResponseDTO enrollUser = moduleEnrollmentService.enrollUser(new ModuleEnrollmentRequestDTO(user.getId(), id));

                AnswerCallbackQuery answerCallbackQuery = sendMsg.answerCallbackQuery(callbackQueryId, "Obuna boldingiz");

                bot.myExecute(answerCallbackQuery);
            }

        }
        String join = String.join(":",
                Utils.CallbackData.ALL_COURSES_PREFIX,
                Utils.CallbackData.LESSON_PREFIX,
                datum,
                id.toString(),
                Utils.CallbackData.ACTION_PAGE,
                "0"
        );
        String[] split = join.split(":");
        showAllCourseModuleLessons(user, chatId, messageId, split);


    }

    private void sendAllCoursesLessonContent(User user, Long chatId, Integer messageId, String[] data, String callbackQueryId) {


        String datum = data[2];
        Long id = Long.parseLong(data[3]);

        LessonResponseDTO lessonResponseDTO = lessonService.read(id);

        System.out.println(lessonResponseDTO);

        Lesson lesson = lessonRepository.findById(id).orElseThrow(() -> new RuntimeException("Lesson Not Found"));
        Module module = moduleRepository.findById(lessonResponseDTO.getModuleId()).orElse(null);
        String title = module.getTitle();
        String courseTitle = module.getCourse().getTitle();

        boolean exists = paymentRepository.existsByUserAndModuleId(user, lessonResponseDTO.getModuleId());

        if (exists || lessonResponseDTO.isFree()
        ) {

            String lessonTemplate = String.format("""
                            📚 Kurs: %s
                            📦 Modul: %s
                            🎓 Dars: %s
                            
                            📝 %s
                            """,
                    courseTitle,
                    title,
                    lessonResponseDTO.getTitle(),
                    lessonResponseDTO.getContent());

            String backCallback = String.join(":",
                    Utils.CallbackData.ALL_COURSES_PREFIX,
                    Utils.CallbackData.LESSON_PREFIX,
                    datum,
                    lessonResponseDTO.getModuleId().toString(),
                    Utils.CallbackData.ACTION_PAGE,
                    "0"
            );
            InlineKeyboardMarkup inlineKeyboardMarkup = studentInlineKeyboardService.lessonContentsMenu(lesson, module.getId(), backCallback);
            bot.myExecute(sendMsg.editMessage(chatId, messageId, lessonTemplate, inlineKeyboardMarkup));

        } else {

            bot.myExecute(sendMsg.answerCallbackQuery(callbackQueryId, "Dasrni korish uchun sotib olisng!"));

        }


    }

    private void showAllCourseModuleLessons(User user, Long chatId, Integer messageId, String[] data) {

        String datum = data[2];
        Long id = Long.parseLong(data[3]);
        int pageNum = Integer.parseInt(data[5]);

        PageDTO<LessonResponseDTO> lessonResponseDTOPageDTO =
                moduleService.readLessons(id, pageNum, 10);

        List<LessonResponseDTO> lessons = lessonResponseDTOPageDTO.getContent();

        boolean purchased = paymentRepository.existsByUserAndModuleId(user, id);
        boolean hasSubscription = moduleEnrollmentRepository.existsByUserAndModuleId(user, id);

        String header = messageService.getMessage(
                BotMessage.LESSON_LIST_TITLE,
                lessons.size()
        );

        StringBuilder list = new StringBuilder();
        for (int i = 0; i < lessons.size(); i++) {
            LessonResponseDTO l = lessons.get(i);

            String status = null;
            if (!purchased) {
                status = l.isFree()
                        ? messageService.getMessage(BotMessage.LESSON_STATUS_FREE)
                        : messageService.getMessage(BotMessage.LESSON_STATUS_PAID);

            } else {
                status = "ochiq";
            }

            list.append(
                    messageService.getMessage(
                            BotMessage.LESSON_LIST_ITEM,
                            i + 1,
                            l.getTitle(),
                            status
                    )
            ).append("\n\n");
        }

        Long course = Objects.requireNonNull(moduleRepository.findCourseByModuleId(id).orElse(null)).getId();
        String finalMessage = header + "\n" + list;
        String backButton = String.join(":", Utils.CallbackData.ALL_COURSES_PREFIX, Utils.CallbackData.MODULE_PREFIX, datum, course.toString(), Utils.CallbackData.ACTION_PAGE, "0");
        InlineKeyboardMarkup inlineKeyboardMarkup = studentInlineKeyboardService.allCourseLessons(lessonResponseDTOPageDTO, backButton, id, datum, purchased, hasSubscription);

        bot.myExecute(sendMsg.editMessage(chatId, messageId, finalMessage, inlineKeyboardMarkup));

    }

    private void showAllCourseModules(User user, Long chatId, Integer messageId, String[] data) {

        Long id = Long.valueOf(data[3]);

        String datum = data[2];
        String[] split = datum.split("\\.");
        String incOrCat = split[0];
        String incOrCatId = split[1];
        int pageNumber = Integer.parseInt(data[5]);

        Course course = courseRepository.findById(id).orElse(null);
        if (course == null) return;

        PageDTO<ModuleDetailDTO> modulePageDTO = moduleService.read(id, pageNumber, 10);

        int number = 1;
        StringBuilder listBuilder = new StringBuilder();

        for (ModuleDetailDTO m : modulePageDTO.getContent()) {
            boolean purchased = paymentRepository.existsByUserAndModuleId(user, m.getId());
            boolean hasSubscription = moduleEnrollmentRepository.existsByUserAndModuleId(user, m.getId());

            String statusIcon;
            String statusText;

            if (hasSubscription && purchased) {

                statusIcon = "✅";
                statusText = messageService.getMessage(BotMessage.MODULE_STATUS_SUB_AND_PURCHASED); // "Obuna + Sotib olingan"
            } else if (hasSubscription) {

                statusIcon = "📖";
                statusText = messageService.getMessage(BotMessage.MODULE_STATUS_SUB_ONLY); // "Faqat obuna"
            } else {

                statusIcon = "🔒";
                statusText = messageService.getMessage(BotMessage.MODULE_STATUS_LOCKED); // "Yopiq"
            }

            listBuilder.append(
                    messageService.getMessage(
                            BotMessage.COURSE_MODULES_LIST_ITEM,
                            Utils.Numbering.toCircled(number++),
                            m.getTitle(),
                            String.valueOf(m.getLessonCount()),
                            m.getModuleEnrollmentsCount()
                    )
            ).append(" ").append(statusIcon).append(" ").append(statusText).append("\n\n");
        }


        String message = messageService.getMessage(
                BotMessage.COURSE_MODULES_LIST,
                course.getTitle(), // "%s" — kurs nomi
                String.valueOf(modulePageDTO.getTotalPages()),
                String.valueOf(modulePageDTO.getPageNumber()),
                listBuilder.toString().trim()
        );

        String backButton = String.join(":",
                Utils.CallbackData.ALL_COURSES_PREFIX,
                Utils.CallbackData.ACTION_LIST,
                incOrCat,
                incOrCatId,
                Utils.CallbackData.ACTION_PAGE,
                "0"
        );

        InlineKeyboardMarkup inlineKeyboardMarkup = studentInlineKeyboardService.allCourseModules(
                modulePageDTO, backButton, id, datum
        );

        bot.myExecute(sendMsg.editMessage(chatId, messageId, message, inlineKeyboardMarkup));
    }


    private void sendAllCoursesPage(Long chatId, Integer messageId) {
        String message = messageService.getMessage(BotMessage.ALL_COURSES_ENTRY_TEXT);
        InlineKeyboardMarkup inlineKeyboardMarkup = studentInlineKeyboardService.selectCategoryAndInstructor();
        bot.myExecute(sendMsg.editMessage(chatId, messageId, message, inlineKeyboardMarkup));
    }

    private void showAllCourse(Long chatId, Integer messageId, String[] data) {

        String type = data[2];
        Long id = Long.valueOf(data[3]);
        int pageNumber = Integer.parseInt(data[5]);
        String backButton = null;

        FilterDTO filterDTO = new FilterDTO();
        if (type.equals(Utils.CallbackData.CATEGORY)) {

            Category category = categoryRepository.findById(id).orElse(null);
            if (category == null || category.getName() == null) {
                showAllCourses_Categories(chatId, messageId, 0);
            }

            filterDTO.setCategoryTitle(List.of(Objects.requireNonNull(category).getName()));
            backButton = String.join(":", Utils.CallbackData.ALL_COURSES_PREFIX, Utils.CallbackData.CATEGORY, Utils.CallbackData.ACTION_PAGE, "0");


        } else if (type.equals(Utils.CallbackData.INSTRUCTOR)) {

            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                showAllCourses_Instructors(chatId, messageId, 0);
            }
            filterDTO.setInstructorName(List.of(Objects.requireNonNull(user).getProfile().getFirstName(), user.getProfile().getLastName()));
            backButton = String.join(":", Utils.CallbackData.ALL_COURSES_PREFIX, Utils.CallbackData.INSTRUCTOR, Utils.CallbackData.ACTION_PAGE, "0");


        }

        PageDTO<CourseDetailDTO> courses = courseService.filter(filterDTO, pageNumber, 10);
        InlineKeyboardMarkup inlineKeyboardMarkup = studentInlineKeyboardService.allCoursesMenu(courses, backButton, type, id);


        int number = 1;
        StringBuilder listBuilder = new StringBuilder();
        for (CourseDetailDTO c : courses.getContent()) {
            listBuilder.append(
                    messageService.getMessage(
                            BotMessage.ALL_COURSES_COURSE_LIST_ITEM,
                            Utils.Numbering.toCircled(number++),
                            c.getTitle(),
                            String.format("%.1f", c.getReviewSummary().getAverageRating()),
                            String.valueOf(c.getReviewSummary().getCount()),
                            String.valueOf(c.getModulesCount())
                    )
            ).append("\n\n");
        }

        String message = messageService.getMessage(
                BotMessage.ALL_COURSES_COURSES_LIST,
                String.valueOf(courses.getTotalPages()),
                String.valueOf(courses.getPageNumber()),
                listBuilder.toString().trim()
        );


        bot.myExecute(sendMsg.editMessage(chatId, messageId, message, inlineKeyboardMarkup));


    }

    /**
     * Another helper method to generate a star emoji string based on a count.
     *
     * @param count The number of stars to generate (1 to 5).
     * @return A string of star emojis (e.g., "⭐⭐⭐⭐").
     */
    private String generateStars(int count) {
        if (count < 1) return "☆☆☆☆☆";
        if (count > 5) count = 5;

        String filledStar = "⭐";
        String emptyStar = "☆";

        return filledStar.repeat(count) + emptyStar.repeat(5 - count);
    }

    /**
     * Foydalanuvchiga kategoriyalar ro'yxatini ko'rsatadi.
     */
    private void showAllCourses_Categories(Long chatId, Integer messageId, int pageNum) {
        Pageable pageable = PageRequest.of(pageNum, 10);

        Page<CategoryInfo> categoryPage = categoryRepository.findCategoriesWithCourseCount(pageable);


        StringBuilder listBuilder = new StringBuilder();
        List<CategoryInfo> categories = categoryPage.getContent();
        for (int i = 0; i < categories.size(); i++) {
            CategoryInfo category = categories.get(i);

            String listItem = messageService.getMessage(BotMessage.ALL_COURSES_LIST_ITEM, category.getName(), category.getCourseCount());

            listBuilder.append(Utils.Numbering.toCircled(i + 1)).append(". ").append(listItem).append("\n\n");
        }


        String finalText = messageService.getMessage(BotMessage.ALL_COURSES_CATEGORIES_LIST, categoryPage.getNumber() + 1, categoryPage.getTotalPages(), listBuilder.toString() // Yasagan ro'yxatimizni %s o'rniga qo'yamiz
        );


        InlineKeyboardMarkup keyboard = studentInlineKeyboardService.allCourses_categoriesMenu(categoryPage);


        bot.myExecute(sendMsg.editMessage(chatId, messageId, finalText, keyboard)); // ParseMode'ni HTML ga o'zgartiramiz
    }


    /**
     * Foydalanuvchiga mentorlar (instruktorlar) ro'yxatini ko'rsatadi.
     */
    private void showAllCourses_Instructors(Long chatId, Integer messageId, int pageNum) {
        Pageable pageable = PageRequest.of(pageNum, 10);

        Page<UserInfo> instructorPage = userRepository.findInstructorsWithSuccessfulCourses(pageable);


        StringBuilder listBuilder = new StringBuilder();
        List<UserInfo> instructors = instructorPage.getContent();
        for (int i = 0; i < instructors.size(); i++) {
            UserInfo instructor = instructors.get(i);
            String fullName = instructor.getFirstName() + " " + instructor.getLastName();

            String listItem = messageService.getMessage(BotMessage.ALL_COURSES_LIST_ITEM, fullName, instructor.getCourseCount());
            listBuilder.append(Utils.Numbering.toCircled(i + 1)).append(". ").append(listItem).append("\n\n");
        }


        String finalText = messageService.getMessage(BotMessage.ALL_COURSES_MENTORS_LIST, instructorPage.getNumber() + 1, instructorPage.getTotalPages(), listBuilder.toString());


        InlineKeyboardMarkup keyboard = studentInlineKeyboardService.allCourses_instructorsMenu(instructorPage);


        bot.myExecute(sendMsg.editMessage(chatId, messageId, finalText, keyboard));
    }



    /**
     * Foydalanuvchi a'zo bo'lgan kurslarni sahifalangan holda ko'rsatadi.
     */
    private void showMyCourses(User user, Long chatId, Integer messageId, int pageNum) {
        Pageable pageable = PageRequest.of(pageNum, PAGE_SIZE, Sort.by("title"));
        Page<Course> coursePage = courseRepository.findDistinctEnrolledCoursesForUser(user.getId(), pageable);
        String text = messageService.getMessage(BotMessage.MY_COURSES_TITLE, pageNum + 1, coursePage.getTotalPages());
        InlineKeyboardMarkup keyboard = studentInlineKeyboardService.myCoursesMenu(coursePage);

        try {
            EditMessageText editMessageText = new EditMessageText(text);
            editMessageText.setReplyMarkup(keyboard);
            editMessageText.setMessageId(messageId);
            editMessageText.setChatId(chatId);

            bot.execute(editMessageText);
        } catch (Exception e) {
            bot.myExecute(sendMsg.deleteMessage(chatId, messageId));
            bot.myExecute(sendMsg.sendMessage(chatId, text, keyboard));
        }
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
        if (module.getCourse().getThumbnailUrl() == null) {
            bot.myExecute(sendMsg.editMessage(chatId, messageId, text, keyboard));
        } else {
            bot.myExecute(sendMsg.deleteMessage(chatId, messageId));
            bot.myExecute(sendMsg.sendMessage(chatId, text, keyboard));
        }
    }

    /**
     * Tanlangan darsning kontentini (vazifa, video, matn) ko'rish uchun menyu ko'rsatadi.
     */
    private void showLessonContents(Long chatId, Integer messageId, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId).orElse(null);
        if (lesson == null) return;

        String text = messageService.getMessage(BotMessage.LESSON_DETAIL_TITLE, lesson.getTitle(), lesson.getContent() != null ? lesson.getContent() : "");
        String backCallback = String.join(":", Utils.CallbackData.MODULE_PREFIX, Utils.CallbackData.ACTION_VIEW, lesson.getModule().getId().toString());
        InlineKeyboardMarkup keyboard = studentInlineKeyboardService.lessonContentsMenu(lesson, lesson.getModule().getId(), backCallback);
        bot.myExecute(sendMsg.editMessage(chatId, messageId, text, keyboard));
    }



    /**
     * Foydalanuvchi kursga to'liq a'zo bo'lganligini tekshiradi.
     */
    private boolean checkIfEnrolledToFullCourse(User user, Course course) {
        long totalModulesInCourse = moduleRepository.countByCourse(course);
        if (totalModulesInCourse == 0) return true; // Modulsiz kursga a'zo hisoblanadi
        long enrolledModulesCount = moduleEnrollmentRepository.countByUserAndCourse(user.getId(), course.getId());
        return totalModulesInCourse == enrolledModulesCount;
    }

    public static String buildSupportInstructionHtml() {
        return """
                <b>📩 Hurmatli foydalanuvchi!</b>
                
                Savollaringiz biz uchun juda muhim. Siz murojaatingizni quyidagi formatlarda yuborishingiz mumkin:
                📝 <b>Matnli savol</b>
                🖼 <b>Rasmli xabar</b>
                
                <b>❗️ Yuborganingizdan so‘ng</b> savolingizni ko‘rib chiqishingiz uchun <b>tasdiqlash oynasi</b> chiqadi.
                ✅ <b>Tasdiqlaganingizdan keyin</b> savolingiz jamoamizga yuboriladi va siz bilan aloqaga chiqish uchun
                profilingizdagi ism, telefon raqamingiz va emailingiz ham qo‘shib yuboriladi.
                
                Iltimos, savolingizni imkon qadar aniq va batafsil yozing. 🔎
                """;
    }


    public String buildFaqListText(Page<Faq> faqPage) {
        if (faqPage.isEmpty()) {
            return "<b>📖 Yordam markazi (FAQ)</b>\n\n" +
                    "❌ Hozircha savollar mavjud emas.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<b>📖 Yordam markazi (FAQ)</b>\n\n");
        sb.append("Sizni qiziqtirgan savolni tanlang 👇\n\n");

        int startNumber = faqPage.getNumber() * faqPage.getSize() + 1;
        for (int i = 0; i < faqPage.getContent().size(); i++) {
            Faq faq = faqPage.getContent().get(i);
            sb.append("👉 <b>").append(startNumber + i).append(")</b> ")
                    .append(faq.getQuestion()).append("\n");
        }

        sb.append("\n");

        // Pagination bo‘yicha eslatma
        if (faqPage.hasNext()) {
            sb.append("▶️ Keyingi savollar uchun pastdagi tugmadan foydalaning.\n");
        }
        if (faqPage.hasPrevious()) {
            sb.append("◀️ Oldingi savollarni ham ko‘rishingiz mumkin.\n");
        }

        sb.append("\n🏠 Asosiy menyuga qaytish uchun <b>Asosiy menyu</b> tugmasini bosing.");

        return sb.toString();
    }

    public static String buildFaqAnswerText(FaqDTO faqDTO) {
        StringBuilder sb = new StringBuilder();

        sb.append("❓ <b>Savol:</b>\n");
        sb.append(faqDTO.getQuestion()).append("\n\n");

        sb.append("💡 <b>Javob:</b>\n");
        sb.append(faqDTO.getAnswer());

        return sb.toString();
    }
}
package uz.pdp.online_education.telegram.service.student;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import uz.pdp.online_education.enums.MessageStatus;
import uz.pdp.online_education.enums.TransactionStatus;
import uz.pdp.online_education.mapper.ContactMessageMapperImpl;
import uz.pdp.online_education.model.*;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.repository.*;
import uz.pdp.online_education.service.NotificationService;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.config.controller.OnlineEducationBot;
import uz.pdp.online_education.telegram.enums.BotMessage;
import uz.pdp.online_education.telegram.enums.UserState;
import uz.pdp.online_education.telegram.mapper.SendMsg;
import uz.pdp.online_education.telegram.model.TelegramUser;
import uz.pdp.online_education.telegram.service.TelegramUserService;
import uz.pdp.online_education.telegram.service.TelegramUserServiceImpl;
import uz.pdp.online_education.telegram.service.message.MessageService;
import uz.pdp.online_education.telegram.service.student.template.StudentInlineKeyboardService;
import uz.pdp.online_education.telegram.service.student.template.StudentProcessMessageService;
import uz.pdp.online_education.telegram.service.student.template.StudentReplyKeyboardService;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class StudentProcessMessageServiceImpl implements StudentProcessMessageService {

    private static final int PAGE_SIZE = 5; // Sahifadagi elementlar soni

    // --- DEPENDENCIES ---
    private final SendMsg sendMsg;
    private final MessageService messageService;
    private final TelegramUserRepository telegramUserRepository;
    private final StudentInlineKeyboardService studentInlineKeyboardService;
    private final OnlineEducationBot onlineEducationBot;
    private final StudentReplyKeyboardService studentReplyKeyboardService;
    private final CourseRepository courseRepository;
    private final ModuleEnrollmentRepository moduleEnrollmentRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final ContactMessageMapperImpl contactMessageMapperImpl;

    private final SpringTemplateEngine templateEngine;
    private final JavaMailSender mailSender;
    private final UserProfileRepository userProfileRepository;
    private final NotificationService notificationService;
    private final TelegramUserService telegramUserService;
    // --- PUBLIC METHODS (from Interface) ---

    @Override
    public void handleMessage(Message message) {
        Long chatId = message.getChatId();
        String text = message.getText();

        TelegramUser telegramUser = getOrCreateTelegramUser(chatId);
        if (telegramUser.getUser() == null) {
            onlineEducationBot.myExecute(sendMsg.sendMessage(chatId, "Please authenticate first."));
            return;
        }


        User user = telegramUser.getUser();
        UserProfile profile = user.getProfile();


        switch (text) {
            case Utils.START -> startMessage(chatId, profile);
            case Utils.DASHBOARD -> dashboardMessage(user, profile, chatId);
            case Utils.ReplyButtons.STUDENT_MY_COURSES -> sendMyCoursesPage(user, chatId);
            case Utils.ReplyButtons.STUDENT_ALL_COURSES -> sendAllCoursesPage(chatId);
            case Utils.ReplyButtons.STUDENT_BALANCE -> sendBalanceMenu(chatId, user);
            case Utils.ReplyButtons.STUDENT_HELP -> askForSupportMessage(chatId,user);

        }
    }

    @Override
    public void showMainMenu(User user, Long chatId) {
        String welcomeMessage = messageService.getMessage(
                BotMessage.START_MESSAGE_STUDENT,
                user.getProfile().getFirstName()
        );

        ReplyKeyboardMarkup replyKeyboardMarkup = studentReplyKeyboardService.studentMainMenu();
        SendMessage messageToSend = sendMsg.sendMessage(chatId, welcomeMessage, replyKeyboardMarkup);
        onlineEducationBot.myExecute(messageToSend);
    }

    /**
     * {@inheritDoc}
     * Bu metod mavjud dashboard xabarini tahrirlash uchun ishlatiladi.
     */
    @Override
    @Transactional(readOnly = true)
    public void showDashboard(User user, Long chatId, Integer messageId) {
        UserProfile profile = user.getProfile();
        String dashboardText = prepareStudentDashboardText(user, profile);
        InlineKeyboardMarkup inlineKeyboardMarkup = studentInlineKeyboardService.dashboardMenu();
        onlineEducationBot.myExecute(sendMsg.editMessage(chatId, messageId, dashboardText, inlineKeyboardMarkup));
    }

    // --- MESSAGE HANDLERS (Private methods for `handleMessage`) ---

    /**
     * Handles the /start command, sends a welcome message and main menu.
     */
    private void startMessage(Long chatId, UserProfile from) {
        telegramUserRepository.updateStateByChatId(chatId, UserState.STUDENT_MAIN_MENU);
        ReplyKeyboardMarkup replyKeyboardMarkup = studentReplyKeyboardService.studentMainMenu();
        SendMessage messageToSend = sendMsg.sendMessage(
                chatId,
                messageService.getMessage(BotMessage.START_MESSAGE_STUDENT, from.getFirstName()),
                replyKeyboardMarkup);
        onlineEducationBot.myExecute(messageToSend);
    }

    /**
     * Sends a new message containing the student's dashboard.
     */
    private void dashboardMessage(User user, UserProfile profile, Long chatId) {
        String dashboardText = prepareStudentDashboardText(user, profile);
        InlineKeyboardMarkup inlineKeyboardMarkup = studentInlineKeyboardService.dashboardMenu();
        onlineEducationBot.myExecute(sendMsg.sendMessage(chatId, dashboardText, inlineKeyboardMarkup));
    }

    /**
     * Displays a paginated list of courses the student is enrolled in.
     */
    private void sendMyCoursesPage(User user, Long chatId) {
        telegramUserRepository.updateStateByChatId(chatId, UserState.STUDENT_VIEWING_MY_COURSES);

        Pageable pageable = PageRequest.of(0, PAGE_SIZE, Sort.by("title"));
        Page<Course> coursePage = courseRepository.findDistinctEnrolledCoursesForUser(user.getId(), pageable);

        String messageText;
        InlineKeyboardMarkup keyboard;

        if (coursePage.isEmpty()) {
            messageText = messageService.getMessage(BotMessage.NO_ENROLLED_COURSES);
            String backCallback = String.join(":",
                    Utils.CallbackData.STUDENT_PREFIX,
                    Utils.CallbackData.ACTION_BACK,
                    Utils.CallbackData.BACK_TO_MAIN_MENU);
            keyboard = studentInlineKeyboardService.createSingleButtonKeyboard("⬅️ " + Utils.InlineButtons.BACK_TO_MAIN_MENU_TEXT, backCallback);
        } else {
            messageText = messageService.getMessage(
                    BotMessage.MY_COURSES_TITLE,
                    coursePage.getNumber() + 1,
                    coursePage.getTotalPages()
            );
            keyboard = studentInlineKeyboardService.myCoursesMenu(coursePage);
        }
        onlineEducationBot.myExecute(sendMsg.sendMessage(chatId, messageText, keyboard));
    }


    public void sendAllCoursesPage(Long chatId) {

        String message = messageService.getMessage(BotMessage.ALL_COURSES_ENTRY_TEXT);
        InlineKeyboardMarkup inlineKeyboardMarkup = studentInlineKeyboardService.selectCategoryAndInstructor();
        onlineEducationBot.myExecute(sendMsg.sendMessage(chatId, message, inlineKeyboardMarkup));

    }

//    private void sendBalanceMenu(Long chatId, User user) {
//
//        List<Module> modules = moduleEnrollmentRepository.findUnpaidModulesByUserId(user.getId());
//
//        Long totalAmount = paymentRepository.findTotalSuccessfulPaymentsByUserId(user.getId(), TransactionStatus.SUCCESS);
//
//        Payment payment = paymentRepository.findTopByUser_IdAndStatusOrderByCreatedAtDesc(user.getId(), TransactionStatus.SUCCESS).orElse(null);
//
//        messageService.getMessage(BotMessage.BALANCE_INFO_WITH_PENDING_PAYMENT,
//                // bu yerda %s orniga qoyiladigan malumitlarni berish kerka
//        );
//
//        messageService.getMessage(BotMessage.BALANCE_INFO_NO_PENDING_PAYMENT,
//                // bu yerda %s orniga qoyiladigan malumitlarni berish kerka
//        )
//
//    }

    @Override
    public void sendBalanceMenu(Long chatId, User user) {
        // --- 1. MA'LUMOTLARNI BAZADAN OLISH ---
        List<Module> unpaidModules = moduleEnrollmentRepository.findUnpaidModulesByUserId(user.getId());
        Long totalAmount = paymentRepository.findTotalSuccessfulPaymentsByUserId(user.getId(), TransactionStatus.SUCCESS);
        Payment lastPayment = paymentRepository.findTopByUser_IdAndStatusOrderByCreatedAtDesc(user.getId(), TransactionStatus.SUCCESS).orElse(null);
        long purchasedCoursesCount = paymentRepository.countByUser_IdAndStatus(user.getId(), TransactionStatus.SUCCESS);

        // --- 2. XABAR MATNINI TAYYORLASH ---
        String messageText;

        /**
         * ⏳ Kutilayotgan to'lovlar (1)	📜 To'lovlar tarixi
         * ⬅️ Orqaga"
         */

        if (!unpaidModules.isEmpty()) {
            // Agar to'lovni kutayotgan modullar bo'lsa
            Module firstUnpaidModule = unpaidModules.get(0);

            messageText = messageService.getMessage(
                    BotMessage.BALANCE_INFO_WITH_PENDING_PAYMENT,
                    // %s o'rniga qo'yiladigan ma'lumotlar (TARTIB MUHIM!):
                    formatAmount(totalAmount),                                 // 1. Umumiy xaridlar
                    purchasedCoursesCount,                                     // 2. Sotib olingan kurslar soni
                    lastPayment != null ? lastPayment.getModule().getTitle() : "Mavjud emas", // 3. O'ZGARDI: .getName() -> .getTitle()
                    lastPayment != null ? formatAmount(lastPayment.getAmount()) : "0 so'm",  // 4. Oxirgi to'lov summasi
                    lastPayment != null ? formatDate(lastPayment.getCreatedAt()) : "-",    // 5. Oxirgi to'lov sanasi
                    unpaidModules.size(),                                      // 6. To'lanmagan kurslar soni
                    firstUnpaidModule.getTitle(),                              // 7. O'ZGARDI: .getName() -> .getTitle()
                    formatAmount(firstUnpaidModule.getPrice())                 // 8. To'lanmagan kurs narxi
            );
        } else {
            // Agar qarzdorlik bo'lmasa
            messageText = messageService.getMessage(
                    BotMessage.BALANCE_INFO_NO_PENDING_PAYMENT,
                    // %s o'rniga qo'yiladigan ma'lumotlar (TARTIB MUHIM!):
                    formatAmount(totalAmount),                                 // 1. Umumiy xaridlar
                    purchasedCoursesCount,                                     // 2. Sotib olingan kurslar soni
                    lastPayment != null ? lastPayment.getModule().getTitle() : "Mavjud emas", // 3. O'ZGARDI: .getName() -> .getTitle()
                    lastPayment != null ? formatAmount(lastPayment.getAmount()) : "0 so'm",  // 4. Oxirgi to'lov summasi
                    lastPayment != null ? formatDate(lastPayment.getCreatedAt()) : "-"     // 5. Oxirgi to'lov sanasi
            );
        }
        boolean hasPending = !unpaidModules.isEmpty();
        int pendingCount = unpaidModules.size();
        InlineKeyboardMarkup inlineKeyboardMarkup = studentInlineKeyboardService.createBalanceMenuKeyboard(hasPending, pendingCount);
        onlineEducationBot.myExecute(sendMsg.sendMessage(chatId, messageText, inlineKeyboardMarkup));

    }

    private String formatAmount(Long amount) {
        if (amount == null || amount == 0) {
            return "0 so'm";
        }
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount / 100) + " so'm";
    }

    // FAQAT SHU METOD QOLISHI KERAK
    private String formatDate(Timestamp timestamp) {
        if (timestamp == null) {
            return "-";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return timestamp.toLocalDateTime().format(formatter);
    }

    public void askForSupportMessage(Long chatId, User user) {
        // Foydalanuvchini yordam xabarini kutish holatiga o'tkazamiz
        telegramUserRepository.updateStateByChatId(chatId, UserState.STUDENT_AWAITING_SUPPORT_MESSAGE);

        // Foydalanuvchiga yo'naltiruvchi xabar yuborish
        String supportPromptMessage = "Iltimos, muammo yoki savolingizni bitta xabarda yozib yuboring. " +
                "Operatorlarimiz tez orada siz bilan bog'lanishadi.";

        onlineEducationBot.myExecute(sendMsg.sendMessage(chatId, supportPromptMessage));
        telegramUserService.updateUserState(chatId,UserState.USER_SUPPORT_MESSAGE);
    }


//                    /**
//                     * Foydalanuvchi yordam so'ragan paytdagi xabarni qayta ishlaydi.
//                     */
//                    private void handleSupportMessage(Long chatId, User user, String receivedMessageStudentStudent) {
//                        UserProfile userProfile = user.getProfile();
//                        String studentFullName = userProfile.getFirstName() + " " + userProfile.getLastName();
//                        String studentEmail = userProfile.getEmail();
//                        String studentUsername = user.getUsername() != null ? user.getUsername() : "Mavjud emas";
//
//                        ContactMessageRequestDTO requestDTO = new ContactMessageRequestDTO();
//                        requestDTO.setFullName(studentFullName);
//                        requestDTO.setEmail(studentEmail);
//                        requestDTO.setMessage(receivedMessageStudentStudent);
//
//                        ContactMessage contactMessage = contactMessageMapperImpl.toEntity(requestDTO);
//                        ContactMessage savedMessage = contactMessageRepository.save(contactMessage);
//                        log.info("New contact message saved with ID: {} from user: {}", savedMessage.getId(), studentUsername);
//
//                        sendSupportMessageToAdmins(savedMessage); // Adminlarga xabar yuborish
//
//                        telegramUserRepository.updateStateByChatId(chatId, UserState.STUDENT_MAIN_MENU);
//                        String responseToStudent = "Sizning xabaringiz muvaffaqiyatli yuborildi. Tez orada siz bilan bog'lanishadi.";
//                        onlineEducationBot.myExecute(sendMsg.sendMessage(chatId, responseToStudent));
//                    }
//
//                    /**
//                     * Adminlarga support xabarini yuboradi.
//                     */
//                    private void sendSupportMessageToAdmins(ContactMessage message) {
//                        List<UserProfile> allUsers = userProfileRepository.findAll(); // Optimallashtirish kerak
//                        List<UserProfile> adminUsers = allUsers.stream()
//                                .filter(user -> user.getUser().getRole() != null && user.getUser().getRole().name().equals("ADMIN"))
//                                .collect(Collectors.toList());
//
//                        if (adminUsers.isEmpty()) {
//                            log.warn("No admin users found to send support message.");
//                            return;
//                        }
//
//                        // Har bir admin uchun ma'lumotlarni tayyorlash va yuborish
//                        for (UserProfile admin : adminUsers) {
//                            Optional<TelegramUser> telegramUserOptional = telegramUserRepository.findById(admin.getId());
//
//                            // Telegram orqali xabar yuborish
//                            if (telegramUserOptional.isPresent() && telegramUserOptional.get().getChatId() != null) {
//                                Long adminChatId = telegramUserOptional.get().getChatId();
//                                try {
//                                    String telegramMessageText = String.format(
//                                            "Yangi support xabari:\n\n" +
//                                                    "Ism: %s\n" +
//                                                    "Email: %s\n" +
//                                                    "Xabar: %s",
//                                            message.getFullName(),
//                                            message.getEmail(),
//                                            message.getMessage()
//                                    );
//                                    SendMessage sendMessage = sendMsg.sendMessage(adminChatId, telegramMessageText);
//                                    onlineEducationBot.myExecute(sendMessage);
//                                    log.info("Support message sent via Telegram to admin chat ID: {}", adminChatId);
//                                } catch (Exception e) {
//                                    log.error("Failed to send Telegram message to admin {}: {}", adminChatId, e.getMessage());
//                                }
//            }
//
//            // Email orqali xabar yuborish
//            if (admin.getEmail() != null && !admin.getEmail().isEmpty()) {
//                try {
//                    // Ushbu qism sizning talabingiz asosida o'zgartirildi
//                    // Xabarga javob berish uchun alohida metod yaratildi
//                    sendReplyToStudentByEmail(message, admin.getEmail()); // Bu metodni quyida ko'rsataman
//                    log.info("Support message email sent to admin: {}", admin.getEmail());
//                } catch (Exception e) {
//                    log.error("Failed to send support message email to admin {}: {}", admin.getEmail(), e.getMessage());
//                }
//
//            }
//        }
//    }

    /**
     * Ushbu metod foydalanuvchi (student) yuborgan support xabariga adminning javobini
     * HTML formatida email orqali yuboradi.
     *
     * @param originalMessage Talabadan kelgan asl support xabari
     * @param replyText       Admin tomonidan yozilgan javob matni
     * @param studentEmail    Talabaning email manzili
     */
    public void sendReplyToStudent(ContactMessage originalMessage, String replyText, String studentEmail) throws MessagingException {

        // Thymeleaf context yaratish va ma'lumotlarni joylash
        Context context = new Context();
        context.setVariable("fullName", originalMessage.getFullName()); // Talabaning ismi
        context.setVariable("userMessage", originalMessage.getMessage()); // Talabaning asl xabari
        context.setVariable("replyText", replyText); // Adminning javobi

        // Template'ni render qilish
        String htmlContent = templateEngine.process("contact_reply", context); // "contact_reply" bu resources/templates/contact_reply.html

        // Emailni tayyorlash
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8"); // true - multipart


        helper.setTo(studentEmail); // Talabaning email'iga yuborish
        helper.setSubject("Javobingiz - Online Education Support"); // Xat mavzusi
        helper.setText(htmlContent, true); // HTML formatida
        helper.setFrom("noreply@online-education.com"); // Yuboruvchi manzil

        // Emailni yuborish
        mailSender.send(mimeMessage);

        // Xabarni "REPLIED" holatiga o'tkazish
        originalMessage.setStatus(MessageStatus.REPLIED);
        contactMessageRepository.save(originalMessage);
        log.info("Reply email sent successfully to {} for message ID {}", studentEmail, originalMessage.getId());


    }

    /**
     * Ushbu metod adminlar uchun mo'ljallangan bo'lib, admin uni chaqirib,
     * talabaga javob yozgandan so'ng, shu metod orqali javob yuboriladi.
     *
     * @param originalMessage Talabadan kelgan asl support xabari
    //     * @param replyText       Admin tomonidan yozilgan javob matni
     */
//    private void sendReplyToStudentByEmail(ContactMessage originalMessage, String adminEmail) {
//        // Bu yerda aslida admin qaysi talabaga javob yozayotganini aniqlash kerak.
//        // Hozircha, faqat misol uchun, originalMessage.getEmail() ni o'zgartirmayman.
//        // Agar sizda adminning javob yozayotgan talabaning email'ini bilish mexanizmi bo'lsa,
//        // shu yerda to'g'ri studentEmail ni topish kerak.
//
//        // Misol: Agar admin uni shu yerda chaqirayotgan bo'lsa, unda originalMessage ning o'ziga
//        // qarab studentEmail topilsin.
//        // Agar AdminView dan yuborilsa, u yerda studentEmail olinadi.
//
//        // Shartli ravishda, biz originalMessage.getEmail() ni ishlatamiz
//        // agar sizning loyihangizda bu boshqa bo'lsa, o'zgartiring.
//        String studentEmail = originalMessage.getEmail();
//
//        // Sizning bergan kod blokidagi logikani ushbu metodga o'tkazamiz
//        try {
//            Context context = new Context();
//            context.setVariable("fullName", originalMessage.getFullName());
//            context.setVariable("userMessage", originalMessage.getMessage());
//            // ReplyText ni qayerdandir olish kerak. Hozircha bu placeholder.
//            // Agar admin javobi boshqa manbadan kelsa, uni shu yerga o'tkazish kerak.
//            // Masalan: context.setVariable("replyText", adminReplyText);
//            // Hozircha uni bo'sh qoldiramiz yoki log da ko'rsatamiz.
//            // Bizning logikamizda, admin javob yozgandan keyin bu metod chaqiriladi,
//            // shuning uchun replyText mavjud bo'lishi kerak.
//            // Shartli ravishda "Admin hali javob yozmagan" deb qo'yamiz
//            String adminProvidedReplyText = "Admin hali javob yozmagan."; // Placeholder
//
//            // Agar sizda haqiqiy admin javobi bor bo'lsa, uni bu yerga qo'ying:
//            // String adminReplyText = getAdminReplyForMessage(originalMessage.getId());
//            // context.setVariable("replyText", adminReplyText);
//
//            // Yoki, agar admin javob yozish uchun xabar yuborgan bo'lsa,
//            // uni boshqa bir state dan ushlab olish kerak.
//
//            // Sizning bergan kod blokidagi "replyText" ni qanday olishni aniqlashtirish kerak.
//            // Agar u oldingi qadamlardan kelgan bo'lsa, shu yerga o'tkazilsin.
//            // Hozircha, biz uni placeholder qilib qoldiramiz yoki null qilib qoldiramiz.
//            // Agar sizning "sendSupportMessageToAdmins" metodida adminlar uchun olingan javob bo'lsa,
//            // uni shu yerga uzatish kerak.
//
//            // Agar sizning kodingizda `replyText`ni qayerdandir olish imkoniyati bo'lsa,
//            // shu yerga qo'ying. Masalan, agar admin javob yozib, uni ma'lumotlar bazasida
//            // ContactMessage ga bog'langan bo'lsa.
//
//            // Hozirgi holatda, bizda "replyText" qayerdan kelishi aniq emas.
//            // Agar siz admin javobini yuborish uchun alohida metod yozayotgan bo'lsangiz,
//            // u yerda `replyText` ham birga uzatilishi kerak.
//
//            // Misol: Agar sizda `adminReplyService` bo'lsa:
//            // String adminReply = adminReplyService.getReply(originalMessage.getId());
//            // context.setVariable("replyText", adminReply);
//
//            // Agar sizning talabingiz bu: "admin yozgan javobni studentga yuborish"
//            // unda bu metodni chaqirishdan oldin admin javobini olishingiz kerak.
//
//            // Agar siz shu metodda email template'ni ochib, administratorga (adminEmail)
//            // studentning xabarini va siz yozgan javobni ko'rsatmoqchi bo'lsangiz,
//            // unda ushbu metod emas, balki `sendSupportMessageToAdmins` metodi
//            // ga o'zgartirish kiritish kerak.
//
//            // Sizning bergan kod blokingiz "studentga javob yuborish" haqida emas,
//            // balki "adminning javobini studentga yuborish" haqida.
//            // Bu ma'noda, yuqoridagi `sendReplyToStudent` metodi to'g'riroq.
//
//            // Lekin sizning talabingiz bo'yicha, bu qismni shunaqa logikaga o'zgartirish
//            // kerak bo'lsa, unda `replyText` qayerdandir kelishi kerak.
//
//            // Agar siz shu kodni `sendSupportMessageToAdmins` ichida adminlarga
//            // "sizning javobingizni studentga yuborish uchun shunday qiling" deb
//            // ko'rsatmoqchi bo'lsangiz, unda bu yerda `replyText` ni placeholder qilib
//            // qoldiramiz.
//
//            // Agar siz studentga javob yuborishni shu yerda amalga oshirmoqchi bo'lsangiz,
//            // unda `studentEmail` va `replyText`ni to'g'ri o'tkazishingiz kerak.
//
//            // Mana, sizning kod blokidagi logikani studentga javob yuborish uchun
//            // `sendReplyToStudent` metodi ichiga moslashtiramiz.
//            // QAYTA KO'RIB CHIQISH: Bu yerda `adminEmail` emas, `studentEmail` bo'lishi kerak.
//            // Shuning uchun `sendReplyToStudent` metodini chaqirish to'g'riroq.
//
//            // Agar sizning maqsad: admin yozgan javobni (replyText) studentga HTML formatida yuborish
//            // Unda `sendReplyToStudent` metodini to'g'ri `replyText` bilan chaqiring.
//
//            // Misol: agar sizda admin javobi bor bo'lsa:
//            // String adminReply = "Sizning muammongiz hal qilindi.";
//            // sendReplyToStudent(originalMessage, adminReply, originalMessage.getEmail());
//
//            // Shunday qilib, sizning bergan kod blokingizni umumiy qilib olsak:
//            // Biz ushbu metodni (sendReplyToStudentByEmail) ishlatib,
//            // email jo'natishni amalga oshiramiz.
//
//            // Bu yerda `replyText`ni qanday olinishini aniqlashtirish muhim.
//            // Agar `replyText` admin tomonidan yuborilgan bo'lsa, uni shu yerga olish kerak.
//            // Agar siz adminlar uchun ham shunday funksionallik yaratayotgan bo'lsangiz,
//            // unda bu metodni administrator interfeysidan chaqirishingiz kerak.
//
//            // Hozircha, men bu yerda `replyText`ni "Admin javobi kelib tushdi" deb qoldiryapman,
//            // chunki uning qayerdan kelishi aniq emas. Agar uni yuborish kerak bo'lsa,
//            // `sendReplyToStudent` metodini to'g'ri `replyText` bilan chaqiring.
//
//            // Sizning bergan kod blokida "message" bu ContactMessage obyekti,
//            // "replyText" esa javob matni.
//            // "message.getEmail()" - bu talabaning emaili.
//
//            // Kodni to'g'rilab, `sendReplyToStudent` metodini chaqiramiz.
//            // Lekin bizda `replyText` yo'q. Agar uni shu yerda ta'minlasak, unda `sendReplyToStudent` ishlaydi.
//
//            // Yoki, sizning bergan kod blokini shunday tushunish mumkin:
//            // "Admin yozgan javobni (replyText) studentning emailiga HTML qilib yuborish"
//
//            // Bu holatda, `sendReplyToStudent` metodi eng mos keladi.
//            // Lekin `replyText` va `originalMessage` ni shu metodga o'tkazish kerak.
//
//            // Shuning uchun men `sendReplyToStudent` metodini chaqirishni tavsiya qilaman
//            // va `replyText` ni shu yerda aniqlab oling.
//
//            // Agar sizning maqsadingiz shu metod orqali studentga javob yuborish bo'lsa,
//            // unda `sendReplyToStudent` metodini `replyText` bilan chaqiring.
//            // QAYTA O'ZARTIRISH: Men `sendReplyToStudent` metodini chaqiraman, lekin
//            // `replyText` placeholder bo'ladi.
//
//            // Sizning kodingizda emailni `noreply@online-education.com` dan yuborish nazarda tutilgan.
//            // Men bu manzilni `supportEmailAddress` ga bog'ladim.
//
//            // Shunday qilib, sizning bergan kod blokini `sendReplyToStudent` ichiga joylashtirdim.
//            // Agar siz `sendReplyToStudent` metodini chaqirayotgan bo'lsangiz,
//            // uni to'g'ri `replyText` bilan chaqiring.
//
//            // Misol:
//            // String adminReply = "Sizning so'rovingiz ko'rib chiqildi...";
//            // sendReplyToStudent(originalMessage, adminReply, originalMessage.getEmail());
//
//
//            // Sizning bergan kod blokini ushbu metod ichiga moslashtiramiz:
//            Context contextForReply = new Context();
//            contextForReply.setVariable("fullName", originalMessage.getFullName());
//            contextForReply.setVariable("userMessage", originalMessage.getMessage());
//            // Bu yerda sizning "replyText" ni qayerdandir olishingiz kerak.
//            // Agar u admin tomonidan yozilgan bo'lsa, uni shu yerga uzating.
//            // Hozircha placeholder:
//            String adminGeneratedReplyText = "Admin hali javob yozmagan."; // Bu placeholder!
//            // Agar admin javobi boshqa bir manbadan kelsa, uni bu yerga o'tkazing.
//            // contextForReply.setVariable("replyText", adminGeneratedReplyText);
//
//            // Agar sizning `sendSupportMessageToAdmins` metodida olingan `adminEmail`
//            // emas, balki `studentEmail` bo'lsa, bu yerda o'zgartirish kiritish kerak.
//            // Hozirgi `sendSupportMessageToAdmins` da `adminEmail` ketyapti.
//            // Bu metod adminlarga xabar yuborish uchun emas, balki studentga javob yuborish uchun.
//
//            // SHUNING UCHUN ALOHIDA METODNI ISHLATAMIZ.
//            // Yuqoridagi `sendReplyToStudent` metodi aniq ishlaydi.
//
//            // Quyidagi kodni shunchaki yo'nalish ko'rsatish uchun qoldiramiz:
//            // Bu kod `sendReplyToStudent` ichida bo'lishi kerak.
//            //
//            // MimeMessage mimeMessage = mailSender.createMimeMessage();
//            // MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
//            // helper.setTo(originalMessage.getEmail()); // Student email
//            // helper.setSubject("Javobingiz - Online Education Support");
//            // helper.setText(htmlContent, true); // HTML kontent
//            // helper.setFrom(supportEmailAddress);
//            // mailSender.send(mimeMessage);
//            // originalMessage.setStatus(MessageStatus.REPLIED);
//            // contactMessageRepository.save(originalMessage);
//
//        } catch (Exception e) {
//            // Xatolikni log qilish
//            log.error("Email yuborishda xatolik (talabaga javob): {}", e.getMessage());
//            // Xatolikni tashlash
//            throw new IllegalStateException("Email yuborishda xatolik yuz berdi", e);
//        }
//    }


    // --- HELPER METHODS ---

    /**
     * Prepares the formatted text for the student's dashboard.
     */
    private String prepareStudentDashboardText(User user, UserProfile profile) {
        Integer activeCoursesCount = moduleEnrollmentRepository.countActiveCoursesByUserId(user.getId());
        Double averageProgressDouble = moduleEnrollmentRepository.findAverageProgressByUserId(user.getId());
        int averageProgress = (averageProgressDouble != null) ? averageProgressDouble.intValue() : 0;
        Integer completedModulesCount = moduleEnrollmentRepository.countCompletedModulesByUserId(user.getId());
        String progressBar = createProgressBar(averageProgress);

        return messageService.getMessage(
                BotMessage.DASHBOARD_STUDENT,
                profile.getFirstName() + " " + profile.getLastName(),
                profile.getEmail(), user.getUsername(), user.getRole().name(),
                activeCoursesCount, averageProgress, progressBar, completedModulesCount
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
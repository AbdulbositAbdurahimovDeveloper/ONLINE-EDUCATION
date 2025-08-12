package uz.pdp.online_education.telegram.service.student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.model.UserProfile;
import uz.pdp.online_education.repository.*;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.config.controller.OnlineEducationBot;
import uz.pdp.online_education.telegram.enums.BotMessage;
import uz.pdp.online_education.telegram.enums.UserState;
import uz.pdp.online_education.telegram.mapper.SendMsg;
import uz.pdp.online_education.telegram.model.TelegramUser;
import uz.pdp.online_education.telegram.service.message.MessageService;
import uz.pdp.online_education.telegram.service.student.template.StudentInlineKeyboardService;
import uz.pdp.online_education.telegram.service.student.template.StudentProcessMessageService;
import uz.pdp.online_education.telegram.service.student.template.StudentReplyKeyboardService;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentProcessMessageServiceImpl implements StudentProcessMessageService {

    private static final int PAGE_SIZE = 5; // Sahifadagi elementlar soni

    // --- DEPENDENCIES ---
    private final SendMsg sendMsg;
    private final MessageService messageService;
    private final ModuleEnrollmentRepository moduleEnrollmentRepository;
    private final TelegramUserRepository telegramUserRepository;
    private final StudentInlineKeyboardService studentInlineKeyboardService;
    private final OnlineEducationBot onlineEducationBot;
    private final StudentReplyKeyboardService studentReplyKeyboardService;
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final ContentRepository contentRepository;
    private final PaymentRepository paymentRepository;

    // --- PUBLIC HANDLER METHOD ---
    @Override
    public void handleMessage(Message message) {
        Long chatId = message.getChatId();
        String text = message.getText();
        Integer messageId = message.getMessageId();

        TelegramUser telegramUser = getOrCreateTelegramUser(chatId);
        if (telegramUser.getUser() == null) {
            onlineEducationBot.myExecute(sendMsg.sendMessage(chatId, "Please authenticate first."));
            return;
        }

        User user = telegramUser.getUser();
        UserProfile profile = user.getProfile();

        switch (text) {
            case Utils.START -> startMessage(chatId, profile);
            case Utils.DASHBOARD -> dashboardMessage(user, profile, chatId, messageId);
            case Utils.ReplyButtons.STUDENT_MY_COURSES -> sendMyCoursesPage(user, chatId, 0);

            case Utils.ReplyButtons.STUDENT_ALL_COURSES -> sendAllCoursesPage(chatId, 0);
            case Utils.ReplyButtons.STUDENT_BALANCE -> sendBalanceMenu(chatId);
            case Utils.ReplyButtons.STUDENT_HELP -> askForSupportMessage(chatId);
        }
    }

    @Override
    public void showMainMenu(User user, Long chatId) {
//        telegramUserRepository.updateStateByChatId(chatId, UserState.STUDENT_MAIN_MENU);

        String welcomeMessage = messageService.getMessage(
                BotMessage.START_MESSAGE_STUDENT,
                user.getProfile().getFirstName() // Ismni User'dan olamiz
        );

        ReplyKeyboardMarkup replyKeyboardMarkup = studentReplyKeyboardService.studentMainMenu();
        SendMessage sendMessage = sendMsg.sendMessage(chatId, welcomeMessage, replyKeyboardMarkup);
        onlineEducationBot.myExecute(sendMessage);
    }


    /**
     * {@inheritDoc}
     * Bu metod mavjud dashboard xabarini tahrirlash uchun ishlatiladi.
     * Masalan, "Logout -> No" tugmasi bosilganda.
     */
    @Override
    @Transactional(readOnly = true) // Bu metod faqat bazadan o'qiydi
    public void showDashboard(User user, Long chatId, Integer messageId) {
        // 1. Foydalanuvchi profilini olamiz
        UserProfile profile = user.getProfile();

        // 2. Dashboard uchun matnni tayyorlaymiz (bu metod sizda allaqachon mavjud)
        String dashboardText = prepareStudentDashboardText(user, profile);

        // 3. "Logout" tugmasini yasaymiz (bu ham sizda mavjud)
        InlineKeyboardMarkup inlineKeyboardMarkup = studentInlineKeyboardService.dashboardMenu();

        // 4. Mavjud xabarni yangi matn va tugmalar bilan tahrirlaymiz
        onlineEducationBot.myExecute(sendMsg.editMessage(chatId, messageId, dashboardText, inlineKeyboardMarkup));
    }
    private void startMessage(Long chatId, UserProfile from) {
        telegramUserRepository.updateStateByChatId(chatId, UserState.STUDENT_MAIN_MENU);
        ReplyKeyboardMarkup replyKeyboardMarkup = studentReplyKeyboardService.studentMainMenu();
        SendMessage sendMessage = sendMsg.sendMessage(
                chatId,
                messageService.getMessage(BotMessage.START_MESSAGE_STUDENT, from.getFirstName()),
                replyKeyboardMarkup);
        onlineEducationBot.myExecute(sendMessage);
    }

//    private void dashboardMessage(User user, UserProfile profile, Long chatId, Integer messageId) {
//        // Dashboard is a one-time view, so no state change is needed.
//        String dashboardText = prepareStudentDashboardText(user, profile);
//        InlineKeyboardMarkup inlineKeyboardMarkup = studentInlineKeyboardService.dashboardMenu();
//        SendMessage sendMessage = sendMsg.sendMessage(chatId, dashboardText, inlineKeyboardMarkup);
//        onlineEducationBot.myExecute(sendMessage);
//        onlineEducationBot.myExecute(sendMsg.editMarkup(chatId, messageId));
//    }
private void dashboardMessage(User user, UserProfile profile, Long chatId, Integer messageId) {
    // Yangi metodni chaqiramiz, lekin u EditMessage o'rniga SendMessage qilishi kerak bo'ladi
    // Keling, logikani to'g'ridan-to'g'ri shu yerda qoldirib, showDashboard'da tahrirlashni qilamiz.

    String dashboardText = prepareStudentDashboardText(user, profile); // Bu metod sizda mavjud
    InlineKeyboardMarkup inlineKeyboardMarkup = studentInlineKeyboardService.dashboardMenu();

    // Bu joyda yangi xabar yuborilishi kerak, chunki /dashboard buyrug'i keldi
    onlineEducationBot.myExecute(sendMsg.sendMessage(chatId, dashboardText, inlineKeyboardMarkup));

    // Eski xabarni (buyruqni) o'chirib yuborsak bo'ladi, ixtiyoriy
    // bot.myExecute(sendMsg.deleteMessage(chatId, messageId));
}

    // Mavjud "sendMyCoursesPage" metodini to'liq yozamiz
    private void sendMyCoursesPage(User user, Long chatId, int pageNumber) {
        telegramUserRepository.updateStateByChatId(chatId, UserState.STUDENT_VIEWING_MY_COURSES);

        // 1. Ma'lumotlarni bazadan sahifalangan holda olamiz
        Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE, Sort.by("title"));
        Page<Course> coursePage = courseRepository.findDistinctEnrolledCoursesForUser(user.getId(), pageable);

        String messageText;
        InlineKeyboardMarkup keyboard;

        // 2. Kurslar bor yoki yo'qligini tekshiramiz
        if (coursePage.isEmpty()) {
            messageText = messageService.getMessage(BotMessage.NO_ENROLLED_COURSES);
            // Faqat "Bosh menyuga qaytish" tugmasini yasaymiz
            String backCallback = String.join(":",
                    Utils.CallbackData.STUDENT_PREFIX,
                    Utils.CallbackData.ACTION_BACK,
                    Utils.CallbackData.BACK_TO_MAIN_MENU);
            keyboard = studentInlineKeyboardService.createSingleButtonKeyboard("⬅️ " + Utils.InlineButtons.BACK_TO_MAIN_MENU_TEXT, backCallback);
        } else {
            // 3. Xabar matni va tugmalarni tayyorlaymiz
            messageText = messageService.getMessage(
                    BotMessage.MY_COURSES_TITLE,
                    coursePage.getNumber() + 1,
                    coursePage.getTotalPages()
            );
            keyboard = studentInlineKeyboardService.myCoursesMenu(coursePage);
        }

        // 4. Foydalanuvchiga yangi xabar yuboramiz
        SendMessage sendMessage = sendMsg.sendMessage(chatId, messageText, keyboard);
        onlineEducationBot.myExecute(sendMessage);
    }

    private void sendAllCoursesPage(Long chatId, int pageNumber) {

    }

    private void sendBalanceMenu(Long chatId) {
        telegramUserRepository.updateStateByChatId(chatId, UserState.STUDENT_MANAGING_BALANCE);
        // TODO: Implement logic for showing the balance menu
        onlineEducationBot.myExecute(sendMsg.sendMessage(chatId, "'Balans va To'lovlar' bo'limi ishlab chiqilmoqda."));
    }

    private void askForSupportMessage(Long chatId) {
        telegramUserRepository.updateStateByChatId(chatId, UserState.STUDENT_AWAITING_SUPPORT_MESSAGE);
        // TODO: Implement logic for asking for a support message
        onlineEducationBot.myExecute(sendMsg.sendMessage(chatId, "'Yordam' bo'limi ishlab chiqilmoqda."));
    }

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

    private String createProgressBar(int percentage) {
        if (percentage < 0) percentage = 0;
        if (percentage > 100) percentage = 100;
        int filledBlocks = Math.round(percentage / 10.0f);
        int emptyBlocks = 10 - filledBlocks;
        return "█".repeat(filledBlocks) + "░".repeat(emptyBlocks);
    }

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
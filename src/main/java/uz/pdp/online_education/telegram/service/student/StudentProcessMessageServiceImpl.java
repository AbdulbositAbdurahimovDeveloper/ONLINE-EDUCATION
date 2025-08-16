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
    private final TelegramUserRepository telegramUserRepository;
    private final StudentInlineKeyboardService studentInlineKeyboardService;
    private final OnlineEducationBot onlineEducationBot;
    private final StudentReplyKeyboardService studentReplyKeyboardService;
    private final CourseRepository courseRepository;
    private final ModuleEnrollmentRepository moduleEnrollmentRepository;

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
            case Utils.ReplyButtons.STUDENT_BALANCE -> sendBalanceMenu(chatId);
            case Utils.ReplyButtons.STUDENT_HELP -> askForSupportMessage(chatId);
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
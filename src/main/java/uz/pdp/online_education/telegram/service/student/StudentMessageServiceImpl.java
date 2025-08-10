package uz.pdp.online_education.telegram.service.student;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import uz.pdp.online_education.enums.Role;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.model.UserProfile;
import uz.pdp.online_education.repository.ModuleEnrollmentRepository;
import uz.pdp.online_education.repository.TelegramUserRepository;
import uz.pdp.online_education.repository.UserRepository;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.config.controller.OnlineEducationBot;
import uz.pdp.online_education.telegram.enums.BotMessage;
import uz.pdp.online_education.telegram.mapper.SendMsg;
import uz.pdp.online_education.telegram.model.TelegramUser;
import uz.pdp.online_education.telegram.service.RoleService;
import uz.pdp.online_education.telegram.service.message.MessageService;
import uz.pdp.online_education.telegram.service.student.template.StudentInlineKeyboardService;
import uz.pdp.online_education.telegram.service.student.template.StudentMessageService;
import uz.pdp.online_education.telegram.service.student.template.StudentReplyKeyboardService;

@Service
@RequiredArgsConstructor
public class StudentMessageServiceImpl implements StudentMessageService {

    private final RoleService roleService;
    private final SendMsg sendMsg;
    private final MessageService messageService;
    private final UserRepository userRepository;
    private final ModuleEnrollmentRepository moduleEnrollmentRepository;
    private final TelegramUserRepository telegramUserRepository;
    private final StudentInlineKeyboardService studentInlineKeyboardService;
    private final OnlineEducationBot onlineEducationBot;
    private final StudentReplyKeyboardService studentReplyKeyboardService;

    /**
     * @param message
     * @return
     */
    @Override
    public void handleMessage(Message message) {
        Long chatId = message.getChatId();

        String text = message.getText();

        TelegramUser telegramUser = checkTelegramUser(chatId);

        User user = telegramUser.getUser();
        UserProfile profile = user.getProfile();

        Role currentRole = roleService.getUserRole(chatId);

        switch (text) {
            case Utils.START -> startMessage(chatId, message.getFrom());
            case Utils.DASHBOARD -> dashboardMessage(user, profile, chatId);
            case Utils.ReplyButtons.STUDENT_MY_COURSES -> {




            }
        }

    }

    private void dashboardMessage(User user, UserProfile profile, Long chatId) {
        String dashboardText = prepareStudentDashboardText(user, profile);
        InlineKeyboardMarkup inlineKeyboardMarkup = studentInlineKeyboardService.dashboardMenu();
        SendMessage sendMessage = sendMsg.sendMessage(chatId, dashboardText, inlineKeyboardMarkup);
        onlineEducationBot.myExecute(sendMessage);
    }

    private void startMessage(Long chatId, org.telegram.telegrambots.meta.api.objects.User from) {
        ReplyKeyboardMarkup replyKeyboardMarkup = studentReplyKeyboardService.studentMainMenu();
        SendMessage sendMessage = sendMsg.sendMessage(
                chatId,
                messageService.getMessage(BotMessage.START_MESSAGE_STUDENT,from.getUserName()),
                replyKeyboardMarkup);
        onlineEducationBot.myExecute(sendMessage);
    }


    /**
     * Prepares the formatted text for the Student Dashboard.
     * This version focuses solely on profile and learning progress, without balance information.
     *
     * @param user The User entity.
     * @param profile The UserProfile entity.
     * @return Formatted dashboard text ready to be sent.
     */
    private String prepareStudentDashboardText(User user, UserProfile profile) {
        Integer activeCoursesCount = moduleEnrollmentRepository.countActiveCoursesByUserId(user.getId());
        Double averageProgressDouble = moduleEnrollmentRepository.findAverageProgressByUserId(user.getId());
        int averageProgress = (averageProgressDouble != null) ? averageProgressDouble.intValue() : 0;

        Integer completedModulesCount = moduleEnrollmentRepository.countCompletedModulesByUserId(user.getId());

        String progressBar = createProgressBar(averageProgress);

        return messageService.getMessage(
                BotMessage.DASHBOARD_STUDENT,

                profile.getFirstName() + " " + profile.getLastName(), // %s: Ism-Familiya
                profile.getEmail(),                                 // %s: Email
                user.getUsername(),                                 // %s: Username
                user.getRole().name(),                              // %s: Rol

                activeCoursesCount,                                 // %d: Aktiv Kurslar
                averageProgress,                                    // %d: Umumiy O'zlashtirish
                progressBar,                                        // %s: Progress Bar
                completedModulesCount                               // %d: Yakunlangan Modullar
        );
    }

    /**
     * Helper method to create a visual progress bar string.
     * @param percentage The progress percentage (0-100).
     * @return A string like [███░░░░░░░].
     */
    private String createProgressBar(int percentage) {
        if (percentage < 0) percentage = 0;
        if (percentage > 100) percentage = 100;
        int filledBlocks = Math.round(percentage / 10.0f); // Yaxlitlash
        int emptyBlocks = 10 - filledBlocks;

        return "█".repeat(filledBlocks) + "░".repeat(emptyBlocks);
    }

    private TelegramUser checkTelegramUser(Long chatId) {
        TelegramUser user = telegramUserRepository.findByChatId(chatId).orElse(null);

        if (user == null) {
            TelegramUser telegramUser = new TelegramUser();
            telegramUser.setChatId(chatId);
            telegramUserRepository.save(telegramUser);
        }
        return user;
    }
}

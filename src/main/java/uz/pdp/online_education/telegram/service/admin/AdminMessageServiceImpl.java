package uz.pdp.online_education.telegram.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.model.UserProfile;
import uz.pdp.online_education.payload.AdminDashboardDTO;
import uz.pdp.online_education.repository.CourseRepository;
import uz.pdp.online_education.repository.TelegramUserRepository;
import uz.pdp.online_education.repository.UserRepository;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.config.controller.OnlineEducationBot;
import uz.pdp.online_education.telegram.enums.BotMessage;
import uz.pdp.online_education.telegram.enums.UserState;
import uz.pdp.online_education.telegram.mapper.SendMsg;
import uz.pdp.online_education.telegram.model.TelegramUser;
import uz.pdp.online_education.telegram.service.admin.template.AdminMessageService;
import uz.pdp.online_education.telegram.service.admin.template.InlineKeyboardService;
import uz.pdp.online_education.telegram.service.admin.template.ReplyKeyboardService;
import uz.pdp.online_education.telegram.service.message.MessageService;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * The primary service for handling text-based messages and commands for the Administrator role.
 * It acts as a router for admin commands and initiates various workflows.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMessageServiceImpl implements AdminMessageService {

    // Dependencies
    private final MessageService messageService;
    private final TelegramUserRepository telegramUserRepository;
    private final UserRepository userRepository;
    private final OnlineEducationBot onlineEducationBot;
    private final ReplyKeyboardService replyKeyboardService;
    private final InlineKeyboardService inlineKeyboardService;
    private final SendMsg sendMsg;
    private final CourseRepository courseRepository;

    /**
     * {@inheritDoc}
     */

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


        // Agar holat boshqacha bo'lsa, unda tugmalarni tekshiramiz.
        switch (text) {
            case Utils.START -> sendAdminWelcomeMessage(chatId, profile);
            case Utils.DASHBOARD -> sendDashboardMessage(chatId, user, profile);
            case Utils.ReplyButtons.ADMIN_USERS -> sendUsersMainMenu(chatId);
            case Utils.ReplyButtons.ADMIN_COURSES -> sendCoursesMainMenu(chatId);
            case Utils.ReplyButtons.ADMIN_SEND_MESSAGE -> initiateBroadcast(chatId);
            case Utils.ReplyButtons.ADMIN_STATISTICS -> sendStatisticsMessage(chatId);
        }

        UserState currentState = telegramUser.getUserState();
        if (currentState == UserState.ADMIN_AWAITING_USER_SEARCH_QUERY) {
            processUserSearch(chatId, text);
        }
        if (currentState == UserState.ADMIN_AWAITING_COURSE_SEARCH_QUERY) {
            // --- YANGI QISM ---
            processCourseSearch(chatId, text); // Kurs qidiruvini boshlaymiz
            return;
        }
    }

    /**
     * Sends the main welcome message for the admin panel along with the main ReplyKeyboard.
     * This is the entry point for an admin session.
     *
     * @param chatId The admin's chat ID.
     */
    @Override
    public void sendAdminWelcomeMessage(Long chatId, UserProfile profile) {
        telegramUserRepository.updateStateByChatId(chatId, UserState.ADMIN_MAIN_MENU);
        ReplyKeyboardMarkup keyboard = replyKeyboardService.adminMainMenu();
        SendMessage sendMessage = sendMsg.sendMessage(
                chatId,
                messageService.getMessage(BotMessage.START_MESSAGE_ADMIN, profile.getFirstName()),
                keyboard
        );
        onlineEducationBot.myExecute(sendMessage);
    }

    private void initiateBroadcast(Long chatId) {
        telegramUserRepository.updateStateByChatId(chatId, UserState.AWAITING_BROADCAST_MESSAGE);
        String infoText = messageService.getMessage(BotMessage.ADMIN_BROADCAST_INIT);
        onlineEducationBot.myExecute(sendMsg.sendMessage(chatId, infoText));
    }

    private void sendUsersMainMenu(Long chatId) {
        telegramUserRepository.updateStateByChatId(chatId, UserState.ADMIN_MANAGING_USERS);
        String menuText = messageService.getMessage(BotMessage.ADMIN_USERS_MENU);
        InlineKeyboardMarkup keyboard = inlineKeyboardService.usersMainMenu();
        onlineEducationBot.myExecute(sendMsg.sendMessage(chatId, menuText, keyboard));
    }

    private void sendCoursesMainMenu(Long chatId) {
        telegramUserRepository.updateStateByChatId(chatId, UserState.ADMIN_MANAGING_COURSES);
        String menuText = "📚 Kurslarni boshqarish bo'limi.";
        InlineKeyboardMarkup keyboard = inlineKeyboardService.coursesMainMenu();
        onlineEducationBot.myExecute(sendMsg.sendMessage(chatId, menuText, keyboard));
    }

    private void sendStatisticsMessage(Long chatId) {
        AdminDashboardDTO stats = userRepository.getAdminDashboardStats();
        String statsText = formatDashboardText("Admin", stats);
        onlineEducationBot.myExecute(sendMsg.sendMessage(chatId, statsText));
    }

//    private void sendDashboardMessage(Long chatId, User user, UserProfile profile) {
//
//        String dashboardText = prepareAdminDashboardText(user, profile);
//        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.dashboardMenu();
//        onlineEducationBot.myExecute(sendMsg.sendMessage(chatId, dashboardText, inlineKeyboardMarkup));
//
//    }

    private void sendDashboardMessage(Long chatId, User user, UserProfile profile) {
        long totalUsers = userRepository.count();
        long totalCourses = courseRepository.count();

        String dashboardText = messageService.getMessage(BotMessage.DASHBOARD_ADMIN,
                profile.getFirstName() + " " + profile.getLastName(),
                profile.getEmail(), user.getUsername(), user.getRole().name(),
                totalUsers, totalCourses
        );
        InlineKeyboardMarkup keyboard = inlineKeyboardService.dashboardMenu();
        SendMessage messageToSend = sendMsg.sendMessage(chatId, dashboardText, keyboard);
        messageToSend.setParseMode("Markdown"); // Formatlashni yoqish
        onlineEducationBot.myExecute(messageToSend);
    }

    private String prepareAdminDashboardText(User user, UserProfile profile) {

        long totalUsers = userRepository.count();
        long totalCourses = courseRepository.count();

        return messageService.getMessage(BotMessage.DASHBOARD_ADMIN,
                profile.getFirstName() + " " + profile.getLastName(),
                profile.getEmail(), user.getUsername(), user.getRole().name(),
                totalUsers, totalCourses
        );
    }

    /**
     * A helper method to delete a message after a specified delay in seconds.
     * This runs in a new thread to avoid blocking the main application flow.
     *
     * @param chatId         The chat where the message is.
     * @param messageId      The ID of the message to delete.
     * @param delayInSeconds The delay in seconds before deletion.
     */
    private void deleteMessageAfterDelay(Long chatId,
                                         Integer messageId,
                                         int delayInSeconds) {
        new Thread(() -> {
            try {
                // Soniyalarni millisekundlarga o'girish
                Thread.sleep(delayInSeconds * 1000L);

                // O'chirish uchun DeleteMessage obyektini yaratish
                DeleteMessage deleteMessage = new DeleteMessage(chatId.toString(), messageId);

                // O'chirish
                onlineEducationBot.myExecute(deleteMessage);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Thread for delayed message deletion was interrupted.", e);
            }
        }).start();
    }

    @Transactional(readOnly = true)
    @Override
    public void showDashboard(User user, Long chatId, Integer messageId) {
        UserProfile profile = user.getProfile();
        String dashboardText = prepareAdminDashboardText(user, profile);
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.dashboardMenu();
        onlineEducationBot.myExecute(sendMsg.editMessage(chatId, messageId, dashboardText, inlineKeyboardMarkup));
    }

    private String formatDashboardText(String adminName, AdminDashboardDTO stats) {
        return messageService.getMessage(
                BotMessage.DASHBOARD_ADMIN,
                adminName,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm")),
                stats.getTotalUsers(),
                stats.getTotalInstructors(),
                stats.getTotalCourses(),
                "$" + stats.getRevenueThisMonth().toPlainString(),
                stats.getNewUsersToday(),
                stats.getSalesToday(),
                stats.getNewSupportTickets()
        );
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

    //
//    Foydalanuvchi tomonidan kiritilgan qidiruv matnini qayta ishlaydi va
// topilgan natijalarni sahifalangan ro'yxat ko'rinishida yuboradi.
//
    private void processUserSearch(Long chatId, String searchTerm) {
        telegramUserRepository.updateStateByChatId(chatId, UserState.ADMIN_MANAGING_USERS);
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = userRepository.searchUsers(searchTerm, pageable);

        StringBuilder text = new StringBuilder();
        InlineKeyboardMarkup keyboard;

        if (userPage.isEmpty()) {
            text.append("😔 `").append(searchTerm).append("` bo'yicha foydalanuvchi topilmadi.");
            keyboard = inlineKeyboardService.createSingleButtonKeyboard("⬅️ Orqaga", "admin:users:main_menu");
        } else {
            text.append(String.format("🔍 *'%s' bo'yicha topilgan natijalar*\n_Sahifa: 1/%d_\n\n", searchTerm, userPage.getTotalPages()));
            List<User> usersOnPage = userPage.getContent();
            for (int i = 0; i < usersOnPage.size(); i++) {
                User user = usersOnPage.get(i);
                UserProfile profile = user.getProfile();
                String name = (profile != null && profile.getFirstName() != null && !profile.getFirstName().isBlank()) ? profile.getFirstName() + " " + profile.getLastName() : user.getUsername();
                text.append(String.format("`%d.` 👤 **%s** — `%s`\n", i + 1, escapeMarkdown(name), user.getRole().name()));
            }
            text.append("\n🔽 Tanlash uchun tegishli tugmani bosing.");
            keyboard = inlineKeyboardService.usersPageMenu(userPage, searchTerm);
        }

        SendMessage message = sendMsg.sendMessage(chatId, text.toString(), keyboard);
        message.setParseMode("Markdown");
        onlineEducationBot.myExecute(message);
    }
    private String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replace("_", "\\_").replace("*", "\\*").replace("`", "\\`")
                .replace("[", "\\[").replace("]", "\\]");
    }


    private void processCourseSearch(Long chatId, String searchTerm) {
        // Foydalanuvchi holatini yana asosiy menyuga qaytaramiz
        telegramUserRepository.updateStateByChatId(chatId, UserState.ADMIN_MANAGING_COURSES);

        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));
        // Bizga endi CourseRepository'da search metodi kerak bo'ladi
        Page<Course> coursePage = courseRepository.searchByTitle(searchTerm, pageable);

        StringBuilder text = new StringBuilder();
        InlineKeyboardMarkup keyboard;

        if (coursePage.isEmpty()) {
            text.append("😔 `").append(searchTerm).append("` bo'yicha hech qanday kurs topilmadi.");
            keyboard = inlineKeyboardService.createSingleButtonKeyboard("⬅️ Orqaga", "admin:courses:main_menu");
        } else {
            text.append(String.format("🔍 *'%s' bo'yicha topilgan natijalar*\n_Sahifa: 1/%d_\n\n", escapeMarkdown(searchTerm), coursePage.getTotalPages()));

            List<Course> coursesOnPage = coursePage.getContent();
            for (int i = 0; i < coursesOnPage.size(); i++) {
                Course course = coursesOnPage.get(i);
                String statusEmoji = course.isDeleted() ? "❌" : "✅";
                text.append(String.format("`%d.` %s **%s**\n", i + 1, statusEmoji, escapeMarkdown(course.getTitle())));
            }
            text.append("\n🔽 Tanlash uchun tegishli tugmani bosing.");

            // Klaviatura yasashda 'searchTerm'ni berib yuboramiz
            keyboard = inlineKeyboardService.coursesPageMenu(coursePage, searchTerm, null,null);
        }

        SendMessage message = sendMsg.sendMessage(chatId, text.toString(), keyboard);
        message.setParseMode("Markdown"); // Yoki MarkdownV2
        onlineEducationBot.myExecute(message);
    }
}
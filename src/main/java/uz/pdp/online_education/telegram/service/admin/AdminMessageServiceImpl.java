package uz.pdp.online_education.telegram.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.model.UserProfile;
import uz.pdp.online_education.payload.AdminDashboardDTO;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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


        // Route commands and button presses to their respective handlers
        switch (text) {
            case Utils.START -> sendAdminWelcomeMessage(chatId, profile);
            case Utils.DASHBOARD -> sendDashboardMessage(chatId,user, profile);
            case Utils.ReplyButtons.ADMIN_USERS -> sendUsersMainMenu(chatId);
            case Utils.ReplyButtons.ADMIN_COURSES -> sendCoursesMainMenu(chatId);
            case Utils.ReplyButtons.ADMIN_SEND_MESSAGE -> initiateBroadcast(chatId);
            case Utils.ReplyButtons.ADMIN_STATISTICS -> sendStatisticsMessage(chatId);
        }
    }

    /**
     * Sends the main welcome message for the admin panel along with the main ReplyKeyboard.
     * This is the entry point for an admin session.
     * @param chatId The admin's chat ID.
     */
    @Override
    public void sendAdminWelcomeMessage(Long chatId, UserProfile profile) {
        telegramUserRepository.updateStateByChatId(chatId, UserState.ADMIN_MAIN_MENU);
        ReplyKeyboardMarkup keyboard = replyKeyboardService.adminMainMenu();
        SendMessage sendMessage = sendMsg.sendMessage(
                chatId,
                messageService.getMessage(BotMessage.START_MESSAGE_ADMIN,profile.getFirstName()),
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

        // 1. Vaqtinchalik "Loading" xabarini yuborish va Reply klaviaturani o'chirish
        String loadingText = "Foydalanuvchilar bo'limi ochilmoqda...";
        Message loadingMessage = onlineEducationBot.MyExecute(sendMsg.sendReplyKeyboardRemove(chatId, loadingText));

        if (loadingMessage == null) {
            log.warn("Could not send the loading message to chat_id: {}", chatId);
            return;
        }

        // 2. Asosiy Inline menyuni darhol yuborish
        String menuText = messageService.getMessage(BotMessage.ADMIN_USERS_MENU);
        InlineKeyboardMarkup keyboard = inlineKeyboardService.usersMainMenu();
        onlineEducationBot.myExecute(sendMsg.sendMessage(chatId, menuText, keyboard));

        // 3. Alohida thread'da "Loading" xabarini ozginadan keyin o'chirish
        deleteMessageAfterDelay(chatId, loadingMessage.getMessageId(), 1); // 2 soniyadan keyin o'chirish
    }

    private void sendCoursesMainMenu(Long chatId) {
        telegramUserRepository.updateStateByChatId(chatId, UserState.ADMIN_MANAGING_COURSES);

        String loadingText = "Kurslar bo'limi ochilmoqda...";
        Message loadingMessage = onlineEducationBot.MyExecute(sendMsg.sendReplyKeyboardRemove(chatId, loadingText));

        if (loadingMessage == null) return;

        String menuText = messageService.getMessage(BotMessage.ADMIN_COURSES_MENU);
        InlineKeyboardMarkup keyboard = inlineKeyboardService.coursesMainMenu();
        onlineEducationBot.myExecute(sendMsg.sendMessage(chatId, menuText, keyboard));

        deleteMessageAfterDelay(chatId, loadingMessage.getMessageId(), 1);
    }

    private void sendStatisticsMessage(Long chatId) {
        AdminDashboardDTO stats = userRepository.getAdminDashboardStats();
        String statsText = formatDashboardText("Admin", stats);
        onlineEducationBot.myExecute(sendMsg.sendMessage(chatId, statsText));
    }

    private void sendDashboardMessage(Long chatId, User user, UserProfile profile) {

        AdminDashboardDTO stats = userRepository.getAdminDashboardStats();
        String dashboardText = formatDashboardText(profile.getFirstName() + " " + profile.getLastName(), stats);
        onlineEducationBot.myExecute(sendMsg.sendMessage(chatId, dashboardText));
    }

    /**
     * A helper method to delete a message after a specified delay in seconds.
     * This runs in a new thread to avoid blocking the main application flow.
     *
     * @param chatId The chat where the message is.
     * @param messageId The ID of the message to delete.
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
}
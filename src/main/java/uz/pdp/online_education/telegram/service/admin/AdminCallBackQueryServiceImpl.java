package uz.pdp.online_education.telegram.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import uz.pdp.online_education.repository.TelegramUserRepository;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.config.controller.OnlineEducationBot;
import uz.pdp.online_education.telegram.enums.UserState;
import uz.pdp.online_education.telegram.mapper.SendMsg;
import uz.pdp.online_education.telegram.service.admin.template.AdminCallBackQueryService;
import uz.pdp.online_education.telegram.service.admin.template.AdminMessageService;

/**
 * Handles all callback queries originating from the admin panel's inline keyboards.
 * This service acts as a router, delegating actions based on the callback data.
 */
@Service
@RequiredArgsConstructor
public class AdminCallBackQueryServiceImpl implements AdminCallBackQueryService {

    private final TelegramUserRepository telegramUserRepository;
    private final OnlineEducationBot onlineEducationBot;
    private final SendMsg sendMsg;
    // We depend on the interface, not the implementation.
    private final AdminMessageService adminMessageService; 

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCallback(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String queryData = callbackQuery.getData();

        if (queryData.equals(Utils.CallbackData.BACK_TO_ADMIN_MENU_CALLBACK)) {
            handleBackToAdminMainMenu(chatId, messageId);
        }
        // TODO: Add more handlers for other callback data like "users:list:page:0"
        // else if (queryData.startsWith("users:")) { ... }
    }

    /**
     * Handles the action when the "Back to Main Menu" button is pressed.
     * It deletes the current inline menu, resets the user's state, and resends
     * the main admin welcome message with the main ReplyKeyboard.
     *
     * @param chatId    The admin's chat ID.
     * @param messageId The ID of the message with the inline keyboard to be deleted.
     */
    private void handleBackToAdminMainMenu(Long chatId, Integer messageId) {
        // Step 1: Delete the previous message that contained the inline keyboard.
        DeleteMessage deleteMessage = sendMsg.deleteMessage(chatId, messageId);
        onlineEducationBot.myExecute(deleteMessage);

        // Step 2: Let the AdminMessageService handle sending the welcome message,
        // as it also resets the state and sends the correct ReplyKeyboard.
        adminMessageService.sendAdminWelcomeMessage(chatId);
    }
}
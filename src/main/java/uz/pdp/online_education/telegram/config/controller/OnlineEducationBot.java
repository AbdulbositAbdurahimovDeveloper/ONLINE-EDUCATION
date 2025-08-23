package uz.pdp.online_education.telegram.config.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVenue;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.online_education.config.TelegramUpdateLogger;
import uz.pdp.online_education.telegram.config.service.UpdateDispatcherService;

import java.io.Serializable;

/**
 * The main Telegram Bot class that handles webhook updates.
 * This class is registered as a Spring Component to be managed by the Spring Context.
 */
@Slf4j
@Getter
@Component
@RequiredArgsConstructor
public class OnlineEducationBot extends TelegramWebhookBot {

    // --- Bot Credentials and Configuration (injected from application.yml/properties) ---

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.webhook-path}")
    private String botPath;

    // --- Service Dependencies ---

    private final UpdateDispatcherService updateDispatcherService;
    private final TelegramUpdateLogger logger;

    /**
     * This is the entry point for all incoming updates from Telegram's webhook.
     * It immediately delegates the processing to an asynchronous service (`UpdateDispatcherService`)
     * and returns null to send a quick 200 OK response to Telegram, preventing timeouts and retries.
     *
     * @param update The incoming update object from Telegram.
     * @return Always returns null to acknowledge the request instantly.
     */
    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        logger.logUpdate(update);
        updateDispatcherService.dispatch(update); // This method should be marked @Async
        return null;
    }

    /**
     * A private, core method for sending any type of response to Telegram.
     * It wraps the standard `execute()` call in a try-catch block to handle potential
     * Telegram API exceptions gracefully.
     *
     * @param method The BotApiMethod object to be executed (e.g., SendMessage, EditMessageText).
     * @return The result of the execution, which could be a Message, Boolean, or other Serializable type.
     */
    private <T extends Serializable, Method extends BotApiMethod<T>> T executeMethod(Method method) {
        try {
            return this.execute(method);
        } catch (TelegramApiException e) {
            e.printStackTrace();
//            log.error("Failed to execute Telegram API method: {}", e.getMessage());
//            return null; // Return null on failure
        }
        return  null;
    }

    /**
     * A convenience method that delegates to the more general `executeMethod`.
     * This can be used to maintain the original method name if desired.
     * It is recommended to use `executeMethod` directly for clarity.
     *
     * @param method The BotApiMethod object to be executed.
     */
    public void myExecute(BotApiMethod<?> method) {
        executeMethod(method);
    }

//    public void myExecute(SendPhoto sendPhoto) {
//        try {
//            execute(sendPhoto);
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//        }
//    }

    public Message myExecute(SendPhoto sendPhoto) {
        try {
            Message execute = execute(sendPhoto);
            return execute;
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void myExecute(SendVideo sendVideo) {
        try {
            execute(sendVideo);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * PUBLIC METHOD: Executes a command that is expected to return a Message object.
     * This is essential for getting the message_id of a newly sent message,
     * which can then be edited.
     *
     * @param method A BotApiMethod that returns a Message (typically a SendMessage object).
     * @return The scent Message object if successful, otherwise null.
     */
    @SuppressWarnings("unchecked") // Suppress warning for the necessary cast
    public Message MyExecute(BotApiMethod<?> method) {
        // Ensure the method is expected to return a Message
        if (method != null) {
            // We cast the generic method to one that specifically returns a Message
            BotApiMethod<Message> messageMethod = (BotApiMethod<Message>) method;
            Message result = executeMethod(messageMethod);
            if (result != null) {
                return result;
            }
        }
        assert method != null;
        log.warn("MyExecute(BotApiMethod<Message>) was called with a method that does not return a Message. Method: {}", method.getClass().getSimpleName());
        return null;
    }

    public void myExecute(EditMessageMedia editMessageMedia) {
        try {
            execute(editMessageMedia);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
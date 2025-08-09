package uz.pdp.online_education.telegram.config.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.pdp.online_education.telegram.config.service.UpdateDispatcherService;

@Slf4j
@Getter
@Component
@RequiredArgsConstructor
public class OnlineEducationBot extends TelegramWebhookBot {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.webhook-path}")
    private String botPath;

     private final UpdateDispatcherService updateDispatcherService;

    /**
     * @param update update
     * @return response
     */
    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
//        log.info("update: {}", update.getMessage().getChatId());
        return updateDispatcherService.dispatch(update);
    }




}

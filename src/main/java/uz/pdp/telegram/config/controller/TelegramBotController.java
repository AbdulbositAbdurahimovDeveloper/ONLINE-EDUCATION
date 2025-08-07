package uz.pdp.telegram.config.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TelegramBotController {

    private final OnlineEducationBot onlineEducationBot;

    @PostMapping("/telegram-bot")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return onlineEducationBot.onWebhookUpdateReceived(update);
    }
}

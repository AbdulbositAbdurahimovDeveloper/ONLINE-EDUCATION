package uz.pdp.online_education.telegram.config.service;

import org.springframework.stereotype.Service;
import uz.pdp.online_education.telegram.enums.BotMessage;

import java.util.Objects;

@Service
public class MessageService {

    public String getMessage(BotMessage key) {
        return Objects.requireNonNullElse(key, BotMessage.KEY_NOT_FOUND).getMessage();
    }

    public String getMessage(BotMessage key, Object... args) {
        if (key == null) {
            return BotMessage.KEY_NOT_FOUND.getMessage();
        }
        return key.getMessage(args);
    }
}

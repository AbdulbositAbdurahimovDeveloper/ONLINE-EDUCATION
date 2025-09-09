package uz.pdp.online_education.telegram.service.message;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import uz.pdp.online_education.telegram.enums.BotMessage;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MessageService {


    private final Environment environment;

    /**
     * Oddiy xabarni kalit orqali oladi.
     */
    public String getMessage(BotMessage key) {

        return environment.getProperty(key.getKey(), Objects.requireNonNull(environment.getProperty(BotMessage.KEY_NOT_FOUND.getKey())));
    }

    /**
     * Dinamik qiymatlar bilan formatlanadigan xabarni oladi.
     * Masalan, %s o'rniga foydalanuvchi nomini qo'yish.
     */
    public String getMessage(BotMessage key, Object... args) {
        String messageTemplate = getMessage(key);


        if (messageTemplate != null) {
            return String.format(messageTemplate, args);
        }


        return environment.getProperty(BotMessage.KEY_NOT_FOUND.getKey());
    }
}
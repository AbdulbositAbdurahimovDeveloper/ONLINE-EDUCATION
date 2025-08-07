package uz.pdp.telegram.config.service.template;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramInstructorService {
    BotApiMethod<?> onUpdateResave(Update update);
}

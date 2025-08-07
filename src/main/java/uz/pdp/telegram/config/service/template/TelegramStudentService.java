package uz.pdp.telegram.config.service.template;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramStudentService {
    BotApiMethod<?> onUpdateResave(Update update);
}

package uz.pdp.online_education.telegram.config.service.template;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramAdminService {
    BotApiMethod<?> onUpdateResave(Update update);
}

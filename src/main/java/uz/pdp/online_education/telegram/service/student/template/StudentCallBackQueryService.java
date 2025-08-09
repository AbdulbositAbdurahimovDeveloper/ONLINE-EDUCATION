package uz.pdp.online_education.telegram.service.student.template;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public interface StudentCallBackQueryService {
    BotApiMethod<?> handleCallback(CallbackQuery callbackQuery);

}

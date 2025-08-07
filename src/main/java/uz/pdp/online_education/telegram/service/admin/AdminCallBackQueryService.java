package uz.pdp.online_education.telegram.service.admin;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public interface AdminCallBackQueryService {
    BotApiMethod<?> handleCallback(CallbackQuery callbackQuery);

}

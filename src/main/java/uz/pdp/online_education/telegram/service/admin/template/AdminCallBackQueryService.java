package uz.pdp.online_education.telegram.service.admin.template;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public interface AdminCallBackQueryService {
    void handleCallback(CallbackQuery callbackQuery);

}

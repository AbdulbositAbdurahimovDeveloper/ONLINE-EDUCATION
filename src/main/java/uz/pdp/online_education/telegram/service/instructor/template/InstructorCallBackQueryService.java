package uz.pdp.online_education.telegram.service.instructor.template;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public interface InstructorCallBackQueryService {
    void handleCallback(CallbackQuery callbackQuery);

}

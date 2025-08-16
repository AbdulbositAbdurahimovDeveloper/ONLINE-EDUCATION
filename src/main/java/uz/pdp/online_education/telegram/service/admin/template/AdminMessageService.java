package uz.pdp.online_education.telegram.service.admin.template;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.pdp.online_education.model.UserProfile;

public interface AdminMessageService {
    void handleMessage(Message message);

    void sendAdminWelcomeMessage(Long chatId, UserProfile from);
}

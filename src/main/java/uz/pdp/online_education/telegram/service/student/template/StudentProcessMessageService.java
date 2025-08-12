package uz.pdp.online_education.telegram.service.student.template;

import org.telegram.telegrambots.meta.api.objects.Message;
import uz.pdp.online_education.model.User;

public interface StudentProcessMessageService {
    void handleMessage(Message message);

    void showMainMenu(User user, Long chatId);

    void showDashboard(User user, Long chatId, Integer messageId);

}

package uz.pdp.online_education.telegram.service.student.template;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface StudentMessageService {
    void handleMessage(Message message);

}

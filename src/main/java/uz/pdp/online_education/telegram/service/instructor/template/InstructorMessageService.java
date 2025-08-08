package uz.pdp.online_education.telegram.service.instructor.template;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface InstructorMessageService {
    BotApiMethod<?> handleMessage(Message message);

}

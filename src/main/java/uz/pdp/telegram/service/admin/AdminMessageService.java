package uz.pdp.telegram.service.admin;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface AdminMessageService {
    BotApiMethod<?> handleMessage(Message message);
}

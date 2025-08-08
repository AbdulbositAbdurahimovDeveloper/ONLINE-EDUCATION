package uz.pdp.online_education.telegram.service.student;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.pdp.online_education.enums.Role;
import uz.pdp.online_education.repository.TelegramUserRepository;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.enums.BotMessage;
import uz.pdp.online_education.telegram.mapper.SendMsg;
import uz.pdp.online_education.telegram.model.TelegramUser;
import uz.pdp.online_education.telegram.service.RoleService;
import uz.pdp.online_education.telegram.service.message.MessageService;
import uz.pdp.online_education.telegram.service.student.template.StudentMessageService;

@Service
@RequiredArgsConstructor
public class StudentMessageServiceImpl implements StudentMessageService {

    private final RoleService roleService;
    private final SendMsg sendMsg;
    private final MessageService messageService;
    private final TelegramUserRepository telegramUserRepository;

    /**
     * @param message
     * @return
     */
    @Override
    public BotApiMethod<?> handleMessage(Message message) {
        Long chatId = message.getChatId();

        String text = message.getText();

        TelegramUser user = checkTelegramUser(chatId);

        Role currentRole = roleService.getUserRole(chatId);

        if (text.equals(Utils.START)) {
            return startMessage(chatId);
        }

        return null;
    }

    private TelegramUser checkTelegramUser(Long chatId) {
        TelegramUser user = telegramUserRepository.findByChatId(chatId).orElse(null);

        if (user == null) {
            TelegramUser telegramUser = new TelegramUser();
            telegramUser.setChatId(chatId);
            telegramUserRepository.save(telegramUser);


        }
        return user;
    }

    private SendMessage startMessage(Long chatId) {
        return sendMsg.sendMessage(
                chatId,
                messageService.getMessage(BotMessage.START_MESSAGE_STUDENT));
    }
}

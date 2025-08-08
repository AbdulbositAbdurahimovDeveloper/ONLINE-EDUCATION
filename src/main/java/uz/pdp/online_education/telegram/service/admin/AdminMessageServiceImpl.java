package uz.pdp.online_education.telegram.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.pdp.online_education.enums.Role;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.enums.BotMessage;
import uz.pdp.online_education.telegram.mapper.SendMsg;
import uz.pdp.online_education.telegram.service.RoleService;
import uz.pdp.online_education.telegram.service.admin.template.AdminMessageService;
import uz.pdp.online_education.telegram.service.message.MessageService;

@Service
@RequiredArgsConstructor
public class AdminMessageServiceImpl implements AdminMessageService {

    private final RoleService roleService;
    private final SendMsg sendMsg;
    private final MessageService messageService;

    /**
     * @param message message
     * @return sendMessage
     */
    @Override
    public BotApiMethod<?> handleMessage(Message message) {

        Long chatId = message.getChatId();

        String text = message.getText();

        Role currentRole = roleService.getUserRole(chatId);

        if (text.equals(Utils.START)) {
            return startMessage(chatId);
        }

        return null;
    }

    private SendMessage startMessage(Long chatId) {
        return sendMsg.sendMessage(
                chatId,
                messageService.getMessage(BotMessage.START_MESSAGE_ADMIN)
        );
    }


}

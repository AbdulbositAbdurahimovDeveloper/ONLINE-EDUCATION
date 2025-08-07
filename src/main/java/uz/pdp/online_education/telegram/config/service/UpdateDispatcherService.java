package uz.pdp.online_education.telegram.config.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.pdp.online_education.enums.Role;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.service.RoleService;
import uz.pdp.online_education.telegram.config.service.template.TelegramAdminService;
import uz.pdp.online_education.telegram.config.service.template.TelegramInstructorService;
import uz.pdp.online_education.telegram.config.service.template.TelegramStudentService;
import uz.pdp.online_education.telegram.enums.BotMessage;
import uz.pdp.online_education.telegram.mapper.SendMsg;

import java.util.Objects;

@Component
public class UpdateDispatcherService {

    private final RoleService roleService;
    private final TelegramAdminService telegramAdminService;
    private final TelegramInstructorService telegramInstructorService;
    private final TelegramStudentService telegramStudentService;
    private final MessageService message;
    private final SendMsg sendMsg;


    public UpdateDispatcherService(@Lazy RoleService roleService,
                                   @Lazy TelegramAdminService telegramAdminService,
                                   @Lazy TelegramInstructorService telegramInstructorService,
                                   @Lazy TelegramStudentService telegramStudentService,
                                   @Lazy MessageService message,
                                   @Lazy SendMsg sendMsg) {
        this.roleService = roleService;
        this.telegramAdminService = telegramAdminService;
        this.telegramInstructorService = telegramInstructorService;
        this.telegramStudentService = telegramStudentService;
        this.message = message;
        this.sendMsg = sendMsg;
    }

    public BotApiMethod<?> dispatch(Update update) {
        Long chatId = getUserChatId(update);

        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            if (text.equals(Utils.ADMIN) || text.equals(Utils.INSTRUCTOR) || text.equals(Utils.STUDENT)) {
                return changedCurrentRole(chatId, text);
            }
        }

        Role currentRole = roleService.getCurrentRole(chatId);

        if (Objects.equals(currentRole, Role.ADMIN)) {

            return telegramAdminService.onUpdateResave(update);

        } else if (Objects.equals(currentRole, Role.INSTRUCTOR)) {

            return telegramInstructorService.onUpdateResave(update);

        } else if (Objects.equals(currentRole, Role.STUDENT)) {

            return telegramStudentService.onUpdateResave(update);

        }

        return null;
    }

    private BotApiMethod<?> changedCurrentRole(Long chatId, String text) {

        Role userRole = roleService.getUserRole(chatId);

        if (Objects.equals(userRole, Role.ADMIN)) {
            roleService.update(chatId, text);
        } else if (Objects.equals(userRole, Role.INSTRUCTOR)) {
            roleService.update(chatId, text);
        }

        return sendMsg.sendMessage(chatId, message.getMessage(BotMessage.CHANGED_ROLE) );
    }


    private Long getUserChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId();
        }
        return null;
    }
}

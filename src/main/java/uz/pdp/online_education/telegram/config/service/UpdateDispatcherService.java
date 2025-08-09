package uz.pdp.online_education.telegram.config.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import uz.pdp.online_education.enums.Role;
import uz.pdp.online_education.repository.TelegramUserRepository;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.enums.UserState;
import uz.pdp.online_education.telegram.model.TelegramUser;
import uz.pdp.online_education.telegram.service.RoleService;
import uz.pdp.online_education.telegram.config.service.template.TelegramAdminService;
import uz.pdp.online_education.telegram.config.service.template.TelegramInstructorService;
import uz.pdp.online_education.telegram.config.service.template.TelegramStudentService;
import uz.pdp.online_education.telegram.enums.BotMessage;
import uz.pdp.online_education.telegram.mapper.SendMsg;
import uz.pdp.online_education.telegram.service.admin.template.InlineKeyboardService;
import uz.pdp.online_education.telegram.service.message.MessageService;

import java.util.Objects;

@Component
public class UpdateDispatcherService {

    private final RoleService roleService;
    private final TelegramAdminService telegramAdminService;
    private final TelegramInstructorService telegramInstructorService;
    private final TelegramStudentService telegramStudentService;
    private final MessageService message;
    private final SendMsg sendMsg;
    private final TelegramUserRepository telegramUserRepository;
    private final InlineKeyboardService inlineKeyboardService;


    public UpdateDispatcherService(@Lazy RoleService roleService,
                                   @Lazy TelegramAdminService telegramAdminService,
                                   @Lazy TelegramInstructorService telegramInstructorService,
                                   @Lazy TelegramStudentService telegramStudentService,
                                   @Lazy MessageService message,
                                   @Lazy SendMsg sendMsg, TelegramUserRepository telegramUserRepository, InlineKeyboardService inlineKeyboardService) {
        this.roleService = roleService;
        this.telegramAdminService = telegramAdminService;
        this.telegramInstructorService = telegramInstructorService;
        this.telegramStudentService = telegramStudentService;
        this.message = message;
        this.sendMsg = sendMsg;
        this.telegramUserRepository = telegramUserRepository;
        this.inlineKeyboardService = inlineKeyboardService;
    }

    public BotApiMethod<?> dispatch(Update update) {
        Long chatId = getUserChatId(update);

        TelegramUser currentUser = telegramUserRepository.findByChatId(chatId)
                .orElse(null);

        if (Objects.isNull(currentUser) || Objects.nonNull(currentUser.getUser())) {
            Long userChatId = getUserChatId(update);

            TelegramUser telegramUser = new TelegramUser();
            telegramUser.setChatId(chatId);
            telegramUser.setUserState(UserState.AUTHENTICATED);
            telegramUserRepository.save(telegramUser);

            String sendMessage = message.getMessage(BotMessage.WELCOME_FIRST_TIME);
            InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.welcomeFirstTime(userChatId);
            return sendMsg.sendMessage(userChatId, sendMessage, inlineKeyboardMarkup);

        }

        Role currentRole = roleService.getUserRole(chatId);

        if (Objects.equals(currentRole, Role.ADMIN)) {

            return telegramAdminService.onUpdateResave(update);

        } else if (Objects.equals(currentRole, Role.INSTRUCTOR)) {

            return telegramInstructorService.onUpdateResave(update);

        } else if (Objects.equals(currentRole, Role.STUDENT)) {

            return telegramStudentService.onUpdateResave(update);

        }

        return null;
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

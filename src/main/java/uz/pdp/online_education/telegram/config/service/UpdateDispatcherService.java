package uz.pdp.online_education.telegram.config.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.online_education.enums.Role;
import uz.pdp.online_education.repository.TelegramUserRepository;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.config.controller.OnlineEducationBot;
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
    private final OnlineEducationBot onlineEducationBot;


    public UpdateDispatcherService(@Lazy RoleService roleService,
                                   @Lazy TelegramAdminService telegramAdminService,
                                   @Lazy TelegramInstructorService telegramInstructorService,
                                   @Lazy TelegramStudentService telegramStudentService,
                                   @Lazy MessageService message,
                                   @Lazy SendMsg sendMsg,
                                   @Lazy TelegramUserRepository telegramUserRepository,
                                   @Lazy InlineKeyboardService inlineKeyboardService,
                                   @Lazy OnlineEducationBot onlineEducationBot) {
        this.roleService = roleService;
        this.telegramAdminService = telegramAdminService;
        this.telegramInstructorService = telegramInstructorService;
        this.telegramStudentService = telegramStudentService;
        this.message = message;
        this.sendMsg = sendMsg;
        this.telegramUserRepository = telegramUserRepository;
        this.inlineKeyboardService = inlineKeyboardService;
        this.onlineEducationBot = onlineEducationBot;
    }

    @Async
    public void dispatch(Update update) {
        Long userChatId = getUserChatId(update);

        TelegramUser currentUser = telegramUserRepository.findByChatId(userChatId)
                .orElse(null);

        if (Objects.isNull(currentUser) || Objects.isNull(currentUser.getUser())) {

            TelegramUser telegramUser = new TelegramUser();
            telegramUser.setChatId(userChatId);
            telegramUser.setUserState(UserState.AUTHENTICATED);
            telegramUserRepository.save(telegramUser);
            telegramUserRepository.flush();

            String sendMessage = message.getMessage(BotMessage.WELCOME_FIRST_TIME);
            InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardService.welcomeFirstTime(userChatId);
            try {
                onlineEducationBot.execute(sendMsg.sendMessage(userChatId, sendMessage, inlineKeyboardMarkup));
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            return;

        }
        Long chatId = getUserChatId(update);
        Role currentRole = roleService.getUserRole(chatId);

        if (Objects.equals(currentRole, Role.ADMIN)) {

            telegramAdminService.onUpdateResave(update);

        } else if (Objects.equals(currentRole, Role.INSTRUCTOR)) {

            telegramInstructorService.onUpdateResave(update);

        } else if (Objects.equals(currentRole, Role.STUDENT)) {

            telegramStudentService.onUpdateResave(update);

        }
    }


    private Long getUserChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId();
        }else if (update.hasMyChatMember()) {
            return update.getMyChatMember().getChat().getId();
        }
        return null;
    }
}

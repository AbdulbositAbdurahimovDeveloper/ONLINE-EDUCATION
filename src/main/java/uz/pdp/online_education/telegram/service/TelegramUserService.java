package uz.pdp.online_education.telegram.service;

import uz.pdp.online_education.telegram.enums.UserState;

public interface TelegramUserService {
    UserState getUserState(Long chatId);

    void unregistered(Long chatId);
}

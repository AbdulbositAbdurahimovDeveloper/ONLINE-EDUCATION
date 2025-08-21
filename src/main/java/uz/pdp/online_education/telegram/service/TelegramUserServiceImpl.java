package uz.pdp.online_education.telegram.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.repository.TelegramUserRepository;
import uz.pdp.online_education.repository.UserRepository;
import uz.pdp.online_education.telegram.enums.UserState;
import uz.pdp.online_education.telegram.model.TelegramUser;

@Service
@RequiredArgsConstructor
public class TelegramUserServiceImpl implements TelegramUserService {

    private final TelegramUserRepository telegramUserRepository;
    private final UserRepository userRepository;

    /**
     * @param chatId
     * @return
     */
    @Override
    public UserState getUserState(Long chatId) {
        return telegramUserRepository.getCurrentUser(chatId).getUserState();
    }


    public void unregistered(Long chatId) {
        telegramUserRepository.unregisterUserByChatId(chatId);
    }

    /**
     * @param chatId
     * @param userState
     */
    @Override
    @Transactional
    public void updateUserState(Long chatId, UserState userState) {

        telegramUserRepository.findByChatId(chatId).ifPresent(
                telegramUser -> {
                    telegramUser.setUserState(userState);
                    telegramUserRepository.save(telegramUser);
                }
        );

    }
}

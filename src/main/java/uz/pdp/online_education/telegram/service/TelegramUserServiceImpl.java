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

    /**
     * @param chatId
     */
//    @Override
//    @Transactional
//    public void unregistered(Long chatId) {
//        // 1. TelegramUser'ni topamiz
//        TelegramUser telegramUser = telegramUserRepository.findByChatId(chatId)
//                .orElseThrow(() -> new EntityNotFoundException("Telegram User with id " + chatId + " not found"));
//
//        // 2. Unga bog'liq bo'lgan User'ni olamiz
//        User user = telegramUser.getUser();
//
//        // 3. Agar bog'liqlik mavjud bo'lsa, ikki tomondan uzamiz
//        if (user != null) {
//            // Birinchi tomon: User'dan TelegramUser'ni olib tashlaymiz
//            user.setTelegramUser(null);
//            userRepository.save(user);
//
//        }
//
//        // Ikkinchi tomon: TelegramUser'dan User'ni olib tashlaymiz
//        telegramUser.setUser(null);
//        telegramUser.setUserState(UserState.UNREGISTERED);
//
//        // O'zgarishlarni saqlaymiz. JPA qaysi birini saqlashdan qat'iy nazar,
//        // o'zgarishni to'g'ri aks ettiradi.
//        // Odatda, ega tomonni saqlash kifoya.
//        telegramUserRepository.save(telegramUser);
//    }
    // Bu metodga endi @Transactional kerak emas, chunki repository metodining o'zi transactional
    public void unregistered(Long chatId) {
        telegramUserRepository.unregisterUserByChatId(chatId);
    }

}

package uz.pdp.telegram.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uz.pdp.online_education.enums.Role;
import uz.pdp.telegram.model.TelegramUser;
import uz.pdp.telegram.repository.TelegramUserRepository;

@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

    private final TelegramUserRepository telegramUserRepository;

    public RoleServiceImpl(@Lazy TelegramUserRepository telegramUserRepository) {
        this.telegramUserRepository = telegramUserRepository;
    }

    @Override
    public Role getCurrentRole(Long chatId) {
        TelegramUser telegramUser = telegramUserRepository.findByChatId(chatId)
                .orElse(null);

        if (telegramUser == null) {
            return Role.STUDENT;
        }

        return telegramUser.getCurrentRole();
    }

    @Override
    public Role getUserRole(Long chatId) {
        TelegramUser telegramUser = telegramUserRepository.findByChatId(chatId)
                .orElse(null);

        if (telegramUser == null) {
            return Role.STUDENT;
        }

        return telegramUser.getUser().getRole();

    }

    /**
     * @param chatId Long
     * @param text String
     */
    @Override
    public void update(Long chatId, String text) {

        log.warn("method not found");
        log.error("method not found");
    }
}

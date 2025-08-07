package uz.pdp.online_education.telegram.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uz.pdp.online_education.enums.Role;
import uz.pdp.online_education.telegram.model.TelegramUser;
import uz.pdp.online_education.repository.TelegramUserRepository;

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
     * @param roleStr String
     */
    @Override
    public void update(Long chatId, String roleStr) {
        TelegramUser currentUser = telegramUserRepository.getCurrentUser(chatId);
        Role currentRole = Role.valueOf(roleStr.substring(1).toUpperCase());
        currentUser.setCurrentRole(currentRole);
        telegramUserRepository.save(currentUser);
    }
}

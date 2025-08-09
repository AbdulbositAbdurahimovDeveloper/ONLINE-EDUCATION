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
    public Role getUserRole(Long chatId) {
        TelegramUser telegramUser = telegramUserRepository.findByChatId(chatId)
                .orElse(null);

        if (telegramUser == null) {
            return Role.STUDENT;
        }

        if (telegramUser.getUser() != null) {
            return Role.STUDENT;
        }

        return telegramUser.getUser().getRole();

    }
}

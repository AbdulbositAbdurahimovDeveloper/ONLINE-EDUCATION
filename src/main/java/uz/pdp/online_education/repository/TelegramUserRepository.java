package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.telegram.model.TelegramUser;

import java.util.Optional;

@Repository
public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long> {
    Optional<TelegramUser> findByChatId(Long chatId);

    default TelegramUser getCurrentUser(Long chatId) {
        return findByChatId(chatId).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}
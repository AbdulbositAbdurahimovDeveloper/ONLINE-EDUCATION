package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.telegram.enums.UserState;
import uz.pdp.online_education.telegram.model.TelegramUser;

import java.util.Optional;

@Repository
public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long> {
    Optional<TelegramUser> findByChatId(Long chatId);

    default TelegramUser getCurrentUser(Long chatId) {
        return findByChatId(chatId).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    /**
     * Updates the UserState for a specific TelegramUser identified by their chatId.
     * This is a custom query because Spring Data JPA cannot derive UPDATE queries from method names.
     *
     * @param chatId The unique ID of the Telegram user.
     * @param state  The new UserState to set.
     */
    @Transactional // O'zgartirish kiritadigan metodlar uchun @Transactional tavsiya etiladi
    @Modifying   // Bu so'rovning ma'lumotni o'zgartirishini (UPDATE, DELETE) bildiradi
    @Query("UPDATE telegram_users tu SET tu.userState = :state WHERE tu.chatId = :chatId")
    void updateStateByChatId(@Param("chatId") Long chatId, @Param("state") UserState state);

    @Modifying // Bu annotatsiya yozish operatsiyasi ekanligini bildiradi
    @Transactional // Bu metod o'zining alohida tranzaksiyasida ishlashi kerak
    @Query("UPDATE telegram_users tu SET tu.user = NULL, tu.userState = 'UNREGISTERED' WHERE tu.chatId = :chatId")
    void unregisterUserByChatId(@Param("chatId") Long chatId);

}
package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.enums.Role;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.telegram.enums.UserState;
import uz.pdp.online_education.telegram.model.TelegramUser;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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



    /**
     * Berilgan chatId bo'yicha foydalanuvchining vaqtinchalik ma'lumotlarini
     * (temporary_data) yangilaydi.
     * Bu metod ko'p qadamli operatsiyalar uchun ma'lumotlarni saqlashda ishlatiladi.
     * @param chatId Foydalanuvchining chat ID'si
     * @param data Yangi vaqtinchalik ma'lumotlar (JSON formatidagi String)
     */
    @Transactional
    @Modifying
    @Query("UPDATE telegram_users tu SET tu.temporaryData = :data WHERE tu.chatId = :chatId")
    void updateTemporaryDataByChatId(@Param("chatId") Long chatId, @Param("data") String data);



    @Query("SELECT COUNT(tu) FROM telegram_users tu JOIN tu.user u WHERE u.role IN :roles")
    long countAllByRoleIn(@Param("roles") Set<Role> roles);


    /**
     * Berilgan rollar to'plamiga tegishli bo'lgan barcha foydalanuvchilarning
     * telegramdagi chat ID'lari ro'yxatini qaytaradi.
     * Bu ommaviy xabar yuborish (broadcast) uchun ishlatiladi.
     * @param roles Qidirilayotgan rollar to'plami (masalan, {STUDENT, INSTRUCTOR})
     * @return Topilgan chat ID'lar ro'yxati
     */
    @Query("SELECT tu.chatId FROM telegram_users tu JOIN tu.user u WHERE u.role IN :roles")
    List<Long> findAllChatIdsByRoles(@Param("roles") Set<Role> roles);

}
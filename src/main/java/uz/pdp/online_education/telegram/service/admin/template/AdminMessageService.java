package uz.pdp.online_education.telegram.service.admin.template;

import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.model.UserProfile;

/**
 * Admin rolidagi foydalanuvchilardan kelgan matnli xabarlar va ReplyKeyboard
 * tugmalarini qayta ishlash uchun asosiy kirish nuqtasi.
 * Bu servis "dispatcher" vazifasini bajarib, ishni maxsus handler'larga yo'naltiradi.
 */
public interface AdminMessageService {

    /**
     * Foydalanuvchi matnli xabar yuborganda yoki ReplyKeyboard tugmasini bosganda
     * ishga tushadigan asosiy metod.
     * @param message Telegram'dan kelgan Message obyekti.
     */
    void handleMessage(Message message);

    /**
     * Adminga asosiy "Xush kelibsiz" xabarini, ReplyKeyboard bilan birga yuboradi.
     * Bu metod orqaga qaytish ('Back') funksionalligi uchun ham ishlatiladi.
     * @param chatId Adminning chat ID'si.
     * @param profile Adminning saytdagi profili ma'lumotlari.
     */
    void sendAdminWelcomeMessage(Long chatId, UserProfile profile);

    @Transactional(readOnly = true)
    void showDashboard(User user, Long chatId, Integer messageId);
}
package uz.pdp.online_education.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Bu Controller faqat veb-sahifalarni (view) ochib berish uchun javobgar.
 * U API chaqiruvlarini emas, balki foydalanuvchiga ko'rinadigan HTML sahifalarni qaytaradi.
 */
@Controller // @RestController EMAS!
public class AuthPageController {

    /**
     * Telegram botidan kelgan so'rovni qabul qilib,
     * `templates` papkasidagi `auth.html` sahifasini ochib beradi.
     *
     * @param chatId Telegram botdan yuborilgan foydalanuvchining chat IDsi (hozircha ishlatilmayapti,
     *               lekin kelajakda kerak bo'lishi mumkin).
     * @return Shablon nomi ("auth"), Spring Boot buni `templates/auth.html` deb tushunadi.
     */
//    @GetMapping("/auth/telegram") // Botdan keladigan yangi manzil
//    public String showTelegramAuthPage(@RequestParam("chat_id") String chatId) {
//        // Bu yerda hozircha hech qanday logika shart emas.
//        // JavaScript o'zi 'chat_id'ni URL'dan oladi.
//        // Asosiy vazifa - shablon nomini qaytarish.
//        return "auth"; // Bu `resources/templates/auth.html` faylini qidirishni bildiradi
//    }
    @GetMapping("/auth/telegram-init")
    public String showAuthPage(@RequestParam("chat_id") String chatId, Model model) {
        // chat_id'ni shablonga yuborish mumkin, lekin JavaScript orqali olish osonroq.
        // model.addAttribute("chatId", chatId);
        return "auth"; // Bu resources/templates/auth.html faylini qidiradi
    }
}
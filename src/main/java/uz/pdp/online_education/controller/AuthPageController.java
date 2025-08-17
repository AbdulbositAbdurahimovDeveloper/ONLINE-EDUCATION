package uz.pdp.online_education.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * AuthPageController foydalanuvchilarning autentifikatsiya (kirish) jarayoniga oid
 * sahifalarni ochib berish uchun mas’ul bo‘lgan controller hisoblanadi.
 * <p>
 * Ushbu controller REST API emas, balki oddiy HTML sahifalarni qaytaradi.
 * Shuning uchun {@link Controller} annotatsiyasidan foydalanilgan
 * ({@link org.springframework.web.bind.annotation.RestController} emas!).
 * </p>
 *
 * <p>
 * Vazifasi: Telegram bot orqali foydalanuvchini tizimga yo‘naltirish
 * va unga autentifikatsiya sahifasini (`auth.html`) ko‘rsatish.
 * </p>
 */
@Slf4j
@Controller
public class AuthPageController {

    /**
     * Telegram botdan kelgan foydalanuvchining chat ID si orqali
     * autentifikatsiya sahifasini ochib beradi.
     *
     * <p>
     * Nima uchun kerak?
     * <ul>
     *     <li>Bot foydalanuvchini tashqi veb-sahifaga yuboradi</li>
     *     <li>URL orqali `chat_id` yuboriladi</li>
     *     <li>JavaScript shu `chat_id` qiymatini URL dan olib, keyingi API chaqiruvlarda foydalanadi</li>
     * </ul>
     * </p>
     *
     * @param chatId Telegram foydalanuvchisining chat IDsi.
     *               Majburiy param bo‘lib, botdan keladigan link orqali keladi.
     * @param model  Spring’ning {@link Model} obyekti.
     *               Agar xohlasak, `chatId`ni sahifaga ham uzatishimiz mumkin.
     * @return `auth.html` shabloni nomi (`resources/templates/auth.html` fayli ochiladi).
     */
    @GetMapping("/auth/telegram-init")
    public String showAuthPage(@RequestParam("chat_id") String chatId, Model model) {
        // Logga yozib qo'yamiz (diagnostika uchun foydali)
        log.info("Telegram autentifikatsiya sahifasi chaqirildi. chat_id = {}", chatId);

        // Hozircha chat_id'ni shablonga uzatish shart emas, JS uni URL'dan oladi.
        // Lekin kelajakda kerak bo‘lsa, quyidagicha ishlatsa bo‘ladi:
        // model.addAttribute("chatId", chatId);

        // "auth" — bu `resources/templates/auth.html` fayliga mos keladi.
        return "auth";
    }

    /**
     * Kelajakda qo‘shilishi mumkin bo‘lgan yana bir endpoint.
     * Masalan, foydalanuvchi login jarayonidan so‘ng boshqa sahifaga yo‘naltirish uchun ishlatiladi.
     * <p>
     * Hozircha bu metod faqat namunaviy ko‘rinishda yozilgan.
     * </p>
     *
     * @return "login-success.html" (kelajakda kerak bo‘lsa ishlatiladi).
     */
    @GetMapping("/auth/success")
    public String showSuccessPage() {
        log.info("Foydalanuvchi muvaffaqiyatli autentifikatsiyadan o‘tdi.");
        return "login-success"; // Bu faylni ham `resources/templates/` ichiga qo‘yish kerak.
    }

    /**
     * Kelajak uchun yana bir metod — autentifikatsiya muvaffaqiyatsiz bo‘lganda
     * xato sahifasini qaytaradi.
     *
     * @param errorMessage Xato haqida ma’lumot (masalan: "Token muddati tugagan").
     * @param model        Model orqali xato xabari view ga yuboriladi.
     * @return "auth-error.html" sahifasi.
     */
    @GetMapping("/auth/error")
    public String showErrorPage(@RequestParam(name = "message", required = false, defaultValue = "Noma’lum xato") String errorMessage,
                                Model model) {
        log.warn("Autentifikatsiya xatosi: {}", errorMessage);
        model.addAttribute("errorMessage", errorMessage);
        return "auth-error"; // Bu fayl ham `resources/templates/` ichida bo‘lishi kerak.
    }
}

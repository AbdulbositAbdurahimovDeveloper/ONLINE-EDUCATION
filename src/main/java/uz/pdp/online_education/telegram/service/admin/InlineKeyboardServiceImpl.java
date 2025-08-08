package uz.pdp.online_education.telegram.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import uz.pdp.online_education.telegram.service.admin.template.InlineKeyboardService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InlineKeyboardServiceImpl implements InlineKeyboardService {

    @Value("${telegram.bot.webhook-path}")
    private String url;

    /**
     * Foydalanuvchi botga birinchi marta kirganda yuboriladigan
     * "Kirish / Ro'yxatdan o'tish" tugmasini yaratib beradi.
     *
     * @param chatId Foydalanuvchining unikal chat IDsi, bu URLga qo'shiladi.
     * @return Yagona tugmali InlineKeyboardMarkup.
     */
    public InlineKeyboardMarkup welcomeFirstTime(Long chatId) {
        // 1. Asosiy klaviatura obyektini yaratish
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        // 2. Tugmaning o'zini yaratish
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("🚀 Kirish / Ro'yxatdan o'tish");

        // 3. Foydalanuvchi uchun unikal URLni generatsiya qilish
        // TODO: Production uchun "localhost:8080"ni o'z domeningizga o'zgartiring!
        String authenticationUrl = url + "/auth.html?chat_id=" + chatId;
        button.setUrl(authenticationUrl);

        // 4. Tugmani qatorga va qatorni klaviaturaga joylash
        // List.of() - bu qisqa va qulay usul
        inlineKeyboardMarkup.setKeyboard(List.of(List.of(button)));

        // 5. Tayyor klaviaturani qaytarish
        return inlineKeyboardMarkup;
    }
}

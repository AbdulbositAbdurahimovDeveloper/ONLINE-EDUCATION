package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.online_education.service.interfaces.TelegramService;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramServiceImpl implements TelegramService {

    // Bu yerga siz o'z Telegram Bot komponentingizni inject qilishingiz kerak
    // private final YourTelegramBotComponent telegramBot;

    @Override
    public void sendNotification(String chatId, String message) {
        // TODO: Bu yerga o'z Telegram botingiz orqali xabar yuborish mantiqini yozing.
        // Masalan:
//         SendMessage sendMessage = new SendMessage(chatId, message);
//         try {
//             telegramBot.execute(sendMessage);
//             log.info("Successfully sent Telegram notification to chat_id: {}", chatId);
//         } catch (TelegramApiException e) {
//             log.error("Failed to send Telegram notification to chat_id: {}. Error: {}", chatId, e.getMessage());
//         }

        // Hozircha shunchaki logga chiqaramiz:
        log.info("IMITATION: Sending Telegram message to chatId {}: '{}'", chatId, message);
    }
}
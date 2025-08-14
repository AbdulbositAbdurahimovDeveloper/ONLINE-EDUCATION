package uz.pdp.online_education.service.interfaces;

public interface TelegramService {
    void sendNotification(String chatId, String message);
}
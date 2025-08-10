package uz.pdp.online_education.telegram.mapper;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

public interface SendMsg {

    SendMessage sendMessage(Long chatId, String message);

    SendMessage sendMessage(String chatId, String message);

    SendMessage sendMessage(Long chatId, String message, ReplyKeyboard keyboard);
    DeleteMessage deleteMessage(Long string, Integer messageId);
    SendMessage sendReplyKeyboardRemove(Long chatId, String text);

    EditMessageText editMessage(Long chatId, Integer messageId, String menuText, InlineKeyboardMarkup keyboard);

    EditMessageText editMessage(Long chatId, Integer messageId, String menuText);

    EditMessageReplyMarkup editMarkup(Long chatId, Integer messageId);
}

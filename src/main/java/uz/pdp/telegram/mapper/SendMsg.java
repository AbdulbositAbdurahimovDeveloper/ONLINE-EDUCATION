package uz.pdp.telegram.mapper;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

public interface SendMsg {

    SendMessage sendMessage(Long chatId, String message);

    SendMessage sendMessage(String chatId, String message);


    SendMessage sendMessage(Long chatId, String message, ReplyKeyboardMarkup replyKeyboardMarkup);


    SendMessage sendMessage(Long chatId, String message, InlineKeyboardMarkup inlineKeyboardMarkup);


}

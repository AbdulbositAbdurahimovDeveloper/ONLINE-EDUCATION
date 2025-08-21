package uz.pdp.online_education.telegram.mapper;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

public interface SendMsg {

    SendMessage sendMessage(Long chatId, String message);

    SendMessage sendMessage(String chatId, String message);

    SendMessage sendMessage(Long chatId, String message, ReplyKeyboard keyboard);

    DeleteMessage deleteMessage(Long chatId, Integer messageId);

    SendMessage sendReplyKeyboardRemove(Long chatId, String text);

    EditMessageText editMessage(Long chatId, Integer messageId, String menuText, InlineKeyboardMarkup keyboard);

    EditMessageText editMessage(Long chatId, Integer messageId, String menuText);

    EditMessageReplyMarkup editMarkup(Long chatId, Integer messageId);

    /**
     * Xabardagi rasmni, sarlavhani (caption) va tugmalarni bir vaqtda tahrirlaydi.
     *
     * @param chatId    Kimga yuborilishi
     * @param messageId Qaysi xabar tahrirlanishi
     * @param fileId    Rasmning telegramdagi file_id si
     * @param caption   Rasm ostidagi matn
     * @param keyboard  Yangi inline tugmalar
     * @return Tayyor EditMessageMedia obyekti
     */
    EditMessageMedia editMessageMedia(Long chatId, Integer messageId, String fileId, String caption, InlineKeyboardMarkup keyboard);

    /**
     * Rasm, sarlavha va tugmalar bilan xabar yuboradi.
     *
     * @param chatId   Kimga yuborilishi
     * @param file     Rasm (InputFile)
     * @param caption  Rasm ostidagi matn
     * @param keyboard Inline tugmalar
     * @return Tayyor SendPhoto obyekti
     */
    SendPhoto sendPhoto(Long chatId, String file, String caption, InlineKeyboardMarkup keyboard);

    // metod botga jonatilgan rasmni kanalga tashlash uchun kerak
    SendPhoto sendPhoto(String channelId, InputFile file);

    SendPhoto sendPhoto(Long chatId, String fileId, String caption);

    AnswerCallbackQuery answerCallbackQuery(String callbackQueryId, String text);

}

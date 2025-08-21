package uz.pdp.online_education.telegram.mapper;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

@Service
public class SendMsgImpl implements SendMsg {

    @Override
    public SendMessage sendMessage(Long chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        sendMessage.setParseMode("HTML");
        return sendMessage;
    }

    @Override
    public SendMessage sendMessage(String chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        return sendMessage;
    }

    @Override
    public SendMessage sendMessage(Long chatId, String message, ReplyKeyboard keyboard) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        sendMessage.setParseMode("HTML");
        sendMessage.setReplyMarkup(keyboard);
        return sendMessage;
    }

    /**
     * @param chatId
     * @param messageId
     * @return
     */
    @Override
    public DeleteMessage deleteMessage(Long chatId, Integer messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);
        return deleteMessage;
    }

    /**
     * Creates a message specifically to remove the ReplyKeyboard from the user's screen.
     *
     * @param chatId The target chat ID.
     * @param text   A temporary text to be shown while the keyboard is being removed.
     * @return A SendMessage object configured to remove the reply keyboard.
     */
    @Override
    public SendMessage sendReplyKeyboardRemove(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), text);

        ReplyKeyboardRemove removeKeyboard = new ReplyKeyboardRemove(true);
        sendMessage.setReplyMarkup(removeKeyboard);

        return sendMessage;
    }

    /**
     * PRIMARY METHOD: Creates a fully configured EditMessageText object.
     *
     * @param chatId      The target chat ID.
     * @param messageId   The ID of the message to be edited.
     * @param newText     The new text for the message.
     * @param newKeyboard The new inline keyboard for the message.
     * @return A configured {@link EditMessageText} object.
     */
    public EditMessageText editMessage(Long chatId, Integer messageId, String newText, InlineKeyboardMarkup newKeyboard) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId.toString());
        editMessageText.setMessageId(messageId);
        editMessageText.setText(newText);
        editMessageText.setParseMode("HTML");
        editMessageText.setReplyMarkup(newKeyboard);
        return editMessageText;
    }

    /**
     * OVERLOADED: Edits only the text of a message, leaving the keyboard unchanged.
     *
     * @param chatId    The target chat ID.
     * @param messageId The ID of the message to be edited.
     * @param newText   The new text for the message.
     * @return A configured {@link EditMessageText} object.
     */
    @Override
    public EditMessageText editMessage(Long chatId, Integer messageId, String newText) {
        return editMessage(chatId, messageId, newText, null);
    }

    @Override
    public EditMessageReplyMarkup editMarkup(Long chatId, Integer messageId) {
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(chatId.toString());
        editMessageReplyMarkup.setMessageId(messageId);
        editMessageReplyMarkup.setReplyMarkup(null);
        return editMessageReplyMarkup;
    }

    @Override
    public EditMessageMedia editMessageMedia(Long chatId, Integer messageId, String fileId, String caption, InlineKeyboardMarkup keyboard) {
        InputMediaPhoto media = new InputMediaPhoto();
        media.setMedia(fileId);
        media.setCaption(caption);
        media.setParseMode("HTML");

        EditMessageMedia editMessageMedia = new EditMessageMedia();
        editMessageMedia.setChatId(String.valueOf(chatId));
        editMessageMedia.setMessageId(messageId);
        editMessageMedia.setMedia(media);
        editMessageMedia.setReplyMarkup(keyboard);

        return editMessageMedia;
    }

    @Override
    public SendPhoto sendPhoto(Long chatId, String file, String caption, InlineKeyboardMarkup keyboard) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(chatId));
        sendPhoto.setPhoto(new InputFile(file));
        sendPhoto.setCaption(caption);
        sendPhoto.setReplyMarkup(keyboard);
        sendPhoto.setParseMode("HTML");
        return sendPhoto;
    }

    @Override
    public SendPhoto sendPhoto(String channelId, InputFile file) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(channelId);
        sendPhoto.setPhoto(file);
        sendPhoto.setParseMode("HTML");
        return sendPhoto;
    }

    @Override
    public SendPhoto sendPhoto(Long chatId, String fileId, String caption) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(chatId));  // qaysi userga yuborish kerak
        sendPhoto.setPhoto(new InputFile(fileId));    // kanaldagi rasm file_id
        sendPhoto.setCaption(caption);
        sendPhoto.setParseMode("HTML");
        return sendPhoto;
    }

    /**
     * @param callbackQueryId
     * @param text
     * @return
     */
    @Override
    public AnswerCallbackQuery answerCallbackQuery(String callbackQueryId, String text) {
        return AnswerCallbackQuery.builder().callbackQueryId(callbackQueryId).text(text).build();
    }
}

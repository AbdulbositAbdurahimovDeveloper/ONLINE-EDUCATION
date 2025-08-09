package uz.pdp.online_education.telegram.service.admin;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.service.admin.template.ReplyKeyboardService;

import java.util.ArrayList;
import java.util.List;

/**
 * The concrete implementation of the {@link ReplyKeyboardService} interface.
 * This service is responsible for building the actual Reply Keyboard layouts for different
 * user roles, using centralized constants from the Utils interface.
 */
@Service
public class ReplyKeyboardServiceImpl implements ReplyKeyboardService {

    private ReplyKeyboardMarkup createBaseReplyKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        return replyKeyboardMarkup;
    }

    @Override
    public ReplyKeyboardMarkup adminMainMenu() {
        ReplyKeyboardMarkup replyKeyboardMarkup = createBaseReplyKeyboard();
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(Utils.ReplyButtons.ADMIN_USERS));
        row1.add(new KeyboardButton(Utils.ReplyButtons.ADMIN_COURSES));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton(Utils.ReplyButtons.ADMIN_SEND_MESSAGE));
        row2.add(new KeyboardButton(Utils.ReplyButtons.ADMIN_STATISTICS));

        keyboardRows.add(row1);
        keyboardRows.add(row2);
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        return replyKeyboardMarkup;
    }

    @Override
    public ReplyKeyboardMarkup instructorMainMenu() {
        ReplyKeyboardMarkup replyKeyboardMarkup = createBaseReplyKeyboard();
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(Utils.ReplyButtons.INSTRUCTOR_MY_COURSES));
        row1.add(new KeyboardButton(Utils.ReplyButtons.INSTRUCTOR_MY_STUDENTS));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton(Utils.ReplyButtons.INSTRUCTOR_REVIEWS));
        row2.add(new KeyboardButton(Utils.ReplyButtons.INSTRUCTOR_MY_REVENUE));

        keyboardRows.add(row1);
        keyboardRows.add(row2);
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        return replyKeyboardMarkup;
    }

    @Override
    public ReplyKeyboardMarkup studentMainMenu() {
        ReplyKeyboardMarkup replyKeyboardMarkup = createBaseReplyKeyboard();
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(Utils.ReplyButtons.STUDENT_MY_COURSES));
        row1.add(new KeyboardButton(Utils.ReplyButtons.STUDENT_ALL_COURSES));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton(Utils.ReplyButtons.STUDENT_BALANCE));
        row2.add(new KeyboardButton(Utils.ReplyButtons.STUDENT_HELP));

        keyboardRows.add(row1);
        keyboardRows.add(row2);
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        return replyKeyboardMarkup;
    }
}
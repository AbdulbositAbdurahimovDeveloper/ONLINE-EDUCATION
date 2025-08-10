package uz.pdp.online_education.telegram.service.student;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.service.student.template.StudentReplyKeyboardService;

import java.util.ArrayList;
import java.util.List;

@Service
public class StudentReplyKeyboardServiceImpl implements StudentReplyKeyboardService {

    /**
     * Creates a base {@link ReplyKeyboardMarkup} with common settings.
     * @return A pre-configured ReplyKeyboardMarkup object.
     */
    private ReplyKeyboardMarkup createBaseReplyKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        return replyKeyboardMarkup;
    }


    /**
     * {@inheritDoc}
     * Creates the main menu keyboard for the Student.
     * The layout is a 2x2 grid with the most common actions.
     */
    @Override
    public ReplyKeyboardMarkup studentMainMenu() {
        // Step 1: Create a base keyboard with standard settings.
        ReplyKeyboardMarkup replyKeyboardMarkup = createBaseReplyKeyboard();

        // Step 2: Initialize the list that will hold all rows of buttons.
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        // Step 3: Create the first row of buttons.
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(Utils.ReplyButtons.STUDENT_MY_COURSES));
        row1.add(new KeyboardButton(Utils.ReplyButtons.STUDENT_ALL_COURSES));

        // Step 4: Create the second row of buttons.
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton(Utils.ReplyButtons.STUDENT_BALANCE));
        row2.add(new KeyboardButton(Utils.ReplyButtons.STUDENT_HELP));

        // Step 5: Add all rows to the keyboard list.
        keyboardRows.add(row1);
        keyboardRows.add(row2);

        // Step 6: Set the completed list of rows to the keyboard object.
        replyKeyboardMarkup.setKeyboard(keyboardRows);

        // Step 7: Return the fully constructed keyboard.
        return replyKeyboardMarkup;
    }
}

package uz.pdp.online_education.telegram.service.instructor;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.service.instructor.template.InstructorReplyKeyboardService;

import java.util.ArrayList;
import java.util.List;

@Service
public class InstructorReplyKeyboardServiceImpl implements InstructorReplyKeyboardService {

    @Override
    public ReplyKeyboardMarkup buildMentorMenu() {
        KeyboardButton btn1 = new KeyboardButton(Utils.ReplyButtons.INSTRUCTOR_MY_COURSES);
        KeyboardButton btn2 = new KeyboardButton(Utils.ReplyButtons.INSTRUCTOR_MY_STUDENTS);
        KeyboardButton btn3 = new KeyboardButton(Utils.ReplyButtons.INSTRUCTOR_REVIEWS);
        KeyboardButton btn4 = new KeyboardButton(Utils.ReplyButtons.INSTRUCTOR_MY_REVENUE);

        // 1-qator
        KeyboardRow row1 = new KeyboardRow();
        row1.add(btn1);
        row1.add(btn2);

        // 2-qator
        KeyboardRow row2 = new KeyboardRow();
        row2.add(btn3);
        row2.add(btn4);

        // barcha qatorlarni qo‘shamiz
        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(row1);
        rows.add(row2);

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(rows);
        keyboard.setResizeKeyboard(true);     // telefon ekraniga moslashadi
        keyboard.setOneTimeKeyboard(false);   // doimiy turadi

        return keyboard;
    }


}

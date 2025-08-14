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
     * Creates the main menu keyboard for the Student.
     * The layout is a 2x2 grid with the most common actions.
     */
    @Override
    public ReplyKeyboardMarkup studentMainMenu() {
        // 1. Standart sozlamalarga ega bo'lgan asosiy klaviatura yaratib olamiz.
        ReplyKeyboardMarkup replyKeyboardMarkup = createBaseReplyKeyboard();

        // 2. Tugmalar qatorlarini saqlash uchun ro'yxat (list) ochamiz.
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        // 3. Birinchi qator tugmalarini yaratamiz.
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(Utils.ReplyButtons.STUDENT_MY_COURSES));
        row1.add(new KeyboardButton(Utils.ReplyButtons.STUDENT_ALL_COURSES));

        // 4. Ikkinchi qator tugmalarini yaratamiz.
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton(Utils.ReplyButtons.STUDENT_BALANCE));
        row2.add(new KeyboardButton(Utils.ReplyButtons.STUDENT_HELP));

        // 5. Yaratilgan qatorlarni umumiy ro'yxatga qo'shamiz.
        keyboardRows.add(row1);
        keyboardRows.add(row2);

        // 6. Tugmalar ro'yxatini klaviaturaga o'rnatamiz.
        replyKeyboardMarkup.setKeyboard(keyboardRows);

        // 7. Tayyor klaviaturani qaytaramiz.
        return replyKeyboardMarkup;
    }



    /**
     * Creates a base {@link ReplyKeyboardMarkup} with common settings
     * to avoid code duplication.
     * @return A pre-configured ReplyKeyboardMarkup object.
     */
    private ReplyKeyboardMarkup createBaseReplyKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        // Klaviaturani ekran o'lchamiga moslashtirish
        replyKeyboardMarkup.setResizeKeyboard(true);
        // Faqat shu foydalanuvchiga ko'rsatish
        replyKeyboardMarkup.setSelective(true);
        // Tugma bosilgandan so'ng klaviaturani yashirish
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        return replyKeyboardMarkup;
    }
}
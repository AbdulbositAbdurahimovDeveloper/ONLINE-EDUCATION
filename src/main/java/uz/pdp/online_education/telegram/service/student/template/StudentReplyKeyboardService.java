package uz.pdp.online_education.telegram.service.student.template;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

/**
 * An interface for creating Reply Keyboards (the buttons at the bottom of the screen)
 * for users in the "Student" role.
 */
public interface StudentReplyKeyboardService {

    /**
     * Constructs the main navigation keyboard for a student.
     * This keyboard is displayed at the bottom of the screen and contains buttons for
     * top-level actions such as viewing courses, checking balance, and seeking help.
     *
     * @return A {@link ReplyKeyboardMarkup} object configured with the student's main menu layout.
     */
    ReplyKeyboardMarkup studentMainMenu();
}
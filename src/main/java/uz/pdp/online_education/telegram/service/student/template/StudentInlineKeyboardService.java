package uz.pdp.online_education.telegram.service.student.template;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface StudentInlineKeyboardService {

    /**
     * Creates the keyboard for the dashboard message, which includes a "Logout" button.
     * @return An InlineKeyboardMarkup with a logout button.
     */
    InlineKeyboardMarkup dashboardMenu();

    /**
     * Creates a confirmation keyboard for the logout action.
     * Asks the user to confirm if they really want to log out.
     * @return An InlineKeyboardMarkup with "Yes" and "No" buttons.
     */
    InlineKeyboardMarkup logoutConfirmation();
}

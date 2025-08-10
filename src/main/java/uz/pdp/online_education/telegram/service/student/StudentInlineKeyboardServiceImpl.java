package uz.pdp.online_education.telegram.service.student;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.service.student.template.StudentInlineKeyboardService;

import java.util.List;


@Service
public class StudentInlineKeyboardServiceImpl implements StudentInlineKeyboardService {

    /**
     * {@inheritDoc}
     */
    @Override
    public InlineKeyboardMarkup dashboardMenu() {
        return createSingleButtonKeyboard(
                Utils.InlineButtons.LOGOUT_TEXT,
                Utils.CallbackData.AUTH_LOGOUT_INIT_CALLBACK
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InlineKeyboardMarkup logoutConfirmation() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        // Create the "Yes" and "No" buttons
        InlineKeyboardButton yesButton = createButton(
                Utils.InlineButtons.LOGOUT_CONFIRM_YES_TEXT,
                Utils.CallbackData.AUTH_LOGOUT_CONFIRM_CALLBACK
        );

        InlineKeyboardButton noButton = createButton(
                Utils.InlineButtons.LOGOUT_CONFIRM_NO_TEXT,
                Utils.CallbackData.AUTH_LOGOUT_CANCEL_CALLBACK
        );

        // Place them side-by-side in a single row
        markup.setKeyboard(List.of(List.of(yesButton, noButton)));

        return markup;
    }

    /**
     * A private helper method to create a single {@link InlineKeyboardButton}.
     * This centralizes button creation.
     *
     * @param text         The text to be displayed on the button.
     * @param callbackData The data to be sent when the button is pressed.
     * @return A configured {@link InlineKeyboardButton} object.
     */
    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton(text);
        if (callbackData != null) {
            button.setCallbackData(callbackData);
        }
        return button;
    }

    /**
     * A private helper to create a keyboard with a single, centered button.
     * Useful for simple actions like "Back".
     *
     * @param text         The button's text.
     * @param callbackData The button's callback data.
     * @return An {@link InlineKeyboardMarkup} with one button.
     */
    private InlineKeyboardMarkup createSingleButtonKeyboard(String text, String callbackData) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        InlineKeyboardButton button = createButton(text, callbackData);
        markup.setKeyboard(List.of(List.of(button)));
        return markup;
    }
}

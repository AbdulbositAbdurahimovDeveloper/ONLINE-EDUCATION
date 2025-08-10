package uz.pdp.online_education.telegram.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.service.admin.template.InlineKeyboardService;

import java.util.List;

/**
 * The concrete implementation of the {@link InlineKeyboardService} interface.
 * This service builds context-specific Inline Keyboards for various bot interactions.
 */
@Service
@RequiredArgsConstructor
public class InlineKeyboardServiceImpl implements InlineKeyboardService {

    // This value should be defined in your application.yml/properties
    // Example: app.domain=http://your-domain.com
    @Value("${telegram.bot.webhook-path}")
    private String appDomain;

    @Override
    public InlineKeyboardMarkup welcomeFirstTime(Long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton button = new InlineKeyboardButton();

        button.setText("🚀 Kirish / Ro'yxatdan o'tish");

        // Creates the correct authentication URL for the user
        String authenticationUrl = appDomain + "/auth.html?chat_id=" + chatId;
        button.setUrl(authenticationUrl);

        inlineKeyboardMarkup.setKeyboard(List.of(List.of(button)));
        return inlineKeyboardMarkup;
    }

    @Override
    public InlineKeyboardMarkup usersMainMenu() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        // Create buttons using text from the Utils interface
        InlineKeyboardButton listBtn = new InlineKeyboardButton(Utils.InlineButtons.USER_LIST_TEXT);
        InlineKeyboardButton searchBtn = new InlineKeyboardButton(Utils.InlineButtons.USER_SEARCH_TEXT);
        InlineKeyboardButton statsBtn = new InlineKeyboardButton(Utils.InlineButtons.USER_STATS_TEXT);
        InlineKeyboardButton backBtn = new InlineKeyboardButton(Utils.InlineButtons.BACK_TO_MAIN_MENU_TEXT);

        // Set callback data using constants from the Utils interface
        listBtn.setCallbackData(Utils.CallbackData.USER_LIST_CALLBACK);
        searchBtn.setCallbackData(Utils.CallbackData.USER_SEARCH_CALLBACK);
        statsBtn.setCallbackData(Utils.CallbackData.USER_STATS_CALLBACK);
        backBtn.setCallbackData(Utils.CallbackData.BACK_TO_ADMIN_MENU_CALLBACK);

        // Arrange buttons vertically for better readability on mobile devices
        markup.setKeyboard(List.of(
                List.of(listBtn),
                List.of(searchBtn),
                List.of(statsBtn),
                List.of(backBtn)
        ));

        return markup;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InlineKeyboardMarkup coursesMainMenu() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        // Create buttons using text from the Utils interface
        InlineKeyboardButton listBtn = new InlineKeyboardButton(Utils.InlineButtons.COURSE_LIST_TEXT);
        InlineKeyboardButton addBtn = new InlineKeyboardButton(Utils.InlineButtons.COURSE_ADD_TEXT);
        InlineKeyboardButton searchBtn = new InlineKeyboardButton(Utils.InlineButtons.COURSE_SEARCH_TEXT);
        InlineKeyboardButton backBtn = new InlineKeyboardButton(Utils.InlineButtons.BACK_TO_MAIN_MENU_TEXT);

        // Set callback data using constants from the Utils interface
        listBtn.setCallbackData(Utils.CallbackData.COURSE_LIST_CALLBACK);
        addBtn.setCallbackData(Utils.CallbackData.COURSE_ADD_CALLBACK);
        searchBtn.setCallbackData(Utils.CallbackData.COURSE_SEARCH_CALLBACK);
        backBtn.setCallbackData(Utils.CallbackData.BACK_TO_ADMIN_MENU_CALLBACK);

        // Arrange buttons vertically
        markup.setKeyboard(List.of(
                List.of(listBtn),
                List.of(addBtn),
                List.of(searchBtn),
                List.of(backBtn)
        ));

        return markup;
    }
}
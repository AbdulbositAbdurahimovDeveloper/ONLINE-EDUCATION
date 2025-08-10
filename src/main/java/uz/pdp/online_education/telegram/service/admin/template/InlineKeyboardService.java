package uz.pdp.online_education.telegram.service.admin.template;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface InlineKeyboardService {
    InlineKeyboardMarkup welcomeFirstTime(Long chatId);

    InlineKeyboardMarkup usersMainMenu();

    /**
     * Creates the main menu for the course management section.
     * @return An InlineKeyboardMarkup with course-related options.
     */
    InlineKeyboardMarkup coursesMainMenu();
}

package uz.pdp.online_education.telegram.service.admin.template;

import org.springframework.data.domain.Page;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.User;

public interface InlineKeyboardService {
    InlineKeyboardMarkup dashboardMenu();

    InlineKeyboardMarkup logoutConfirmation();

    InlineKeyboardMarkup createSingleButtonKeyboard(String text, String callbackData);
    InlineKeyboardMarkup welcomeFirstTime(Long chatId);
    InlineKeyboardMarkup usersMainMenu();
    InlineKeyboardMarkup coursesMainMenu();
    InlineKeyboardMarkup usersPageMenu(Page<User> userPage, String searchTerm);
    InlineKeyboardMarkup userDetailMenu(Long userId, String backCallbackData);

    InlineKeyboardMarkup coursesPageMenu(Page<Course> coursePage, String searchTerm);
    InlineKeyboardMarkup courseDetailMenu(Long courseId, String backCallbackData);
}
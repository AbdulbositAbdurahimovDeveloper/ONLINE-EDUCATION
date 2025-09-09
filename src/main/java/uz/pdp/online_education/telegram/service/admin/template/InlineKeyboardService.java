package uz.pdp.online_education.telegram.service.admin.template;

import org.springframework.data.domain.Page;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import uz.pdp.online_education.enums.Role;
import uz.pdp.online_education.model.Category;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.User;

import java.util.Set;

public interface InlineKeyboardService {

    InlineKeyboardMarkup dashboardMenu();

    InlineKeyboardMarkup logoutConfirmation();

    InlineKeyboardMarkup createSingleButtonKeyboard(String text, String callbackData);
    InlineKeyboardMarkup welcomeFirstTime(Long chatId);


    InlineKeyboardMarkup usersMainMenu();
    InlineKeyboardMarkup usersPageMenu(Page<User> userPage, String searchTerm);
    InlineKeyboardMarkup userDetailMenu(Long userId, String backCallbackData);


    InlineKeyboardMarkup coursesMainMenu();
    InlineKeyboardMarkup courseBrowseMethodMenu(String backCallback);
    InlineKeyboardMarkup mentorsPageMenu(Page<User> mentorPage, String backCallback);


    InlineKeyboardMarkup coursesPageMenu(Page<Course> coursePage, String searchTerm, Long mentorId, Long categoryId);

    /**
     * Bitta kurs haqidagi ma'lumot oynasi uchun klaviatura yaratadi.
     * @param courseId Kurs ID'si.
     * @param backCallbackData Orqaga qaytish tugmasi uchun to'liq callback ma'lumoti.
     * @return Tayyor InlineKeyboardMarkup.
     */
    InlineKeyboardMarkup courseDetailMenu(Long courseId, String backCallbackData);


    InlineKeyboardMarkup categoriesPageMenu(Page<Category> categoryPage, String backCallback);

    InlineKeyboardMarkup allCoursesPageMenu(Page<Course> coursePage);

    InlineKeyboardMarkup coursesByMentorPageMenu(Page<Course> coursePage, Long mentorId);

    InlineKeyboardMarkup coursesByCategoryPageMenu(Page<Course> coursePage, Long categoryId);



    /**
     * Xabar yuborish turini (Rasm+Matn / Faqat Matn) tanlash uchun klaviatura yaratadi.
     */
    InlineKeyboardMarkup broadcastTypeMenu();

    /**
     * Xabarni kimlarga (qaysi rollarga) yuborishni tanlash uchun klaviatura yaratadi.
     * Bu klaviatura dinamik bo'ladi, tanlangan rollarni ko'rsatib turadi.
     *
     * @param selectedRoles Hozirda tanlangan rollar to'plami.
     * @return Tayyor InlineKeyboardMarkup.
     */
//    InlineKeyboardMarkup broadcastTargetRolesMenu(Set<Role> selectedRoles);

    InlineKeyboardMarkup broadcastTargetRolesMenu();

    /**
     * Jarayonni tasdiqlash yoki bekor qilish uchun oddiy klaviatura.
     */
    InlineKeyboardMarkup confirmationMenu(String confirmCallback, String cancelCallback);

}
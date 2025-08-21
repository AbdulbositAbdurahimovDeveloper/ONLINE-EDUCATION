package uz.pdp.online_education.telegram.service.admin.template;

import org.springframework.data.domain.Page;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.User;

public interface InlineKeyboardService {
    // --- UMUMIY ---
    InlineKeyboardMarkup dashboardMenu();

    InlineKeyboardMarkup logoutConfirmation();

    InlineKeyboardMarkup createSingleButtonKeyboard(String text, String callbackData);
    InlineKeyboardMarkup welcomeFirstTime(Long chatId);

    // --- FOYDALANUVCHILAR UCHUN ---
    InlineKeyboardMarkup usersMainMenu();
    InlineKeyboardMarkup usersPageMenu(Page<User> userPage, String searchTerm);
    InlineKeyboardMarkup userDetailMenu(Long userId, String backCallbackData);

    // --- KURSLAR UCHUN ---
    InlineKeyboardMarkup coursesMainMenu();
    InlineKeyboardMarkup courseBrowseMethodMenu(String backCallback);
    InlineKeyboardMarkup mentorsPageMenu(Page<User> mentorPage, String backCallback);

    /**
     * Kurslar ro'yxati uchun sahifalangan klaviatura yaratadi.
     * @param coursePage Kurslar sahifasi.
     * @param mentorId Agar bu mentorga tegishli kurslar bo'lsa, o'sha mentorning ID'si. Aks holda null.
     * @return Tayyor InlineKeyboardMarkup.
     */
//    InlineKeyboardMarkup coursesPageMenu(Page<Course> coursePage, Long mentorId);

    // InlineKeyboardServiceImpl.java
    InlineKeyboardMarkup coursesPageMenu(Page<Course> coursePage, String searchTerm, Long mentorId);

    /**
     * Bitta kurs haqidagi ma'lumot oynasi uchun klaviatura yaratadi.
     * @param courseId Kurs ID'si.
     * @param backCallbackData Orqaga qaytish tugmasi uchun to'liq callback ma'lumoti.
     * @return Tayyor InlineKeyboardMarkup.
     */
    InlineKeyboardMarkup courseDetailMenu(Long courseId, String backCallbackData);
}
package uz.pdp.online_education.telegram.service.student.template;

import org.springframework.data.domain.Page;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.ModuleEnrollment;
import uz.pdp.online_education.model.lesson.Lesson;
import uz.pdp.online_education.payload.CategoryInfo;
import uz.pdp.online_education.payload.UserInfo;

import java.util.List;

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

    /**
     * Foydalanuvchi obuna bo'lgan kurslar ro'yxati uchun klaviatura yaratadi.
     */
    InlineKeyboardMarkup myCoursesMenu(Page<Course> coursePage);

    /**
     * Tanlangan kursning modullari ro'yxati uchun klaviatura yaratadi.
     */
    InlineKeyboardMarkup modulesMenu(Page<Module> modulePage, Long courseId, List<Long> enrolledModuleIds,boolean isEnrolledToFullCourse);

    /**
     * Bitta tugmadan iborat oddiy inline klaviatura yaratadi.
     * @param text Tugma matni
     * @param callbackData Tugma callback ma'lumoti
     * @return Bitta tugmali InlineKeyboardMarkup
     */
    InlineKeyboardMarkup createSingleButtonKeyboard(String text, String callbackData);

    InlineKeyboardMarkup lessonsMenu(Page<Lesson> lessonPage, Long moduleId, Long id, boolean isModuleEnrolled);

    InlineKeyboardMarkup lessonContentsMenu(Lesson lesson, Long id);

    /**
     * Veb-saytga olib boruvchi URL tugmasidan iborat klaviatura yaratadi.
     */
    InlineKeyboardMarkup createUrlButton(String text, String url);


}

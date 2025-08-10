package uz.pdp.online_education.telegram.service.student.template;

import org.springframework.data.domain.Page;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.ModuleEnrollment;
import uz.pdp.online_education.model.lesson.Lesson;

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

    InlineKeyboardMarkup myCoursesListPage(Page<Course> coursesPage);

    InlineKeyboardMarkup courseModulesPage(Long courseId, List<ModuleEnrollment> enrollments);

    /**
     * Creates a simple keyboard with a single "Back to My Courses" button.
     * Useful for views where the user needs to navigate back to their course list.
     *
     * @return An {@link InlineKeyboardMarkup} with a single back button.
     */
    InlineKeyboardMarkup backToMyCourses();

    /**
     * Darslar ro'yxati uchun to'liq klaviaturani yaratadi.
     * @param module Modul haqida to'liq ma'lumot (ID, narx, kurs IDsi uchun).
     * @param lessons Ushbu modulga tegishli darslar ro'yxati.
     * @param isEnrolled Foydalanuvchi bu modulga a'zo bo'lganmi yoki yo'q.
     * @return Tayyor InlineKeyboardMarkup.
     */
    InlineKeyboardMarkup lessonListPage(Module module, List<Lesson> lessons, boolean isEnrolled);

    InlineKeyboardMarkup lessonViewKeyboard(Long moduleId, String lessonUrlOnSite);

    InlineKeyboardMarkup lessonContentMenu(Lesson lesson);


    // Quiz uchun alohida klaviatura
    InlineKeyboardMarkup quizButton(Long quizId);

    InlineKeyboardMarkup buyOnlyKeyboard(Module module);
}

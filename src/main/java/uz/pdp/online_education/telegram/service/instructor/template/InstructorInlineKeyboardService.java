package uz.pdp.online_education.telegram.service.instructor.template;

import org.springframework.data.domain.Page;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import uz.pdp.online_education.model.Category;
import uz.pdp.online_education.model.Course;

public interface InstructorInlineKeyboardService {
    InlineKeyboardMarkup dashboardMenu();

    InlineKeyboardMarkup createSingleButtonKeyboard(String text, String callbackData);

    /**
     * Generates a confirmation keyboard for the logout action.
     * It prompts the user to confirm their intention to log out by providing "Yes" and "No" options.
     *
     * @return An {@link InlineKeyboardMarkup} containing "Yes" and "No" buttons for logout confirmation.
     */
    InlineKeyboardMarkup logoutConfirmation();

    InlineKeyboardMarkup instructorNoDraftCourse();

    InlineKeyboardMarkup myFullOrDraftCourses();

    InlineKeyboardMarkup myViewCourses(Page<Course> coursePage, String backButton, boolean successOrDraft);


    InlineKeyboardMarkup instructorViewCourses(Long CourseId, String backButton);

    InlineKeyboardMarkup succesOrDraftBtn(String processKey, String action);

    InlineKeyboardMarkup categorySelect(Page<Category> categories);
}

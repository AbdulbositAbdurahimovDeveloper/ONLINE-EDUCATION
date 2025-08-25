package uz.pdp.online_education.telegram.service.student.template;

import org.springframework.data.domain.Page;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.Payment;
import uz.pdp.online_education.model.lesson.Lesson;
import uz.pdp.online_education.payload.CategoryInfo;
import uz.pdp.online_education.payload.PageDTO;
import uz.pdp.online_education.payload.UserInfo;
import uz.pdp.online_education.payload.course.CourseDetailDTO;
import uz.pdp.online_education.payload.lesson.LessonResponseDTO;
import uz.pdp.online_education.payload.module.ModuleDetailDTO;

import java.util.List;

/**
 * An interface for creating various inline keyboards specific to the student's user journey.
 * It provides methods to generate dynamic menus for courses, modules, lessons, and other interactions.
 */
public interface StudentInlineKeyboardService {

    /**
     * Creates an inline keyboard for the student's dashboard.
     * This keyboard typically contains a button to initiate the logout process.
     *
     * @return An {@link InlineKeyboardMarkup} with a "Logout" button.
     */
    InlineKeyboardMarkup dashboardMenu();

    /**
     * Generates a confirmation keyboard for the logout action.
     * It prompts the user to confirm their intention to log out by providing "Yes" and "No" options.
     *
     * @return An {@link InlineKeyboardMarkup} containing "Yes" and "No" buttons for logout confirmation.
     */
    InlineKeyboardMarkup logoutConfirmation();

    /**
     * Constructs a paginated menu for the list of courses a student is enrolled in.
     * Each course is represented by a button, and the keyboard includes navigation controls (next/previous page).
     *
     * @param coursePage A {@link Page} object containing the courses for the current page.
     * @return An {@link InlineKeyboardMarkup} displaying the list of enrolled courses with pagination.
     */
    InlineKeyboardMarkup myCoursesMenu(Page<Course> coursePage);

    /**
     * Builds a menu for the modules within a specific course.
     * It displays module buttons with different icons (e.g., locked, unlocked, free) based on the user's enrollment status.
     * Also includes pagination and a "back" button to return to the course list.
     *
     * @param modulePage             A {@link Page} object of modules for the current page.
     * @param courseId               The ID of the parent course, used for the "back" button callback.
     * @param enrolledModuleIds      A list of module IDs the user is explicitly enrolled in.
     * @param isEnrolledToFullCourse A boolean flag indicating if the user has purchased the entire course.
     * @return An {@link InlineKeyboardMarkup} for navigating modules.
     */
    InlineKeyboardMarkup modulesMenu(Page<Module> modulePage, Long courseId, List<Long> enrolledModuleIds, boolean isEnrolledToFullCourse);

    /**
     * Generates a menu for the lessons within a specific module.
     * It shows locked or unlocked status for each lesson and includes pagination controls.
     *
     * @param lessonPage       A {@link Page} object containing the lessons.
     * @param moduleId         The ID of the parent module, used for pagination callbacks.
     * @param courseId         The ID of the course, used for the "back" button to return to the module list.
     * @param isModuleEnrolled A boolean indicating if the user is enrolled in the parent module.
     * @return An {@link InlineKeyboardMarkup} for navigating lessons.
     */
    InlineKeyboardMarkup lessonsMenu(Page<Lesson> lessonPage, Long moduleId, Long courseId, boolean isModuleEnrolled);

    /**
     * Creates a menu that lists the content blocks (e.g., text, video, quiz) of a specific lesson.
     * Each content block is a button that, when clicked, will display the content.
     *
     * @param lesson   The {@link Lesson} object whose contents are to be displayed.
     * @param moduleId The ID of the parent module, used for the "back" button callback.
     * @return An {@link InlineKeyboardMarkup} listing the available content blocks for the lesson.
     */
    InlineKeyboardMarkup lessonContentsMenu(Lesson lesson, Long moduleId,String  backCallback);

    /**
     * Creates a simple inline keyboard with a single button.
     * This is useful for simple actions like "Back to Main Menu" or a call-to-action.
     *
     * @param text         The text to be displayed on the button.
     * @param callbackData The callback data associated with the button press.
     * @return A new {@link InlineKeyboardMarkup} containing a single button.
     */
    InlineKeyboardMarkup createSingleButtonKeyboard(String text, String callbackData);

    /**
     * Creates an inline keyboard with a single button that links to an external URL.
     * This is typically used for redirecting users to a website for payment or external resources.
     *
     * @param text The text displayed on the button (e.g., "Buy Now").
     * @param url  The URL to which the user will be redirected.
     * @return An {@link InlineKeyboardMarkup} with a single URL button.
     */
    InlineKeyboardMarkup createUrlButton(String text, String url);

    InlineKeyboardMarkup selectCategoryAndInstructor();


    InlineKeyboardMarkup allCourses_categoriesMenu(Page<CategoryInfo> categoryPage);

    InlineKeyboardMarkup allCourses_instructorsMenu(Page<UserInfo> instructorPage);

    InlineKeyboardMarkup allCoursesMenu(PageDTO<CourseDetailDTO> categoryPageDTO, String backButton, String type, Long id);

    InlineKeyboardMarkup allCourseModules(PageDTO<ModuleDetailDTO> modulePageDTO, String backButton, Long id, String datum);

    InlineKeyboardMarkup allCourseLessons(PageDTO<LessonResponseDTO> lessonResponseDTOPageDTO, String backButton, Long id, String datum, boolean purchased, boolean queryData);

    InlineKeyboardMarkup buildYesNoKeyboard(Long id, String datum);

    InlineKeyboardMarkup buildPurchaseButton(Long id, String datum);

    InlineKeyboardMarkup createQuizContent(String s, String url);

    InlineKeyboardMarkup createBalanceMenuKeyboard(boolean hasPending, int pendingCount);

    InlineKeyboardMarkup userPaymentsHistory(Page<Payment> payments);

    InlineKeyboardMarkup userPendingPaymentsKeyboard(Page<Module> modules);

    InlineKeyboardMarkup buildModuleButtons(Module module);

}
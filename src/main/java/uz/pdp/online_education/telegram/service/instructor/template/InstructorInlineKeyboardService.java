package uz.pdp.online_education.telegram.service.instructor.template;

import org.springframework.data.domain.Page;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import uz.pdp.online_education.model.Category;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.payload.PageDTO;
import uz.pdp.online_education.payload.course.CourseDetailDTO;
import uz.pdp.online_education.payload.course.CourseStudentStatsProjection;
import uz.pdp.online_education.payload.lesson.LessonResponseDTO;
import uz.pdp.online_education.payload.module.ModuleDetailDTO;
import uz.pdp.online_education.payload.user.UserProjection;

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

    InlineKeyboardMarkup createUrlButton(String text, String url);

    InlineKeyboardMarkup instructorNoDraftCourse();

    InlineKeyboardMarkup myFullOrDraftCourses();

    InlineKeyboardMarkup myViewCourses(Page<Course> coursePage, String backButton, boolean successOrDraft);


    InlineKeyboardMarkup instructorViewCourses(Long CourseId, String backButton, Long modulesCount);

    InlineKeyboardMarkup succesOrDraftBtn(String processKey, String action);

    InlineKeyboardMarkup categorySelect(Page<Category> categories);

    InlineKeyboardMarkup readModule(PageDTO<ModuleDetailDTO> modulePage, String backButton);

    InlineKeyboardMarkup instructorViewModule(Long moduleId, String backButton, Integer lessonCount);

    InlineKeyboardMarkup isFree();


    InlineKeyboardMarkup instructorViewLesson(Long lessonId, String backButton, Integer contentSize);

    InlineKeyboardMarkup instructorViewLesson(PageDTO<LessonResponseDTO> lessonResponseDTOPageDTO, String backButton,Long id);

    InlineKeyboardMarkup chooseContent(long lessonId);

    InlineKeyboardMarkup instructorContent(LessonResponseDTO lessonResponseDTO, String backButton);

    InlineKeyboardMarkup instructorEditCourses(CourseDetailDTO courseDetailDTO, String backButton);

    InlineKeyboardMarkup deleteCourse(Long id);

    InlineKeyboardMarkup categorySelect(Page<Category> categories, String cancelBtn);

    InlineKeyboardMarkup instructorEditModules(ModuleDetailDTO moduleDetailDTO, String backButton);

    InlineKeyboardMarkup instructorEditLessons(LessonResponseDTO lessonResponseDTO, String backButton);

    InlineKeyboardMarkup lessonEditIsFree(Long id);

    InlineKeyboardMarkup deleteModule(Long id);

    InlineKeyboardMarkup deleteLesson(Long id);

    InlineKeyboardMarkup instructorMyStudents(Page<CourseStudentStatsProjection> stats, String backButton);

    InlineKeyboardMarkup instructorCourseViewStudents(Long courseId, String backButton);

    InlineKeyboardMarkup instructorStudentCourseById(Page<UserProjection> users, Long id, String backButton);
}

package uz.pdp.online_education.telegram.service.student.template;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface StudentMessageService {
    void handleMessage(Message message);

    // sendWelcomeMessage metodi endi public bo'lishi kerak,
    // chunki uni Callback servisi ham chaqiradi.
    void sendWelcomeMessage(Long chatId, String username);

    void editMyCoursesPage(Long chatId, Integer messageId, int pageNumber);

    void sendCourseModulesPage(Long chatId, Integer messageId, Long courseId);

    void sendLessonListPage(Long chatId, Integer messageId, Long moduleId);

//    void sendLessonContent(Long chatId, Integer messageId, Long lessonId);

    void sendLessonMainMenu(Long chatId, Integer messageId, Long lessonId);

    void sendContent(Long chatId, Long contentId);
}

package uz.pdp.online_education.telegram.service.instructor.template;

import org.telegram.telegrambots.meta.api.objects.Message;
import uz.pdp.online_education.model.User;

public interface InstructorProcessMessageService {
    void handleMessage(Message message);

    /**
     * Displays the student's personal dashboard.
     * This typically shows statistics like active courses, progress, and other profile information.
     * This method is designed to edit an existing message to show the dashboard.
     *
     * @param user      The authenticated User object.
     * @param chatId    The chat ID where the dashboard should be shown.
     * @param messageId The ID of the message to be edited.
     */
    void showDashboard(User user, Long chatId, Integer messageId);

    void instructorMyCourseHandle(Long chatId, User user, Integer messageId);
}

package uz.pdp.online_education.telegram.service.student.template;

import org.telegram.telegrambots.meta.api.objects.Message;
import uz.pdp.online_education.model.User;

/**
 * An interface for handling interactions with users in the "Student" role.
 * It defines the contract for processing incoming messages and displaying various menus and information.
 */
public interface StudentProcessMessageService {
    /**
     * The main entry point for processing text messages from a student.
     * It routes the message to the appropriate handler based on its content.
     *
     * @param message The incoming message object from the Telegram API.
     */
    void handleMessage(Message message);

    /**
     * Displays the main menu to the student. This menu contains the primary
     * navigation options like "My Courses", "Dashboard", etc.
     *
     * @param user   The authenticated User object representing the student.
     * @param chatId The chat ID to send the menu to.
     */
    void showMainMenu(User user, Long chatId);

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
}

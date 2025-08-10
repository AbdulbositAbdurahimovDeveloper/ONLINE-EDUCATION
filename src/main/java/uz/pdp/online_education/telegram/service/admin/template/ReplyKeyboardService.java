package uz.pdp.online_education.telegram.service.admin.template;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

/**
 * Defines a contract for services that create Reply Keyboards for different user roles.
 * This interface abstracts the keyboard creation logic, allowing other parts of the application
 * to depend on this contract rather than a specific implementation. This promotes loose coupling
 * and makes the system more modular and testable.
 */
public interface ReplyKeyboardService {

    /**
     * Creates and returns the main menu keyboard for an Administrator.
     *
     * @return A {@link ReplyKeyboardMarkup} configured for the admin panel.
     */
    ReplyKeyboardMarkup adminMainMenu();

    /**
     * Creates and returns the main menu keyboard for an Instructor.
     *
     * @return A {@link ReplyKeyboardMarkup} configured for the instructor panel.
     */
    ReplyKeyboardMarkup instructorMainMenu();

    /**
     * Creates and returns the main menu keyboard for a Student.
     *
     * @return A {@link ReplyKeyboardMarkup} configured for the student panel.
     */
    ReplyKeyboardMarkup studentMainMenu();
}
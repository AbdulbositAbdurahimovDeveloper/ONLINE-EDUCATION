package uz.pdp.online_education.telegram.service;

/**
 * A service dedicated to constructing URLs for external web resources.
 * This centralizes routing logic and makes the application more maintainable.
 */
public interface UrlBuilderService {

    /**
     * Generates a full URL for the module checkout page.
     * @param moduleId The ID of the module.
     * @return A complete, ready-to-use URL string.
     */
    String generateModuleCheckoutUrl(Long moduleId);

    /**
     * Generates a full URL for the quiz page.
     * @param quizId The ID of the quiz.
     * @return A complete, ready-to-use URL string.
     */
    String generateQuizUrl(Long quizId);
}
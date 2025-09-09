package uz.pdp.online_education.telegram.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UrlBuilderServiceImpl implements UrlBuilderService {

    @Value("${telegram.bot.webhook-path}")
    private final String baseUrl;


    private static final String MODULE_CHECKOUT_PATH = "/checkout/module/";
    private static final String QUIZ_PATH = "/quiz/";


    public UrlBuilderServiceImpl(@Value("${telegram.bot.webhook-path}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public String generateModuleCheckoutUrl(Long moduleId) {
        return baseUrl + MODULE_CHECKOUT_PATH + moduleId;
    }

    @Override
    public String generateQuizUrl(Long quizId) {
        return baseUrl + QUIZ_PATH + quizId;
    }

    /**
     * @param accessToken
     */
    @Override
    public String generateQuizCreationUrl(String accessToken) {

        // todo url generate
        return baseUrl + MODULE_CHECKOUT_PATH + accessToken;
    }
}
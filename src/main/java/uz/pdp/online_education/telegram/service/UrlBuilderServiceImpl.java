package uz.pdp.online_education.telegram.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UrlBuilderServiceImpl implements UrlBuilderService {

    @Value("${telegram.bot.webhook-path}")
    private final String baseUrl;

    // URL yo'llarini shu yerda, private konstantalarda saqlaymiz
    private static final String MODULE_CHECKOUT_PATH = "/checkout/module/";
    private static final String QUIZ_PATH = "/quiz/";

    // Base URL'ni application.properties'dan faqat shu servis oladi
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
}
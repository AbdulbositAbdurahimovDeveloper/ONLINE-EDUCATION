package uz.pdp.online_education.telegram.service.message;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:config/bot-messages.yml", factory = YamlPropertySourceFactory.class)
public class BotMessagesConfig {
    // Bu klassning ichi bo'sh bo'lishi kifoya.
    // Uning asosiy vazifasi - @PropertySource annotatsiyasi orqali
    // 'bot-messages.yml' faylini Spring kontekstiga yuklash.
}
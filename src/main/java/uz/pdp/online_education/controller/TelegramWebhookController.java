package uz.pdp.online_education.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/admin/telegram")
@RequiredArgsConstructor
public class TelegramWebhookController {

    @Value("${telegram.bot.token}")
    private String botToken; // application.yml dan olinadi

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/set-webhook")
    public ResponseEntity<String> setWebhook(@RequestParam String domain) {
        String webhookUrl = domain.replaceAll("/+$", "") + "/telegram-bot";
        String url = "https://api.telegram.org/bot" + botToken + "/setWebhook?url=" + webhookUrl;

        String response = restTemplate.getForObject(url, String.class);
        return ResponseEntity.ok(response);
    }
}

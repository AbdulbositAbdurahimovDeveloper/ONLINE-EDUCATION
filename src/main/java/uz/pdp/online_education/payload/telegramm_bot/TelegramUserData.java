package uz.pdp.online_education.payload.telegramm_bot;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TelegramUserData {
    @JsonProperty("id")
    private Long id; // Bu bizning CHAT_ID

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("username")
    private String username;
}
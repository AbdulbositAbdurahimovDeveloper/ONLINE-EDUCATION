package uz.pdp.online_education.payload.telegramm_bot;

import lombok.Data;

@Data
public class RegisterDTO {
    private String name;
    private String phone;
    private String username;
    private String password;
    private String initData;
}
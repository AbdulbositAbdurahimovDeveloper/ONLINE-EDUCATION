package uz.pdp.online_education.payload.telegramm_bot;

import lombok.Data;

@Data // Getter, Setter, toString kabilarni avtomatik yaratadi
public class WebAppUserDTO {
    // Formadan keladigan ma'lumotlar
    private String name;
    private String phone;

    // Xavfsizlik va user ma'lumotlari uchun
    private String initData;
}
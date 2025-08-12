package uz.pdp.online_education.payload; // Yoki sizdagi DTO'lar uchun paket nomi

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bu DTO mijoz (frontend) tomonidan fayl MinIO'ga muvaffaqiyatli
 * yuklangandan so'ng, backend'ga yuboriladigan ma'lumotlarni saqlaydi.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmUploadDTO {

    /**
     * Faylning MinIO'dagi unikal kaliti (nomi).
     * Bu qiymat backend tomonidan `/generate-upload-url` endpoint'ida
     * generatsiya qilinib, mijozga yuborilgan edi.
     */
    private String minioKey;

    /**
     * Foydalanuvchi yuklagan faylning asl nomi.
     * Masalan, "tesla-rasmi.png".
     */
    private String originalName;

    /**
     * Faylning MIME turi.
     * Masalan, "image/jpeg", "video/mp4", "application/pdf".
     */
    private String contentType;

    /**
     * Faylning hajmi (baytlarda).
     */
    private long size;
}
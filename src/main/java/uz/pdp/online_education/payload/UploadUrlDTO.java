package uz.pdp.online_education.payload; // Yoki sizdagi DTO'lar uchun paket nomi

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bu DTO backend tomonidan generatsiya qilingan, faylni to'g'ridan-to'g'ri
 * MinIO'ga yuklash uchun mo'ljallangan ma'lumotlarni saqlaydi.
 * Mijoz (frontend) bu ma'lumotlardan foydalanib, faylni yuklaydi.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadUrlDTO {

    /**
     * Faylni yuklash uchun mo'ljallangan maxsus, vaqtinchalik URL.
     * Mijoz aynan shu URL'ga faylni PUT so'rovi orqali yuboradi.
     * Bu URL qisqa vaqt (masalan, 10 daqiqa) amal qiladi.
     */
    private String uploadUrl;

    /**
     * Faylning MinIO'dagi unikal kaliti (nomi).
     * Mijoz faylni yuklab bo'lgandan so'ng, tasdiqlash uchun
     * aynan shu kalitni backend'ga qaytarib yuboradi.
     */
    private String minioKey;
}
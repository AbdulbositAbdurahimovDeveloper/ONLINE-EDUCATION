package uz.pdp.online_education.payload.lesson;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modul sahifasida darslar ro'yxatini ko'rsatish uchun ishlatiladigan DTO.
 * Faqat eng kerakli ma'lumotlarni o'z ichiga oladi.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonSummaryDTO {

    /**
     * Darsning unikal ID'si.
     */
    private Long id;

    /**
     * Dars sarlavhasi.
     */
    private String title;

    /**
     * Darsning modul ichidagi tartib raqami.
     */
    private Integer orderIndex;

    /**
     * Dars tekin yoki pullik ekanligini bildiradi.
     * Frontend bu ma'lumot asosida dars yonida "Bepul" yorlig'ini
     * yoki "qulf" belgisini ko'rsatishi mumkin.
     */
    private boolean isFree;

    /**
     * Darsning umumiy davomiyligi (agar hisoblash mumkin bo'lsa).
     * Bu ma'lumot barcha 'VIDEO' turidagi kontent bloklarining
     * davomiyliklari yig'indisidan olinishi mumkin.
     */
    private Long totalDurationInSeconds;

    /**
     * Foydalanuvchi bu darsni tugatgan yoki tugatmaganligi.
     * Bu ma'lumot joriy foydalanuvchi uchun alohida so'rov bilan
     * LessonProgress jadvalidan olinadi.
     */
    private boolean completedByUser;
}
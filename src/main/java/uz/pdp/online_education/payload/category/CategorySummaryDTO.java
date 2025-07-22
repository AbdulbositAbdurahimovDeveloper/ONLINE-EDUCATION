package uz.pdp.online_education.payload.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Boshqa DTO'lar ichida kategoriya haqida qisqacha ma'lumot berish uchun ishlatiladi.
 * Masalan, biror kursning qaysi kategoriyaga tegishli ekanligini ko'rsatish uchun.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySummaryDTO {

    /**
     * Kategoriyaning unikal ID'si.
     */
    private Long id;

    /**
     * Kategoriyaning nomi.
     */
    private String name;

    /**
     * Kategoriyaning URL'da ishlatiladigan qismi (slug).
     * Bu frontend'ga kategoriya sahifasiga havola yaratish imkonini beradi.
     */
    private String slug;

    /**
     * Kategoriya ikonkasining URL manzili (ixtiyoriy, lekin foydali).
     */
    private String icon;

}
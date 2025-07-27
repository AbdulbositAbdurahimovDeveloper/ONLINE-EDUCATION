package uz.pdp.online_education.payload.comment;

import lombok.Data;

@Data
public class CommentCreateDto {
    private String text; // Komment matni

    // Ierarxiya uchun: agar bu javob bo'lsa, ota-komment id si (ixtiyoriy)
    private Long parentId;

    // Polimorfik bog'lanish uchun
    // "course" yoki "lesson" qiymatlarini qabul qiladi
    private String commentableType;

    // Kurs yoki darsning id si
    private Long commentableId;
}
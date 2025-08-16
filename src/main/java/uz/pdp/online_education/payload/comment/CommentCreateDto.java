package uz.pdp.online_education.payload.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data // Getter, Setter, toString, equals, hashCode ni avtomatik yaratadi
@AllArgsConstructor
@NoArgsConstructor
public class CommentCreateDto {

    @NotBlank(message = "Komment matni bo'sh bo'lmasligi kerak")
    private String text;

    @NotNull(message = "Foydalanuvchi IDsi majburiy")
    private Long userId;

    private Long courseId; // Agar komment kursga tegishli bo'lsa
    private Long lessonId; // Agar komment darsga tegishli bo'lsa
    private Long parentId; // Agar bu komment javob bo'lsa (qaysi kommentga javob ekanligi)
}

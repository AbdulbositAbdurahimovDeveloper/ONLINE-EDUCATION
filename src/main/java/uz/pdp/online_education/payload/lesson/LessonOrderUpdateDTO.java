package uz.pdp.online_education.payload.lesson;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link uz.pdp.online_education.model.lesson.Lesson}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonOrderUpdateDTO implements Serializable {


    @NotEmpty(message = "IDs list cannot be empty") // Ro'yxat bo'sh bo'lmasligi kerakligini tekshiramiz
    List<Long> orderedIds;
}
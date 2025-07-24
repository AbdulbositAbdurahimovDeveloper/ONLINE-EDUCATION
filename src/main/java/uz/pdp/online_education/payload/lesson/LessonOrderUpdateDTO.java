package uz.pdp.online_education.payload.lesson;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link uz.pdp.online_education.model.lesson.Lesson}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonOrderUpdateDTO implements Serializable {


    private Long id;

    @NotNull
    private Integer orderIndex;
}
package uz.pdp.online_education.payload.lesson;

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
public class LessonUpdateDTO implements Serializable {

    private String title;

    private String content;

    private Integer orderIndex;

    private boolean isFree = false;

}
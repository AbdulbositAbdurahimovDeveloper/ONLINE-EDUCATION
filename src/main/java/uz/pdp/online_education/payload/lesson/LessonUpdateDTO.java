package uz.pdp.online_education.payload.lesson;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotBlank
    @Size(min = 5, max = 150)
    private String title;

    @NotBlank
    @Size(min = 5, max = 1000)
    @JsonProperty("description")
    private String content;


    @NotNull
    private boolean isFree = false;

}
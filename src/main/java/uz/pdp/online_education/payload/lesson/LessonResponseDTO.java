package uz.pdp.online_education.payload.lesson;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;
import uz.pdp.online_education.payload.content.ContentDTO;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link uz.pdp.online_education.model.lesson.Lesson}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonResponseDTO implements Serializable {

    private Long id;

    private String title;

    @JsonProperty("description")
    private String content;

    private Integer orderIndex;

    private boolean isFree = false;

    private Long moduleId;

    private List<ContentDTO> contents;
}
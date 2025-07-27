package uz.pdp.online_education.payload.text;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link uz.pdp.online_education.model.lesson.TextContent}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TextContentResponseDTO implements Serializable {

    private Long id;

    private Integer blockOrder;

    private Long lessonId;

    private String text;
}
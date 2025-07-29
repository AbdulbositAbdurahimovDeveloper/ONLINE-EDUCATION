package uz.pdp.online_education.payload.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
public class CommentCreateDTO {
    @NotBlank
    private String text;

    private Long courseId;
    private Long lessonId;
    private Long parentId;
}

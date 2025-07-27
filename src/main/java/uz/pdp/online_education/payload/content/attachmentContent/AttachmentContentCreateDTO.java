package uz.pdp.online_education.payload.content.attachmentContent;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link uz.pdp.online_education.model.lesson.AttachmentContent}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentContentCreateDTO implements Serializable {
    @NotNull
    private Long lessonId;
    @NotNull
    private Long attachmentId;
}
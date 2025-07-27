package uz.pdp.online_education.payload.lesson;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * DTO for {@link uz.pdp.online_education.model.lesson.AttachmentContent}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentContentDTO implements Serializable {
    private Long id;
    private Integer blockOrder;
    private Long lessonId;
    private Long attachmentId;
    private String contentUrl;
    private Long updatedAt;
    private Long createdAt;
}
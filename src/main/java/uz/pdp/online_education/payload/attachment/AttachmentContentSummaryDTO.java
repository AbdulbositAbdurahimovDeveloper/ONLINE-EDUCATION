package uz.pdp.online_education.payload.attachment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.online_education.payload.content.ContentDTO;

import java.io.Serializable;

/**
 * DTO for {@link uz.pdp.online_education.model.lesson.AttachmentContent}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentContentSummaryDTO implements Serializable, ContentDTO {
    private Long id;
    private int blockOrder;
    private String contentType;
    private Long attachmentId;
    private String attachmentOriginalName;
}
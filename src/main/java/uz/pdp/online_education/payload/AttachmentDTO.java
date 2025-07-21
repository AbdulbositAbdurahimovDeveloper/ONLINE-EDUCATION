package uz.pdp.online_education.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * DTO for {@link uz.pdp.online_education.model.Attachment}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentDTO implements Serializable {
    private Long id;
    private String originalName;
    private String contentType;
    private Long fileSize;
    private String path;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
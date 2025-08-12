package uz.pdp.online_education.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import uz.pdp.online_education.model.Abs.AbsLongEntity;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity(name = "attachment")
@FieldNameConstants
public class Attachment extends AbsLongEntity {

    @Column(columnDefinition = "text")
    private String originalName;

    private String contentType;

    private Long fileSize;

    @Column(name = "minio_key", columnDefinition = "text")
    private String minioKey;

    @Column(name = "bucket_name", nullable = false)
    private String bucketName;

    private String telegramFileId;

}

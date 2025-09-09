package uz.pdp.online_education.payload.content;

import lombok.Data;

@Data
public class AttachmentRequestDTO {
    private Long lessonId;       // qaysi darsga bog‘liq
    private String originalName; // original fayl nomi
    private String contentType;  // video/mp4
    private Long fileSize;       // baytda
    private String minioKey;     // presigned URL orqali yuklaganda berilgan key
    private String bucketName;   // qaysi bucket’ga yuklandi
}

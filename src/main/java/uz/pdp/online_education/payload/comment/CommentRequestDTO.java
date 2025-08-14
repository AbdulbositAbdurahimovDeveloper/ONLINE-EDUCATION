package uz.pdp.online_education.payload.comment;

import lombok.Data;

@Data
public class CommentRequestDTO {
    private String text;
    private Long courseId;
    private Long lessonId;
    private Long parentId;
}
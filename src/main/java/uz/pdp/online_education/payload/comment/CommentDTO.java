package uz.pdp.online_education.payload.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentDTO extends BaseDTO {

    private String text;

    private Long userId;

    private String userFullName;

    private Long lessonId;

    private String lessonTitle;

    private Long courseId;

    private String courseTitle;
}

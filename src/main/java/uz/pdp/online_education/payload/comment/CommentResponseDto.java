package uz.pdp.online_education.payload.comment;

import lombok.Data;
import java.util.List;

@Data
public class CommentResponseDto {
    private Long id;
    private String text;
    private String authorUsername; // Komment yozgan odamning username'i
    private List<CommentResponseDto> replies; // Bu kommentga yozilgan javoblar
}
package uz.pdp.online_education.payload.course;

import lombok.*;

/**
 * Created by: suhrob
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseResponseDto {
    private Long id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private Double averageRating;
}

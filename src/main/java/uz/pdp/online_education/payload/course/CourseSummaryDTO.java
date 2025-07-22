package uz.pdp.online_education.payload.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSummaryDTO {
    private Long id;
    private String title;
    private String slug;
    private String thumbnailUrl; // <<< URL, ID emas
    private String categoryName;
    private String instructorName;
}
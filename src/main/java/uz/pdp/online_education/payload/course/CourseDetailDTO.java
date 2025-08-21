package uz.pdp.online_education.payload.course;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import uz.pdp.online_education.payload.review.ReviewSummaryDTO;

//@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseDetailDTO {
    private Long id;
    private String title;
    private String description;
    private String slug;
    private String thumbnailUrl;
    private Long instructorId;
    private Long categoryId;
    private Long modulesCount;
    private boolean success;
    private ReviewSummaryDTO reviewSummary;
    private Long createdAt;
    private Long updatedAt;
}
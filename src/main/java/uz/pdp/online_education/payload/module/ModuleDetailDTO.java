package uz.pdp.online_education.payload.module;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import uz.pdp.online_education.payload.course.CourseSummaryDTO;
import uz.pdp.online_education.payload.lesson.LessonSummaryDTO;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModuleDetailDTO {
    private Long id;
    private String title;
    private String description;
    private Long price;
    private Integer orderIndex;
    private Long courseId;
    private Integer lessonCount;
    private Integer moduleEnrollmentsCount;
    private Long createdAt;
    private Long updatedAt;
}
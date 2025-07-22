package uz.pdp.online_education.payload.course;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import uz.pdp.online_education.payload.category.CategorySummaryDTO;
import uz.pdp.online_education.payload.user.UserSummaryDTO;

import java.time.Instant;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseDetailDTO extends RepresentationModel<CourseDetailDTO> {
    private Long id;
    private String title;
    private String slug;
    private String description;
    private Long thumbnailUrl;

    private CategorySummaryDTO category; // Batafsilroq ma'lumot uchun ichki DTO
    private UserSummaryDTO instructor; // Batafsilroq ma'lumot uchun ichki DTO

    private List<ModuleSummaryDTO> modules; // Modullarning qisqa ro'yxati

    private Long createdAt;
    private Long updatedAt;
}
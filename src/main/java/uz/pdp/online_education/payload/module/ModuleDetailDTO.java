package uz.pdp.online_education.payload.module;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import uz.pdp.online_education.payload.course.CourseSummaryDTO;
import uz.pdp.online_education.payload.lesson.LessonSummaryDTO;

import java.time.Instant;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModuleDetailDTO extends RepresentationModel<ModuleDetailDTO> {
    private Long id;
    private String title;
    private String description;
    private Long price;
    private Integer orderIndex;
    
    // Modul qaysi kursga tegishli ekanligi haqida qisqa ma'lumot
    private CourseSummaryDTO course;
    
    // Bu modulga tegishli barcha darslar ro'yxati
    private List<LessonSummaryDTO> lessons; // Lesson uchun ham SummaryDTO kerak bo'ladi

    private Long createdAt;
    private Long updatedAt;
}
package uz.pdp.online_education.mapper;

import org.springframework.stereotype.Component;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.payload.CourseRequestDTO;
import uz.pdp.online_education.payload.CourseResponseDTO;

@Component
public class CourseMapper {

    public CourseResponseDTO toDto(Course course) {
        CourseResponseDTO dto = new CourseResponseDTO();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setSlug(course.getSlug());
        dto.setInstructorFullName(course.getInstructor().getUsername());
        dto.setCategoryName(course.getCategory().getName());
        return dto;
    }

    public void updateCourseFromDto(CourseRequestDTO dto, Course course) {
        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setSlug(dto.getSlug());
        // Thumbnail, Instructor, Category tashqaridan set qilinadi
    }
}

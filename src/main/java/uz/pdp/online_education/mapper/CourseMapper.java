package uz.pdp.online_education.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.payload.CourseRequestDTO;
import uz.pdp.online_education.payload.CourseResponseDTO;


@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface CourseMapper {
    CourseResponseDTO toDto(Course course);
    void updateCourseFromDto(CourseRequestDTO dto, Course course);
}

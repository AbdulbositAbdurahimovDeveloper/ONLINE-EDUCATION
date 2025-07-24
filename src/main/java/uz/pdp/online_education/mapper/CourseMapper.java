package uz.pdp.online_education.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.payload.course.CourseDetailDTO;

import java.sql.Timestamp;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    @Mapping(target = "thumbnailUrl", source = "thumbnailUrl.id")
    @Mapping(target = "category")
    @Mapping(target = "instructor")
    @Mapping(target = "modules")
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "timestampToLong")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "timestampToLong")
    CourseDetailDTO courseToCourseDetailDTO(Course course);

    @Named("timestampToLong")
    default Long timestampToLong(Timestamp timestamp)    {
        return timestamp != null ? timestamp.getTime() : null;
    }


}

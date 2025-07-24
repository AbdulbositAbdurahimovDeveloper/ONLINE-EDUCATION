package uz.pdp.online_education.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.payload.course.CourseDetailDTO;

import java.sql.Timestamp;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    @Mapping(target = "thumbnailUrl", source = "thumbnailUrl.id")
    @Mapping(target = "instructorId", source = "instructor.id")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "modulesCount", source = "modules", qualifiedByName = "modulesToCount")
//    @Mapping(target = "modulesCount",ignore = true)
    @Mapping(target = "reviewSummary",ignore = true)
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "timestampToLong")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "timestampToLong")
    CourseDetailDTO courseToCourseDetailDTO(Course course);

    @Named("timestampToLong")
    default Long timestampToLong(Timestamp timestamp) {
        return timestamp != null ? timestamp.getTime() : null;
    }

    @Named("modulesToCount")
    default Long modulesToCount(List<?> list) { // <?> - har qanday List uchun ishlashi uchun
        return list == null ? 0L : (long) list.size();
    }


}

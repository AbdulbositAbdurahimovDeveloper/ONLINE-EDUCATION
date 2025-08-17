package uz.pdp.online_education.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.Review;
import uz.pdp.online_education.payload.course.CourseDetailDTO;
import uz.pdp.online_education.payload.course.CourseInfoDTO;
import uz.pdp.online_education.payload.review.ReviewSummaryDTO;

import java.sql.Timestamp;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    @Mapping(target = "thumbnailUrl", source = "thumbnailUrl.id")
    @Mapping(target = "instructorId", source = "instructor.id")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "modulesCount", source = "modules", qualifiedByName = "modulesToCount")
//    @Mapping(target = "modulesCount",ignore = true)
    @Mapping(target = "reviewSummary", source = "reviews", qualifiedByName = "mapToReviewSummary")
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "timestampToLong")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "timestampToLong")
    CourseDetailDTO courseToCourseDetailDTO(Course course);

    @Named("timestampToLong")
    default Long timestampToLong(Timestamp timestamp)    {
        return timestamp != null ? timestamp.getTime() : null;
    }

    @Named("modulesToCount")
    default Long modulesToCount(List<?> list) { // <?> - har qanday List uchun ishlashi uchun
        return list == null ? 0L : (long) list.size();
    }

    @Named("mapToReviewSummary")
    default ReviewSummaryDTO mapToReviewSummary(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) return new ReviewSummaryDTO(0, 0.0);

        int count = reviews.size();
        double averageRating = reviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);

        return new ReviewSummaryDTO(count, averageRating);
    }

    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "instructor.profile.firstName", target = "instructorName") // Bu yerda ism-familiyani birlashtirish ham mumkin
    @Mapping(target = "moduleCount", expression = "java(course.getModules() != null ? course.getModules().size() : 0)")
    CourseInfoDTO toInfoDto(Course course);

}

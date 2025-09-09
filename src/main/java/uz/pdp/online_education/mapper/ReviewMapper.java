package uz.pdp.online_education.mapper;

import org.mapstruct.*;
import uz.pdp.online_education.model.*;
import uz.pdp.online_education.payload.review.ReviewCreateDTO;
import uz.pdp.online_education.payload.review.ReviewDTO;
import uz.pdp.online_education.payload.review.ReviewUpdateDTO;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {


    @Mapping(source = "course.id", target = "courseId")
    @Mapping(source = "user.id", target = "userId")
    ReviewDTO toDto(Review review);


     @Mapping(target = "id", ignore = true)
     @Mapping(target = "createdAt", ignore = true)
     @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "course", source = "course")
    @Mapping(target = "user", source = "user")
    Review toEntity(ReviewCreateDTO dto, Course course, User user);


    @Mapping(target = "rating", source = "dto.rating")
    @Mapping(target = "comment", source = "dto.comment")
    void updateReview(@MappingTarget Review review, ReviewUpdateDTO dto);
}

package uz.pdp.online_education.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.Review;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.ReviewCreateDTO;
import uz.pdp.online_education.payload.ReviewDTO;
import uz.pdp.online_education.payload.ReviewUpdateDTO;


@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface ReviewMapper {
//    ReviewDTO toDto(Review review);
//    Review toEntity(ReviewCreateDTO dto, Course course, User user);
//    void updateReview(Review review, ReviewUpdateDTO dto);
}

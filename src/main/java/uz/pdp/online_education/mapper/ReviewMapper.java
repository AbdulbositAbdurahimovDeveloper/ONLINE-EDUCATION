package uz.pdp.online_education.mapper;

import uz.pdp.online_education.payload.*;
import uz.pdp.online_education.model.*;

public class ReviewMapper {

    public static ReviewDTO toDto(Review review) {

        return ReviewDTO.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .courseId(review.getCourse().getId())
                .userId(review.getUser().getId())
                .build();
    }

    public static Review toEntity(ReviewCreateDTO dto, Course course, User user) {

        Review review = new Review();
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setCourse(course);
        review.setUser(user);


        return review;
    }

    public static void updateReview(Review review, ReviewUpdateDTO dto) {
        review.setRating(dto.getRating());

        review.setComment(dto.getComment());

    }

}

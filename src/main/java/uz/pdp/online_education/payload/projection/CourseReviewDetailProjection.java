package uz.pdp.online_education.payload.projection;

import java.sql.Timestamp;

public interface CourseReviewDetailProjection {

    Timestamp getJoinedAt();

    String getStudentName();

    String getBio();

    Timestamp getPurchasedAt();

    Timestamp getReviewDate();

    Integer getRating();

    Integer getTotalCourses();

    Double getAverageRating();

    String getComment();
}
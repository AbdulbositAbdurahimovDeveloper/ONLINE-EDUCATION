package uz.pdp.online_education.payload.projection;

public interface CourseReviewStatsProjection {
    Long getCourseId();
    String getCourseTitle();
    Double getAverageRating();
    Long getTotalReviews();
    Long getTotalComments();
    Long getActiveStudents(); // ixtiyoriy
}

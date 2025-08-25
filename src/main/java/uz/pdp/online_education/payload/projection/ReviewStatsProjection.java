// ReviewStatsProjection.java
package uz.pdp.online_education.payload.projection;

public interface ReviewStatsProjection {
    Double getAverageRating();
    Long getTotalReviews();     // Baholar soni
    Long getTotalComments();    // Umumiy sharhlar soni
    Long getActiveStudents();   // Unikal sharhlovchilar soni
}
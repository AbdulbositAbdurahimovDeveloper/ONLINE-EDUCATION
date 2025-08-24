// CourseStudentStatsProjection.java
package uz.pdp.online_education.payload.course;

/**
 * Nativ SQL so'rovining natijasini saqlash uchun Projection interfeysi.
 * Metod nomlari SQL'dagi ustun nomlariga (AS ... dan keyingi) mos kelishi kerak.
 */
public interface CourseStudentStatsProjection {
    Long getCourse_id();
    String getCourse_title();
    Integer getUnique_student_count();
    Integer getTotal_sales_count();
}
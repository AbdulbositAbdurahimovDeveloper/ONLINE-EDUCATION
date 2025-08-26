package uz.pdp.online_education.payload.projection;

import java.sql.Timestamp;

public interface CourseReviewProjection {
    Long getStudentId();       // Sharh qoldirgan foydalanuvchining ID'si
    Long getCourseId();        // Sharh tegishli bo'lgan kursning ID'si

    String getStudentName();   // Foydalanuvchi ismi
    Integer getRating();       // Baho (1–5)
    String getComment();       // Sharh matni
    Timestamp getCreatedAt();  // Sharh qoldirilgan vaqt
}
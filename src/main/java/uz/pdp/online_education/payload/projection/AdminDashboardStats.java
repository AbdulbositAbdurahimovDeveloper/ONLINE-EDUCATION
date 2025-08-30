package uz.pdp.online_education.payload.projection;

import java.math.BigDecimal;

public interface AdminDashboardStats {
    Long getTotalUsers();
    Long getTotalStudents();
    Long getTotalInstructors();
    Long getTotalCourses();
    Long getTotalModules();
    Long getTotalLessons();
    Long getTotalEnrollments();
    Double getAverageQuizPercentage();
    BigDecimal getRevenueThisMonth();
    Long getNewUsersToday();
    BigDecimal getRevenueToday();
    BigDecimal getTotalRevenue();
}

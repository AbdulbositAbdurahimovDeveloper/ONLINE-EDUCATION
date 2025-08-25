package uz.pdp.online_education.payload.projection;

public interface MentorIncomeProjection {
    Long getTodayIncome();
    Long getTodaySales();
    Long getMonthlyIncome();
    Long getTotalIncome();
    Long getTotalStudents();
    String getTopCourseName();
    Long getTopCourseSales();
    Double getAverageRating();
}
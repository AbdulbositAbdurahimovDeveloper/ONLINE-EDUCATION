package uz.pdp.online_education.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor // Bu JPQL konstruktor ifodasi uchun juda muhim!
@NoArgsConstructor
public class AdminDashboardDTO {
    // UMUMIY STATISTIKA
    private Long totalUsers;
    private Long totalInstructors;
    private Long totalCourses;
    private BigDecimal revenueThisMonth;

    // BUGUNGI FAOLLIK
    private Long newUsersToday;
    private Long salesToday;
    private Long newSupportTickets;
}
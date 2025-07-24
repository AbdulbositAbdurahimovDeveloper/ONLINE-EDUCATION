package uz.pdp.online_education.payload.review;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryDTO {
    private int count; // Sharhlar soni
    private double averageRating; // O'rtacha reyting
}
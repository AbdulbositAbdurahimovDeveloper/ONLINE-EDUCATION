package uz.pdp.online_education.payload.review;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewDTO {

    private Long id;

    private int rating;

    private String comment;

    private Long courseId;

    private Long userId;
}
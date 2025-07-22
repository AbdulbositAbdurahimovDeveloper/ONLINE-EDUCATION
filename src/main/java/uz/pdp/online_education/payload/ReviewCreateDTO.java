// ReviewCreateDto.java
package uz.pdp.online_education.payload;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewCreateDTO {

    private int rating;

    private String comment;

    private Long courseId;

    private Long userId;

}

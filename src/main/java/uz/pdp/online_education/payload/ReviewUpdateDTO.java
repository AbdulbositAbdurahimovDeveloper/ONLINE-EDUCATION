// ReviewUpdateDto.java
package uz.pdp.online_education.payload;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewUpdateDTO {

    private int rating;

    private String comment;

}

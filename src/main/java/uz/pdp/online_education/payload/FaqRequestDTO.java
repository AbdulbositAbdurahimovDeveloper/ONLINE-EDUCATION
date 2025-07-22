package uz.pdp.online_education.payload;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaqRequestDTO {
    private String question;
    private String answer;
    private int displayOrder;
}

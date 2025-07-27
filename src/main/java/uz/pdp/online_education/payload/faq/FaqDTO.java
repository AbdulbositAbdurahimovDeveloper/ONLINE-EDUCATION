package uz.pdp.online_education.payload.faq;

import lombok.Data;

@Data
public class FaqDTO {
    private Long id;
    private String question;
    private String answer;
    private int displayOrder;
}

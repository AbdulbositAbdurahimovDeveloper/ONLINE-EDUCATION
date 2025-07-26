package uz.pdp.online_education.payload.faq;

import lombok.Data;

@Data
public class FaqRequestDTO {
    private String question;
    private String answer;
}

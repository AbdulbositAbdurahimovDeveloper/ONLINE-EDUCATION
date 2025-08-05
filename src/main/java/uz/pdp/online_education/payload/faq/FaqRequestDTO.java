package uz.pdp.online_education.payload.faq;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FaqRequestDTO {
    @NotBlank
    private String question;
    @NotBlank
    private String answer;
}

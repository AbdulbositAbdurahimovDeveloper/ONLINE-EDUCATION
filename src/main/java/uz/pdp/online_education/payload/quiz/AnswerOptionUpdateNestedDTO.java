package uz.pdp.online_education.payload.quiz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AnswerOptionUpdateNestedDTO implements Serializable, AnswerOptionData {

    private Long id;

    @NotBlank
    @Size(max = 1000)
    private String text;

    @NotNull
    private Boolean isCorrect;

}

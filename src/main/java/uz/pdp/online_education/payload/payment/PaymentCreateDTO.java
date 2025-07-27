package uz.pdp.online_education.payload.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCreateDTO implements Serializable {

    @NotNull
    private Long moduleId;

    @PositiveOrZero
    private Double amount;

    @Size(min = 16, max = 16)
    private String maskedCardNumber;

    private String description;

}

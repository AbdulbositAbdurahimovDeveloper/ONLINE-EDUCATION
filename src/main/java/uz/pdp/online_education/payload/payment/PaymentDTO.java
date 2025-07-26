package uz.pdp.online_education.payload.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.online_education.enums.TransactionStatus;

import java.io.Serializable;

/**
 * DTO for {@link uz.pdp.online_education.model.Payment}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO implements Serializable {
    private Long id;
    private Long userId;
    private Long moduleId;
    private Long amount;
    private TransactionStatus status;
    private String maskedCardNumber;
    private String description;
    private Long createdAt;
    private Long updatedAt;
}
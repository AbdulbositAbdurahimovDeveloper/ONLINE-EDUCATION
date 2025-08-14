// Yangi DTO
package uz.pdp.online_education.payload;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserModuleStatusDTO {
    private Long moduleId;
    private boolean isEnrolled; // ModuleEnrollment'da yozuvi bormi?
    private boolean isPurchased; // Payment'da SUCCESS to'lovi bormi?
}
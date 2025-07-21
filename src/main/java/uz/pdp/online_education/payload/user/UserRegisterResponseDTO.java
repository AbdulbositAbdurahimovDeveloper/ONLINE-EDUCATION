package uz.pdp.online_education.payload.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; // <-- Timestamp o'rniga ishlatamiz

/**
 * Data Transfer Object returned after a successful user registration.
 * It confirms the creation of the user account and includes key metadata
 * like ID and creation timestamp, inherited from base entities.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRegisterResponseDTO {

    // --- AbsLongEntity'dan keladigan ma'lumot ---
    private Long id;

    // --- User entity'sidan keladigan ma'lumotlar ---
    private String username;
    private String role; // Tizim tomonidan belgilangan rol

    // --- UserProfile entity'sidan keladigan ma'lumotlar ---
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String bio;
    private String profilePictureUrl;

    // --- AbsDateEntity'dan keladigan ma'lumotlar ---
    private Long createdAt;
    private Long updatedAt;
}
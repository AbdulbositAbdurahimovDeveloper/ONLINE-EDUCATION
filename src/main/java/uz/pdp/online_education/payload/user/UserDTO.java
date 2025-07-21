package uz.pdp.online_education.payload.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.util.Set;

/**
 * A comprehensive Data Transfer Object representing a user's public profile.
 * It combines information from both User and UserProfile entities for display purposes.
 * It intentionally omits sensitive data like passwords.
 * Timestamps are returned as Long (epoch milliseconds).
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO extends RepresentationModel<UserDTO> {

    // --- AbsLongEntity'dan ---
    private Long id;

    // --- User entity'sidan ---
    private String username;
    private String role; // Rollarni Set<String> ko'rinishida saqlash qulay

    // --- UserProfile entity'sidan ---
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String bio;
    private String profilePictureUrl; // Kelajakda Attachment uchun URL qo'shsa bo'ladi

    // --- AbsDateEntity'dan (Long formatida) ---
    private Long createdAt;
    private Long updatedAt;
}
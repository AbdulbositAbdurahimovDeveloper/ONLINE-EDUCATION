package uz.pdp.online_education.payload.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user update requests.
 * Fields are optional. Validation is applied only if a value is provided.
 * Password is not included here for security; it should be updated via a separate endpoint.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequestDTO {

    // @NotBlank olib tashlandi. Faqat yuborilsa, o'lchami tekshiriladi.
    @Size(min = 3, max = 50, message = "Username must be between {min} and {max} characters")
    private String username;

    @Size(min = 2, max = 50, message = "First name must be between {min} and {max} characters")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between {min} and {max} characters")
    private String lastName;

    // @NotBlank olib tashlandi. Faqat yuborilsa, formati tekshiriladi.
    @Email(message = "Please provide a valid email address")
    private String email;

    private String bio;

    private Long profilePictureId;

    private String phoneNumber;
}
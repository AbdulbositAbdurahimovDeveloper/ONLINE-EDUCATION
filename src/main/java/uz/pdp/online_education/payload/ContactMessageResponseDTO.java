package uz.pdp.online_education.payload;

import lombok.Getter;
import lombok.Setter;
import uz.pdp.online_education.enums.MessageStatus;

@Getter
@Setter
public class ContactMessageResponseDTO {
    private Long id;
    private String fullName;
    private String email;
    private String message;
    private MessageStatus status;
}

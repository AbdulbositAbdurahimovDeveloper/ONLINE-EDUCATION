package uz.pdp.online_education.payload.user;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginDTO {

    @Size(min = 4, max = 20)
    private String username;

    @Size(min = 3, max = 20)
    private String password;


}

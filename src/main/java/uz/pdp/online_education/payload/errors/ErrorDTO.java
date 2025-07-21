package uz.pdp.online_education.payload.errors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 Created by: Mehrojbek
 DateTime: 27/05/25 19:53
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ErrorDTO {

    private int status;
    private String message;
    private List<FieldErrorDTO> fieldErrors;

    public ErrorDTO(int status, String message) {
        this.status = status;
        this.message = message;
    }
}

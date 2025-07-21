package uz.pdp.online_education.payload.errors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 Created by: Mehrojbek
 DateTime: 27/05/25 20:53
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FieldErrorDTO {

    private String field;
    private String message;

}

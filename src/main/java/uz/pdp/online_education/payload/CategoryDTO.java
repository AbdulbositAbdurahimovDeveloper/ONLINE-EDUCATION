package uz.pdp.online_education.payload;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by: suhrob
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO implements Serializable {
    private Long id;
    private String name;
    private String icon;
    private String slug;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}

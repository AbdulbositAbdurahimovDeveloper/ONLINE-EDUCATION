package uz.pdp.online_education.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.online_education.enums.Role;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BroadcastDataDTO {
    private String type; // "PHOTO_WITH_TEXT" yoki "TEXT_ONLY"
    private String text;
    private String photoFileId;
    private Set<Role> roles = new HashSet<>();
    private String buttonText; // Tugma matni, masalan "Batafsil..."
    private String buttonUrl;
}

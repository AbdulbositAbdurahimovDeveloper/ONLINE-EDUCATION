package uz.pdp.online_education.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import uz.pdp.online_education.model.Abs.AbsLongEntity;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity(name = "attachment")
@SQLDelete(sql = "UPDATE attachment SET deleted = true WHERE id = ?")
@SQLRestriction(value = "deleted=false")
@FieldNameConstants
public class Attachment extends AbsLongEntity {

    @Column(columnDefinition = "text")
    private String originalName;

    private String contentType;

    private Long fileSize;

    @Column(columnDefinition = "text")
    private String path;

}

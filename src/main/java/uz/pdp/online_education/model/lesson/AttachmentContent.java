package uz.pdp.online_education.model.lesson;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import uz.pdp.online_education.model.Attachment;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@ToString
@Table(name = "attachment_contents")
@SQLDelete(sql = "UPDATE attachment_contents SET deleted = true WHERE id = ?")
@SQLRestriction(value = "deleted=false")
@DiscriminatorValue("ATTACHMENT")
@FieldNameConstants
public class AttachmentContent extends Content {

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "attachment_id")
    private Attachment attachment; // Mavjud Attachment entitysiga bog'lanish
}
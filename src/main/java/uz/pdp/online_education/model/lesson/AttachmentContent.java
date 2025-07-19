package uz.pdp.online_education.model.lesson;

import jakarta.persistence.*;
import uz.pdp.online_education.model.Attachment;

@Entity
@Table(name = "attachment_contents")
@DiscriminatorValue("ATTACHMENT")
public class AttachmentContent extends Content {

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "attachment_id")
    private Attachment attachment; // Mavjud Attachment entitysiga bog'lanish
}
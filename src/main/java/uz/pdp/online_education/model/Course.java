package uz.pdp.online_education.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import uz.pdp.online_education.model.Abs.AbsLongEntity;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "courses")
@FieldNameConstants
@SQLDelete(sql = "UPDATE courses SET deleted = true WHERE id = ?")
@SQLRestriction(value = "deleted=false")
public class Course extends AbsLongEntity {

    private String title;

    private String description;

    private String slug;

    @OneToOne
    private Attachment thumbnailUrl;// course lar royxatida korinatigan rasm 1 ta bolsa yetadi

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private User instructor; // courseni qaysi user yaratgan

    @ManyToOne
    private Category category;//course qaysi categoryda

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Module> modules; // coursening modullari

}
package uz.pdp.online_education.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import uz.pdp.online_education.model.Abs.AbsLongEntity;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity(name = "courses")
@FieldNameConstants
@SQLDelete(sql = "UPDATE courses SET deleted = true WHERE id = ?")
@SQLRestriction(value = "deleted=false and success = true")
public class Course extends AbsLongEntity {

    @Column(nullable = false, unique = true)
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
    @ToString.Exclude
    private List<Module> modules; // coursening modullari

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Review> reviews;

    private boolean deleted = false;

    private boolean success = false;

}
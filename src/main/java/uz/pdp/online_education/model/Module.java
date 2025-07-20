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
import uz.pdp.online_education.model.lesson.Lesson;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "modules")
@SQLDelete(sql = "UPDATE modules SET deleted = true WHERE id = ?")
@SQLRestriction(value = "deleted=false")
@FieldNameConstants
public class Module extends AbsLongEntity {

    @Column(nullable = false, unique = true)
    private String title;

    private String description;

    private Long price;

    private Integer orderIndex; // bu yerda bu coursening nechanchi modulligini aytadi front ketmaketligi uchun

    @ManyToOne
    private Course course;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lesson> lessons;

    @OneToMany(mappedBy = "module",fetch = FetchType.LAZY)
    private List<Payment> payments;

    @OneToMany(mappedBy = "module",fetch =  FetchType.LAZY)
    private List<ModuleEnrollment> enrollments;



}
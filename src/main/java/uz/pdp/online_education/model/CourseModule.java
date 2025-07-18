package uz.pdp.online_education.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.pdp.online_education.model.Abs.AbsLongEntity;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "course_modules")
public class CourseModule extends AbsLongEntity {

    private String title;

    private String description;

    private Long price;

    private Integer orderIndex; // bu yerda bu coursening nechanchi modulligini aytadi front ketmaketligi uchun

    @ManyToOne
    private Course course;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lesson> lessons;

}
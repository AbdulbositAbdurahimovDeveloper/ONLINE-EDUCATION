package uz.pdp.online_education.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import uz.pdp.online_education.model.Abs.AbsLongEntity;

@Getter
@Setter
@Entity
@Table(name = "lesson")
public class Lesson extends AbsLongEntity {

    private String title;

    private String content;

    @OneToOne
    private Attachment videoUrl; // hozircha bita moduleda 1 ta video boladi dedim ?

    private Integer orderIndex; // bu moduldagi nechanchi lessonligini aytib turadi

    @ManyToOne
    @JoinColumn(name = "modules_id")
    private CourseModule module; // bu qaysi modulga tegishliligini aytadi


    /// bu yerga quiz ham qoshamizmi?



}
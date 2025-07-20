package uz.pdp.online_education.model.lesson;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import uz.pdp.online_education.model.Abs.AbsLongEntity;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.User;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "lesson")
@SQLDelete(sql = "UPDATE lesson SET deleted = true WHERE id = ?")
@SQLRestriction(value = "deleted=false")
@FieldNameConstants
public class Lesson extends AbsLongEntity {

    @Column(nullable = false)
    private String title;

    private String content;

    private Integer orderIndex; // bu moduldagi nechanchi lessonligini aytib turadi

    @Column(nullable = false)
    private boolean isFree = false;

    @ManyToOne
    @JoinColumn(name = "modules_id")
    private Module module; // bu qaysi modulga tegishliligini aytadi

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("blockOrder ASC")
    private List<Content> contents;

    @OneToOne
    private User view; // ushbu lesson ni qaysi userlar korgani uchun kerak progress uchun
    /// bu yerga quiz ham qoshamizmi?



}
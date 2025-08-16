package uz.pdp.online_education.model;

import jakarta.persistence.*;
import lombok.*;
// ... importlar
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import uz.pdp.online_education.model.Abs.AbsLongEntity;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.lesson.Lesson;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "comments")
@FieldNameConstants
public class Comment extends AbsLongEntity {

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;
    
    // --- POLIMORFIK BOG'LANISHNI SIMULYATSIYA QILISH ---
    // Har bir komment yo kursga, yo darsga tegishli bo'ladi. Bittasi har doim NULL.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;
    
    // O'z-o'ziga ishora (ierarxiya uchun)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> replies = new ArrayList<>();

    // Yaratishda qaysi turdagi komment ekanligini aniqlash uchun
    @Transient // Bu maydon bazaga saqlanmaydi
    private String commentableType; // "course" yoki "lesson"
    
    @Transient
    private Long commentableId;
}
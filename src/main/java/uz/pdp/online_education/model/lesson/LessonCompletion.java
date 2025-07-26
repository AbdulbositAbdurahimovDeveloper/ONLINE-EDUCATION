package uz.pdp.online_education.model.lesson;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.pdp.online_education.model.Abs.AbsLongEntity;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.model.lesson.Lesson;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "lesson_completions", uniqueConstraints = {
    // Bitta foydalanuvchi bitta darsni faqat bir marta "tugatgan" deb belgilashi mumkin
    @UniqueConstraint(columnNames = {"user_id", "lesson_id"})
})
public class LessonCompletion extends AbsLongEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;
    
    // Agar dars qachon tugatilganini saqlash kerak bo'lsa:
    // @Column(name = "completed_at")
    // private LocalDateTime completedAt;
}
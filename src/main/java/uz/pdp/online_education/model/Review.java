package uz.pdp.online_education.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import uz.pdp.online_education.model.Abs.AbsLongEntity;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.User;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"user", "course"})
@Entity
@Table(name = "reviews", uniqueConstraints = {
    // Bitta foydalanuvchi bitta kursga faqat bir marta sharh qoldirishi mumkin
    @UniqueConstraint(columnNames = {"user_id", "course_id"})
})
@FieldNameConstants
public class Review extends AbsLongEntity {

    // Foydalanuvchi qo'ygan baho (1 dan 5 gacha)
    @Column(nullable = false)
    private int rating;

    // Sharh matni (ixtiyoriy bo'lishi mumkin)
    @Column(columnDefinition = "TEXT")
    private String comment;

    // Sharh qaysi kursga tegishli
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    // Sharhni kim yozganligi
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

}
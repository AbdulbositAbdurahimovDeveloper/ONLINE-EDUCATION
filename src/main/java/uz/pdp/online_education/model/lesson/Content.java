package uz.pdp.online_education.model.lesson;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import uz.pdp.online_education.model.Abs.AbsLongEntity;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@ToString
@Table(name = "contents")
@Inheritance(strategy = InheritanceType.JOINED) // yoki SINGLE_TABLE, TABLE_PER_CLASS
@DiscriminatorColumn(name = "content_type") // Faqat SINGLE_TABLE va JOINED uchun
@FieldNameConstants
public abstract class Content extends AbsLongEntity {

    @Column(nullable = false)
    private Integer blockOrder; // Dars ichidagi tartibi

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    @ToString.Exclude
    private Lesson lesson;
}


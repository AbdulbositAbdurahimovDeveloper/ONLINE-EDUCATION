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

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "contents")
@SQLDelete(sql = "UPDATE contents SET deleted = true WHERE id = ?")
@SQLRestriction(value = "deleted=false")
@Inheritance(strategy = InheritanceType.JOINED) // yoki SINGLE_TABLE, TABLE_PER_CLASS
@DiscriminatorColumn(name = "content_type") // Faqat SINGLE_TABLE va JOINED uchun
@FieldNameConstants
public abstract class Content extends AbsLongEntity {

    @Column(nullable = false)
    private int blockOrder; // Dars ichidagi tartibi

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;
}
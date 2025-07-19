package uz.pdp.online_education.model;

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
@ToString
@Entity(name = "module_enrollments")
@SQLDelete(sql = "UPDATE module_enrollments SET deleted = true WHERE id = ?")
@SQLRestriction(value = "deleted=false")
@FieldNameConstants
@Table(uniqueConstraints = {
        // Bitta foydalanuvchi bitta modulga faqat bir marta yozilishi mumkin
        @UniqueConstraint(columnNames = {"user_id", "course_module_id"})
})
public class ModuleEnrollment extends AbsLongEntity {

    // Modulga yozilgan foydalanuvchi
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    // Foydalanuvchi yozilgan modul
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id")
    private Module module;

    // Foydalanuvchining ushbu modulni o'zlashtirish darajasi (%)
    private double progressPercentage = 0.0;
}
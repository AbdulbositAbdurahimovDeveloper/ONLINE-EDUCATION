package uz.pdp.online_education.model.lesson;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import uz.pdp.online_education.model.Abs.AbsLongEntity;
import uz.pdp.online_education.model.Module;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "lesson", uniqueConstraints = {
        // Bitta modul ichida darslarning tartib raqami unikal bo'lishi kerak
        @UniqueConstraint(columnNames = {"module_id", "orderIndex"})
})
@SQLDelete(sql = "UPDATE lesson SET deleted = true WHERE id = ?")
@SQLRestriction(value = "deleted=false")
public class Lesson extends AbsLongEntity {

    @Column(nullable = false)
    private String title;

    @Lob // Katta hajmdagi matnlar uchun
    private String content;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(nullable = false)
    private boolean isFree = false;

    @Column(nullable = false)
    private boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false) // Liquibase'da `modules_id`, lekin JPA konvensiyasi bo'yicha `module_id`
    private Module module;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("blockOrder ASC") // `Content` entitisida `blockOrder` maydoni bo'lishi kerak
    private List<Content> contents;

    // `view` maydoni butunlay olib tashlandi.

    // `lesson_completions` bilan bog'liqlik bu yerda bo'lmaydi.
    // Chunki Lesson o'zini kimlar ko'rganini bilishi shart emas.
    // Bu ma'lumotni LessonCompletionRepository orqali olamiz.
}
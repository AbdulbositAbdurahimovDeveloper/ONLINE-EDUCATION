package uz.pdp.online_education.model.lesson;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
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
@ToString
@Table(name = "lesson", uniqueConstraints = {
        // Bitta modul ichida darslarning tartib raqami unikal bo'lishi kerak
        @UniqueConstraint(columnNames = {"modules_id", "order_index"})
})
@FieldNameConstants
public class Lesson extends AbsLongEntity {

    @Column(nullable = false)
    private String title;

//    @Lob // Katta hajmdagi matnlar uchun
    private String content;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(nullable = false)
    private boolean isFree = false;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modules_id", nullable = false)
    @ToString.Exclude // Liquibase'da `modules_id`, lekin JPA konvensiyasi bo'yicha `module_id`
    private Module module;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("blockOrder ASC")
    @ToString.Exclude // `Content` entitisida `blockOrder` maydoni bo'lishi kerak
    private List<Content> contents;

}
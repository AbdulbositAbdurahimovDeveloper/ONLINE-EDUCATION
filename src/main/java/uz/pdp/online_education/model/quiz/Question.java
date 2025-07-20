package uz.pdp.online_education.model.quiz;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import uz.pdp.online_education.enums.QuestionType; // Savol turini saqlash uchun
import uz.pdp.online_education.model.Abs.AbsLongEntity;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"quiz", "options"}) // Infinite loop'ning oldini olish uchun
@Entity
@Table(name = "questions")
@SQLDelete(sql = "UPDATE questions SET deleted = true WHERE id = ?")
@SQLRestriction(value = "deleted=false")
@FieldNameConstants
public class Question extends AbsLongEntity {

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text; // Savol matni

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type; // Savol turi: SINGLE_CHOICE yoki MULTIPLE_CHOICE

    // Bu savol qaysi quizga tegishli. optional=false - har bir savol quizga tegishli bo'lishi SHART.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    // Savolning javob variantlari
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnswerOption> options = new ArrayList<>();

    // Yordamchi metodlar
    public void addOption(AnswerOption option) {
        this.options.add(option);
        option.setQuestion(this);
    }
}
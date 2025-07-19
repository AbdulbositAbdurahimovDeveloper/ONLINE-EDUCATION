package uz.pdp.online_education.model.quiz;

import jakarta.persistence.*;
import lombok.*;
import uz.pdp.online_education.model.Abs.AbsLongEntity;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = "question")
@Entity
@Table(name = "answer_options")
public class AnswerOption extends AbsLongEntity {

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text; // Javob varianti matni

    @Column(nullable = false)
    private boolean isCorrect; // Bu javobning to'g'ri yoki noto'g'riligini belgilaydi

    // Bu javob varianti qaysi savolga tegishli
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id")
    private Question question;
}
package uz.pdp.online_education.model.quiz;

import jakarta.persistence.*;
import lombok.*;
import uz.pdp.online_education.model.Abs.AbsLongEntity;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"attempt", "question", "selectedOptions"})
@Entity(name = "user_answers")
public class UserAnswer extends AbsLongEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id")
    private QuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id")
    private Question question;

    // Foydalanuvchi tanlagan javob variant(lar)i
    @ManyToMany(fetch = FetchType.EAGER) // Javobni tekshirish uchun darhol yuklaganimiz ma'qul
    @JoinTable(
            name = "user_selected_options",
            joinColumns = @JoinColumn(name = "user_answer_id"),
            inverseJoinColumns = @JoinColumn(name = "answer_option_id")
    )
    private Set<AnswerOption> selectedOptions = new HashSet<>();

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;
}

package uz.pdp.online_education.model.lesson;

import jakarta.persistence.*;
import lombok.*;
import uz.pdp.online_education.model.quiz.Quiz; // To'g'ri paketdan import

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "quiz_contents")
@DiscriminatorValue("QUIZ")
public class QuizContent extends Content { // 'Content' ota-klassidan meros oladi

    // Bitta content bloki faqat bitta quizga ishora qiladi.
    // Cascade.ALL: Agar QuizContent yaratilsa va unga yangi Quiz berilsa, u ham saqlanadi.
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "quiz_id", unique = true) // Har bir quiz faqat bitta content blokida bo'lishi mumkin
    private Quiz quiz;
}
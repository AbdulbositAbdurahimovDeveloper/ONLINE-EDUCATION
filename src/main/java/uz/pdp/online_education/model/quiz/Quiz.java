package uz.pdp.online_education.model.quiz;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import uz.pdp.online_education.model.Abs.AbsLongEntity;
import uz.pdp.online_education.model.lesson.QuizContent; // Content bloki bilan bog'lanish uchun

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "quizzes")
@FieldNameConstants
public class Quiz extends AbsLongEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description; // Quiz haqida yo'riqnoma yoki qo'shimcha ma'lumot

    // Bitta quizga tegishli savollar ro'yxati.
    // Cascade.ALL: Quiz saqlansa/yangilansa, savollar ham saqlanadi/yangilanadi.
    // orphanRemoval=true: Agar ro'yxatdan savol olib tashlansa, u bazadan ham o'chiriladi.
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @ToString.Exclude // Savollar har doim bir xil tartibda kelishi uchun
    private List<Question> questions = new ArrayList<>();

    // Bu Quiz qaysi Content blokiga tegishli.
    // mappedBy: Bog'lanishning "egasi" QuizContent entitysi ekanligini bildiradi.
    @OneToOne(mappedBy = "quiz", fetch = FetchType.LAZY)
    @ToString.Exclude
    private QuizContent quizContent;

    // Yordamchi metodlar (Fluent API)
    public void addQuestion(Question question) {
        this.questions.add(question);
        question.setQuiz(this);
    }
}
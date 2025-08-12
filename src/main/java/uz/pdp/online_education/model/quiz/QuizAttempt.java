package uz.pdp.online_education.model.quiz;

import jakarta.persistence.*;
import lombok.*;
import uz.pdp.online_education.enums.AttemptStatus;
import uz.pdp.online_education.model.Abs.AbsLongEntity;
import uz.pdp.online_education.model.User;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"user", "quiz"})
@Entity(name = "quiz_attempts")
public class QuizAttempt extends AbsLongEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttemptStatus status;

    @Column(nullable = false)
    private int score = 0; // To'g'ri topilgan savollar soni

    @Column(name = "total_questions", nullable = false)
    private int totalQuestions; // O'sha paytdagi umumiy savollar soni

    @Column(nullable = false)
    private double percentage = 0.0; // Natija foizda
}

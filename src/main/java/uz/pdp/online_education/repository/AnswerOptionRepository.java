package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.pdp.online_education.model.quiz.AnswerOption;

public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Long> {

    /**
     * Berilgan savolga tegishli bo'lgan va ID'si berilgan ID'dan farq qiladigan
     * barcha javob variantlarining 'isCorrect' statusini 'false' ga o'zgartiradi.
     * @param questionId Savolning ID'si
     * @param exceptOptionId O'zgartirilmasligi kerak bo'lgan javob variantining ID'si
     */
    @Modifying
    @Query("UPDATE AnswerOption ao SET ao.isCorrect = false WHERE ao.question.id = :questionId AND ao.id != :exceptOptionId")
    void setAllOtherOptionsAsIncorrect(@Param("questionId") Long questionId, @Param("exceptOptionId") Long exceptOptionId);
}
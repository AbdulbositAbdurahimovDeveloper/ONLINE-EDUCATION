package uz.pdp.online_education.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "faqs")
@SQLDelete(sql = "UPDATE faqs SET deleted = true WHERE id = ?")
@SQLRestriction(value = "deleted=false")
@FieldNameConstants
public class Faq extends AbsLongEntity {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question; // Savol matni

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;   // Javob matni
    
    // FAQ'larni tartiblash uchun
    private int displayOrder;
}
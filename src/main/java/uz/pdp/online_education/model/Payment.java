package uz.pdp.online_education.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import uz.pdp.online_education.enums.TransactionStatus;
import uz.pdp.online_education.model.Abs.AbsLongEntity;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "payment")
@SQLDelete(sql = "UPDATE payment SET deleted = true WHERE id = ?")
@SQLRestriction(value = "deleted=false")
@FieldNameConstants
@Table(uniqueConstraints = {
        // Bitta foydalanuvchi bitta modulga faqat bir marta yozilishi mumkin
        @UniqueConstraint(columnNames = {"user_id", "module_id"})
})
public class Payment extends AbsLongEntity {

  @ManyToOne
  private User user;

  @ManyToOne
  private Module module;

  private Long amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TransactionStatus status;

  @Column(length = 16)
  private String maskedCardNumber;

  // Tranzaksiya haqida izoh (masalan, "To'lov simulyatsiyasi")
  private String description;

  private boolean deleted = false;
}
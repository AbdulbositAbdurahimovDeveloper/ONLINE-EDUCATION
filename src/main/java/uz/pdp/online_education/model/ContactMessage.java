package uz.pdp.online_education.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.*;
import uz.pdp.online_education.enums.MessageStatus;
import uz.pdp.online_education.model.Abs.AbsLongEntity;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "contact_messages")
public class ContactMessage extends AbsLongEntity {

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    
    // Xabarning holatini kuzatib borish uchun
    @Enumerated(EnumType.STRING)
    private MessageStatus status = MessageStatus.NEW;


}
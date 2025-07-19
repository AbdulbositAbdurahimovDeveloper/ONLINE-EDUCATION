package uz.pdp.online_education.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import uz.pdp.online_education.model.Abs.AbsLongEntity;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "user_profiles")
@SQLDelete(sql = "UPDATE user_profiles SET deleted = true WHERE id = ?")
@SQLRestriction(value = "deleted=false")
@FieldNameConstants
public class UserProfile extends AbsLongEntity {

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String email; // Emailni bu yerda saqlash ham mumkin

    private String phoneNumber;

    @OneToOne
    private Attachment profilePicture;

    @Column(columnDefinition = "TEXT")
    private String bio; // O'qituvchi uchun qisqa ma'lumot

    // --- BOG'LANISHNING ASOSIY QISMI ---
    // Har bir profil faqat bitta User'ga tegishli bo'ladi
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // email uchun unique indeks shart lekin delted falselar ortasida
}
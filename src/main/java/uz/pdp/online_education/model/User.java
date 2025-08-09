package uz.pdp.online_education.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import uz.pdp.online_education.enums.Role;
import uz.pdp.online_education.model.Abs.AbsLongEntity;
import uz.pdp.online_education.telegram.model.TelegramUser;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted = true WHERE id = ?")
@SQLRestriction(value = "deleted=false")
@FieldNameConstants
public class User extends AbsLongEntity implements UserDetails {

    private String username;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(mappedBy = "user", orphanRemoval = true,cascade = CascadeType.ALL)
    @JsonManagedReference
    private UserProfile profile;

    /**
     * The associated TelegramUser account for this user.
     * This is the inverse side of the OneToOne relationship.
     * The 'mappedBy = "user"' indicates that the relationship is managed
     * by the 'user' field in the TelegramUser entity.
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private TelegramUser telegramUser;

    private boolean deleted = false;

    private boolean enabled = false;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
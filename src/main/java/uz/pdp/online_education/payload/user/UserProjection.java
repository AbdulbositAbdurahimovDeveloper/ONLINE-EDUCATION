// UserProjection.java
package uz.pdp.online_education.payload.user;

import java.sql.Timestamp;

public interface UserProjection {
    Long getId();
    String getUsername();
    String getRole();

    String getFirstName();
    String getLastName();
    String getEmail();
    String getPhoneNumber();
    String getBio();
    Long getProfilePictureId();

    Timestamp getCreatedAt();
    Timestamp getUpdatedAt();

    Integer getRating();
}
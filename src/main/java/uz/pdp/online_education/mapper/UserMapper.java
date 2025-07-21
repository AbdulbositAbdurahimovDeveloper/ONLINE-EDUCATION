package uz.pdp.online_education.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uz.pdp.online_education.enums.Role;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.user.UserDTO;
import uz.pdp.online_education.payload.user.UserRegisterResponseDTO;

import java.sql.Timestamp;

@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Converts a User entity to a UserRegisterResponseDTO.
     */
    @Mapping(source = "profile.firstName", target = "firstName")
    @Mapping(source = "profile.lastName", target = "lastName")
    @Mapping(source = "profile.email", target = "email")
    @Mapping(source = "profile.bio", target = "bio")
    @Mapping(source = "profile.profilePicture.id", target = "profilePictureUrl")
    @Mapping(source = "profile.phoneNumber", target = "phoneNumber")
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "timestampToLong")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "timestampToLong")
    UserRegisterResponseDTO toRegisterResponseDTO(User user);


    /**
     * Converts a User entity to a detailed UserDTO for display (read) purposes.
     * Maps the single Role enum to a String.
     */
    @Mapping(source = "profile.profilePicture.id", target = "profilePictureId")
    @Mapping(source = "profile.firstName", target = "firstName")
    @Mapping(source = "profile.lastName", target = "lastName")
    @Mapping(source = "profile.email", target = "email")
    @Mapping(source = "profile.phoneNumber", target = "phoneNumber")
    @Mapping(source = "profile.bio", target = "bio")
    @Mapping(source = "role", target = "role", qualifiedByName = "roleToString") // <-- O'ZGARISH
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "timestampToLong")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "timestampToLong")
    UserDTO toDTO(User user);


    /**
     * Custom mapping method to convert Timestamp to Long.
     *
     * @Named allows us to specifically call this method for a given mapping.
     */
    @Named("timestampToLong")
    default Long timestampToLong(Timestamp timestamp) {
        return timestamp != null ? timestamp.getTime() : null;
    }


    /**
     * Converts a Role enum to its String representation.
     * This is a cleaner way than letting MapStruct do it implicitly.
     */
    @Named("roleToString")
    default String roleToString(Role role) {
        // Null qiymat kelishi mumkinligini hisobga olamiz
        return role != null ? role.name() : null;
    }
}
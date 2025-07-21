package uz.pdp.online_education.assembler;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import uz.pdp.online_education.controller.UserController;
import uz.pdp.online_education.mapper.UserMapper;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.user.UserDTO;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * A HATEOAS assembler for converting a User entity to its DTO representation (UserDTO).
 * This assembler is responsible for the conversion logic and adding entity-specific links,
 * such as the 'self' link.
 *
 * It extends RepresentationModelAssemblerSupport to simplify the creation of links
 * and to integrate seamlessly with PagedResourcesAssembler.
 */
@Component // Bu klassni Spring Bean sifatida ro'yxatdan o'tkazadi
public class UserModelAssembler extends RepresentationModelAssemblerSupport<User, UserDTO> {

    // Konvertatsiya uchun biz avval yaratgan MapStruct mapper'ini ishlatamiz
    private final UserMapper userMapper;

    /**
     * Konstruktor.
     * Ota-klassga kontroller va DTO turlarini beramiz.
     * Bu Spring HATEOAS'ga havolalarni qaysi kontrollerga qarab yaratishni aytadi.
     */
    public UserModelAssembler(UserMapper userMapper) {
        super(UserController.class, UserDTO.class);
        this.userMapper = userMapper;
    }

    /**
     * Bu asosiy metod. Bitta User entity'sini olib, uni UserDTO'ga o'giradi va
     * havolalar bilan boyitadi.
     * PagedResourcesAssembler sahifadagi har bir User uchun shu metodni chaqiradi.
     *
     * @param entity The source User entity from the database.
     * @return The converted and enriched UserDTO.
     */
    @Override
    public UserDTO toModel(User entity) {
        // 1. Mapper yordamida ma'lumotlarni User'dan UserDTO'ga ko'chiramiz.
        UserDTO userDTO = userMapper.toDTO(entity);

        // 2. DTO'ga o'ziga ishora qiluvchi "self" havolasini qo'shamiz.
        // Havola UserController'dagi getUserById metodiga ishora qiladi.
        // Masalan: http://localhost:8080/api/v1/users/1
        userDTO.add(linkTo(methodOn(UserController.class)
                .read(entity.getId())) // Bu proksi chaqiruv, haqiqiy emas
                .withSelfRel());

        return userDTO;
    }
}
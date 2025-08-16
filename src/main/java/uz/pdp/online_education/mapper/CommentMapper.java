package uz.pdp.online_education.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uz.pdp.online_education.model.Comment;
import uz.pdp.online_education.payload.comment.CommentResponseDto;
import uz.pdp.online_education.payload.comment.CommentUpdateDto;

import java.util.List;

@Mapper(componentModel = "spring") // MapStruct'ni Spring Bean sifatida ro'yxatdan o'tkazadi
public interface CommentMapper {

    /**
     * Comment (Entity) -> CommentResponseDto (DTO)
     * Foydalanuvchi nomini `user.username` dan oladi.
     */
    @Mapping(source = "user.username", target = "authorUsername")
    CommentResponseDto toDto(Comment comment);

    /**
     * CommentCreateDto (DTO) -> Comment (Entity)
     * E'tibor bering: user, course, lesson, parent kabi maydonlar DTO'da yo'q.
     * Ular servis qatlamida alohida o'rnatilishi kerak, shuning uchun ularni ignore qilamiz.
     */
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "user", ignore = true)
//    @Mapping(target = "course", ignore = true)
//    @Mapping(target = "lesson", ignore = true)
//    @Mapping(target = "parent", ignore = true)
//    @Mapping(target = "replies", ignore = true)
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
//    Comment toEntity(CommentCreateDto createDto);

    /**
     * CommentUpdateDto (DTO) ma'lumotlari bilan mavjud Comment (Entity)ni yangilaydi.
     * @MappingTarget annotatsiyasi mavjud ob'ektni yangilash uchun ishlatiladi.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(CommentUpdateDto updateDto, @MappingTarget Comment comment);

    /**
     * MapStruct avtomatik ravishda List<Entity> ni List<DTO> ga o'gira oladi.
     */
    List<CommentResponseDto> toDtoList(List<Comment> comments);

}
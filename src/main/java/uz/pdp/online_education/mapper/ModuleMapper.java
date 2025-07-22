package uz.pdp.online_education.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.payload.module.ModuleDetailDTO;

import java.sql.Timestamp;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,componentModel = "spring")
public interface ModuleMapper {

    @Mapping(target = "orderIndex")
    @Mapping(target = "course",ignore = true)
    @Mapping(target = "lessons")
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "timestampToLong")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "timestampToLong")
    ModuleDetailDTO toModuleDetailsDTO(Module entity);

    @Named("timestampToLong")
    default Long timestampToLong(Timestamp timestamp) {
        return timestamp != null ? timestamp.getTime() : null;
    }
}

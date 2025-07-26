package uz.pdp.online_education.mapper;

import org.mapstruct.*;
import uz.pdp.online_education.model.Payment;
import uz.pdp.online_education.payload.payment.PaymentDTO;

import java.sql.Timestamp;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {


    @Mapping(target = "userId",source = "user.id")
    @Mapping(target = "moduleId",source = "module.id")
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "timestampToLong")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "timestampToLong")
    PaymentDTO toDTO(Payment payment);

    /**
     * Custom mapping method to convert Timestamp to Long.
     *
     * @Named allows us to specifically call this method for a given mapping.
     */
    @Named("timestampToLong")
    default Long timestampToLong(Timestamp timestamp) {
        return timestamp != null ? timestamp.getTime() : null;
    }

}
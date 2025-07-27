package uz.pdp.online_education.service.interfaces;

import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.PageDTO;
import uz.pdp.online_education.payload.payment.PaymentCreateDTO;
import uz.pdp.online_education.payload.payment.PaymentDTO;

public interface PaymentService {
    PageDTO<PaymentDTO> read(Integer page, Integer size);

    PaymentDTO read(Long id);

    PageDTO<PaymentDTO> readPayments(Long id, Integer page, Integer size);

    PaymentDTO create(PaymentCreateDTO paymentCreateDTO, User currentUser);

}

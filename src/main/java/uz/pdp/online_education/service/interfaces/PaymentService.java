package uz.pdp.online_education.service.interfaces;

import uz.pdp.online_education.payload.PageDTO;
import uz.pdp.online_education.payload.payment.PaymentDTO;

public interface PaymentService {
    PageDTO<PaymentDTO> read(Integer page, Integer size);
}

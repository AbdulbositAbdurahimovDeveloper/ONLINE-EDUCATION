package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uz.pdp.online_education.model.Abs.AbsLongEntity;
import uz.pdp.online_education.model.Payment;
import uz.pdp.online_education.mapper.PaymentMapper;
import uz.pdp.online_education.payload.PageDTO;
import uz.pdp.online_education.payload.payment.PaymentDTO;
import uz.pdp.online_education.repository.PaymentRepository;
import uz.pdp.online_education.service.interfaces.PaymentService;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    /**
     * @param page Integer
     * @param size Integer
     * @return page
     */
    @Override
    public PageDTO<PaymentDTO> read(Integer page, Integer size) {
        Sort sort = Sort.by(Sort.Direction.ASC, AbsLongEntity.Fields.id);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<Payment> payments = paymentRepository.findAll(pageRequest);

        return new PageDTO<>(
                payments.getContent().stream().map(paymentMapper::toDTO).toList(),
                payments.getNumber(),
                payments.getSize(),
                payments.getTotalElements(),
                payments.getTotalPages(),
                payments.isLast(),
                payments.isFirst(),
                payments.getNumberOfElements(),
                payments.isEmpty()
        );

    }
}

package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uz.pdp.online_education.enums.TransactionStatus;
import uz.pdp.online_education.exceptions.DataConflictException;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.mapper.PaymentMapper;
import uz.pdp.online_education.model.Abs.AbsLongEntity;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.Payment;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.model.UserProfile;
import uz.pdp.online_education.payload.PageDTO;
import uz.pdp.online_education.payload.payment.PaymentCreateDTO;
import uz.pdp.online_education.payload.payment.PaymentDTO;
import uz.pdp.online_education.repository.ModuleRepository;
import uz.pdp.online_education.repository.PaymentRepository;
import uz.pdp.online_education.service.interfaces.EmailService;
import uz.pdp.online_education.service.interfaces.PaymentService;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final ModuleRepository moduleRepository;
    private final EmailService emailService;

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

    /**
     * @param id Long
     * @return payment
     */
    @Override
    public PaymentDTO read(Long id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("payment not fount with id: " + id));

        return paymentMapper.toDTO(payment);
    }

    /**
     * @param id   Long
     * @param page Integer
     * @param size Integer
     * @return payments
     */
    @Override
    public PageDTO<PaymentDTO> readPayments(Long id, Integer page, Integer size) {

        PageRequest pageRequest = PageRequest.of(page,size);
        Page<Payment> payments = paymentRepository.findByModule_Id(id, pageRequest);

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

    /**
     * @param paymentCreateDTO klass
     * @param currentUser
     * @return paymentDTO
     */
    @Override
    public PaymentDTO create(PaymentCreateDTO paymentCreateDTO, User currentUser) {

        Module module = moduleRepository.findById(paymentCreateDTO.getModuleId())
                .orElseThrow(() -> new EntityNotFoundException("module not fount with id: " + paymentCreateDTO.getModuleId()));

        Optional<Payment> optionalPayment = module.getPayments().stream()
                .filter(filter -> filter.getUser().getId().equals(currentUser.getId()))
                .findFirst();

        if (optionalPayment.isPresent()) {
            throw new DataConflictException("Payment already exist");
        }

        // 1. Bazadagi narxni TIYINda olamiz.
        BigDecimal modulePriceInTiyin = BigDecimal.valueOf(module.getPrice()); // Masalan, 1500000

        // 2. Bazadagi narxni solishtirish uchun SO'Mga o'giramiz.
        // 1500000 (tiyin) -> 15000.00 (so'm)
        BigDecimal modulePriceInSom = modulePriceInTiyin.divide(new BigDecimal(100));

        // 3. Frontend'dan kelgan narxni SO'Mda olamiz.
        BigDecimal amountInSomFromDto = BigDecimal.valueOf(paymentCreateDTO.getAmount()); // Masalan, 15000.00

        // 4. Endi ikkita SO'M qiymatini solishtiramiz.
        // .compareTo() metodi teng bo'lsa 0 qaytaradi.
        if (amountInSomFromDto.compareTo(modulePriceInSom) != 0) {
            // Xatolik xabarida ham SO'Mdagi qiymatni ko'rsatamiz.
            throw new DataConflictException("The payment amount is incorrect. Expected: " + modulePriceInSom.toPlainString());
        }


        Payment payment = new Payment();
        payment.setUser(currentUser);
        payment.setModule(module);
        payment.setAmount(module.getPrice());
        payment.setStatus(TransactionStatus.SUCCESS);
        payment.setMaskedCardNumber(paymentCreateDTO.getMaskedCardNumber());
        payment.setDescription(paymentCreateDTO.getDescription());

        paymentRepository.save(payment);

        emailService.sendPaymentReceipt(payment);

        return paymentMapper.toDTO(payment);


    }

    /**
     * A helper method to generate a professional-looking HTML receipt.
     * @param payment The payment entity to generate a receipt for.
     * @return A String containing the HTML content of the receipt.
     */
    private String generateReceiptHtml(Payment payment) {
        User user = payment.getUser();
        UserProfile profile = user.getProfile();

        // Summani tiyindan so'mga o'tkazib, formatlaymiz
        BigDecimal amountInSom = BigDecimal.valueOf(payment.getAmount()).divide(new BigDecimal(100));
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("uz", "UZ")); // "so'm" formatida
        String formattedAmount = currencyFormatter.format(amountInSom);

        // Sanani chiroyli formatga o'tkazamiz
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        String formattedDate = payment.getCreatedAt().toLocalDateTime().format(dateFormatter);

        return "<!DOCTYPE html>"
                + "<html><head><style>"
                + "body{font-family: Arial, sans-serif; color: #333;}"
                + ".container{width: 80%; margin: auto; border: 1px solid #eee; padding: 20px; box-shadow: 0 0 10px rgba(0,0,0,0.1);}"
                + ".header{text-align: center; border-bottom: 1px solid #eee; padding-bottom: 10px;}"
                + ".details-table{width: 100%; margin-top: 20px; border-collapse: collapse;}"
                + ".details-table td{padding: 10px; border: 1px solid #eee;}"
                + ".details-table .label{font-weight: bold; width: 40%;}"
                + ".footer{text-align: center; margin-top: 20px; font-size: 0.9em; color: #777;}"
                + "</style></head><body>"
                + "<div class='container'>"
                + "<div class='header'><h2>To'lov Cheki</h2></div>"
                + "<p>Hurmatli " + profile.getFirstName() + " " + profile.getLastName() + ",</p>"
                + "<p>Sizning to'lovingiz muvaffaqiyatli amalga oshirildi. Quyida to'lov tafsilotlari keltirilgan:</p>"
                + "<table class='details-table'>"
                + "<tr><td class='label'>To'lov ID:</td><td>" + payment.getId() + "</td></tr>"
                + "<tr><td class='label'>Kurs Moduli:</td><td>" + payment.getModule().getTitle() + "</td></tr>"
                + "<tr><td class='label'>Summa:</td><td>" + formattedAmount + "</td></tr>"
                + "<tr><td class='label'>To'lov Sanasi:</td><td>" + formattedDate + "</td></tr>"
                + "<tr><td class='label'>Karta Raqami:</td><td>**** **** **** " + payment.getMaskedCardNumber().substring(12) + "</td></tr>"
                + "</table>"
                + "<div class='footer'><p>Hurmat bilan, Sizning Online Ta'lim Platformangiz</p></div>"
                + "</div>"
                + "</body></html>";
    }
}

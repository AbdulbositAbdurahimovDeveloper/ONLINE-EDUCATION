package uz.pdp.online_education.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import uz.pdp.online_education.enums.MessageStatus;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.mapper.ContactMessageMapper;
import uz.pdp.online_education.model.ContactMessage;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.ContactMessageRequestDTO;
import uz.pdp.online_education.payload.ContactMessageResponseDTO;
import uz.pdp.online_education.repository.ContactMessageRepository;
import uz.pdp.online_education.service.interfaces.ContactMessageService;

import org.thymeleaf.context.Context;

import java.util.List;
import java.util.stream.Collectors;

@Service // Bu klass Spring tomonidan servis sifatida boshqarilishini bildiradi.
@RequiredArgsConstructor // Final maydonlar uchun constructor'ni avtomatik generatsiya qiladi (dependency injection uchun).
public class ContactMessageServiceImpl implements ContactMessageService { // ContactMessageService interfeysini implementatsiya qiladi.

    // --- DEPENDENCY INJECTION ---
    // Kerakli dependency'lar (mailSender, templateEngine, repositories, mapper) constructor orqali inject qilinadi.

    private final JavaMailSender mailSender; // JavaMailSender - elektron pochta xabarlarini yuborish uchun asosiy klass.
    private final TemplateEngine templateEngine; // Thymeleaf template engine - HTML template'larni qayta ishlash uchun.
    private final ContactMessageRepository contactMessageRepository; // ContactMessage obyektlari bilan ma'lumotlar bazasida ishlash uchun repository.
    private final ContactMessageMapper contactMessageMapper; // ContactMessage obyektlarini DTO ga va DTO ni ContactMessage obyektiga aylantirish uchun mapper.

    /**
     * Yangi Aloqa Xabari (ContactMessage) yaratish metodi.
     * Kelgan `ContactMessageRequestDTO` ni `ContactMessageMapper` yordamida `ContactMessage` entity'siga aylantiradi.
     * Xabarga default holat sifatida `MessageStatus.NEW` beradi va uni ma'lumotlar bazasiga saqlaydi.
     *
     * @param dto `ContactMessage` yaratish uchun kerakli ma'lumotlarni o'z ichiga olgan DTO.
     */
    @Override
    public void create(ContactMessageRequestDTO dto) {
        // Request DTO dan ContactMessage entity'siga aylantirish.
        ContactMessage message = contactMessageMapper.toEntity(dto);
        // Xabarning statusini "NEW" (yangi) qilib belgilaymiz.
        message.setStatus(MessageStatus.NEW);
        // Xabarni ma'lumotlar bazasiga saqlaymiz.
        contactMessageRepository.save(message);
    }

    /**
     * Javob berilmagan (REPLIED holatidan boshqa) barcha Aloqa Xabarlarini olish metodi.
     * Bu metod `contactMessageRepository` dan `MessageStatus.REPLIED` dan boshqa statusdagi barcha xabarlarni oladi.
     * Olingan `ContactMessage` obyektlarini `ContactMessageResponseDTO` ga aylantirib, list sifatida qaytaradi.
     *
     * @return Javob berilmagan aloqa xabarlarining `ContactMessageResponseDTO` ko'rinishidagi listi.
     */
    @Override
    public List<ContactMessageResponseDTO> getAllExceptReplied() {
        // `findAllByStatusNot` repository metodi yordamida REPLIED statusidan boshqa barcha xabarlarni olamiz.
        return contactMessageRepository.findAllByStatusNot(MessageStatus.REPLIED)
                .stream() // Olingan listni streamga olamiz.
                .map(contactMessageMapper::toDto) // Har bir ContactMessage obyektini ContactMessageResponseDTO ga aylantiramiz.
                .collect(Collectors.toList()); // Natijalarni List ga yig'amiz.
    }

    /**
     * Barcha Aloqa Xabarlarini olish metodi.
     * Barcha `ContactMessage` obyektlarini ma'lumotlar bazasidan oladi va ularni `ContactMessageResponseDTO` ga aylantirib list sifatida qaytaradi.
     *
     * @return Barcha aloqa xabarlarining `ContactMessageResponseDTO` ko'rinishidagi listi. Agar hech qanday xabar bo'lmasa, bo'sh list qaytariladi.
     */
    @Override
    public List<ContactMessageResponseDTO> getAll() {
        // Barcha ContactMessage obyektlarini repository dan olamiz.
        return contactMessageRepository.findAll()
                .stream() // Olingan listni streamga olamiz.
                .map(contactMessageMapper::toDto) // Har bir ContactMessage obyektini ContactMessageResponseDTO ga aylantiramiz.
                .collect(Collectors.toList()); // Natijalarni List ga yig'amiz.
    }

    /**
     * Aloqa Xabarini ID bo'yicha olish va uni "READ" (o'qilgan) holatiga o'tkazish metodi.
     * Ushbu metod `@Transactional` bo'lib, o'qilgan holatga o'tkazish va uni saqlash operatsiyasini bir butunlikda bajaradi.
     * Agar berilgan `id` bilan xabar topilmasa, `EntityNotFoundException` tashlanadi.
     *
     * @param id Olinishi va "READ" holatiga o'tkazilishi kerak bo'lgan xabarning ID si.
     * @return Olingan va "READ" holatiga o'tkazilgan xabarning `ContactMessageResponseDTO` ko'rinishi.
     * @throws EntityNotFoundException Agar berilgan `id` bilan hech qanday `ContactMessage` topilmasa.
     */
    @Override
    @Transactional
    public ContactMessageResponseDTO getByIdAndMarkRead(Long id) {
        // Berilgan `id` bo'yicha `ContactMessage`ni topishga harakat qilamiz.
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ContactMessage not found"));
        // Topilgan xabarning statusini "READ" (o'qilgan) qilib belgilaymiz.
        message.setStatus(MessageStatus.READ);
        // O'zgartirilgan `ContactMessage`ni ma'lumotlar bazasiga saqlash (implicit save, chunki metod transactional).
        // Uni DTO ga aylantirib qaytaramiz.
        return contactMessageMapper.toDto(message);
    }

    /**
     * Aloqa Xabarini ID bo'yicha tahrirlash metodi.
     * Faqat "REPLIED" (javob berilgan) holatdagi xabarlarni tahrirlash taqiqlanadi.
     * Agar xabar topilmasa, `EntityNotFoundException` tashlanadi.
     *
     * @param id        Tahrirlanishi kerak bo'lgan xabarning ID si.
     * @param dto       Tahrirlash uchun yangi ma'lumotlarni o'z ichiga olgan `ContactMessageRequestDTO`.
     * @throws EntityNotFoundException Agar berilgan `id` bilan hech qanday `ContactMessage` topilmasa.
     * @throws RuntimeException Agar xabar allaqachon javob berilgan bo'lsa.
     */
    @Override
    public void edit(Long id, ContactMessageRequestDTO dto){
        // Berilgan `id` bo'yicha `ContactMessage`ni topamiz.
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ContactMessage not found"));

        // Agar xabar allaqachon javob berilgan bo'lsa (status REPLIED bo'lsa), tahrirlashga ruxsat bermaymiz.
        if (message.getStatus() == MessageStatus.REPLIED)
            throw new RuntimeException("Cannot edit a replied message"); // Xatolik tashlaymiz.

        // `ContactMessageMapper` yordamida DTO dagi ma'lumotlarni mavjud `ContactMessage` obyektiga ko'chiradi.
        contactMessageMapper.updateEntityFromDto(dto, message);
        // Yangilangan `ContactMessage`ni ma'lumotlar bazasiga saqlaymiz.
        contactMessageRepository.save(message);
    }

    /**
     * Aloqa Xabarini ID bo'yicha o'chirish metodi.
     * O'chirish uchun foydalanuvchining o'z xabarlarini o'chirishga ruxsat beriladi va
     * agar xabar allaqachon javob berilgan bo'lsa, o'chirishga ruxsat berilmaydi.
     *
     * @param id             O'chirilishi kerak bo'lgan xabarning ID si.
     * @param requesterEmail O'chirishni amalga oshirayotgan foydalanuvchining email manzili (o'ziga tegishli xabarni tekshirish uchun).
     * @throws EntityNotFoundException Agar berilgan `id` bilan hech qanday `ContactMessage` topilmasa.
     * @throws RuntimeException      Agar foydalanuvchi o'ziga tegishli bo'lmagan xabarni yoki allaqachon javob berilgan xabarni o'chirmoqchi bo'lsa.
     */
    @Override
    public void delete(Long id, String requesterEmail) {
        // Berilgan `id` bo'yicha `ContactMessage`ni topamiz.
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ContactMessage not found"));

        // Tekshirish: Faqat o'ziga tegishli bo'lgan xabarni o'chirishga ruxsat beriladi.
        // `message.getEmail()` - xabarni yuborgan foydalanuvchining email'i.
        if (!message.getEmail().equals(requesterEmail)) {
            throw new RuntimeException("You can only delete your own messages"); // Ruxsat yo'q.
        }

        // Tekshirish: Agar xabar allaqachon javob berilgan bo'lsa (REPLIED statusida), o'chirishga ruxsat berilmaydi.
        if (message.getStatus() == MessageStatus.REPLIED) {
            throw new RuntimeException("Cannot delete a replied message"); // Ruxsat yo'q.
        }

        // Barcha tekshiruvlardan o'tgan bo'lsa, xabarni ma'lumotlar bazasidan o'chiramiz.
        contactMessageRepository.delete(message);
    }

    /**
     * Aloqa Xabariga javob yuborish metodi.
     * Bu metod ma'lum bir `id` ga ega bo'lgan xabarga `replyText`ni HTML formatida elektron pochta orqali yuboradi.
     * Xat `contact_reply` nomli Thymeleaf template'idan foydalanib tayyorlanadi.
     * Agar xat yuborishda xatolik bo'lsa, `IllegalStateException` tashlanadi.
     * Yuborilgan xabar statusi `REPLIED` ga o'zgartiriladi.
     *
     * @param id        Javob yuborilishi kerak bo'lgan Aloqa Xabarining ID si.
     * @param replyText Admin yoki tizim tomonidan yozilgan javob matni.
     * @throws EntityNotFoundException Agar berilgan `id` bilan hech qanday `ContactMessage` topilmasa.
     * @throws IllegalStateException Agar elektron pochta yuborishda xatolik yuz bersa.
     */
    @Override
    public void replyToMessage(Long id, String replyText) {
        // Javob yuborilishi kerak bo'lgan `ContactMessage`ni topamiz.
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ContactMessage not found"));
        try {
            // Thymeleaf template'iga uzatiladigan ma'lumotlarni tayyorlaymiz.
            Context context = new Context();
            // Talabaning to'liq ismini template ga uzatamiz.
            context.setVariable("fullName", message.getFullName());
            // Talabaning asl xabarini template ga uzatamiz.
            context.setVariable("userMessage", message.getMessage());
            // Admin yoki tizim tomonidan yozilgan javob matnini template ga uzatamiz.
            context.setVariable("replyText", replyText); // E'tibor bering: "reply  Text" emas, "replyText" bo'lishi kerak

            // "contact_reply" nomli Thymeleaf template'ini ishga tushirib, HTML kontentni olamiz.
            // Bu template `src/main/resources/templates/contact_reply.html` da joylashgan bo'lishi kerak.
            String htmlContent = templateEngine.process("contact_reply", context);

            // Elektron pochta xabarni tayyorlaymiz.
            MimeMessage mimeMessage = mailSender.createMimeMessage(); // Yangi MimeMessage yaratamiz.
            // MimeMessageHelper yordamida xabarni yanada qulayroq formatlash. `true` - multipart xabar uchun. "UTF-8" - kodlash.
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Xatni kimga yuborishni belgilaymiz (bu xolatda xabarni yuborgan talabaga).
            helper.setTo(message.getEmail());
            // Xatning mavzusini belgilaymiz.
            helper.setSubject("Javobingiz - Online Education Support");
            // Xatning asosiy mazmunini HTML formatida belgilaymiz.
            helper.setText(htmlContent, true);
            // Xatni kimdan yuborayotganini belgilaymiz ("noreply" manzili).
            helper.setFrom("noreply@online-education.com");

            // Tayyorlangan elektron pochtani yuboramiz.
            mailSender.send(mimeMessage);

            // Xabarni yuborganimizdan so'ng, uning statusini "REPLIED" (javob berilgan) qilib o'zgartiramiz.
            message.setStatus(MessageStatus.REPLIED);
            // O'zgartirilgan statusni ma'lumotlar bazasiga saqlaymiz.
            contactMessageRepository.save(message);

        } catch (MessagingException e) {
            // Agar elektron pochta yuborishda `MessagingException` yuz bersa, uni ushlab olib, yangi `IllegalStateException` tashlaymiz.
            throw new IllegalStateException("Email yuborishda xatolik yuz berdi", e);
        }
    }
}
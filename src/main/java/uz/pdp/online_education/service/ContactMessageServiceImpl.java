package uz.pdp.online_education.service;

import  jakarta.mail.MessagingException;
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

@Service
@RequiredArgsConstructor
public class ContactMessageServiceImpl implements ContactMessageService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final ContactMessageRepository contactMessageRepository;
    private final ContactMessageMapper contactMessageMapper;

    @Override
    public void create(ContactMessageRequestDTO dto) {
        ContactMessage message = contactMessageMapper.toEntity(dto);
        message.setStatus(MessageStatus.NEW);
        contactMessageRepository.save(message);
    }

    @Override
    public List<ContactMessageResponseDTO> getAllExceptReplied() {
        return contactMessageRepository.findAllByStatusNot(MessageStatus.REPLIED)
                .stream()
                .map(contactMessageMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ContactMessageResponseDTO> getAll() {
        return contactMessageRepository.findAll()
                .stream()
                .map(contactMessageMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ContactMessageResponseDTO getByIdAndMarkRead(Long id) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ContactMessage not found"));
        message.setStatus(MessageStatus.READ);
        return contactMessageMapper.toDto(message);
    }

    @Override
    public void edit(Long id, ContactMessageRequestDTO dto){
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ContactMessage not found"));


        if (message.getStatus() == MessageStatus.REPLIED)
            throw new RuntimeException("Cannot edit a replied message");

        contactMessageMapper.updateEntityFromDto(dto, message);
        contactMessageRepository.save(message);
    }

    @Override
    public void delete(Long id, String requesterEmail) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ContactMessage not found"));

        // Faqat o'ziga tegishli bo'lgan message'ni delete qilishga ruxsat beriladi
        if (!message.getEmail().equals(requesterEmail)) {
            throw new RuntimeException("You can only delete your own messages");
        }

        // Agar message REPLIED bo'lsa, o'chirishga ruxsat berilmaydi
        if (message.getStatus() == MessageStatus.REPLIED) {
            throw new RuntimeException("Cannot delete a replied message");
        }

        contactMessageRepository.delete(message);
    }


    @Override
    public void replyToMessage(Long id, String replyText) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ContactMessage not found"));
        try {
        Context context = new Context();
        context.setVariable("fullName", message.getFullName());
        context.setVariable("userMessage", message.getMessage());
        context.setVariable("reply  Text", replyText);

        String htmlContent = templateEngine.process("contact_reply", context);


            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(message.getEmail());
            helper.setSubject("Javobingiz - Online Education Support");
            helper.setText(htmlContent, true);
            helper.setFrom("noreply@online-education.com");

            mailSender.send(mimeMessage);

            message.setStatus(MessageStatus.REPLIED);
            contactMessageRepository.save(message);

        } catch (MessagingException e) {
            throw new IllegalStateException("Email yuborishda xatolik yuz berdi", e);
        }
    }



}

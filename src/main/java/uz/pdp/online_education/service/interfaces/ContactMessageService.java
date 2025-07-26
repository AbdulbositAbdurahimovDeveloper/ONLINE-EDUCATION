package uz.pdp.online_education.service.interfaces;

import uz.pdp.online_education.payload.ContactMessageRequestDTO;
import uz.pdp.online_education.payload.ContactMessageResponseDTO;

import java.util.List;

public interface ContactMessageService {
    void create(ContactMessageRequestDTO dto);

    List<ContactMessageResponseDTO> getAllExceptReplied();

    List<ContactMessageResponseDTO> getAll();

    ContactMessageResponseDTO getByIdAndMarkRead(Long id);

    void edit(Long id, String requesterEmail, ContactMessageRequestDTO dto);


    public String replyToMessage(Long id, String replyText);

    void delete(Long id, String requesterEmail);

}

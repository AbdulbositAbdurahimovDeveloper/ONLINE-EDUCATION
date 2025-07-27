package uz.pdp.online_education.service.interfaces;

import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.ContactMessageRequestDTO;
import uz.pdp.online_education.payload.ContactMessageResponseDTO;

import java.util.List;

public interface ContactMessageService {
    void create(ContactMessageRequestDTO dto);

    List<ContactMessageResponseDTO> getAllExceptReplied();

    List<ContactMessageResponseDTO> getAll();

    ContactMessageResponseDTO getByIdAndMarkRead(Long id);

    void edit(Long id,  ContactMessageRequestDTO dto);


    public void replyToMessage(Long id, String replyText);

    void delete(Long id, String requesterEmail);

}

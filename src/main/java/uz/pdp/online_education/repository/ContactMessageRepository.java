package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.online_education.enums.MessageStatus;
import uz.pdp.online_education.model.ContactMessage;

import java.util.List;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
    List<ContactMessage> findAllByStatusNot(MessageStatus status);

}

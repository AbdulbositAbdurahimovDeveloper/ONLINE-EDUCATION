package uz.pdp.online_education.service.interfaces;


import uz.pdp.online_education.payload.faq.FaqDTO;
import uz.pdp.online_education.payload.faq.FaqRequestDTO;

import java.util.List;

public interface FaqService {
    FaqDTO create(FaqRequestDTO requestDTO);
    FaqDTO update(Long id, FaqRequestDTO requestDTO);
    void delete(Long id);
    List<FaqDTO> getAll();
    FaqDTO getById(Long id);
    void swapDisplayOrder(Long faqId, int newDisplayOrder);

}

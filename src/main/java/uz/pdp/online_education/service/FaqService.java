package uz.pdp.online_education.service;

import uz.pdp.online_education.payload.FaqDTO;
import uz.pdp.online_education.payload.FaqRequestDTO;

import java.util.List;

public interface FaqService {
    List<FaqDTO> getAll();
    FaqDTO getById(Long id);
    FaqDTO create(FaqRequestDTO dto);
    FaqDTO update(Long id, FaqRequestDTO dto);
    void delete(Long id);
}

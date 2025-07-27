package uz.pdp.online_education.service.interfaces;

import uz.pdp.online_education.payload.text.TextContentCreateDTO;
import uz.pdp.online_education.payload.text.TextContentResponseDTO;
import uz.pdp.online_education.payload.text.TextContentUpdateDTO;

public interface TextContentService {

    TextContentResponseDTO create(TextContentCreateDTO createDTO);
    TextContentResponseDTO getById(Long id);
    TextContentResponseDTO update(Long id, TextContentUpdateDTO updateDTO);
    void delete(Long id);
}

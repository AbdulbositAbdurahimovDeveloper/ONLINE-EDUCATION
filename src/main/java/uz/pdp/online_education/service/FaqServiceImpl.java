package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.mapper.FaqMapper;
import uz.pdp.online_education.model.Faq;
import uz.pdp.online_education.payload.FaqDTO;
import uz.pdp.online_education.payload.FaqRequestDTO;
import uz.pdp.online_education.repository.FaqRepository;
import uz.pdp.online_education.service.interfaces.FaqService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FaqServiceImpl implements FaqService {

    private final FaqRepository faqRepository;
    private final FaqMapper faqMapper;

    @Override
    public List<FaqDTO> getAll() {
        List<Faq> faqList = faqRepository.findAll();
        log.info("Faq list requested. Size: {}", faqList.size());
        return faqMapper.toDtoList(faqList);
    }

    @Override
    public FaqDTO getById(Long id) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Faq not found with id: {}", id);
                    return new EntityNotFoundException("Faq not found with id: " + id);
                });
        log.info("Faq retrieved with id: {}", id);
        return faqMapper.toDto(faq);
    }

    @Override
    public FaqDTO create(FaqRequestDTO dto) {
        Faq faq = faqMapper.toEntity(dto);
        faq = faqRepository.save(faq);
        log.info("Faq created with id: {}", faq.getId());
        return faqMapper.toDto(faq);
    }

    @Override
    public FaqDTO update(Long id, FaqRequestDTO dto) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Faq not found for update with id: {}", id);
                    return new EntityNotFoundException("Faq not found with id: " + id);
                });

        faqMapper.updateFaqFromDto(dto, faq);
        faq = faqRepository.save(faq);
        log.info("Faq updated with id: {}", faq.getId());
        return faqMapper.toDto(faq);
    }

    @Override
    public void delete(Long id) {
        if (!faqRepository.existsById(id)) {
            log.warn("Faq not found for delete with id: {}", id);
            throw new EntityNotFoundException("Faq not found with id: " + id);
        }
        faqRepository.deleteById(id);
        log.info("Faq deleted with id: {}", id);
    }
}

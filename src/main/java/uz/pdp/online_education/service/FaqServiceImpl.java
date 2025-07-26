package uz.pdp.online_education.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.mapper.FaqMapper;
import uz.pdp.online_education.model.Faq;
import uz.pdp.online_education.payload.faq.FaqDTO;
import uz.pdp.online_education.payload.faq.FaqRequestDTO;
import uz.pdp.online_education.repository.FaqRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FaqServiceImpl implements FaqService {

    private final FaqRepository faqRepository;
    private final FaqMapper faqMapper;

    @Override
    public FaqDTO create(FaqRequestDTO requestDTO) {
        Faq faq = faqMapper.toEntity(requestDTO);
        int maxDisplayOrder = faqRepository.getMaxDisplayOrder();
        faq.setDisplayOrder(maxDisplayOrder + 1);
        faqRepository.save(faq);
        log.info("FAQ created with id={}, displayOrder={}", faq.getId(), faq.getDisplayOrder());
        return faqMapper.toDto(faq);
    }


    @Override
    public FaqDTO update(Long id, FaqRequestDTO requestDTO) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("FAQ not found"));
        faqMapper.updateFaqFromDto(requestDTO, faq);
        faqRepository.save(faq);
        log.info("FAQ updated with id={}", id);
        return faqMapper.toDto(faq);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Faq not found with id: " + id));

        // 1. Avval Faq ni o'chiramiz
        faqRepository.delete(faq);

        // 2. Qolgan faqlarni displayOrder bo'yicha sortlab olamiz
        List<Faq> remainingFaqs = faqRepository.findAll(Sort.by(Sort.Direction.ASC, Faq.Fields.displayOrder));

        // 3. Har birining displayOrder ni yangilaymiz: 0 dan boshlanadi
        for (int i = 0; i < remainingFaqs.size(); i++) {
            Faq f = remainingFaqs.get(i);
            f.setDisplayOrder(i);
        }

        // 4. Hammasini saqlaymiz
        faqRepository.saveAll(remainingFaqs);
    }


    @Override
    public List<FaqDTO> getAll() {

        reorderDisplayOrders();
        Sort sort = Sort.by(Sort.Direction.ASC,Faq.Fields.displayOrder);
        List<Faq> allFaqs = faqRepository.findAll(sort);


        return faqMapper.toDtoList(allFaqs);
    }

    @Override
    public FaqDTO getById(Long id) {
        reorderDisplayOrders();
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("FAQ not found"));
        return faqMapper.toDto(faq);
    }


    @Override
    public void swapDisplayOrder(Long faqId, int newDisplayOrder) {
        Faq currentFaq = faqRepository.findById(faqId)
                .orElseThrow(() -> new EntityNotFoundException("Faq not found by id: " + faqId));

        Faq targetFaq = faqRepository.findByDisplayOrderAndDeletedFalse(newDisplayOrder)
                .orElseThrow(() -> new EntityNotFoundException("Faq not found at displayOrder: " + newDisplayOrder));

        int oldOrder = currentFaq.getDisplayOrder();
        currentFaq.setDisplayOrder(newDisplayOrder);
        targetFaq.setDisplayOrder(oldOrder);

        faqRepository.save(currentFaq);
        faqRepository.save(targetFaq);

        log.info("Swapped displayOrder between faqId={} and faqId={}", currentFaq.getId(), targetFaq.getId());
    }

    private void reorderDisplayOrders() {
        List<Faq> activeFaqs = faqRepository.findAllByDeletedFalseOrderByDisplayOrderAsc();

        for (int i = 0; i < activeFaqs.size(); i++) {
            activeFaqs.get(i).setDisplayOrder(i);
        }

        faqRepository.saveAll(activeFaqs);
    }

}

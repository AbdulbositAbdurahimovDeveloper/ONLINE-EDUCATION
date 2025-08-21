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
import uz.pdp.online_education.service.interfaces.FaqService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FaqServiceImpl implements FaqService {

    private final FaqRepository faqRepository;
    private final FaqMapper faqMapper;

    /**
     * Yangi FAQ (Ko'p so'raladigan savollar) yaratish metodi.
     * Yaratilayotgan FAQ ga avtomatik ravishda keyingi bo'sh `displayOrder` qiymatini beradi.
     *
     * @param requestDTO FAQ yaratish uchun kerakli ma'lumotlarni o'z ichiga olgan DTO
     * @return Yaratilgan FAQ ning DTO ko'rinishi
     */
    @Override
    public FaqDTO create(FaqRequestDTO requestDTO) {
        Faq faq = faqMapper.toEntity(requestDTO);
        int maxDisplayOrder = faqRepository.getMaxDisplayOrder();
        faq.setDisplayOrder(maxDisplayOrder + 1);
        faqRepository.save(faq);
        log.info("FAQ created with id={}, displayOrder={}", faq.getId(), faq.getDisplayOrder());
        return faqMapper.toDto(faq);
    }

    /**
     * Mavjud FAQ ni berilgan ID bo'yicha yangilash metodi.
     *
     * @param id         Yangilanishi kerak bo'lgan FAQ ning ID si
     * @param requestDTO Yangilash uchun yangi ma'lumotlarni o'z ichiga olgan DTO
     * @return Yangilangan FAQ ning DTO ko'rinishi
     * @throws EntityNotFoundException Agar berilgan ID bilan FAQ topilmasa
     */
    @Override
    public FaqDTO update(Long id, FaqRequestDTO requestDTO) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("FAQ not found"));
        faqMapper.updateFaqFromDto(requestDTO, faq);
        faqRepository.save(faq);
        log.info("FAQ updated with id={}", id);
        return faqMapper.toDto(faq);
    }

    /**
     * FAQ ni o'chirish va qolgan FAQ larning displayOrder tartibini yangilash metodi.
     * Bu metod transaksionel bo'lib, o'chirish va tartibni yangilash bir butunlikda bajariladi.
     *
     * @param id O'chirilishi kerak bo'lgan FAQ ning ID si
     * @throws EntityNotFoundException Agar berilgan ID bilan FAQ topilmasa
     */
    @Override
    @Transactional
    public void delete(Long id) {
        // 1. FAQ ni topamiz, agar topilmasa istisno tashlaymiz
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Faq not found with id: " + id));

        // 2. FAQ ni ma'lumotlar bazasidan o'chiramiz
        faqRepository.delete(faq);

        // 3. Qolgan (o'chirilmagan) FAQ larni displayOrder bo'yicha o'suvchi tartibda olamiz
        // Qaydnoma: Agar Faq modelida "deleted" degan boolean maydon bo'lsa, uni ham filterlash kerak.
        // Agar 'deleted' maydoni bo'lmasa, faqRepository.findAll(Sort.by(Sort.Direction.ASC, Faq.Fields.displayOrder)); kabi bo'lishi kerak.
        List<Faq> remainingFaqs = faqRepository.findAll(Sort.by(Sort.Direction.ASC, Faq.Fields.displayOrder));

        // 4. Qolgan har bir FAQ ning displayOrder tartibini 0 dan boshlab yangilaymiz
        for (int i = 0; i < remainingFaqs.size(); i++) {
            Faq f = remainingFaqs.get(i);
            f.setDisplayOrder(i);
        }

        // 5. Yangilangan displayOrder larni ma'lumotlar bazasiga saqlaymiz
        faqRepository.saveAll(remainingFaqs);

        log.info("FAQ deleted with id={}", id);
    }

    /**
     * Barcha FAQ larni displayOrder bo'yicha saralab, DTO ko'rinishida qaytarish metodi.
     * Bu metod chaqirilganda, avval qolgan FAQ larning displayOrder tartibi ham yangilanadi.
     *
     * @return Barcha FAQ larning DTO ko'rinishidagi listi
     */
    @Override
    public List<FaqDTO> getAll() {

        reorderDisplayOrders();

        Sort sort = Sort.by(Sort.Direction.ASC, Faq.Fields.displayOrder);
        List<Faq> allFaqs = faqRepository.findAll(sort);

        return faqMapper.toDtoList(allFaqs);
    }

    /**
     * Berilgan ID bo'yicha FAQ ni topish va DTO ko'rinishida qaytarish metodi.
     * Bu metod ham chaqirilganda, qolgan FAQ larning displayOrder tartibini yangilaydi.
     *
     * @param id Qidirilayotgan FAQ ning ID si
     * @return Topilgan FAQ ning DTO ko'rinishi
     * @throws EntityNotFoundException Agar berilgan ID bilan FAQ topilmasa
     */
    @Override
    public FaqDTO getById(Long id) {
        reorderDisplayOrders();
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("FAQ not found"));
        return faqMapper.toDto(faq);
    }

    /**
     * Ikki FAQ ning displayOrder tartibini almashtirish metodi.
     * Bu metod ma'lum bir FAQ ning joyini o'zgartirish uchun ishlatiladi.
     *
     * @param faqId Yangi joyga ko'chirilishi kerak bo'lgan FAQ ning ID si
     * @param newDisplayOrder FAQ ning ko'chirilishi kerak bo'lgan yangi tartib raqami
     * @throws EntityNotFoundException Agar `faqId` yoki `newDisplayOrder` ga mos FAQ topilmasa
     */
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

    /**
     * Faqat o'chirilmagan FAQ larni olib, ularning displayOrder tartibini
     * ketma-ket (0, 1, 2, ...) qilib qayta nomerlash metodi.
     * Bu metod ichki (private) bo'lib, faqat boshqa servis metodlari tomonidan chaqiriladi.
     */
    private void reorderDisplayOrders() {
        List<Faq> activeFaqs = faqRepository.findAllByDeletedFalseOrderByDisplayOrderAsc();

        for (int i = 0; i < activeFaqs.size(); i++) {
            activeFaqs.get(i).setDisplayOrder(i);
        }

        faqRepository.saveAll(activeFaqs);
    }
}
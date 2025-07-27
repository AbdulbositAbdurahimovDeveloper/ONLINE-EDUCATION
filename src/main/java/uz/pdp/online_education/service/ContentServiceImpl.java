package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.exceptions.DataConflictException;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.model.lesson.Content;
import uz.pdp.online_education.model.lesson.Lesson;
import uz.pdp.online_education.repository.ContentRepository;
import uz.pdp.online_education.service.interfaces.ContentService;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

    private final ContentRepository contentRepository;

    @Transactional
    @Override
    public void deleteContent(Long id) {
        Content contentToDelete = contentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Content not found with id: " + id));

        Lesson lesson = contentToDelete.getLesson();
        int blockOrderOfDeleted = contentToDelete.getBlockOrder();


        contentRepository.delete(contentToDelete);

        contentRepository.shiftBlockOrdersAfterDelete(lesson.getId(), blockOrderOfDeleted);
    }

//    @Override
//    @Transactional
//    public void updateAllContentOrders(Long lessonId, List<Long> orderedContentIds) {
//        // 1. Tekshiruvlar o'zgarishsiz qoladi. Bu hali ham muhim.
//        List<Content> contentsInDb = contentRepository.findAllByLessonId(lessonId);
//
//        if (orderedContentIds.size() != contentsInDb.size()) {
//            throw new IllegalStateException("The number of sent IDs does not match the number of contents in the lesson.");
//        }
//        if (!contentsInDb.stream().map(Content::getId).collect(Collectors.toSet()).equals(new HashSet<>(orderedContentIds))) {
//            throw new IllegalStateException("The provided content IDs are invalid or do not match the lesson's contents.");
//        }
//
//        // 2. Ma'lumotlarni Repository kutayotgan formatga o'tkazamiz.
//        // Bizga List<Object[2]> kerak: [[id1, 0], [id2, 1], [id3, 2], ...]
//        List<Object[]> batchArgs = IntStream.range(0, orderedContentIds.size())
//                .mapToObj(i -> new Object[]{orderedContentIds.get(i), i})
//                .collect(Collectors.toList());
//
//        // Agar yuboriladigan ma'lumotlar bo'lsa...
//        if (!batchArgs.isEmpty()) {
//            // 3. Bitta so'rov bilan barcha o'zgarishlarni bazaga yuboramiz.
//            contentRepository.updateAllOrdersInBatch(batchArgs);
//        }
//    }


    @Transactional
    @Override
    public void updateAllContentOrders(Long lessonId, List<Long> orderedContentIds) {
        // 1. O'sha darsga tegishli barcha kontentlarni bazadan bitta so'rov bilan olamiz.
        List<Content> contentsInDb = contentRepository.findAllByLessonId(lessonId);

        // 2. Xavfsizlik va yaxlitlik tekshiruvlari
        if (orderedContentIds.size() != contentsInDb.size()) {
            throw new DataConflictException("The number of sent IDs does not match the number of contents in the lesson.");
        }

        Map<Long, Content> contentMap = contentsInDb.stream()
                .collect(Collectors.toMap(Content::getId, Function.identity()));

        if (!contentMap.keySet().equals(new HashSet<>(orderedContentIds))) {
            throw new DataConflictException("The provided content IDs are invalid or do not match the lesson's contents.");
        }

        // 3. Yangi tartib raqamlarini ('blockOrder') o'rnatamiz.
        // Bu eng sodda, eng xavfsiz va eng to'g'ri qism.
        for (int i = 0; i < orderedContentIds.size(); i++) {
            Long contentId = orderedContentIds.get(i);
            Content contentToUpdate = contentMap.get(contentId);
            contentToUpdate.setBlockOrder(i);
        }

        // @Transactional tufayli Hibernate o'zgargan barcha obyektlarni
        // tranzaksiya oxirida avtomatik ravishda UPDATE qiladi.
    }
}

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




    @Transactional
    @Override
    public void updateAllContentOrders(Long lessonId, List<Long> orderedContentIds) {

        List<Content> contentsInDb = contentRepository.findAllByLessonId(lessonId);


        if (orderedContentIds.size() != contentsInDb.size()) {
            throw new DataConflictException("The number of sent IDs does not match the number of contents in the lesson.");
        }

        Map<Long, Content> contentMap = contentsInDb.stream()
                .collect(Collectors.toMap(Content::getId, Function.identity()));

        if (!contentMap.keySet().equals(new HashSet<>(orderedContentIds))) {
            throw new DataConflictException("The provided content IDs are invalid or do not match the lesson's contents.");
        }


        for (int i = 0; i < orderedContentIds.size(); i++) {
            Long contentId = orderedContentIds.get(i);
            Content contentToUpdate = contentMap.get(contentId);
            contentToUpdate.setBlockOrder(i);
        }


    }
}

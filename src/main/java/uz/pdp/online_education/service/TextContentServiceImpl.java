package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.model.lesson.Lesson;
import uz.pdp.online_education.model.lesson.TextContent;
import uz.pdp.online_education.mapper.TextContentMapper;
import uz.pdp.online_education.payload.text.TextContentCreateDTO;
import uz.pdp.online_education.payload.text.TextContentResponseDTO;
import uz.pdp.online_education.payload.text.TextContentUpdateDTO;
import uz.pdp.online_education.repository.ContentRepository;
import uz.pdp.online_education.repository.LessonRepository;
import uz.pdp.online_education.repository.TextContentRepository;
import uz.pdp.online_education.service.interfaces.ContentService;
import uz.pdp.online_education.service.interfaces.TextContentService;


@Service
@RequiredArgsConstructor
public class TextContentServiceImpl implements TextContentService {


    private final LessonRepository lessonRepository;
    private final TextContentRepository textContentRepository;
    private final TextContentMapper textContentMapper;
    private final ContentRepository contentRepository;
    private final ContentService contentService;

    @Transactional
    @Override
    public TextContentResponseDTO create(TextContentCreateDTO createDTO) {
        Lesson lesson = lessonRepository.findById(createDTO.getLessonId()).orElseThrow(
                () -> new EntityNotFoundException("Lesson not found with id: " + createDTO.getLessonId()));


        TextContent textContent = new TextContent();
        textContent.setBlockOrder(lesson.getContents().size());
        textContent.setText(createDTO.getText());
        textContent.setLesson(lesson);
        TextContent save = textContentRepository.save(textContent);

        System.out.println(save);

        return textContentMapper.toDTO(textContent);
    }

    @Override
    public TextContentResponseDTO getById(Long id) {
        TextContent textContent = textContentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Text content not found with id: " + id));

        return textContentMapper.toDTO(textContent);
    }

    @Transactional
    @Override
    public TextContentResponseDTO update(Long id, TextContentUpdateDTO updateDTO) {
        TextContent textContent = textContentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Text content not found with id: " + id));

        if (updateDTO.getText() != null) {
            textContent.setText(updateDTO.getText());
        }

        textContentRepository.save(textContent);
        return textContentMapper.toDTO(textContent);
    }

    @Override
    public void delete(Long id) {
        contentService.deleteContent(id);
    }
}

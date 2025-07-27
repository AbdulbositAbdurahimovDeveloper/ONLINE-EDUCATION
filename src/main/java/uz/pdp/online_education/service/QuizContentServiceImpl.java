package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.mapper.QuizContentMapper;
import uz.pdp.online_education.model.lesson.Lesson;
import uz.pdp.online_education.model.lesson.QuizContent;
import uz.pdp.online_education.model.quiz.Quiz;
import uz.pdp.online_education.payload.quiz.QuizContentCreateDTO;
import uz.pdp.online_education.payload.quiz.QuizContentResponseDTO;
import uz.pdp.online_education.payload.quiz.QuizUpdateDTO;
import uz.pdp.online_education.repository.LessonRepository;
import uz.pdp.online_education.repository.QuizContentRepository;
import uz.pdp.online_education.service.interfaces.ContentService;
import uz.pdp.online_education.service.interfaces.QuizContentService;

@Service
@RequiredArgsConstructor
public class QuizContentServiceImpl implements QuizContentService {


    private final LessonRepository lessonRepository;
    private final QuizContentRepository quizContentRepository;
    private final QuizContentMapper quizContentMapper;
    private final ContentService contentService;

    @Override
    public QuizContentResponseDTO getById(Long contentId) {
        return quizContentRepository.findById(contentId)
                .map(quizContentMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Quiz content not found with id: " + contentId));
    }

    @Override
    @Transactional
    public QuizContentResponseDTO create(QuizContentCreateDTO createDTO) {

        Lesson lesson = lessonRepository.findById(createDTO.getLessonId())
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found"));

        Quiz quiz = new Quiz();
        quiz.setTitle(createDTO.getQuizTitle());
        quiz.setDescription(createDTO.getQuizDescription());

        QuizContent quizContent = new QuizContent();
        quizContent.setBlockOrder(lesson.getContents().size());
        quizContent.setQuiz(quiz);
        quizContent.setLesson(lesson);

        QuizContent savedQuizContent = quizContentRepository.save(quizContent);

        return quizContentMapper.toDto(savedQuizContent);


    }

    @Override
    @Transactional
    public QuizContentResponseDTO update(Long contentId, QuizUpdateDTO updateDTO) {

        QuizContent quizContent = quizContentRepository.findById(contentId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz content not found with id: " + contentId));

        Quiz quiz = quizContent.getQuiz();
        quiz.setTitle(updateDTO.getTitle());
        quiz.setDescription(updateDTO.getDescription());

        return quizContentMapper.toDto(quizContentRepository.save(quizContent));
    }

    @Override
    @Transactional
    public void delete(Long contentId) {
        contentService.deleteContent(contentId);
    }
}

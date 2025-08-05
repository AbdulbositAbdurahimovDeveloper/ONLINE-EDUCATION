package uz.pdp.online_education.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import uz.pdp.online_education.service.interfaces.LessonService;
import uz.pdp.online_education.service.interfaces.ModuleService;
import uz.pdp.online_education.service.interfaces.QuestionService;


@Component("courseSecurity")
@RequiredArgsConstructor
public class ModuleSecurity {

    private final ModuleService moduleService;
    private final LessonService lessonService;
    private final QuestionService questionService;


    public boolean isUserEnrolled(Authentication authentication, Long courseId) {
        String username = authentication.getName();
        return moduleService.isUserEnrolled(username, courseId);
//        return true;
    }

    public boolean isUserModuleBought(Authentication authentication, Long attachmentContentId) {
        String username = authentication.getName();
        return moduleService.isUserModuleBought(username, attachmentContentId);
//        return true;
    }

    public boolean isPaymentOrFreeLesson(Authentication authentication, Long lessonId) {
        String username = authentication.getName();
        return lessonService.isPaymentOrFreeLesson(username, lessonId);
    }

    public boolean isUserQuestionBought(Authentication authentication, Long quizId) {
        String username = authentication.getName();
        return questionService.isUserQuestionBought(username,quizId);
    }
}

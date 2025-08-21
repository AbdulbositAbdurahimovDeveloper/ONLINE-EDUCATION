package uz.pdp.online_education.telegram.service.instructor.template;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

public interface InstructorReplyKeyboardService {
    ReplyKeyboardMarkup buildMentorMenu();
}

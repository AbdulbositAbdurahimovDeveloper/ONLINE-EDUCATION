package uz.pdp.online_education.service;

import org.springframework.scheduling.annotation.Async;

public interface TelegramUploadTaskService {

    void uploadAttachmentToTelegram(Long attachmentId);
}

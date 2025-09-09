package uz.pdp.online_education.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.model.Attachment;
import uz.pdp.online_education.repository.AttachmentRepository;
import uz.pdp.online_education.service.interfaces.AttachmentService;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

@Slf4j
@Service
public class TelegramUploadTaskServiceImpl implements TelegramUploadTaskService {

    private final AttachmentRepository attachmentRepository;
    private final AttachmentService attachmentService;
    private final MinioClient minioClient;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.channel-id}")
    private String CHANNEL_ID;

    public TelegramUploadTaskServiceImpl(@Lazy AttachmentRepository attachmentRepository, @Lazy AttachmentService attachmentService, MinioClient minioClient) {
        this.attachmentRepository = attachmentRepository;
        this.attachmentService = attachmentService;
        this.minioClient = minioClient;
    }

    @Async
    @Override
    public void uploadAttachmentToTelegram(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Attachment with id " + attachmentId + " not found"));

        try {
            // 1. MinIO’dan fayl oqimini olish
            try (InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(attachment.getBucketName())
                            .object(attachment.getMinioKey())
                            .build())) {

                // 2. Faylni vaqtinchalik saqlash
                File tempFile = File.createTempFile("video-", ".mp4");
                Files.copy(stream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // 3. Telegram API ga yuborish (multipart/form-data)
                String apiUrl = "https://api.telegram.org/bot" + botToken + "/sendVideo";

                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("chat_id", CHANNEL_ID);
                body.add("video", new FileSystemResource(tempFile));

                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, body, String.class);

                // 4. Telegram’dan qaytgan javobni qayta ishlash
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode root = new ObjectMapper().readTree(response.getBody());
                    JsonNode result = root.path("result").path("video").path("file_id");
                    if (!result.isMissingNode()) {
                        String telegramFileId = result.asText();
                        attachment.setTelegramFileId(telegramFileId);
                        attachmentRepository.save(attachment);
                    }
                }

                // 5. Vaqtinchalik faylni o‘chirish
                tempFile.delete();

            }
        } catch (Exception e) {
            log.error("Telegramga video yuklashda xatolik: {}", e.getMessage(), e);
        }
    }



    // getFileIdFromMessage metodini shu yerga ham ko'chirish
    private String getFileIdFromMessage(Message message) {
        if (message.hasPhoto()) {
            return message.getPhoto().stream().max(Comparator.comparing(PhotoSize::getFileSize)).get().getFileId();
        } else if (message.hasVideo()) {
            return message.getVideo().getFileId();
        } else if (message.hasDocument()) {
            return message.getDocument().getFileId();
        }
        return null;
    }
}
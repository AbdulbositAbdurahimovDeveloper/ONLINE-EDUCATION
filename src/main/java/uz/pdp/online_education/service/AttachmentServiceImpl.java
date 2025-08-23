package uz.pdp.online_education.service;

import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.online_education.config.properties.MinioProperties;
import uz.pdp.online_education.exceptions.DataConflictException;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.mapper.AttachmentMapper;
import uz.pdp.online_education.model.Attachment;
import uz.pdp.online_education.payload.content.attachmentContent.AttachmentContentCreateDTO;
import uz.pdp.online_education.payload.content.attachmentContent.AttachmentDTO;
import uz.pdp.online_education.repository.AttachmentRepository;
import uz.pdp.online_education.service.interfaces.AttachmentContentService;
import uz.pdp.online_education.service.interfaces.AttachmentService;
import uz.pdp.online_education.telegram.config.controller.OnlineEducationBot;
import uz.pdp.online_education.telegram.mapper.SendMsg;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Slf4j
@Service
//@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final AttachmentMapper attachmentMapper;
    private final MinioClient minioClient;
    private final MinioProperties minio;
    private final OnlineEducationBot bot;
    private final SendMsg sendMsg;
    private final AttachmentContentService attachmentContentService;

    @Value("${telegram.bot.channel-id}")
    private String CHANNEL_ID;

    // Ruxsat etilgan rasm turlari ro'yxati
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            MediaType.IMAGE_JPEG_VALUE, // "image/jpeg"
            MediaType.IMAGE_PNG_VALUE,  // "image/png"
            "image/svg+xml"             // SVG uchun
    );


    public AttachmentServiceImpl(AttachmentRepository attachmentRepository,
                                 AttachmentMapper attachmentMapper,
                                 MinioClient minioClient,
                                 MinioProperties minio, OnlineEducationBot bot, SendMsg sendMsg, AttachmentContentService attachmentContentService) {
        this.attachmentRepository = attachmentRepository;
        this.attachmentMapper = attachmentMapper;
        this.minioClient = minioClient;
        this.minio = minio;
        this.bot = bot;
        this.sendMsg = sendMsg;
        this.attachmentContentService = attachmentContentService;
    }

    @Override
    public AttachmentDTO read(Long id) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found with id: " + id));
        return attachmentMapper.toDTO(attachment);
    }

    // AttachmentService.java ichida

    /**
     * Telegram'dan kelgan rasmni to'liq qayta ishlaydi: MinIO'ga yuklaydi, DB'ga saqlaydi
     * va kanalga yuboradi.
     *
     * @param photoList Telegramdan kelgan PhotoSize obyektlari ro'yxati.
     * @return Saqlangan Attachment ma'lumotlari bilan DTO.
     */
    public AttachmentDTO saveTg(List<PhotoSize> photoList) {
        if (photoList == null || photoList.isEmpty()) {
            throw new IllegalArgumentException("Photo list cannot be null or empty.");
        }

        // Eng katta o'lchamdagi rasmni tanlab olamiz
        PhotoSize largestPhoto = photoList.stream()
                .max(Comparator.comparing(PhotoSize::getFileSize))
                .orElseThrow(() -> new IllegalArgumentException("Could not find a valid photo in the list."));

        // 1. Rasmni MinIO'ga yuklaymiz (yangi metodimizni chaqiramiz)
        String minioKey = saveTgPhotoToMinio(largestPhoto);

        // 2. Ma'lumotlarni bazaga saqlaymiz
        Attachment attachment = new Attachment();
        try {
            attachment.setOriginalName(new java.io.File(bot.execute(new GetFile(largestPhoto.getFileId())).getFilePath()).getName()); // Haqiqiy nomni olish
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        attachment.setContentType("image/jpeg");
        attachment.setFileSize(Long.valueOf(largestPhoto.getFileSize()));
        attachment.setMinioKey(minioKey);
        attachment.setBucketName(minio.getBuckets().get(0));
        attachment.setTelegramFileId(largestPhoto.getFileId());

        Attachment savedAttachment = attachmentRepository.save(attachment);

        // 3. Rasmni kanalga yuboramiz (file_id orqali, bu tezroq)
        sendPhotoToChannel(savedAttachment.getTelegramFileId());

        // 4. DTO ni qaytaramiz
        return attachmentMapper.toDTO(savedAttachment);
    }

    /**
     * Foydalanuvchi yuborgan videoni qayta ishlash jarayonini asinxron tarzda boshlaydi.
     * Bu metod darhol qaytadi va serverning asosiy thread'ini bloklamaydi.
     * Og'ir ishlar orqa fonda bajariladi.
     *
     * @param video  Telegramdan kelgan Video obyekti.
     * @param chatId
     */
    @Override
    public void saveTgVideoAsync(Video video, Long chatId,Long lessonId) {
        if (video == null) {
            log.warn("Attempted to process a null video object.");
            return;
        }

        log.info("Starting asynchronous processing for video file_id: {}", video.getFileId());
        // Asosiy, @Async annotatsiyasi bor metodni chaqiramiz.
        // Spring bu chaqiruvni tutib olib, uni alohida thread'da bajaradi.
        processAndSaveTgVideo(video, chatId,lessonId);
    }

    /**
     * Videoni to'liq qayta ishlaydigan asosiy asinxron metod.
     * BU METOD BOSHQQA KLASDAN CHAQIRILGANDA ASINXRON ISHLAYDI. Agar shu klass ichidan chaqirilsa,
     * oddiy metod kabi ishlaydi. Shuning uchun `saveTgVideoAsync` yordamchi metodini yaratdik.
     *
     * @param video  Qayta ishlanishi kerak bo'lgan Video obyekti.
     * @param chatId
     */
    @Async
    @Transactional // DB operatsiyalari uchun tranzaksiyani boshqaradi
    public void processAndSaveTgVideo(Video video, Long chatId,Long lessonId) {
        try {
            // 1. Fayl haqidagi ma'lumotni Telegramdan olamiz.
            org.telegram.telegrambots.meta.api.objects.File telegramFile = bot.execute(new GetFile(video.getFileId()));

            // 2. Faylni MinIO'ga yuklaymiz.
            String minioKey = saveTgFileToMinio(telegramFile, video.getMimeType(), video.getFileSize());

            // 3. Ma'lumotlarni bazaga saqlaymiz.
            Attachment attachment = new Attachment();
            attachment.setOriginalName(video.getFileName());
            attachment.setContentType(video.getMimeType());
            attachment.setFileSize(video.getFileSize());
            attachment.setMinioKey(minioKey);
            attachment.setBucketName(minio.getBuckets().get(0));
            attachment.setTelegramFileId(video.getFileId());

            Attachment saved = attachmentRepository.save(attachment);

            attachmentContentService.create(new AttachmentContentCreateDTO(lessonId, saved.getId()));
            bot.myExecute(sendMsg.sendMessage(chatId, buildAttachmentText(saved)));

            // 4. Videoni kanalga yuboramiz.
            sendVideoToChannel(video.getFileId(), video.getFileName());

            log.info("Successfully processed and saved video file_id: {}", video.getFileId());

        } catch (Exception e) {
            log.error("Failed to process video file_id: {} in background.", video.getFileId(), e);
            // Bu yerda xatolik haqida adminlarga xabar yuborish logikasi bo'lishi mumkin.
        }
    }

    private String buildAttachmentText(Attachment a) {
        String name = a.getOriginalName() == null ? "-" : a.getOriginalName();
        String type = a.getContentType() == null ? "-" : a.getContentType();
        String bucket = a.getBucketName() == null ? "-" : a.getBucketName();
        String key = a.getMinioKey() == null ? "-" : a.getMinioKey();
        String tg = a.getTelegramFileId() == null ? "❌ Yo‘q" : "✅ Mavjud";

        String size = a.getFileSize() == null ? "-" : humanReadableSize(a.getFileSize());

        return """
                📎 <b>Fayl haqida</b>
                
                🆔 ID: <code>%d</code>
                🏷️ Nomi: <b>%s</b>
                📂 Turi: %s
                📏 Hajmi: %s
                🪣 Bucket: <code>%s</code>
                🔑 Minio key: <code>%s</code>
                📲 Telegramda: %s
                """.formatted(
                a.getId(), name, type, size, bucket, key, tg
        );
    }

    private String humanReadableSize(Long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }


    /**
     * Har qanday Telegram faylini (rasm, video, hujjat) MinIO'ga yuklaydigan universal metod.
     *
     * @param telegramFile Telegram API'dan olingan File obyekti.
     * @param mimeType     Faylning MIME turi.
     * @param fileSize     Faylning hajmi.
     * @return MinIO'ga saqlangan obyektning unikal kaliti (key).
     */
    private String saveTgFileToMinio(org.telegram.telegrambots.meta.api.objects.File telegramFile, String mimeType, Long fileSize) {
        try (InputStream fileStream = bot.downloadFileAsStream(telegramFile)) {
            String bucketName = minio.getBuckets().get(0);

            // Bucket mavjudligini tekshiramiz va kerak bo'lsa yaratamiz
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Bucket '{}' created successfully.", bucketName);
            }

            // Fayl uchun unikal nom (key) generatsiya qilamiz
            String originalFileName = new java.io.File(telegramFile.getFilePath()).getName();
            String objectKey = UUID.randomUUID() + "_" + originalFileName;

            // Faylni MinIO'ga yuklaymiz
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .stream(fileStream, fileSize, -1)
                            .contentType(mimeType)
                            .build()
            );

            log.info("File successfully uploaded to MinIO. Bucket: {}, Key: {}", bucketName, objectKey);
            return objectKey;

        } catch (MinioException e) {
            log.error("MinIO error during file upload: {}", e.getMessage());
            throw new RuntimeException("MinIO error during file upload.", e);
        } catch (Exception e) {
            log.error("An error occurred during file upload to MinIO.", e);
            throw new RuntimeException("Failed to upload file to MinIO.", e);
        }
    }

    /**
     * Videoni Telegram kanaliga yuboradi.
     *
     * @param fileId  Telegram'dagi fayl ID'si.
     * @param caption Video ostidagi izoh.
     */
    private void sendVideoToChannel(String fileId, String caption) {
        if (CHANNEL_ID == null || CHANNEL_ID.isEmpty()) {
            log.warn("Telegram channel ID is not configured. Skipping sending video.");
            return;
        }
        SendVideo sendVideo = new SendVideo(CHANNEL_ID, new InputFile(fileId));
//        sendVideo.setCaption(caption);
        try {
            bot.execute(sendVideo);
            log.info("Video sent to Telegram channel: {}", CHANNEL_ID);
        } catch (TelegramApiException e) {
            log.error("Failed to send video to Telegram channel.", e);
        }
    }

    /**
     * Rasmni Telegram kanaliga yuboradi.
     */
    private void sendPhotoToChannel(String fileId) {
        if (CHANNEL_ID == null || CHANNEL_ID.isEmpty()) {
            log.warn("Telegram channel ID is not configured.");
            return;
        }
        SendPhoto sendPhoto = new SendPhoto(CHANNEL_ID, new InputFile(fileId));
        bot.myExecute(sendPhoto);
        log.info("Photo sent to Telegram channel: {}", CHANNEL_ID);

    }

    /**
     * @param multipartFile file
     * @return attachmentDTO
     */
    @Override
    public AttachmentDTO saveIcon(MultipartFile multipartFile) {

        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalArgumentException("Fayl bo'sh bo'lishi mumkin emas.");
        }

        // 2. Fayl turini tekshirish
        String contentType = multipartFile.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new DataConflictException("Faqat .jpeg, .png, .svg formatdagi rasmlar yuklash mumkin.");
        }

        String bucketName = minio.getBuckets().get(2);
        String minioKey = saveFileMinio(multipartFile, bucketName);

        Attachment attachment = new Attachment(
                multipartFile.getOriginalFilename(),
                multipartFile.getContentType(),
                multipartFile.getSize(),
                minioKey,
                bucketName,
                null
        );

        attachmentRepository.save(attachment);

        return attachmentMapper.toDTO(attachment);
    }


    /**
     * @param multipartFile file
     * @return AttachmentDTO
     */
    @Override
    public AttachmentDTO create(MultipartFile multipartFile) {
        String bucketName = minio.getBuckets().get(0);
        String minioKey = saveFileMinio(multipartFile, bucketName);

        Attachment attachment = new Attachment(
                multipartFile.getOriginalFilename(),
                multipartFile.getContentType(),
                multipartFile.getSize(),
                minioKey,
                bucketName,
                null
        );

        attachmentRepository.save(attachment);

        log.info("Uploading single file: {}", multipartFile.getOriginalFilename());
        return attachmentMapper.toDTO(attachment);
    }

    @Override
    public String tempLink(Long id, Integer minute) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found with id: " + id));

        String minioKey = attachment.getMinioKey();

        try {

            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(attachment.getBucketName())
                            .object(minioKey)
                            .method(Method.GET)
                            .expiry(minute, TimeUnit.MINUTES)
                            .build()
            );
//            return new ResponseEntity<>(presignedObjectUrl, HttpStatus.OK);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * @param id Long
     */
    @Override
    public void delete(Long id) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not fount with id :" + id));

        attachmentRepository.delete(attachment);
    }

    private String saveFileMinio(MultipartFile multipartFile, String bucketName) {

        try {

            ObjectWriteResponse objectWriteResponse = minioClient.putObject(
                    PutObjectArgs.builder()
                            .object(UUID.randomUUID() + "_" + multipartFile.getOriginalFilename())
                            .contentType(multipartFile.getContentType())
                            .bucket(bucketName)
                            .stream(multipartFile.getInputStream(), multipartFile.getSize(), -1)
                            .build()
            );

            return objectWriteResponse.object();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // AttachmentService.java ichida

    /**
     * Telegramdan kelgan PhotoSize obyektidagi rasmni MinIO'ga yuklaydi.
     *
     * @param photo Eng katta o'lchamdagi PhotoSize obyekti.
     * @return MinIO'ga saqlangan obyektning unikal kaliti (key).
     * @throws RuntimeException Agar faylni yuklab olish yoki MinIO'ga yuklashda xatolik yuz bersa.
     */
    private String saveTgPhotoToMinio(PhotoSize photo) {
        try {
            // 1. Telegram API orqali faylning yo'lini (path) olamiz
            org.telegram.telegrambots.meta.api.objects.File telegramFile = bot.execute(new GetFile(photo.getFileId()));

            // 2. Faylni yuklab olish uchun URL hosil qilamiz va fayl oqimini (InputStream) olamiz
            // Eslatma: Bu metod to'g'ridan-to'g'ri faylni yuklab oladi.
            try (InputStream fileStream = bot.downloadFileAsStream(telegramFile)) {

                String bucketName = minio.getBuckets().get(0);

                // Bucket mavjudligini tekshiramiz va kerak bo'lsa yaratamiz
                if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                    log.info("Bucket '{}' created successfully.", bucketName);
                }

                // 3. Fayl uchun unikal nom (key) generatsiya qilamiz
                // Asl nomni olishga harakat qilamiz, agar bo'lmasa UUID ishlatamiz.
                String originalFileName = telegramFile.getFilePath() != null ?
                        new java.io.File(telegramFile.getFilePath()).getName() :
                        UUID.randomUUID().toString();

                String objectKey = UUID.randomUUID() + "_" + originalFileName;

                // 4. MinIO'ga yuklaymiz
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectKey)
                                .stream(fileStream, photo.getFileSize(), -1)
                                .contentType("image/jpeg") // Odatda Telegram rasmlari jpeg bo'ladi
                                .build()
                );

                log.info("Photo successfully uploaded to MinIO. Bucket: {}, Key: {}", bucketName, objectKey);
                return objectKey;

            } catch (IOException e) {
                log.error("Failed to read file stream from Telegram.", e);
                throw new RuntimeException("Failed to read file stream from Telegram.", e);
            }

        } catch (TelegramApiException e) {
            log.error("Failed to get file info from Telegram API for file_id: {}", photo.getFileId(), e);
            throw new RuntimeException("Failed to get file info from Telegram API.", e);
        } catch (Exception e) {
            log.error("An unexpected error occurred during file upload to MinIO.", e);
            throw new RuntimeException("An unexpected error occurred during file upload to MinIO.", e);
        }
    }
}

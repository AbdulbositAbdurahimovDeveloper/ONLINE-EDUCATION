package uz.pdp.online_education.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.online_education.config.properties.MinioProperties;
import uz.pdp.online_education.exceptions.DataConflictException;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.mapper.AttachmentMapper;
import uz.pdp.online_education.model.Attachment;
import uz.pdp.online_education.payload.content.attachmentContent.AttachmentDTO;
import uz.pdp.online_education.repository.AttachmentRepository;
import uz.pdp.online_education.service.interfaces.AttachmentService;
import uz.pdp.online_education.telegram.config.controller.OnlineEducationBot;

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
                                 MinioProperties minio, OnlineEducationBot bot) {
        this.attachmentRepository = attachmentRepository;
        this.attachmentMapper = attachmentMapper;
        this.minioClient = minioClient;
        this.minio = minio;
        this.bot = bot;
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

package uz.pdp.online_education.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.pdp.online_education.config.properties.MinioProperties;
import uz.pdp.online_education.exceptions.DataConflictException;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.mapper.AttachmentMapper;
import uz.pdp.online_education.model.Attachment;
import uz.pdp.online_education.payload.content.attachmentContent.AttachmentDTO;
import uz.pdp.online_education.repository.AttachmentRepository;
import uz.pdp.online_education.service.interfaces.AttachmentService;

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

    // Ruxsat etilgan rasm turlari ro'yxati
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            MediaType.IMAGE_JPEG_VALUE, // "image/jpeg"
            MediaType.IMAGE_PNG_VALUE,  // "image/png"
            "image/svg+xml"             // SVG uchun
    );


    public AttachmentServiceImpl(AttachmentRepository attachmentRepository,
                                 AttachmentMapper attachmentMapper,
                                 MinioClient minioClient,
                                 MinioProperties minio) {
        this.attachmentRepository = attachmentRepository;
        this.attachmentMapper = attachmentMapper;
        this.minioClient = minioClient;
        this.minio = minio;
    }

    @Override
    public AttachmentDTO read(Long id) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found with id: " + id));
        return attachmentMapper.toDTO(attachment);
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
                bucketName
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
                bucketName
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
}

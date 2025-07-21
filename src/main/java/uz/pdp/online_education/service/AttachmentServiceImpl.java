package uz.pdp.online_education.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uz.pdp.online_education.config.properties.FileStorageProperties;
import uz.pdp.online_education.controller.AttachmentController;
import uz.pdp.online_education.exceptions.AttachmentSaveException;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.exceptions.FileStorageException;
import uz.pdp.online_education.mapper.AttachmentMapper;
import uz.pdp.online_education.model.Attachment;
import uz.pdp.online_education.model.lesson.AttachmentContent;
import uz.pdp.online_education.payload.AttachmentDTO;
import uz.pdp.online_education.repository.AttachmentRepository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@Service
//@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final AttachmentMapper attachmentMapper;


    private final Path iconStorageLocation;
    private final Path baseFolder;

    public AttachmentServiceImpl(FileStorageProperties fileStorageProperties,
                                 AttachmentRepository attachmentRepository,
                                 AttachmentMapper attachmentMapper) {
        this.attachmentRepository = attachmentRepository;
        this.attachmentMapper = attachmentMapper;
        this.iconStorageLocation = Paths.get(fileStorageProperties.getBaseFolder());
        this.baseFolder = Paths.get(fileStorageProperties.getBaseFolder());
    }


    /**
     * Dastur ishga tushganda papkalar mavjudligini tekshiradi va yaratadi.
     */
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(iconStorageLocation);
        } catch (IOException e) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", e);
        }
    }

    @Override
    public AttachmentDTO read(Long id) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found with id: " + id));
        if (attachment.getPath().startsWith("http")) {
            throw new EntityNotFoundException("Attachment not found with id: " + id);
        }
        return attachmentMapper.toDTO(attachment);
    }


    /**
     * @param file
     * @return
     */

    @Override
    public AttachmentDTO saveIcon(MultipartFile file) {
        log.info("Uploading single file: {}", file.getOriginalFilename());

        try {

            String originalFilename = file.getOriginalFilename();
            long size = file.getSize();
            String contentType = file.getContentType();
            String extension = extractExtension(originalFilename);

            Path directoryPath =
                    iconStorageLocation;
//                    buildDirectoryPath();
            Files.createDirectories(directoryPath);

            String uniqueName = UUID.randomUUID() + extension;
            Path filePath = directoryPath.resolve(uniqueName);
            Files.exists(filePath);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath);
            }

            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/attachment/open/file/icons/")
                    .path(uniqueName)
                    .toUriString();

            log.debug("Saved file '{}' to '{}'", originalFilename, filePath);
            Attachment attachment = new Attachment(
                    originalFilename,
                    contentType,
                    size,
                    fileUrl
            );


            Attachment saved = attachmentRepository.save(attachment);
            log.info("File successfully uploaded and saved with ID: {}", saved.getId());
            return attachmentMapper.toDTO(saved);

        } catch (IOException e) {
            log.error("Error saving file: {}", file.getOriginalFilename(), e);
            throw new AttachmentSaveException("Error saving file: " + file.getOriginalFilename());
        }
    }


    /**
     * @param file
     * @return AttachmentDTO
     */
    @Override
    public AttachmentDTO create(MultipartFile file) {
        log.info("Uploading single file: {}", file.getOriginalFilename());

        try {

            String originalFilename = file.getOriginalFilename();
            long size = file.getSize();
            String contentType = file.getContentType();
            String extension = extractExtension(originalFilename);

            Path directoryPath = buildDirectoryPath();
            Files.createDirectories(directoryPath);

            Path filePath = generateUniqueFilePath(directoryPath, extension);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath);
            }

            log.debug("Saved file '{}' to '{}'", originalFilename, filePath);
            Attachment attachment = new Attachment(
                    originalFilename,
                    contentType,
                    size,
                    filePath.toString()
            );


            Attachment saved = attachmentRepository.save(attachment);
            log.info("File successfully uploaded and saved with ID: {}", saved.getId());
            return attachmentMapper.toDTO(saved);

        } catch (IOException e) {
            log.error("Error saving file: {}", file.getOriginalFilename(), e);
            throw new AttachmentSaveException("Error saving file: " + file.getOriginalFilename());
        }
    }

    /**
     * Ikonka faylini nomi bo'yicha topadi va uni 'Resource' sifatida qaytaradi.
     * Bu metod fayl tizimi bilan bog'liq barcha logikani o'z ichiga oladi.
     *
     * @param filename Yuklab olinadigan faylning nomi.
     * @return Faylga ishora qiluvchi Resource obyekti.
     * @throws FileNotFoundException agar fayl topilmasa.
     */
    public Resource loadIconAsResource(String filename) {
        try {
            Path filePath = this.iconStorageLocation.resolve(filename).normalize();

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new EntityNotFoundException("File not found: " + filename);
            }
        } catch (MalformedURLException ex) {
            throw new EntityNotFoundException("File not found: " + filename);
        }
    }

    /**
     * @param id
     */
    @Override
    public void delete(Long id) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not fount with id :" + id));

        attachmentRepository.delete(attachment);
    }

    private Path buildDirectoryPath() {
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String month = now.getMonth().name().charAt(0) + now.getMonth().name().substring(1).toLowerCase();
        Path path = Path.of(String.valueOf(baseFolder), year, month);
        log.debug("Generated directory path: {}", path);
        return path;
    }

    private String extractExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            String extension = filename.substring(filename.lastIndexOf("."));
            log.debug("Extracted extension '{}' from filename '{}'", extension, filename);
            return extension;
        }
        log.debug("No extension found for filename '{}'", filename);
        return "";
    }

    private Path generateUniqueFilePath(Path directoryPath, String extension) throws IOException {
        Path filePath;
        String uniqueName;
        int attempt = 0;
        do {
            uniqueName = UUID.randomUUID() + extension;
            filePath = directoryPath.resolve(uniqueName);
            attempt++;
        } while (Files.exists(filePath));

        log.debug("Generated unique file path '{}' on attempt {}", filePath, attempt);
        return filePath;
    }
}

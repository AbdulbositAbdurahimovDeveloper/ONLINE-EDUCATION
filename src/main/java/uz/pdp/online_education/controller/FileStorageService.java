package uz.pdp.online_education.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import uz.pdp.online_education.config.properties.FileStorageProperties;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.exceptions.FileStorageException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path iconStorageLocation;

    public FileStorageService(FileStorageProperties fileStorageProperties) {
        // Asosiy 'uploads' papkasiga yo'l
        Path rootLocation = Paths.get(fileStorageProperties.getBaseFolder());
        
        // Ikonkalar uchun alohida 'uploads/icons' papkasiga yo'l
        this.iconStorageLocation = rootLocation.resolve("icons");
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

    /**
     * Ikonka faylini saqlash uchun metod.
     * @param file Administrator yuklagan fayl
     * @return Saqlangan faylning unikal nomi
     */
    public String storeIcon(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("Failed to store empty file.");
        }

        // Fayl nomini xavfli belgilardan tozalash
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFilename.contains("..")) {
            throw new FileStorageException("Cannot store file with relative path outside current directory " + originalFilename);
        }
        
        // Fayl kengaytmasini olish (masalan, .png)
        String extension = "";
        int i = originalFilename.lastIndexOf('.');
        if (i > 0) {
            extension = originalFilename.substring(i);
        }

        // Unikal fayl nomini generatsiya qilish
        String uniqueFileName = UUID.randomUUID().toString() + extension;

        try {
            // Faylni saqlash uchun to'liq yo'l
            Path targetLocation = this.iconStorageLocation.resolve(uniqueFileName);
            
            // Faylni serverga nusxalash
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            return uniqueFileName;
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file " + originalFilename, e);
        }
    }

    /**
     * Faylni nomi bo'yicha yuklab olish uchun metod.
     * Bu metodni '/api/open/file/icons/{filename}' endpoint'i chaqiradi.
     * @param filename Fayl nomi
     * @return Faylning o'zi (Resource)
     */
    public Resource loadIconAsResource(String filename) {
        try {
            Path filePath = this.iconStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new EntityNotFoundException("File not found " + filename);
            }
        } catch (MalformedURLException ex) {
            throw new EntityNotFoundException("File not found " + filename);
        }
    }
}
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

/**
 * Service responsible for storing and loading files (mainly icons).
 * Uses server-side file system storage under "uploads/icons".
 *
 * Provides methods for:
 *  - Creating storage directories if missing
 *  - Uploading (storing) files with unique names
 *  - Retrieving files as {@link Resource}
 */
@Service
public class FileStorageService {

    private final Path iconStorageLocation;

    public FileStorageService(FileStorageProperties fileStorageProperties) {
        Path rootLocation = Paths.get(fileStorageProperties.getBaseFolder());
        this.iconStorageLocation = rootLocation.resolve("icons");
    }

    /**
     * Initializes storage directories after application startup.
     * If the folder does not exist, it will be created automatically.
     */
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(iconStorageLocation);
        } catch (IOException e) {
            throw new FileStorageException(
                    "Could not create the directory where the uploaded files will be stored.", e
            );
        }
    }

    /**
     * Stores a given icon file to server storage with a unique name.
     *
     * @param file the uploaded {@link MultipartFile}, must not be empty
     * @return generated unique filename (with extension)
     * @throws FileStorageException if file is invalid or cannot be saved
     */
    public String storeIcon(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("Failed to store empty file.");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFilename.contains("..")) {
            throw new FileStorageException("Invalid file path: " + originalFilename);
        }

        String extension = "";
        int i = originalFilename.lastIndexOf('.');
        if (i > 0) {
            extension = originalFilename.substring(i);
        }

        String uniqueFileName = UUID.randomUUID().toString() + extension;

        try (InputStream inputStream = file.getInputStream()) {
            Path targetLocation = this.iconStorageLocation.resolve(uniqueFileName);
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return uniqueFileName;
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + originalFilename, e);
        }
    }

    /**
     * Loads a file by its filename as {@link Resource}.
     * Used by controller endpoints to serve files.
     *
     * @param filename file name to load
     * @return {@link Resource} representation of the file
     * @throws EntityNotFoundException if the file does not exist
     */
    public Resource loadIconAsResource(String filename) {
        try {
            Path filePath = this.iconStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new EntityNotFoundException("File not found: " + filename);
            }
        } catch (MalformedURLException ex) {
            throw new EntityNotFoundException("File not found: " + filename);
        }
    }
}

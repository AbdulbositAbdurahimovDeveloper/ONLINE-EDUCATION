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
            // Base folder path, e.g. "uploads"
            Path rootLocation = Paths.get(fileStorageProperties.getBaseFolder());

            // Sub-folder specifically for icons: "uploads/icons"
            this.iconStorageLocation = rootLocation.resolve("icons");
        }

        /**
         * Initializes directories when the application starts.
         * Ensures the icon storage folder exists.
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
         * Stores an uploaded icon file.
         *
         * @param file the uploaded file (must not be empty)
         * @return the generated unique file name
         */
        public String storeIcon(MultipartFile file) {
            if (file.isEmpty()) {
                throw new FileStorageException("Failed to store empty file.");
            }

            // Sanitize file name
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            if (originalFilename.contains("..")) {
                throw new FileStorageException(
                        "Cannot store file with relative path outside current directory: " + originalFilename
                );
            }

            // Extract file extension (e.g., ".png")
            String extension = "";
            int i = originalFilename.lastIndexOf('.');
            if (i > 0) {
                extension = originalFilename.substring(i);
            }

            // Generate a unique file name
            String uniqueFileName = UUID.randomUUID().toString() + extension;

            try {
                // Define target path for saving the file
                Path targetLocation = this.iconStorageLocation.resolve(uniqueFileName);

                // Copy the file content to server storage
                try (InputStream inputStream = file.getInputStream()) {
                    Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
                }

                return uniqueFileName;
            } catch (IOException e) {
                throw new FileStorageException("Failed to store file: " + originalFilename, e);
            }
        }

        /**
         * Loads an icon file as a Resource by its filename.
         * Used by endpoint: GET `/api/open/file/icons/{filename}`
         *
         * @param filename the file name to load
         * @return the file as a Resource
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

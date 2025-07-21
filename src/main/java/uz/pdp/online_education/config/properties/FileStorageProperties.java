package uz.pdp.online_education.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "education.app.file")
public class FileStorageProperties {

    private String baseFolder;
    private String icons;
}

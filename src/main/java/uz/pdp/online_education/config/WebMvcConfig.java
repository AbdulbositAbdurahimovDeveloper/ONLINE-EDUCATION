package uz.pdp.online_education.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uz.pdp.online_education.config.properties.MinioProperties;
import uz.pdp.online_education.enums.CacheType;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;
    private final MinioProperties minio;

    @Autowired
    public WebMvcConfig(LoggingInterceptor loggingInterceptor, MinioProperties minioProperties) {
        this.loggingInterceptor = loggingInterceptor;
        this.minio = minioProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor);
    }

    @Bean
    public ValidatorFactory validatorFactory() {
        return Validation.buildDefaultValidatorFactory();
    }

    @Bean
    public Validator validator() {
        return validatorFactory().getValidator();
    }

    /**
     * Creates and configures the primary CacheManager for the application.
     * This manager dynamically builds a set of Caffeine caches based on the
     * configurations defined in the CacheType enum.
     *
     * @return The configured CacheManager.
     */
    @Bean("caffeineCacheManager")
    @Primary // Bu menejerni standart (asosiy) qilib belgilaydi.
    // Endi @Cacheable annotatsiyasida cacheManager nomini ko'rsatish shart emas.
    public CacheManager cacheManager() {
        // CacheType enumidagi har bir element uchun CaffeineCache ob'ektini yaratamiz
        List<CaffeineCache> caches = Arrays.stream(CacheType.values())
                .map(cacheType -> new CaffeineCache(
                        cacheType.getCacheName(), // 1. Har bir keshga o'z nomini beramiz
                        Caffeine.newBuilder()
                                // 2. Har bir kesh uchun o'zining TTL va max hajmini o'rnatamiz
                                .expireAfterWrite(cacheType.getTtlSeconds(), TimeUnit.SECONDS)
                                .maximumSize(cacheType.getMaxSize())
                                .build()
                ))
                .collect(Collectors.toList());

        // SimpleCacheManager - bu oldindan yaratilgan keshlar ro'yxatini boshqarish uchun juda qulay.
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(caches);
        return cacheManager;
    }

    @Bean
    public MinioClient minioClient() throws Exception {
        MinioClient client = MinioClient.builder()
                .endpoint(minio.getEndpoint())
                .credentials(minio.getAccessKey(), minio.getSecretKey())
                .build();

        for (String bucket : minio.getBuckets()) {

            BucketExistsArgs imaExistsArgs = BucketExistsArgs.builder().bucket(bucket).build();
            if (!client.bucketExists(imaExistsArgs)) {
                client.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucket)
                        .build());
            }
        }
        return client;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // barcha endpointlarga
                .allowedOrigins("*") // frontend IP + port
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(false);
    }

}

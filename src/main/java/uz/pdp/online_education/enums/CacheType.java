package uz.pdp.online_education.enums; // Yoki sizning enum paketingiz

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Defines the types of caches used in the application.
 * Each enum constant holds its own configuration for TTL (Time-To-Live) and max size.
 * This approach centralizes cache configuration, making it easy to manage and scale.
 */
@Getter
@RequiredArgsConstructor
public enum CacheType {

    // Har bir kesh uchun alohida sozlamalar
    USERS("users", 10 * 60, 1000),         // 10 daqiqa TTL, 1000 ta element
    COURSES("courses", 60 * 60, 500),      // 1 soat TTL, 500 ta element
    LESSONS("lessons", 30 * 60, 2000),      // 30 daqiqa TTL, 2000 ta element
    USER_PROFILES("user_profiles", 10 * 60, 1000); // 10 daqiqa TTL, 1000 ta element

    private final String cacheName; // Kesh nomi
    private final long ttlSeconds;  // Keshning yashash vaqti (sekundlarda)
    private final long maxSize;     // Keshdagi elementlarning maksimal soni
}
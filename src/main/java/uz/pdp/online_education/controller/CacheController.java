package uz.pdp.online_education.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.cache.Cache;


import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CacheController – tizimda ishlatilayotgan Redis cache’larni tekshirish,
 * ularning ichidagi key-value ma’lumotlarni olish va tozalash uchun REST API.
 * <p>
 * Ushbu controller faqat ADMIN foydalanuvchilarga mo‘ljallangan.
 * Maqsad – debugging va profiling uchun cache ichidagi ma’lumotlarni ko‘rish.
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Tag(name = "Cache Management API", description = "Endpoints for inspecting and managing Redis caches (ADMIN only)")
public class CacheController {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate; // Redis bilan ishlash uchun asosiy vosita

    /**
     * Cache ichidagi bitta yozuvni ifodalaydigan DTO.
     */
    @Schema(description = "Represents a cache entry (key-value pair)")
    public record CacheDTO(
            @Schema(description = "The original cache key (without the cache name prefix)", example = "123") Object key,
            @Schema(description = "The cached value", example = "{id:123, title:'Java Basics'}") Object value
    ) {}

    /**
     * Berilgan cache nomi bo‘yicha barcha key-value juftliklarni olish.
     * <p>
     * <b>DIQQAT:</b> Ushbu metod production'da ko'p ma'lumotga ega Redis serverlarida
     * ishlash unumdorligiga salbiy ta'sir ko'rsatishi mumkin bo'lgan `KEYS` komandasidan foydalanadi.
     * Faqat debugging va kichik hajmdagi keshlar uchun ishlating.
     * </p>
     *
     * @param cacheName Cache nomi (masalan: "courses", "users")
     * @return Cache ichidagi barcha yozuvlar ro‘yxati
     */
    @Operation(
            summary = "Read all entries from a cache",
            description = "Retrieves all key-value pairs from a specific Redis cache. WARNING: Uses the 'KEYS' command, which can be slow on large production databases. Use with caution."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cache entries retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CacheDTO.class)))),
            @ApiResponse(responseCode = "403", description = "Forbidden: Only ADMIN can access this endpoint"),
            @ApiResponse(responseCode = "404", description = "Cache not found or is empty")
    })
    @GetMapping("/{cacheName}")
    @PreAuthorize(value = "hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<CacheDTO> read(
            @Parameter(description = "The name of the cache to inspect", example = "courses")
            @PathVariable String cacheName) {

        log.info("Causerche inspection requested for cacheName={}", cacheName);

        // Spring Cache odatda kalitlarga "cacheName::" prefiksini qo'shadi.
        String keyPattern = cacheName + "::*";
        Set<String> redisKeys = redisTemplate.keys(keyPattern);

        if (redisKeys.isEmpty()) {
            log.warn("No keys found for cache '{}' with pattern '{}'", cacheName, keyPattern);
            return List.of();
        }

        log.debug("Found {} entries for cache '{}'", redisKeys.size(), cacheName);

        // Har bir Redis kaliti uchun uning qiymatini olamiz va DTO ga o'giramiz
        return redisKeys.stream()
                .map(redisKey -> {
                    Object value = redisTemplate.opsForValue().get(redisKey);
                    // "cacheName::" prefiksini olib tashlab, asl kalitni qaytaramiz
                    String originalKey = redisKey.substring(cacheName.length() + 2);
                    return new CacheDTO(originalKey, value);
                })
                .collect(Collectors.toList());
    }

    /**
     * Berilgan cache'dagi barcha yozuvlarni o'chirish.
     *
     * @param cacheName O'chirilishi kerak bo'lgan cache nomi
     * @return Xabar: cache bo‘shatilganligi haqida
     */
    @Operation(summary = "Clear a cache", description = "Evict all entries from a given cache. Only accessible to ADMIN.")
    @DeleteMapping("/{cacheName}")
    @PreAuthorize(value = "hasRole('ADMIN')")
    public String clearCache(
            @Parameter(description = "Cache name to clear", example = "courses")
            @PathVariable String cacheName) {

        log.info("Request to clear cache: {}", cacheName);

        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear(); // Spring Cache abstraction o'zi Redis uchun kerakli komandalarni bajaradi
            log.debug("Cache '{}' cleared successfully", cacheName);
            return "Cache '" + cacheName + "' cleared successfully.";
        }
        log.warn("Cache '{}' not found, nothing to clear", cacheName);
        return "Cache not found: " + cacheName;
    }
}
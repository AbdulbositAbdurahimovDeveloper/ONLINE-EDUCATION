package uz.pdp.online_education.controller;

import com.github.benmanes.caffeine.cache.Cache;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CacheController – tizimda ishlatilayotgan Caffeine cache’larni tekshirish,
 * ularning ichidagi key-value ma’lumotlarni olish uchun REST API.
 * <p>
 * Ushbu controller faqat ADMIN foydalanuvchilarga mo‘ljallangan.
 * Maqsad – debugging va profiling uchun cache ichidagi ma’lumotlarni ko‘rish.
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
public class CacheController {

    private final CacheManager cacheManager;

    /**
     * Cache ichidagi bitta yozuvni ifodalaydigan DTO.
     */
    @Schema(description = "Represents a cache entry (key-value pair)")
    public record CacheDTO(
            @Schema(description = "Cache key", example = "course:123") Object key,
            @Schema(description = "Cache value", example = "{id:123, title:'Java Basics'}") Object value
    ) {}

    /**
     * Berilgan cache nomi bo‘yicha barcha key-value juftliklarni olish.
     *
     * @param cacheName Cache nomi (masalan: "courses", "users")
     * @return Cache ichidagi barcha yozuvlar ro‘yxati
     */
    @Operation(
            summary = "Read cache by name",
            description = "Retrieve all key-value pairs stored in a specific cache. Only accessible to ADMIN users."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cache entries retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CacheDTO.class)))),
            @ApiResponse(responseCode = "403", description = "Forbidden: Only ADMIN can access this endpoint"),
            @ApiResponse(responseCode = "404", description = "Cache not found")
    })
    @GetMapping("/{cacheName}")
    @PreAuthorize(value = "hasRole('ADMIN')")
    public List<CacheDTO> read(
            @Parameter(description = "The name of the cache to inspect", example = "courses")
            @PathVariable String cacheName) {

        log.info("Cache inspection requested for cacheName={}", cacheName);

        CaffeineCache cache = (CaffeineCache) cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("Cache with name '{}' not found", cacheName);
            return List.of();
        }

        Cache<Object, Object> nativeCache = cache.getNativeCache();
        Map<Object, Object> cacheMap = nativeCache.asMap();

        log.debug("Cache '{}' contains {} entries", cacheName, cacheMap.size());

        return cacheMap.entrySet().stream()
                .map(entry -> new CacheDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Kelajak uchun qo‘shimcha metod: cache ichini tozalash.
     * (faqat ADMIN huquqiga ega bo‘lganlar chaqira oladi).
     *
     * @param cacheName Cache nomi
     * @return Xabar: cache bo‘shatilganligi haqida
     */
    @Operation(summary = "Clear cache", description = "Evict all entries from a given cache. Only accessible to ADMIN.")
    @DeleteMapping("/{cacheName}")
    @PreAuthorize(value = "hasRole('ADMIN')")
    public String clearCache(
            @Parameter(description = "Cache name to clear", example = "courses")
            @PathVariable String cacheName) {

        log.info("Clearing cache: {}", cacheName);

        CaffeineCache cache = (CaffeineCache) cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.debug("Cache '{}' cleared successfully", cacheName);
            return "Cache '" + cacheName + "' cleared successfully.";
        }
        log.warn("Cache '{}' not found, nothing to clear", cacheName);
        return "Cache not found: " + cacheName;
    }
}

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
    import org.springframework.cache.CacheManager;
    import org.springframework.cache.caffeine.CaffeineCache;
    import org.springframework.security.access.prepost.PreAuthorize;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;
    import java.util.Map;
    import java.util.stream.Collectors;

    @RestController
    @RequestMapping("/api/cache")
    @RequiredArgsConstructor
    public class CacheController {

        private final CacheManager cacheManager;

        @Schema(description = "Represents a cache entry (key-value pair)")
        public record CacheDTO(
                @Schema(description = "Cache key", example = "course:123") Object key,
                @Schema(description = "Cache value", example = "{id:123, title:'Java Basics'}") Object value
        ) {}

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

            CaffeineCache cache = (CaffeineCache) cacheManager.getCache(cacheName);
            if (cache == null) {
                return List.of(); // Cache not found → empty list
            }

            Cache<Object, Object> nativeCache = cache.getNativeCache();
            Map<Object, Object> cacheMap = nativeCache.asMap();

            return cacheMap.entrySet().stream()
                    .map(entry -> new CacheDTO(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        }

    }

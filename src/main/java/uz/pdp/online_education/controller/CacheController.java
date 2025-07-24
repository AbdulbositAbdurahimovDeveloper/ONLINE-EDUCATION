package uz.pdp.online_education.controller;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
public class CacheController {

    private final CacheManager cacheManager;

    public record CacheDTO(Object key, Object value) {
    }

    @GetMapping("/{cacheName}")
    public List<CacheDTO> read(@PathVariable String cacheName) {

        CaffeineCache courseCache = (CaffeineCache) cacheManager.getCache(cacheName); // ⚠️ Cache nomi "courses" bo‘lsa kerak
        if (courseCache == null) {
            return List.of(); // Cache not found
        }

        Cache<Object, Object> nativeCache = courseCache.getNativeCache();
        Map<Object, Object> cacheMap = nativeCache.asMap();

        return cacheMap.entrySet().stream()
                .map(entry -> new CacheDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}

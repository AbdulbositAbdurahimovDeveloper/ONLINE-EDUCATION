package uz.pdp.online_education.telegram.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * TemporaryDataService interfeysining Redis'ga asoslangan implementatsiyasi.
 */
@Service
@RequiredArgsConstructor
public class RedisTemporaryDataServiceImpl implements RedisTemporaryDataService {

    private final RedisTemplate<String, Object> redisTemplate;

    // 'application.yml' yoki 'application.properties' faylidan standart TTL qiymatini o'qib olamiz.
    @Value("${application.cache.temporary-process-ttl-seconds}")
    private long defaultTtlSeconds;

    @Override
    public void startProcess(String key, Map<String, Object> initialData) {
        // Asosiy metodni standart TTL bilan chaqiramiz.
        // Bu kod takrorlanishining oldini oladi (DRY principle).
        this.startProcess(key, initialData, this.defaultTtlSeconds);
    }

    @Override
    public void startProcess(String key, Map<String, Object> initialData, long ttlSeconds) {
        // Redis'dagi Hash'ga dastlabki ma'lumotlarni yozamiz.
        redisTemplate.opsForHash().putAll(key, initialData);
        // Butun Hash (asosiy kalit) uchun yashash vaqtini (TTL) o'rnatamiz.
        redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void addField(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    @Override
    public Map<String, Object> getFields(String key, List<String> fields) {
        // So'ralgan maydonlarni Object turiga o'tkazamiz, chunki multiGet shuni talab qiladi.
        List<Object> hashKeys = fields.stream().map(f -> (Object) f).collect(Collectors.toList());

        // Redis'dan so'ralgan maydonlarning qiymatlarini List<Object> ko'rinishida olamiz.
        List<Object> values = redisTemplate.opsForHash().multiGet(key, hashKeys);

        Map<String, Object> result = new HashMap<>();
        for (int i = 0; i < fields.size(); i++) {
            // Agar qiymat null bo'lmasa (ya'ni topilgan bo'lsa), natijaviy Map'ga qo'shamiz.
            if (values.get(i) != null) {
                result.put(fields.get(i), values.get(i));
            }
        }
        return result;
    }

    @Override
    public Optional<Map<String, Object>> getAllFields(String key) {
        // Redis'dagi Hash'ning barcha maydon va qiymatlarini olamiz.
        // Natija Map<Object, Object> ko'rinishida keladi.
        Map<Object, Object> rawMap = redisTemplate.opsForHash().entries(key);

        if (rawMap == null || rawMap.isEmpty()) {
            return Optional.empty();
        }

        // Turlarni to'g'rilaymiz (Map<Object, Object> ni Map<String, Object> ga o'giramiz).
        Map<String, Object> resultMap = rawMap.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> (String) entry.getKey(),
                        Map.Entry::getValue
                ));

        return Optional.of(resultMap);
    }

    @Override
    public void endProcess(String key) {
        redisTemplate.delete(key);
    }
}
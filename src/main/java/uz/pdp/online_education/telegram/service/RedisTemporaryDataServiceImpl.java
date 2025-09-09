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

@Service
@RequiredArgsConstructor
public class RedisTemporaryDataServiceImpl implements RedisTemporaryDataService {

    private final RedisTemplate<String, Object> redisTemplate;


    @Value("${application.cache.temporary-process-ttl-seconds}")
    private long defaultTtlSeconds;

    @Override
    public void startProcess(String key, Map<String, Object> initialData) {

        this.startProcess(key, initialData, this.defaultTtlSeconds);
    }

    @Override
    public void startProcess(String key, Map<String, Object> initialData, long ttlSeconds) {

        redisTemplate.opsForHash().putAll(key, initialData);

        redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void addField(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
        redisTemplate.expire(key, defaultTtlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public Map<String, Object> getFields(String key, List<String> fields) {

        List<Object> hashKeys = fields.stream().map(f -> (Object) f).collect(Collectors.toList());


        List<Object> values = redisTemplate.opsForHash().multiGet(key, hashKeys);

        Map<String, Object> result = new HashMap<>();
        for (int i = 0; i < fields.size(); i++) {

            if (values.get(i) != null) {
                result.put(fields.get(i), values.get(i));
            }
        }
        return result;
    }

    @Override
    public Optional<Map<String, Object>> getAllFields(String key) {

        Map<Object, Object> rawMap = redisTemplate.opsForHash().entries(key);

        if (rawMap == null || rawMap.isEmpty()) {
            return Optional.empty();
        }


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
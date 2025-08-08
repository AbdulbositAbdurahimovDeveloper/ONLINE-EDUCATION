package uz.pdp.online_education.service.interfaces;

import org.springframework.cache.annotation.CacheEvict;

public interface CacheManagerService {
    void deleteCacheUser(String username);
}

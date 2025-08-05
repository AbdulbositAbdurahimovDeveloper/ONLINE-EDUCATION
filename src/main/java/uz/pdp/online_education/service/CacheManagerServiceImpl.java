package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import uz.pdp.online_education.service.interfaces.CacheManagerService;

@Service
@RequiredArgsConstructor
public class CacheManagerServiceImpl implements CacheManagerService {

    @CacheEvict(value = "users", key = "#username")
    @Override
    public void deleteCacheUser(String username) {

    }
}

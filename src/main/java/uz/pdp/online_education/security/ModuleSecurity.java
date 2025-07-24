package uz.pdp.online_education.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import uz.pdp.online_education.service.interfaces.ModuleService;


@Component("courseSecurity")
@RequiredArgsConstructor
public class ModuleSecurity {

    private final ModuleService moduleService;


    public boolean isUserEnrolled(Authentication authentication, Long courseId) {
        String username = authentication.getName(); // yoki userId ni JWTdan oling
        return moduleService.isUserEnrolled(username, courseId);
    }
}

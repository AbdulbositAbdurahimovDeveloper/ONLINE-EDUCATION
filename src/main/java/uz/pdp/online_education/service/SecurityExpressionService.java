package uz.pdp.online_education.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import uz.pdp.online_education.model.User; // Sizning User klassingiz

@Service("securityExpression")
public class SecurityExpressionService {

    /**
     * Checks if the authenticated user is the owner of the resource identified by the given id.
     *
     * @param authentication The current authentication object from Spring Security context.
     * @param id             The ID of the resource (e.g., user ID from the URL path).
     * @return True if the authenticated user's ID matches the provided ID, false otherwise.
     */
    public boolean isOwner(Authentication authentication, Long id) {

        if (authentication != null && authentication.getPrincipal() instanceof User authenticatedUser) {

            return authenticatedUser.getId().equals(id);
        }
        return false;
    }
}
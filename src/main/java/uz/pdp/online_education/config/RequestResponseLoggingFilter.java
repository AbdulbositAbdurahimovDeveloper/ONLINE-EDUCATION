package uz.pdp.online_education.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);

        try {
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String query = request.getQueryString();
            String ip = request.getRemoteAddr();

            log.info("➡️ [{}] Incoming request: [{}] {}{} from IP: {}", requestId, method, uri,
                    query != null ? "?" + query : "", ip);

            filterChain.doFilter(request, response);

            int status = response.getStatus();
            log.info("⬅️ [{}] Response status: {} for [{}] {}", requestId, status, method, uri);
        } finally {
            MDC.clear();
        }
    }
}

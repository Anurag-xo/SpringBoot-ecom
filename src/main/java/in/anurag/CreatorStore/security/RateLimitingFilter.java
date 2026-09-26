package in.anurag.CreatorStore.security;

import in.anurag.CreatorStore.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitConfig rateLimitConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIp(request);
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Determine which bucket to use based on the endpoint
        Bucket bucket = resolveBucket(path, method, clientIp);

        // Try to consume a token from the bucket
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // Request is allowed - add rate limit headers for transparency
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded - return 429 Too Many Requests
            long waitForRefillSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefillSeconds));
            response.addHeader("Retry-After", String.valueOf(waitForRefillSeconds));
            
            String errorMessage = String.format(
                "{\"error\": \"Too Many Requests\", \"message\": \"You have exceeded the rate limit. Please try again in %d seconds.\", \"retryAfter\": %d}",
                waitForRefillSeconds, waitForRefillSeconds
            );
            
            response.getWriter().write(errorMessage);
        }
    }

    private Bucket resolveBucket(String path, String method, String ip) {
        // Auth endpoints - strictest limit (5/min) to prevent brute-force
        if (path.startsWith("/api/auth/")) {
            return rateLimitConfig.resolveAuthBucket(ip);
        }
        
        // Admin write operations - moderate limit (30/min)
        if (path.startsWith("/api/products") && 
            (method.equals("POST") || method.equals("PUT") || method.equals("DELETE"))) {
            return rateLimitConfig.resolveAdminBucket(ip);
        }
        
        // Public endpoints (GET requests) - lenient limit (60/min)
        return rateLimitConfig.resolvePublicBucket(ip);
    }

    // Helper method to get the real client IP (handles proxies/load balancers)
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }
        
        return request.getRemoteAddr();
    }

    // Skip rate limiting for static resources, health checks, and docs
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/uploads/") || 
               path.equals("/actuator/health") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs");
    }
}

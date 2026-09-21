package com.smartspace.security.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.local.LocalBucketBuilder;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RateLimitFilter implements Filter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Value("${app.rate-limit.login-per-minute:10}")
    private int loginPerMinute;

    @Value("${app.rate-limit.otp-send-per-hour:6}")
    private int otpSendPerHour;

    @Value("${app.rate-limit.scan-per-minute:60}")
    private int scanPerMinute;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();
        String ip = getClientIP(req);
        
        Bucket bucket = resolveBucket(path, ip);
        
        if (bucket != null) {
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
            if (!probe.isConsumed()) {
                res.setStatus(429);
                res.setHeader("Retry-After", String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));
                res.setContentType("application/json");
                res.getWriter().write("{\"code\":\"RATE_LIMITED\",\"message\":\"Too many requests\"}");
                return;
            }
        }
        
        chain.doFilter(request, response);
    }

    private Bucket resolveBucket(String path, String ip) {
        if (path.contains("/auth/login")) {
            return buckets.computeIfAbsent("login-" + ip, k -> createNewBucket(loginPerMinute, Duration.ofMinutes(1)));
        } else if (path.contains("/auth/otp/send")) {
            return buckets.computeIfAbsent("otp-" + ip, k -> createNewBucket(otpSendPerHour, Duration.ofHours(1)));
        } else if (path.contains("/entry/scan")) {
            return buckets.computeIfAbsent("scan-" + ip, k -> createNewBucket(scanPerMinute, Duration.ofMinutes(1)));
        }
        return null;
    }

    private Bucket createNewBucket(int capacity, Duration refillDuration) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, refillDuration)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}

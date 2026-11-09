package org.geocyber.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory sliding window rate limiter for REST API endpoints.
 */
@Component
public class RateLimitingFilter implements Filter {

    @Value("${geocyber.security.rate-limit.requests-per-minute:120}")
    private int maxRequestsPerMinute;

    private static class ClientCounter {
        long windowStart;
        AtomicInteger count;

        ClientCounter(long start) {
            this.windowStart = start;
            this.count = new AtomicInteger(1);
        }
    }

    private final ConcurrentHashMap<String, ClientCounter> clientBuckets = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();
        if (path.startsWith("/api/")) {
            String clientIp = req.getRemoteAddr();
            long now = System.currentTimeMillis();

            ClientCounter counter = clientBuckets.compute(clientIp, (ip, c) -> {
                if (c == null || (now - c.windowStart) > 60000) {
                    return new ClientCounter(now);
                } else {
                    c.count.incrementAndGet();
                    return c;
                }
            });

            if (counter.count.get() > maxRequestsPerMinute) {
                res.setStatus(429);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\": \"Too Many Requests\", \"message\": \"Rate limit exceeded. Try again in 60 seconds.\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}

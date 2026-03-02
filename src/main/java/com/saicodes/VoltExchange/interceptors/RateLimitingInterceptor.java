package com.saicodes.VoltExchange.interceptors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saicodes.VoltExchange.common.ApiResponse;
import com.saicodes.VoltExchange.entities.User;
import com.saicodes.VoltExchange.util.SecurityUtils;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final ProxyManager<String> proxyManager;
    private final BucketConfiguration bucketConfiguration;
    private final SecurityUtils securityUtils;
    private final ObjectMapper objectMapper;

    @Value("${rate.limit.transfer.capacity}")
    private long transferCapacity;

    @Value("${rate.limit.transfer.refill.minutes}")
    private long transferRefillMinutes;

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        User user = securityUtils.getCurrentUser();

        String bucketKey = "rate_limit:transfer:" + user.getId().toString();

        Bucket bucket = proxyManager.getProxy(bucketKey, () -> bucketConfiguration);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        }

        response.addHeader("X-Ratelimit-Limit", String.valueOf(transferCapacity));
        response.addHeader("X-RateLimit-Retry-After", String.valueOf(probe.getNanosToWaitForRefill()/1_000_000_000));

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Void> body = ApiResponse.error("Too many requests, Retry after " + probe.getNanosToWaitForRefill()/1_000_000_000 + " seconds", null);

        response.getWriter().write(objectMapper.writeValueAsString(body));

        return false;
    }
}

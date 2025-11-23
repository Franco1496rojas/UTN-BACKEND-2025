package backend.tpi_Napoli_Spadoni_Rojas.gateway.filters;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final Map<String, RequestCounter> bucket = new ConcurrentHashMap<>();
    private static final int LIMIT = 5; // Máx. 5 requests por minuto
    private static final long WINDOW_MS = 60000; // 60 segundos

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        if (exchange.getRequest().getURI().getPath().startsWith("/api/operaciones/")) {
            RequestCounter counter = bucket.computeIfAbsent(ip, k -> new RequestCounter());
            synchronized (counter) {
                long now = Instant.now().toEpochMilli();
                if (now - counter.start > WINDOW_MS) {
                    counter.count = 0;
                    counter.start = now;
                }
                counter.count++;
                if (counter.count > LIMIT) {
                    exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    return exchange.getResponse().setComplete();
                }
            }
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 1;
    }

    private static class RequestCounter {
        int count = 0;
        long start = Instant.now().toEpochMilli();
    }
}

package backend.tpi_Napoli_Spadoni_Rojas.gateway.filters;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Component
public class RequestIdFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String reqId = exchange.getRequest().getHeaders().getFirst("X-Request-Id");
        if (reqId == null) {
            reqId = UUID.randomUUID().toString();
        }
        exchange.getResponse().getHeaders().add("X-Request-Id", reqId);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}

package backend.tpi_Napoli_Spadoni_Rojas.gateway.filters;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String reqId = exchange.getRequest().getHeaders().getFirst("X-Request-Id");
        System.out.println(
                "--> [" + reqId + "] " + exchange.getRequest().getMethod() + " " + exchange.getRequest().getURI());
        return chain.filter(exchange)
                .then(Mono.fromRunnable(
                        () -> System.out.println("<-- [" + reqId + "] " + exchange.getResponse().getStatusCode())));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}

package backend.tpi_Napoli_Spadoni_Rojas.operaciones.config;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    // Si luego querés timeouts, interceptores, logs, etc.
    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return builder -> {
        };
    }
}

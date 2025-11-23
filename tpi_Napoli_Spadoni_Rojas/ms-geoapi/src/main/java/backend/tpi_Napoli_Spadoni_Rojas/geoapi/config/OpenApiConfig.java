package backend.tpi_Napoli_Spadoni_Rojas.geoapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8084}")
    private String serverPort;

    @Bean
    public OpenAPI geoApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MS-GEOAPI API")
                        .description("Microservicio de cálculo de distancias geográficas usando Google Distance Matrix API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo TPI")
                                .email("soporte@tpi.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Servidor Local (Directo)"),
                        new Server()
                                .url("http://localhost:8080/api")
                                .description("API Gateway")));
    }
}

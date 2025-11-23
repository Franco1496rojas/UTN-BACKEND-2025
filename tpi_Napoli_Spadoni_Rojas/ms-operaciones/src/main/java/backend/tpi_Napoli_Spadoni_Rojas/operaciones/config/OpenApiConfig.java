package backend.tpi_Napoli_Spadoni_Rojas.operaciones.config;

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

    @Value("${server.port:8083}")
    private String serverPort;

    @Bean
    public OpenAPI operacionesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MS-OPERACIONES API")
                        .description("Microservicio de gestión de solicitudes, rutas, tramos y seguimientos de transporte")
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

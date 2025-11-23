package backend.tpi_Napoli_Spadoni_Rojas.gateway.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi clientesApi() {
        return GroupedOpenApi.builder()
                .group("ms-clientes")
                .pathsToMatch("/api/clientes/**", "/api/contenedores/**", "/api/ciudades/**", "/api/provincias/**")
                .build();
    }

    @Bean
    public GroupedOpenApi flotaApi() {
        return GroupedOpenApi.builder()
                .group("ms-flota")
                .pathsToMatch("/api/camiones/**", "/api/transportistas/**", "/api/administradores/**", 
                             "/api/depositos/**", "/api/tarifas/**", "/api/parametros-tarifa/**")
                .build();
    }

    @Bean
    public GroupedOpenApi operacionesApi() {
        return GroupedOpenApi.builder()
                .group("ms-operaciones")
                .pathsToMatch("/api/solicitudes/**", "/api/rutas/**", "/api/tramos/**", "/api/seguimientos/**")
                .build();
    }

    @Bean
    public GroupedOpenApi geoApiApi() {
        return GroupedOpenApi.builder()
                .group("ms-geoapi")
                .pathsToMatch("/api/distancia/**")
                .build();
    }
}

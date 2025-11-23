package backend.tpi_Napoli_Spadoni_Rojas.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:http://keycloak:8080/realms/tpi}")
    private String issuerUri;

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        // Crear el decoder desde el JWK Set URI
        String jwkSetUri = issuerUri.replace("/realms/tpi", "/realms/tpi/protocol/openid-connect/certs");
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
        
        // NO validar el issuer - esto permite tokens de localhost:8085 y keycloak:8080
        decoder.setJwtValidator(JwtValidators.createDefault());
        
        return decoder;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers("/actuator/**").permitAll()
                        
                        // ============================================
                        // SWAGGER UI - Permitir acceso público
                        // ============================================
                        .pathMatchers("/clientes/swagger-ui.html", "/clientes/swagger-ui/**", "/clientes/api-docs/**",
                                     "/clientes/v3/api-docs/**", "/clientes/webjars/**").permitAll()
                        .pathMatchers("/flota/swagger-ui.html", "/flota/swagger-ui/**", "/flota/api-docs/**",
                                     "/flota/v3/api-docs/**", "/flota/webjars/**").permitAll()
                        .pathMatchers("/operaciones/swagger-ui.html", "/operaciones/swagger-ui/**", "/operaciones/api-docs/**",
                                     "/operaciones/v3/api-docs/**", "/operaciones/webjars/**").permitAll()
                        .pathMatchers("/geoapi/swagger-ui.html", "/geoapi/swagger-ui/**", "/geoapi/api-docs/**",
                                     "/geoapi/v3/api-docs/**", "/geoapi/webjars/**").permitAll()
                        
                        // ============================================
                        // ADMIN - Gestión completa del sistema
                        // ============================================
                        // CRUD de entidades maestras
                        .pathMatchers("/api/clientes/**", "/api/depositos/**", "/api/camiones/**", 
                                     "/api/tarifas/**", "/api/parametros-tarifa/**", 
                                     "/api/ciudades/**", "/api/provincias/**")
                        .hasRole("ADMIN")
                        
                        // Operaciones administrativas de solicitudes
                        .pathMatchers(HttpMethod.GET, "/api/solicitudes/**").hasAnyRole("ADMIN", "CLIENTE")
                        .pathMatchers(HttpMethod.POST, "/api/solicitudes/*/rutas/estimadas").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/solicitudes/*/asignar-ruta").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/solicitudes/*/ruta").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/solicitudes/*/estado").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/solicitudes/**").hasRole("ADMIN")
                        
                        // Asignar camión a tramo (ADMIN/OPERADOR)
                        .pathMatchers(HttpMethod.POST, "/api/tramos/*/asignar-camion").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/tramos/**").hasAnyRole("ADMIN", "TRANSPORTISTA")
                        
                        // Consultar contenedores pendientes y ubicaciones (ADMIN)
                        .pathMatchers(HttpMethod.GET, "/api/contenedores/pendientes").hasRole("ADMIN")
                        
                        // ============================================
                        // CLIENTE - Solicitudes y seguimiento
                        // ============================================
                        // Registrar nueva solicitud de transporte
                        .pathMatchers(HttpMethod.POST, "/api/solicitudes", "/api/solicitudes/registrar")
                        .hasAnyRole("CLIENTE", "ADMIN")
                        
                        // Consultar estado de transportes (seguimiento)
                        .pathMatchers(HttpMethod.GET, "/api/seguimientos/**").hasAnyRole("CLIENTE", "ADMIN")
                        
                        // Gestionar sus propios contenedores
                        .pathMatchers(HttpMethod.GET, "/api/contenedores").hasAnyRole("CLIENTE", "ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/contenedores/*").hasAnyRole("CLIENTE", "ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/contenedores").hasAnyRole("CLIENTE", "ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/contenedores/**").hasAnyRole("CLIENTE", "ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/contenedores/**").hasRole("ADMIN")
                        
                        // ============================================
                        // TRANSPORTISTA - Gestión de tramos
                        // ============================================
                        // Ver sus tramos asignados
                        .pathMatchers(HttpMethod.GET, "/api/transportistas/*/tramos").hasAnyRole("TRANSPORTISTA", "ADMIN")
                        
                        // Iniciar y finalizar tramos (operaciones de transporte)
                        .pathMatchers(HttpMethod.POST, "/api/tramos/*/iniciar").hasAnyRole("TRANSPORTISTA", "ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/tramos/*/finalizar").hasAnyRole("TRANSPORTISTA", "ADMIN")
                        
                        // ============================================
                        // RUTAS - Solo lectura general
                        // ============================================
                        .pathMatchers(HttpMethod.GET, "/api/rutas/**").hasAnyRole("ADMIN", "TRANSPORTISTA")
                        
                        // Resto requiere autenticación
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt
                                .jwtDecoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        return new ReactiveJwtAuthenticationConverterAdapter(jwt -> {
            Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
            return new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken(
                    jwt, authorities, jwt.getClaimAsString("preferred_username"));
        });
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        // Intenta obtener roles desde el claim 'roles' (array de strings)
        List<String> roles = jwt.getClaimAsStringList("roles");
        
        if (roles == null || roles.isEmpty()) {
            System.err.println("⚠️ No se encontraron roles en el token JWT");
            return List.of();
        }

        // Agrega el prefijo ROLE_ que Spring Security espera
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }
}
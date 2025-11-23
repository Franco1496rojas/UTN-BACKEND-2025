package backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations;

import backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.dto.ClienteDTO;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.dto.ContenedorDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente unificado para comunicación con ms-clientes
 * Maneja operaciones de clientes y contenedores
 */
/**
 * Cliente unificado para comunicación con ms-clientes
 * Maneja operaciones de clientes y contenedores
 */
@Component
@RequiredArgsConstructor
public class ClientesApiClient {

    private final RestClient.Builder builder;

    @Value("${clientes.service.url}")
    private String baseUrl;

    // ==================== CONTENEDORES ====================

    /**
     * Obtener contenedor por ID
     */
    // ==================== CONTENEDORES ====================

    /**
     * Obtener contenedor por ID
     */
    public ContenedorDTO getContenedor(Long contenedorId) {
        try {
            String url = baseUrl + "/contenedores/" + contenedorId;
            ResponseEntity<ContenedorDTO> resp = builder.build()
                    .get()
                    .uri(url)
                    .retrieve()
                    .toEntity(ContenedorDTO.class);
            return resp.getBody();
        } catch (Exception e) {
            System.err.println("❌ Error consultando contenedor " + contenedorId + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Buscar contenedor por código
     */
    public ContenedorDTO buscarContenedorPorCodigo(String codigo) {
        // El endpoint actual no filtra por 'codigo'; traer lista y filtrar client-side
        String url = baseUrl + "/contenedores?codigo=" + java.net.URLEncoder.encode(codigo, java.nio.charset.StandardCharsets.UTF_8);
        try {
            ResponseEntity<ContenedorDTO[]> resp = builder.build()
                    .get()
                    .uri(url)
                    .retrieve()
                    .toEntity(ContenedorDTO[].class);

            ContenedorDTO[] contenedores = resp.getBody();
            if (contenedores == null || contenedores.length == 0) {
                return null;
            }
            for (ContenedorDTO c : contenedores) {
                if (c != null && c.getCodigo() != null && c.getCodigo().equalsIgnoreCase(codigo)) {
                    return c;
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("❌ Error buscando contenedor por código: " + e.getMessage());
            return null;
        }
    }

    /**
     * Crear nuevo contenedor
     */
    public ContenedorDTO crearContenedor(ContenedorDTO contenedor) {
        String url = baseUrl + "/contenedores";
        try {
            ResponseEntity<ContenedorDTO> resp = builder.build()
                    .post().uri(url)
                    .body(contenedor)
                    .retrieve()
                    .toEntity(ContenedorDTO.class);
            
            return resp.getBody();
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // Error 4xx desde ms-clientes
            String errorMsg = "Error al crear contenedor: " + e.getResponseBodyAsString();
            System.err.println("❌ " + errorMsg);
            throw new RuntimeException(errorMsg);
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            // Error 5xx desde ms-clientes
            String errorMsg = "Error del servidor al crear contenedor: " + e.getResponseBodyAsString();
            System.err.println("❌ " + errorMsg);
            throw new RuntimeException(errorMsg);
        } catch (Exception e) {
            System.err.println("❌ Error creando contenedor: " + e.getMessage());
            throw new RuntimeException("Error al comunicarse con ms-clientes: " + e.getMessage());
        }
    }

    /**
     * Crear contenedor enviando JSON directamente (para incluir objeto cliente anidado)
     */
    public ContenedorDTO crearContenedorConJson(String contenedorJson) {
        String url = baseUrl + "/contenedores";
        try {
            ResponseEntity<ContenedorDTO> resp = builder.build()
                    .post().uri(url)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(contenedorJson)
                    .retrieve()
                    .toEntity(ContenedorDTO.class);
            
            return resp.getBody();
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // Error 4xx desde ms-clientes - extraer mensaje de error
            String errorMsg = e.getResponseBodyAsString();
            try {
                // Intentar parsear el JSON de error
                com.fasterxml.jackson.databind.JsonNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper().readTree(errorMsg);
                if (jsonNode.has("mensaje")) {
                    errorMsg = jsonNode.get("mensaje").asText();
                }
            } catch (Exception ignored) {
                // Si no se puede parsear, usar el mensaje completo
            }
            System.err.println("❌ Error al crear contenedor: " + errorMsg);
            throw new RuntimeException(errorMsg);
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            String errorMsg = "Error del servidor al crear contenedor: " + e.getResponseBodyAsString();
            System.err.println("❌ " + errorMsg);
            throw new RuntimeException(errorMsg);
        } catch (Exception e) {
            System.err.println("❌ Error creando contenedor: " + e.getMessage());
            throw new RuntimeException("Error al comunicarse con ms-clientes: " + e.getMessage());
        }
    }

    /**
     * Validar dimensiones del contenedor
     */
    public boolean validarDimensiones(Double peso, Double volumen) {
        return peso != null && peso > 0 && volumen != null && volumen > 0;
    }

    // ==================== CLIENTES ====================

    /**
     * Buscar cliente por DNI
     */
    public ClienteDTO buscarClientePorDni(String dni) {
        // El backend aún no filtra por DNI; obtener lista y filtrar localmente
        String url = baseUrl + "/clientes?dni=" + (dni != null ? java.net.URLEncoder.encode(dni, java.nio.charset.StandardCharsets.UTF_8) : "");
        try {
            ResponseEntity<ClienteDTO[]> resp = builder.build()
                    .get().uri(url)
                    .retrieve()
                    .toEntity(ClienteDTO[].class);

            ClienteDTO[] clientes = resp.getBody();
            if (clientes == null) {
                return null;
            }
            for (ClienteDTO c : clientes) {
                if (c != null && c.getDni() != null && dni != null && c.getDni().trim().equalsIgnoreCase(dni.trim())) {
                    return c;
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("❌ Error buscando cliente por DNI: " + e.getMessage());
            return null;
        }
    }

    /**
     * Buscar cliente por Email
     */
    public ClienteDTO buscarClientePorEmail(String email) {
        // El backend aún no filtra por email; obtener lista y filtrar localmente
        String url = baseUrl + "/clientes?email=" + (email != null ? java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8) : "");
        try {
            ResponseEntity<ClienteDTO[]> resp = builder.build()
                    .get().uri(url)
                    .retrieve()
                    .toEntity(ClienteDTO[].class);

            ClienteDTO[] clientes = resp.getBody();
            if (clientes == null) {
                return null;
            }
            for (ClienteDTO c : clientes) {
                if (c != null && c.getEmail() != null && email != null && c.getEmail().trim().equalsIgnoreCase(email.trim())) {
                    return c;
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("❌ Error buscando cliente por email: " + e.getMessage());
            return null;
        }
    }

    /**
     * Crear nuevo cliente
     */
    public ClienteDTO crearCliente(ClienteDTO cliente) {
        // ms-clientes exige ciudad obligatoria; si no la tenemos, asignamos una por defecto
        String url = baseUrl + "/clientes";
        try {
            Long ciudadId = obtenerCiudadDefaultId();

            // Asegurar DNI válido y único (Integer, único según lista actual)
            Integer dniInt = normalizarODefinirDni(cliente.getDni());
            if (dniInt == null) {
                // Generar uno y verificar que no exista
                for (int i = 0; i < 5 && dniInt == null; i++) {
                    int candidato = generarDniCandidato();
                    // Reutilizamos la búsqueda existente (filtra client-side)
                    ClienteDTO existente = buscarClientePorDni(String.valueOf(candidato));
                    if (existente == null) {
                        dniInt = candidato;
                    }
                }
                if (dniInt == null) {
                    // Último recurso
                    dniInt = generarDniCandidato();
                }
            }

            String nombre = valorONull(cliente.getNombre());
            String apellido = extraerApellidoONull(cliente.getNombre());
            String email = valorONull(cliente.getEmail());
            String telefono = valorONull(cliente.getTelefono());
            String direccion = valorONull(cliente.getDireccion());
            String keycloakId = "auto-" + System.currentTimeMillis();

            // Defaults requeridos por ms-clientes
            if (nombre == null) nombre = "Cliente";
            if (apellido == null) apellido = "Generico";
            if (email == null) email = "sin-email-" + System.currentTimeMillis() + "@local";
            if (telefono == null) telefono = "0000000000";
            if (direccion == null) direccion = "No especificado";

            StringBuilder json = new StringBuilder("{");
            json.append("\"dni\": ").append(dniInt).append(",");
            json.append("\"nombre\": \"").append(nombre).append("\",");
            json.append("\"apellido\": \"").append(apellido).append("\",");
            json.append("\"email\": \"").append(email).append("\",");
            json.append("\"telefono\": \"").append(telefono).append("\",");
            json.append("\"domicilio\": \"").append(direccion).append("\",");
            json.append("\"keycloakId\": \"").append(keycloakId).append("\",");
            json.append("\"ciudad\": { \"id\": ").append(ciudadId).append(" }");
            json.append("}");

            ResponseEntity<ClienteDTO> resp = builder.build()
                    .post().uri(url)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(json.toString())
                    .retrieve()
                    .toEntity(ClienteDTO.class);

            return resp.getBody();
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String errorMsg = e.getResponseBodyAsString();
            System.err.println("❌ Error al crear cliente: " + errorMsg);
            throw new RuntimeException("Error al crear cliente: " + errorMsg);
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            String errorMsg = e.getResponseBodyAsString();
            System.err.println("❌ Error del servidor al crear cliente: " + errorMsg);
            throw new RuntimeException("Error del servidor al crear cliente: " + errorMsg);
        } catch (Exception e) {
            System.err.println("❌ Error creando cliente: " + e.getMessage());
            throw new RuntimeException("Error al comunicarse con ms-clientes: " + e.getMessage());
        }
    }

    private Long obtenerCiudadDefaultId() {
        String url = baseUrl + "/ciudades";
        try {
            ResponseEntity<String> resp = builder.build()
                    .get().uri(url)
                    .retrieve()
                    .toEntity(String.class);
            String body = resp.getBody();
            if (body == null || body.isEmpty()) {
                return 1L; // fallback
            }
            com.fasterxml.jackson.databind.JsonNode arr = new com.fasterxml.jackson.databind.ObjectMapper().readTree(body);
            if (arr.isArray() && arr.size() > 0 && arr.get(0).has("id")) {
                return arr.get(0).get("id").asLong();
            }
            return 1L; // fallback razonable
        } catch (Exception e) {
            System.err.println("⚠️ No se pudo obtener ciudad default, usando ID=1. Causa: " + e.getMessage());
            return 1L;
        }
    }

    private Integer normalizarODefinirDni(String dniStr) {
        if (dniStr == null || dniStr.trim().isEmpty()) return null;
        try {
            long dniLong = Long.parseLong(dniStr.trim());
            if (dniLong >= 1 && dniLong <= Integer.MAX_VALUE) {
                return (int) dniLong;
            }
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int generarDniCandidato() {
        long base = System.currentTimeMillis() % 90000000L; // 8 dígitos
        return (int) (10000000L + base); // rango [10,000,000 - 99,999,999]
    }

    private String valorONull(String s) {
        return (s != null && !s.trim().isEmpty()) ? s.trim() : null;
    }

    private String extraerApellidoONull(String nombreCompleto) {
        String n = valorONull(nombreCompleto);
        if (n == null) return null;
        String[] parts = n.split("\\s+");
        if (parts.length >= 2) return parts[parts.length - 1];
        return null;
    }
}

package backend.tpi_Napoli_Spadoni_Rojas.operaciones.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.FlotaApiClient;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.dto.CamionDTO;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.EstadoTramo;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.Tramo;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.services.TramoService;

@RestController
@RequestMapping("/api/transportistas")
public class TransportistaTramoController {

    private final TramoService tramoService;
    private final FlotaApiClient flotaApiClient;

    public TransportistaTramoController(TramoService tramoService, FlotaApiClient flotaApiClient) {
        this.tramoService = tramoService;
        this.flotaApiClient = flotaApiClient;
    }

    /**
     * GET /api/transportistas/{id}/tramos?estado=ASIGNADO
     * Permite a un transportista consultar todos sus tramos (vía sus camiones).
     * Validaciones básicas y mensajes consistentes con otros controladores.
     */
    @GetMapping("/{id}/tramos")
    public ResponseEntity<?> obtenerTramosPorTransportista(
            @PathVariable("id") Long transportistaId,
            @RequestParam(name = "estado", required = false) String estado) {
        try {
            // Validar ID
            if (transportistaId == null || transportistaId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("El ID de transportista debe ser positivo"));
            }

            // Obtener camiones del transportista desde ms-flota
            List<CamionDTO> camiones = flotaApiClient.getCamionesPorTransportista(transportistaId);

            if (camiones == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("No se encontró el transportista con ID: " + transportistaId));
            }

            if (camiones.isEmpty()) {
                // Devuelve lista vacía (sin error) para consistencia
                return ResponseEntity.ok(List.of());
            }

            // Extraer IDs de camiones
            List<Long> camionIds = camiones.stream()
                    .map(CamionDTO::getId)
                    .collect(Collectors.toList());

            // Parsear estado (opcional)
            EstadoTramo estadoEnum = null;
            if (estado != null && !estado.isBlank()) {
                try {
                    estadoEnum = EstadoTramo.valueOf(estado.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ErrorResponse("Estado inválido: " + estado + ". Valores permitidos: " + valoresEstado()));
                }
            }

            // Buscar tramos según filtrado
            List<Tramo> tramos = tramoService.findByTransportistaAndEstado(camionIds, estadoEnum);

            System.out.println("✅ Encontrados " + tramos.size() + " tramos para transportista " + transportistaId
                    + (estadoEnum != null ? " con estado " + estadoEnum : ""));

            return ResponseEntity.ok(tramos);
        } catch (RuntimeException ex) {
            // Si el mensaje sugiere no encontrado
            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("no se encontró")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(ex.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al obtener tramos: " + ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error inesperado al obtener tramos: " + ex.getMessage()));
        }
    }

    private String valoresEstado() {
        return String.join(", ",
                java.util.Arrays.stream(EstadoTramo.values()).map(Enum::name).toList());
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class ErrorResponse {
        private String mensaje;
    }
}

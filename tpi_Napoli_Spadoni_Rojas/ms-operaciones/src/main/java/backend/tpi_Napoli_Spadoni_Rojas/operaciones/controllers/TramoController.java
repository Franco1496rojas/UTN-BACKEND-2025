package backend.tpi_Napoli_Spadoni_Rojas.operaciones.controllers;

import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.Tramo;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.services.TramoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tramos")
public class TramoController {

    private final TramoService service;

    public TramoController(TramoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Tramo>> listar(@RequestParam(name = "rutaId", required = false) Long rutaId) {
        List<Tramo> tramos;
        if (rutaId != null) {
            tramos = service.findByRuta(rutaId);
        } else {
            tramos = service.findAll();
        }
        return ResponseEntity.ok(tramos);
    }

    @PostMapping
    public ResponseEntity<Tramo> crear(@RequestBody Tramo tramo) {
        Tramo tramoCreado = service.save(tramo);
        return ResponseEntity.ok(tramoCreado);
    }

    @PostMapping("/{tramoId}/asignar-camion")
    public ResponseEntity<?> asignarCamion(
            @PathVariable("tramoId") Long tramoId, 
            @RequestBody Map<String, Long> request) {
        try {
            // Validar que el request tenga el camionId
            if (!request.containsKey("camionId") || request.get("camionId") == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("El ID del camión es obligatorio"));
            }

            Long camionId = request.get("camionId");
            Tramo tramo = service.asignarCamion(tramoId, camionId);
            return ResponseEntity.ok(tramo);

        } catch (IllegalArgumentException e) {
            // Errores de validación: 400 Bad Request
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));

        } catch (RuntimeException e) {
            // Errores de servidor o servicios externos: 500 Internal Server Error
            if (e.getMessage().contains("No se encontró") || e.getMessage().contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error al asignar el camión: " + e.getMessage()));

        } catch (Exception e) {
            // Errores inesperados
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error inesperado al asignar el camión: " + e.getMessage()));
        }
    }

    @PostMapping("/{tramoId}/iniciar")
    public ResponseEntity<?> iniciarTramo(@PathVariable("tramoId") Long tramoId) {
        try {
            // Registrar la fecha y hora actual del sistema en formato ISO_OFFSET_DATE_TIME
            String fechaHoraInicioReal = java.time.OffsetDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            Tramo tramo = service.iniciarTramo(tramoId, fechaHoraInicioReal);
            return ResponseEntity.ok(tramo);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));

        } catch (RuntimeException e) {
            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error al iniciar el tramo: " + e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error inesperado al iniciar el tramo: " + e.getMessage()));
        }
    }

    @PostMapping("/{tramoId}/finalizar")
    public ResponseEntity<?> finalizarTramo(@PathVariable("tramoId") Long tramoId) {
        try {
            // La fecha de fin se registra automáticamente con la hora actual del sistema
            // Los cálculos de duración, distancia y costo se hacen automáticamente
            Tramo tramo = service.finalizarTramo(tramoId);
            return ResponseEntity.ok(tramo);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));

        } catch (RuntimeException e) {
            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error al finalizar el tramo: " + e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error inesperado al finalizar el tramo: " + e.getMessage()));
        }
    }

    /**
     * Clase interna para respuestas de error
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class ErrorResponse {
        private String mensaje;
    }
}

package backend.tpi_Napoli_Spadoni_Rojas.operaciones.controllers;

import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.*;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.services.SolicitudService;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.services.RutaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {

    private final SolicitudService service;
    private final RutaService rutaService;

    public SolicitudController(SolicitudService service, RutaService rutaService) {
        this.service = service;
        this.rutaService = rutaService;
    }

    @GetMapping
    public ResponseEntity<List<Solicitud>> listar(@RequestParam(name = "clienteId", required = false) Long clienteId,
            @RequestParam(name = "estado", required = false) Estado estado) {
        try {
            List<Solicitud> solicitudes;
            if (clienteId != null && estado != null) {
                solicitudes = service.findByClienteAndEstado(clienteId, estado);
            } else if (clienteId != null) {
                solicitudes = service.findByCliente(clienteId);
            } else if (estado != null) {
                solicitudes = service.findByEstado(estado);
            } else {
                solicitudes = service.findAll();
            }
            return ResponseEntity.ok(solicitudes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable("id") Long id) {
        try {
            return service.findById(id)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ErrorResponse("No se encontró una solicitud con el ID: " + id)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al obtener la solicitud: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Solicitud solicitud) {
        try {
            Solicitud nuevaSolicitud = service.save(solicitud);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaSolicitud);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al crear la solicitud: " + e.getMessage()));
        }
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarSolicitud(@RequestBody RegistrarSolicitudDTO dto) {
        try {
            Solicitud solicitud = service.registrarSolicitud(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(solicitud);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al registrar la solicitud: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable("id") Long id,
            @RequestParam(name = "nuevoEstado") Estado nuevoEstado,
            @RequestParam(name = "observaciones", required = false) String observaciones) {
        try {
            Solicitud solicitudActualizada = service.actualizarEstado(id, nuevoEstado, observaciones);
            return ResponseEntity.ok(solicitudActualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("no encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al cambiar el estado: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al cambiar el estado: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable("id") Long id) {
        try {
            if (!service.findById(id).isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("No se encontró una solicitud con el ID: " + id));
            }
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al eliminar la solicitud: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/asignar-ruta")
    public ResponseEntity<?> asignarRuta(
            @PathVariable("id") Long id,
            @RequestBody AsignarRutaDTO asignarRutaDTO) {
        try {
            Ruta ruta = rutaService.asignarRuta(id, asignarRutaDTO.getTipo());
            return ResponseEntity.status(HttpStatus.CREATED).body(ruta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("no encontrada") || e.getMessage().contains("No se encontró")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al asignar la ruta: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al asignar la ruta: " + e.getMessage()));
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

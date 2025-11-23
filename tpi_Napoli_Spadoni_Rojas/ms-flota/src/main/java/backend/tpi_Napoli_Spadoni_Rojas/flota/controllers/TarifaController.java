package backend.tpi_Napoli_Spadoni_Rojas.flota.controllers;

import backend.tpi_Napoli_Spadoni_Rojas.flota.models.TarifaRango;
import backend.tpi_Napoli_Spadoni_Rojas.flota.services.TarifaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tarifas")
public class TarifaController {

    private final TarifaService service;

    public TarifaController(TarifaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<TarifaRango>> listar() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable("id") Long id) {
        java.util.Optional<TarifaRango> optional = service.findById(id);
        if (optional.isPresent()) {
            return ResponseEntity.ok(optional.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("No se encontró una tarifa con el ID: " + id));
        }
    }
    
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody TarifaRango tarifa) {
        try {
            // Validación adicional: El ID debe ser null para creación
            if (tarifa.getId() != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("No se debe especificar un ID al crear una nueva tarifa"));
            }

            TarifaRango tarifaCreada = service.save(tarifa);
            return ResponseEntity.status(HttpStatus.CREATED).body(tarifaCreada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al crear la tarifa: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable("id") Long id, @RequestBody TarifaRango tarifa) {
        try {
            return service.findById(id)
                    .map(existingTarifa -> {
                        tarifa.setId(id);
                        try {
                            TarifaRango tarifaActualizada = service.save(tarifa);
                            return ResponseEntity.ok(tarifaActualizada);
                        } catch (IllegalArgumentException e) {
                            return ResponseEntity.status(HttpStatus.CONFLICT)
                                    .body(new ErrorResponse(e.getMessage()));
                        }
                    })
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ErrorResponse("No se encontró una tarifa con el ID: " + id)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al actualizar la tarifa: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable("id") Long id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al eliminar la tarifa: " + e.getMessage()));
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

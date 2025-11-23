package backend.tpi_Napoli_Spadoni_Rojas.clientes.controllers;

import backend.tpi_Napoli_Spadoni_Rojas.clientes.models.Provincia;
import backend.tpi_Napoli_Spadoni_Rojas.clientes.services.ProvinciaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/provincias")
public class ProvinciaController {

    private final ProvinciaService service;

    public ProvinciaController(ProvinciaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Provincia>> listar() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable("id") Long id) {
        java.util.Optional<Provincia> optional = service.findById(id);
        if (optional.isPresent()) {
            return ResponseEntity.ok(optional.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("No se encontró una provincia con el ID: " + id));
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Provincia provincia) {
        try {
            // Validación: El ID debe ser null para creación
            if (provincia.getId() != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("No se debe especificar un ID al crear una nueva provincia"));
            }

            Provincia provinciaCreada = service.save(provincia);
            return ResponseEntity.status(HttpStatus.CREATED).body(provinciaCreada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al crear la provincia: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable("id") Long id, @RequestBody Provincia provincia) {
        try {
            return service.findById(id)
                    .map(existingProvincia -> {
                        provincia.setId(id);
                        try {
                            Provincia provinciaActualizada = service.save(provincia);
                            return ResponseEntity.ok(provinciaActualizada);
                        } catch (IllegalArgumentException e) {
                            return ResponseEntity.status(HttpStatus.CONFLICT)
                                    .body(new ErrorResponse(e.getMessage()));
                        }
                    })
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ErrorResponse("No se encontró una provincia con el ID: " + id)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al actualizar la provincia: " + e.getMessage()));
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
                    .body(new ErrorResponse("Error al eliminar la provincia: " + e.getMessage()));
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

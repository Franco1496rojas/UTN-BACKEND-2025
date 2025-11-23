package backend.tpi_Napoli_Spadoni_Rojas.clientes.controllers;

import backend.tpi_Napoli_Spadoni_Rojas.clientes.models.Ciudad;
import backend.tpi_Napoli_Spadoni_Rojas.clientes.services.CiudadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ciudades")
public class CiudadController {

    private final CiudadService service;

    public CiudadController(CiudadService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Ciudad>> listar(@RequestParam(name = "provinciaId", required = false) Long provinciaId) {
        if (provinciaId != null) {
            return ResponseEntity.ok(service.findByProvinciaId(provinciaId));
        }
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable("id") Long id) {
        java.util.Optional<Ciudad> optional = service.findById(id);
        if (optional.isPresent()) {
            return ResponseEntity.ok(optional.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("No se encontró una ciudad con el ID: " + id));
        }

    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Ciudad ciudad) {
        try {
            // Validación: El ID debe ser null para creación
            if (ciudad.getId() != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("No se debe especificar un ID al crear una nueva ciudad"));
            }

            Ciudad ciudadCreada = service.save(ciudad);
            return ResponseEntity.status(HttpStatus.CREATED).body(ciudadCreada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al crear la ciudad: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable("id") Long id, @RequestBody Ciudad ciudad) {
        try {
            return service.findById(id)
                    .map(existingCiudad -> {
                        ciudad.setId(id);
                        try {
                            Ciudad ciudadActualizada = service.save(ciudad);
                            return ResponseEntity.ok(ciudadActualizada);
                        } catch (IllegalArgumentException e) {
                            return ResponseEntity.status(HttpStatus.CONFLICT)
                                    .body(new ErrorResponse(e.getMessage()));
                        }
                    })
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ErrorResponse("No se encontró una ciudad con el ID: " + id)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al actualizar la ciudad: " + e.getMessage()));
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
                    .body(new ErrorResponse("Error al eliminar la ciudad: " + e.getMessage()));
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

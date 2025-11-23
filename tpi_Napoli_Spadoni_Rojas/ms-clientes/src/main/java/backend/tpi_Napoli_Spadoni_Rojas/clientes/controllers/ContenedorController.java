package backend.tpi_Napoli_Spadoni_Rojas.clientes.controllers;

import backend.tpi_Napoli_Spadoni_Rojas.clientes.models.Contenedor;
import backend.tpi_Napoli_Spadoni_Rojas.clientes.services.ContenedorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contenedores")
public class ContenedorController {

    private final ContenedorService service;

    public ContenedorController(ContenedorService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Contenedor>> listar(@RequestParam(name = "clienteId", required = false) Long clienteId) {
        List<Contenedor> contenedores;
        if (clienteId != null) {
            contenedores = service.findByCliente(clienteId);
        } else {
            contenedores = service.findAll();
        }
        return ResponseEntity.ok(contenedores);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable("id") Long id) {
        return service.findById(id)
                .<ResponseEntity<?>>map(contenedor -> ResponseEntity.ok(contenedor))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("No se encontró un contenedor con el ID: " + id)));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Contenedor contenedor) {
        try {
            // Validación: El ID debe ser null para creación
            if (contenedor.getId() != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("No se debe especificar un ID al crear un nuevo contenedor"));
            }

            Contenedor contenedorCreado = service.save(contenedor);
            return ResponseEntity.status(HttpStatus.CREATED).body(contenedorCreado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al crear el contenedor: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable("id") Long id, @RequestBody Contenedor contenedor) {
        try {
            return service.findById(id)
                    .map(existingContenedor -> {
                        contenedor.setId(id);
                        try {
                            Contenedor contenedorActualizado = service.save(contenedor);
                            return ResponseEntity.ok(contenedorActualizado);
                        } catch (IllegalArgumentException e) {
                            return ResponseEntity.status(HttpStatus.CONFLICT)
                                    .body(new ErrorResponse(e.getMessage()));
                        }
                    })
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ErrorResponse("No se encontró un contenedor con el ID: " + id)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al actualizar el contenedor: " + e.getMessage()));
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
                    .body(new ErrorResponse("Error al eliminar el contenedor: " + e.getMessage()));
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

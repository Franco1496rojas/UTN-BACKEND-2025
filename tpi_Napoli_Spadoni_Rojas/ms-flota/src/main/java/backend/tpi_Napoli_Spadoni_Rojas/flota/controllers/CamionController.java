package backend.tpi_Napoli_Spadoni_Rojas.flota.controllers;

import backend.tpi_Napoli_Spadoni_Rojas.flota.models.Camion;
import backend.tpi_Napoli_Spadoni_Rojas.flota.services.CamionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/camiones")
public class CamionController {

    private final CamionService service;

    public CamionController(CamionService service) {
        this.service = service;
    }

    @GetMapping
    public List<Camion> listar(@RequestParam(name = "transportistaId", required = false) Long transportistaId) {
        if (transportistaId != null)
            return service.findByTransportista(transportistaId);
        return service.findAll();
    }

    // Marcamos el camion como disponible o no disponible
    @PutMapping("/disponibilidad")
    public ResponseEntity<Camion> cambiarDisponibilidad(@RequestParam("camionId") Long camionId,
            @RequestParam("disponible") boolean disponible) {
        return service.findById(camionId)
                .map(camion -> {
                    camion.setDisponibilidad(disponible);
                    service.save(camion);
                    return ResponseEntity.ok(camion);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Obtenemos los camiones disponibles segun el peso maximo indicado y el volumen
    // maximo indicado
    @GetMapping("/disponibles")
    public List<Camion> listarDisponibles(@RequestParam(name = "pesoMaximo", required = false) Double pesoMaximo,
            @RequestParam(name = "volumenMaximo", required = false) Double volumenMaximo) {

        return service.findDisponiblesPesoVolumen(pesoMaximo, volumenMaximo);

    }

    @GetMapping("/{id}")
    public ResponseEntity<Camion> obtener(@PathVariable("id") Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Devolvemos al cantidad de camiones libres
    @GetMapping("/libres")
    public ResponseEntity<?> obtenerLibres() {
        long disponibles = service.findDisponibles().size();
        return ResponseEntity.ok(disponibles);
    }

    // Devolvemos la cantidad de camiones ocupados
    @GetMapping("/ocupados")
    public ResponseEntity<?> obtenerOcupados() {
        long ocupados = service.findAll().size() - service.findDisponibles().size();
        return ResponseEntity.ok(ocupados);
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Camion camion) {
        try {
            Camion camionCreado = service.save(camion);
            return ResponseEntity.status(201).body(camionCreado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(409).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Error al crear el camión: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable("id") Long id, @RequestBody Camion camion) {
        try {
            return service.findById(id)
                    .map(c -> {
                        camion.setId(id);
                        Camion camionActualizado = service.save(camion);
                        return ResponseEntity.ok(camionActualizado);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(409)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("Error al actualizar el camión: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
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

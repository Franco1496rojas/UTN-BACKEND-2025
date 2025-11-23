package backend.tpi_Napoli_Spadoni_Rojas.flota.controllers;

import backend.tpi_Napoli_Spadoni_Rojas.flota.models.Transportista;
import backend.tpi_Napoli_Spadoni_Rojas.flota.services.TransportistaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transportistas")
public class TransportistaController {

    private final TransportistaService service;

    public TransportistaController(TransportistaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Transportista> listar() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transportista> obtener(@PathVariable("id") Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transportista> actualizar(@PathVariable("id") Long id,
            @RequestBody Transportista transportista) {
        return service.findById(id)
                .map(existingTransportista -> {
                    transportista.setId(existingTransportista.getId());
                    return ResponseEntity.ok(service.save(transportista));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Transportista> crear(@RequestBody Transportista transportista) {
        return ResponseEntity.ok(service.save(transportista));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

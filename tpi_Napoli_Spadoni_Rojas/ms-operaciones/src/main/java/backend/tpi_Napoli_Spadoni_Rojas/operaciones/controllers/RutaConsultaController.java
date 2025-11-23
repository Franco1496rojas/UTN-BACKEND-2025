package backend.tpi_Napoli_Spadoni_Rojas.operaciones.controllers;

import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.Ruta;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.services.RutaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/rutas")
public class RutaConsultaController {

    private final RutaService service;

    public RutaConsultaController(RutaService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ruta> obtenerRuta(@PathVariable("id") Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Ruta>> listarRutas(
            @RequestParam(name = "solicitudId", required = false) Long solicitudId) {
        List<Ruta> rutas;
        if (solicitudId != null) {
            rutas = service.findBySolicitud(solicitudId);
        } else {
            rutas = service.findAll();
        }
        return ResponseEntity.ok(rutas);
    }
}

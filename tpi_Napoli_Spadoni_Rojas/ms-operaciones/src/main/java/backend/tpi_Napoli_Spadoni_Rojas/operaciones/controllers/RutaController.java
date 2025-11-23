package backend.tpi_Napoli_Spadoni_Rojas.operaciones.controllers;

import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.RutaTentativaDTO;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.SeleccionRutaDTO;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.services.RutaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/solicitudes/{id}")
public class RutaController {

    private final RutaService service;

    public RutaController(RutaService service) {
        this.service = service;
    }

    @PostMapping("/rutas/estimadas")
    public ResponseEntity<List<RutaTentativaDTO>> obtenerRutasEstimadas(@PathVariable("id") Long id) {
        // Retorna lista de rutas tentativas con tramos y costos estimados
        List<RutaTentativaDTO> rutasEstimadas = service.obtenerRutasEstimadas(id);
        return ResponseEntity.ok(rutasEstimadas);
    }

    @PostMapping("/ruta")
    public ResponseEntity<?> seleccionarRuta(@PathVariable("id") Long id, @RequestBody SeleccionRutaDTO seleccionRuta) {
        // Guarda la ruta seleccionada y sus tramos con estado=ESTIMADO
        // Actualiza los totales en la Solicitud
        Long rutaId = service.guardarRutaSeleccionada(id, seleccionRuta);
        return ResponseEntity.status(201).body(rutaId);
    }
}

package backend.tpi_Napoli_Spadoni_Rojas.flota.controllers;

import backend.tpi_Napoli_Spadoni_Rojas.flota.models.ParametrosTarifa;
import backend.tpi_Napoli_Spadoni_Rojas.flota.services.ParametrosTarifaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/parametros-tarifa")
public class ParametrosTarifaController {

    private final ParametrosTarifaService service;

    public ParametrosTarifaController(ParametrosTarifaService service) {
        this.service = service;
    }

    @GetMapping
    public List<ParametrosTarifa> listar() {
        return service.findAll();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParametrosTarifa> actualizar(@PathVariable("id") Long id,
            @RequestBody ParametrosTarifa parametros) {
        return service.findById(id)
                .map(existingParametros -> {
                    parametros.setId(existingParametros.getId());
                    return ResponseEntity.ok(service.save(parametros));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ParametrosTarifa> crear(@RequestBody ParametrosTarifa parametros) {
        return ResponseEntity.ok(service.save(parametros));
    }
}

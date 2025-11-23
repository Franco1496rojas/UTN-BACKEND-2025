package backend.tpi_Napoli_Spadoni_Rojas.flota.controllers;

import backend.tpi_Napoli_Spadoni_Rojas.flota.models.Administrador;
import backend.tpi_Napoli_Spadoni_Rojas.flota.services.AdministradorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/administradores")
public class AdministradorController {

    private final AdministradorService service;

    public AdministradorController(AdministradorService service) {
        this.service = service;
    }

    @GetMapping
    public List<Administrador> listar() {
        return service.findAll();
    }

    @PostMapping
    public ResponseEntity<Administrador> crear(@RequestBody Administrador admin) {
        return ResponseEntity.ok(service.save(admin));
    }
}

package backend.tpi_Napoli_Spadoni_Rojas.operaciones.controllers;

import backend.tpi_Napoli_Spadoni_Rojas.operaciones.services.ContenedorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contenedores")
public class ContenedorController {

    private final ContenedorService contenedorService;

    public ContenedorController(ContenedorService contenedorService) {
        this.contenedorService = contenedorService;
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<Map<String, Object>>> getContenedoresPendientesByDeposito(
            @RequestParam("depositoId") Long depositoId) {
        List<Map<String, Object>> contenedores = contenedorService.getPendientesByDeposito(depositoId);
        return ResponseEntity.ok(contenedores);
    }
}
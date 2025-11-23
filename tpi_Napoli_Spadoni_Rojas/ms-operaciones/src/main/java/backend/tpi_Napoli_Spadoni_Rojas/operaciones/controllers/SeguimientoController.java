package backend.tpi_Napoli_Spadoni_Rojas.operaciones.controllers;

import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.CambioEstadoSolicitud;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.services.CambioEstadoSolicitudService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/seguimientos")
public class SeguimientoController {

    private final CambioEstadoSolicitudService service;

    public SeguimientoController(CambioEstadoSolicitudService service) {
        this.service = service;
    }

    @GetMapping("/solicitud/{solicitudId}")
    public List<CambioEstadoSolicitud> historial(@PathVariable("solicitudId") Long solicitudId) {
        return service.findBySolicitud(solicitudId);
    }
}

package backend.tpi_Napoli_Spadoni_Rojas.geoapi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import backend.tpi_Napoli_Spadoni_Rojas.geoapi.model.DistanceDTO;
import backend.tpi_Napoli_Spadoni_Rojas.geoapi.service.GeoService;

@RestController
@RequestMapping("/api/distancia")
@RequiredArgsConstructor
public class GeoController {
    private final GeoService geoService;

    @GetMapping
    public DistanceDTO obtenerDistancia(
            @RequestParam("origen") String origen,
            @RequestParam("destino") String destino) throws Exception {
        return geoService.calcularDistancia(origen, destino);
    }
}

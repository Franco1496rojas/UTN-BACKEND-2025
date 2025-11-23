package backend.tpi_Napoli_Spadoni_Rojas.operaciones.services;

import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.Estado;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.Solicitud;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.repositories.SolicitudRepository;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ContenedorService {

    private final SolicitudRepository solicitudRepository;

    public ContenedorService(SolicitudRepository solicitudRepository) {
        this.solicitudRepository = solicitudRepository;
    }

    public List<Map<String, Object>> getPendientesByDeposito(Long depositoId) {
        // Buscar solicitudes pendientes relacionadas con el depósito
        List<Solicitud> solicitudesPendientes = solicitudRepository.findByEstadoActual(Estado.PENDIENTE);

        return solicitudesPendientes.stream()
                .map(solicitud -> {
                    Map<String, Object> contenedor = new HashMap<>();
                    contenedor.put("contenedorId", solicitud.getContenedorId());
                    contenedor.put("solicitudId", solicitud.getId());
                    contenedor.put("desde", solicitud.getFechaSolicitud());
                    contenedor.put("origen", solicitud.getOrigen());
                    contenedor.put("destino", solicitud.getDestino());
                    return contenedor;
                })
                .collect(Collectors.toList());
    }
}
package backend.tpi_Napoli_Spadoni_Rojas.operaciones.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.CambioEstadoSolicitud;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.ContenedorPendienteDTO;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.ContenedorSeguimientoDTO;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.Estado;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.EstadoTramo;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.Ruta;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.Solicitud;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.Tramo;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.repositories.SolicitudRepository;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.services.CambioEstadoSolicitudService;

@RestController
@RequestMapping("/api/contenedores")
public class ContenedorSeguimientoController {

    private final SolicitudRepository solicitudRepository;
    private final CambioEstadoSolicitudService cambioEstadoService;

    public ContenedorSeguimientoController(SolicitudRepository solicitudRepository,
                                           CambioEstadoSolicitudService cambioEstadoService) {
        this.solicitudRepository = solicitudRepository;
        this.cambioEstadoService = cambioEstadoService;
    }

    /**
     * Permite consultar el seguimiento (historial de estados) de los transportes
     * de contenedores filtrando por estado actual de la solicitud y/o cliente.
     * GET /api/contenedores?estado=PROGRAMADA&clienteId=1
     * Si no se provee ningún filtro devuelve 400 para evitar respuestas excesivas.
     * Estado: BORRADOR, PENDIENTE, PROGRAMADA, ASIGNADA, EN_TRANSITO, COMPLETADA, CANCELADA
     */
    @GetMapping
    public ResponseEntity<?> listarConSeguimiento(
            @RequestParam(name = "estado", required = false) String estado,
            @RequestParam(name = "clienteId", required = false) Long clienteId) {
        try {
            // Validar que haya al menos un filtro
            if ((estado == null || estado.isBlank()) && clienteId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Debe especificar al menos uno de los filtros: estado o clienteId"));
            }

            // Validar clienteId positivo si se envía
            if (clienteId != null && clienteId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("El clienteId debe ser un número positivo"));
            }

            Estado estadoEnum = null;
            if (estado != null && !estado.isBlank()) {
                try {
                    estadoEnum = Estado.valueOf(estado.trim().toUpperCase());
                } catch (IllegalArgumentException ex) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ErrorResponse("Estado inválido: " + estado + ". Valores permitidos: " + valoresEstado()));
                }
            }

            // Obtener solicitudes según filtros
            List<Solicitud> solicitudes;
            if (estadoEnum != null && clienteId != null) {
                solicitudes = solicitudRepository.findByClienteIdAndEstadoActual(clienteId, estadoEnum);
            } else if (estadoEnum != null) {
                solicitudes = solicitudRepository.findByEstadoActual(estadoEnum);
            } else { // solo clienteId
                solicitudes = solicitudRepository.findByClienteId(clienteId);
            }

            if (solicitudes.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }

            // Construir DTOs: una entrada por solicitud (cada transporte)
            List<ContenedorSeguimientoDTO> respuesta = solicitudes.stream().map(sol -> {
                List<CambioEstadoSolicitud> historial = cambioEstadoService.findBySolicitud(sol.getId());
                return ContenedorSeguimientoDTO.builder()
                        .contenedorId(sol.getContenedorId())
                        .solicitudId(sol.getId())
                        .estadoActual(sol.getEstadoActual())
                        .historial(historial)
                        .build();
            }).collect(Collectors.toList());

            return ResponseEntity.ok(respuesta);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al consultar seguimiento de contenedores: " + ex.getMessage()));
        }
    }

    private String valoresEstado() {
        return String.join(", ", Arrays.stream(Estado.values()).map(Enum::name).toList());
    }

    /**
     * Lista todos los contenedores (transportes) NO ENTREGADOS de un cliente
     * opcionalmente filtrando por estado (excepto COMPLETADA). Endpoint:
     * GET /api/contenedores/{clienteId}?estado=EN_TRANSITO
     */
    @GetMapping("/{clienteId}")
    public ResponseEntity<?> listarNoEntregados(
            @org.springframework.web.bind.annotation.PathVariable("clienteId") Long clienteId,
            @RequestParam(name = "estado", required = false) String estado) {
        try {
            if (clienteId == null || clienteId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("El clienteId debe ser positivo"));
            }

            Estado filtroEstadoTemp = null;
            if (estado != null && !estado.isBlank()) {
                try {
                    filtroEstadoTemp = Estado.valueOf(estado.trim().toUpperCase());
                    if (filtroEstadoTemp == Estado.COMPLETADA) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ErrorResponse("El estado COMPLETADA implica entrega; no se incluye en no entregados"));
                    }
                } catch (IllegalArgumentException ex) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ErrorResponse("Estado inválido: " + estado + ". Valores permitidos (excepto COMPLETADA): " +
                                    Arrays.stream(Estado.values()).filter(e -> e != Estado.COMPLETADA).map(Enum::name).toList()));
                }
            }
            final Estado filtroEstado = filtroEstadoTemp;

            // Obtener todas las solicitudes del cliente excluyendo COMPLETADA
            List<Solicitud> base = solicitudRepository.findByClienteIdAndEstadoActualNot(clienteId, Estado.COMPLETADA);

            // Filtrar por estado si se proporciona
                List<Solicitud> solicitudes = (filtroEstado != null)
                    ? base.stream().filter(s -> s.getEstadoActual() == filtroEstado).toList()
                    : base;

            if (solicitudes.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }

            List<ContenedorPendienteDTO> dtos = solicitudes.stream()
                    .map(this::mapSolicitudAPendiente)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al consultar contenedores no entregados: " + ex.getMessage()));
        }
    }

    private ContenedorPendienteDTO mapSolicitudAPendiente(Solicitud sol) {
        String ubicacion;
        String tipo;
        Integer tramoOrdenActual = null;

        Estado estado = sol.getEstadoActual();

        if (estado == Estado.BORRADOR || estado == Estado.PENDIENTE || estado == Estado.PROGRAMADA) {
            ubicacion = sol.getOrigen();
            tipo = "ORIGEN";
        } else {
            // Buscar ruta principal (tomamos la primera si hay varias)
            Optional<Ruta> rutaOpt = sol.getRutas() != null && !sol.getRutas().isEmpty()
                    ? Optional.of(sol.getRutas().get(0))
                    : Optional.empty();

            if (rutaOpt.isPresent()) {
                List<Tramo> tramos = rutaOpt.get().getTramos();
                if (tramos != null && !tramos.isEmpty()) {
                    // Tramo en curso: INICIADO
                    Optional<Tramo> iniciado = tramos.stream()
                            .filter(t -> t.getEstado() == EstadoTramo.INICIADO)
                            .findFirst();
                    if (iniciado.isPresent()) {
                        Tramo t = iniciado.get();
                        ubicacion = t.getOrigen() + " -> " + t.getDestino();
                        tipo = "EN_TRANSITO";
                        tramoOrdenActual = t.getOrden();
                    } else {
                        // Tramo asignado pero no iniciado
                        Optional<Tramo> asignado = tramos.stream()
                                .filter(t -> t.getEstado() == EstadoTramo.ASIGNADO)
                                .findFirst();
                        if (asignado.isPresent()) {
                            Tramo t = asignado.get();
                            ubicacion = t.getOrigen();
                            tipo = "ORIGEN"; // Aún no salió
                            tramoOrdenActual = t.getOrden();
                        } else {
                            // Todos finalizados pero solicitud no marcada COMPLETADA (inconsistencia)
                            boolean todosFinalizados = tramos.stream()
                                    .allMatch(t -> t.getEstado() == EstadoTramo.FINALIZADO);
                            if (todosFinalizados) {
                                ubicacion = sol.getDestino();
                                tipo = "DESTINO";
                            } else {
                                // Estado ASIGNADA sin tramo iniciado
                                ubicacion = sol.getOrigen();
                                tipo = estado == Estado.EN_TRANSITO ? "ORIGEN" : "ORIGEN";
                            }
                        }
                    }
                } else {
                    ubicacion = sol.getOrigen();
                    tipo = "ORIGEN";
                }
            } else {
                ubicacion = sol.getOrigen();
                tipo = "ORIGEN";
            }
        }

        if (estado == Estado.CANCELADA) {
            ubicacion = "Cancelada en origen: " + sol.getOrigen();
            tipo = "DESCONOCIDA";
        }

        return ContenedorPendienteDTO.builder()
                .contenedorId(sol.getContenedorId())
                .solicitudId(sol.getId())
                .estadoActual(sol.getEstadoActual())
                .origen(sol.getOrigen())
                .destino(sol.getDestino())
                .ubicacionActual(ubicacion)
                .tipoUbicacion(tipo)
                .tramoOrdenActual(tramoOrdenActual)
                .build();
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class ErrorResponse {
        private String mensaje;
    }
}

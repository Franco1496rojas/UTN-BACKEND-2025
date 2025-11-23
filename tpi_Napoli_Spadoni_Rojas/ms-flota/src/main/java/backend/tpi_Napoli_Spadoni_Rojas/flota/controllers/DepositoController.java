package backend.tpi_Napoli_Spadoni_Rojas.flota.controllers;

import backend.tpi_Napoli_Spadoni_Rojas.flota.models.Deposito;
import backend.tpi_Napoli_Spadoni_Rojas.flota.services.DepositoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/depositos")
public class DepositoController {

    private final DepositoService service;

    public DepositoController(DepositoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Deposito>> listar() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable("id") Long id) {
        java.util.Optional<Deposito> optional = service.findById(id);
        if (optional.isPresent()) {
            return ResponseEntity.ok(optional.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("No se encontró un depósito con el ID: " + id));
        }
    }

    // Endpoint para obtener la cantidad de contenedores ocupando el depósito, la cantidad de contenedores disponibles y su capacidad máxima
    @GetMapping("/{id}/contenedores-pendientes")
    public ResponseEntity<?> obtenerCantidadesDeposito(@PathVariable("id") Long id) {
        try {
            java.util.Optional<Deposito> optional = service.findById(id);
            if (optional.isPresent()) {
                Deposito deposito = optional.get();
                int cantidadOcupada = service.contarContenedoresPendientes(id);
                int capacidadMaxima = deposito.getCapacidadMaxima();
                int cantidadDisponible = capacidadMaxima - cantidadOcupada;

                return ResponseEntity.ok(new CantidadesDepositoResponse(cantidadOcupada, cantidadDisponible, capacidadMaxima));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("No se encontró un depósito con el ID: " + id));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al obtener las cantidades del depósito: " + e.getMessage()));
        }
    }
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Deposito deposito) {
        try {
            // Validación: El ID debe ser null para creación
            if (deposito.getId() != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("No se debe especificar un ID al crear un nuevo depósito"));
            }

            // Inicializar cantidadOcupada en 0 si es null
            if (deposito.getCantidadOcupada() == null) {
                deposito.setCantidadOcupada(0);
            }

            Deposito depositoCreado = service.save(deposito);
            return ResponseEntity.status(HttpStatus.CREATED).body(depositoCreado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al crear el depósito: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable("id") Long id, @RequestBody Deposito deposito) {
        try {
            return service.findById(id)
                    .map(existingDeposito -> {
                        deposito.setId(id);
                        try {
                            Deposito depositoActualizado = service.save(deposito);
                            return ResponseEntity.ok(depositoActualizado);
                        } catch (IllegalArgumentException e) {
                            return ResponseEntity.status(HttpStatus.CONFLICT)
                                    .body(new ErrorResponse(e.getMessage()));
                        }
                    })
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ErrorResponse("No se encontró un depósito con el ID: " + id)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al actualizar el depósito: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable("id") Long id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al eliminar el depósito: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para incrementar manualmente la ocupación del depósito
     * @deprecated Usar /asignar-contenedor en su lugar
     */
    @Deprecated
    @PatchMapping("/{id}/incrementar-ocupacion")
    public ResponseEntity<?> incrementarOcupacion(
            @PathVariable("id") Long id,
            @RequestParam(name = "cantidad", defaultValue = "1") int cantidad) {
        try {
            if (cantidad <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("La cantidad debe ser mayor a cero"));
            }

            Deposito deposito = service.incrementarOcupacion(id, cantidad);
            return ResponseEntity.ok(deposito);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al incrementar la ocupación: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para decrementar manualmente la ocupación del depósito
     * @deprecated Usar /retirar-contenedor en su lugar
     */
    @Deprecated
    @PatchMapping("/{id}/decrementar-ocupacion")
    public ResponseEntity<?> decrementarOcupacion(
            @PathVariable("id") Long id,
            @RequestParam(name = "cantidad", defaultValue = "1") int cantidad) {
        try {
            if (cantidad <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("La cantidad debe ser mayor a cero"));
            }

            Deposito deposito = service.decrementarOcupacion(id, cantidad);
            return ResponseEntity.ok(deposito);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al decrementar la ocupación: " + e.getMessage()));
        }
    }

    /**
     * Clase interna para respuestas de error
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class ErrorResponse {
        private String mensaje;
    }

    /**
     * Clase interna para respuestas con cantidades del depósito
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class CantidadesDepositoResponse {
        private int cantidadOcupada;
        private int cantidadDisponible;
        private int capacidadMaxima;
    }
}

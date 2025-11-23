package backend.tpi_Napoli_Spadoni_Rojas.clientes.controllers;

import backend.tpi_Napoli_Spadoni_Rojas.clientes.models.Cliente;
import backend.tpi_Napoli_Spadoni_Rojas.clientes.services.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @GetMapping
    public List<Cliente> listar(@RequestParam(name = "apellido", required = false) String apellido) {
        if (apellido != null && !apellido.isEmpty()) {
            return service.findByApellido(apellido);
        }
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> obtenerPorId(@PathVariable("id") Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Cliente> crear(@RequestBody Cliente cliente) {
        Cliente creado = service.save(cliente);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizar(@PathVariable("id") Long id, @RequestBody Cliente cliente) {
        return service.findById(id)
                .map(c -> {
                    cliente.setId(id);
                    return ResponseEntity.ok(service.save(cliente));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

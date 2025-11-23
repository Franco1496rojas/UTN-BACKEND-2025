package backend.tpi_Napoli_Spadoni_Rojas.clientes.config;

import backend.tpi_Napoli_Spadoni_Rojas.clientes.models.*;
import backend.tpi_Napoli_Spadoni_Rojas.clientes.repositories.*;
import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Profile("dev")
public class DataInitializer {

    private final ProvinciaRepository provinciaRepository;
    private final CiudadRepository ciudadRepository;
    private final ClienteRepository clienteRepository;
    private final ContenedorRepository contenedorRepository;

    public DataInitializer(ProvinciaRepository provinciaRepository,
            CiudadRepository ciudadRepository,
            ClienteRepository clienteRepository,
            ContenedorRepository contenedorRepository) {
        this.provinciaRepository = provinciaRepository;
        this.ciudadRepository = ciudadRepository;
        this.clienteRepository = clienteRepository;
        this.contenedorRepository = contenedorRepository;
    }

    @PostConstruct
    public void init() {
        if (clienteRepository.count() > 0)
            return; // evita duplicar datos al reiniciar

        // --- Provincias
        Provincia cordoba = provinciaRepository.save(
                Provincia.builder().nombre("Córdoba").build());
        Provincia buenosAires = provinciaRepository.save(
                Provincia.builder().nombre("Buenos Aires").build());
        Provincia santaFe = provinciaRepository.save(
                Provincia.builder().nombre("Santa Fe").build());

        // --- Ciudades
        Ciudad cbaCapital = ciudadRepository.save(
                Ciudad.builder().nombre("Córdoba Capital").codigoPostal("5000").provincia(cordoba).build());
        Ciudad rosario = ciudadRepository.save(
                Ciudad.builder().nombre("Rosario").codigoPostal("2000").provincia(santaFe).build());
        Ciudad laPlata = ciudadRepository.save(
                Ciudad.builder().nombre("La Plata").codigoPostal("1900").provincia(buenosAires).build());

        // --- Clientes
        Cliente lucas = clienteRepository.save(
                Cliente.builder()
                        .dni(40111222)
                        .nombre("Lucas")
                        .apellido("Pérez")
                        .email("lucas.perez@example.com")
                        .telefono("3513332222")
                        .domicilio("Av. Colón 1000")
                        .keycloakId("lucas-1")
                        .ciudad(cbaCapital)
                        .build());

        Cliente martina = clienteRepository.save(
                Cliente.builder()
                        .dni(39222333)
                        .nombre("Martina")
                        .apellido("Gómez")
                        .email("martina.gomez@example.com")
                        .telefono("1122334455")
                        .domicilio("Diag. 74 Nº 450")
                        .keycloakId("martina-1")
                        .ciudad(laPlata)
                        .build());

        Cliente roberto = clienteRepository.save(
                Cliente.builder()
                        .dni(38888111)
                        .nombre("Roberto")
                        .apellido("Alonso")
                        .email("roberto.alonso@example.com")
                        .telefono("3415557788")
                        .domicilio("San Martín 850")
                        .keycloakId("roberto-1")
                        .ciudad(rosario)
                        .build());

        // --- Contenedores
        contenedorRepository.saveAll(List.of(
                Contenedor.builder()
                        .codigo("CONT-A1")
                        .peso(1000.0)
                        .volumen(2.5)
                        .cliente(lucas)
                        .build(),
                Contenedor.builder()
                        .codigo("CONT-B1")
                        .peso(750.0)
                        .volumen(1.8)
                        .cliente(martina)
                        .build(),
                Contenedor.builder()
                        .codigo("CONT-C1")
                        .peso(1200.0)
                        .volumen(3.0)
                        .cliente(roberto)
                        .build(),
                Contenedor.builder()
                        .codigo("CONT-C2")
                        .peso(950.0)
                        .volumen(2.2)
                        .cliente(roberto)
                        .build()));

        System.out.println("✅ DataInitializer: datos de prueba cargados correctamente.");
    }
}

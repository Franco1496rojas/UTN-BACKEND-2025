package backend.tpi_Napoli_Spadoni_Rojas.flota.config;

import backend.tpi_Napoli_Spadoni_Rojas.flota.models.*;
import backend.tpi_Napoli_Spadoni_Rojas.flota.repositories.*;
import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("dev")
public class DataInitializer {

    private final TransportistaRepository transportistaRepository;
    private final AdministradorRepository administradorRepository;
    private final CamionRepository camionRepository;
    private final DepositoRepository depositoRepository;
    private final TarifaRangoRepository tarifaRepository;
    private final ParametrosTarifaRepository parametrosRepository;

    public DataInitializer(TransportistaRepository transportistaRepository,
            AdministradorRepository administradorRepository,
            CamionRepository camionRepository,
            DepositoRepository depositoRepository,
            TarifaRangoRepository tarifaRepository,
            ParametrosTarifaRepository parametrosRepository) {
        this.transportistaRepository = transportistaRepository;
        this.administradorRepository = administradorRepository;
        this.camionRepository = camionRepository;
        this.depositoRepository = depositoRepository;
        this.tarifaRepository = tarifaRepository;
        this.parametrosRepository = parametrosRepository;
    }

    @PostConstruct
    public void init() {
        if (camionRepository.count() > 0)
            return; // evita duplicar datos

        // --- Transportistas ---
        Transportista juan = transportistaRepository.save(
                Transportista.builder()
                        .dni(40888222)
                        .nombre("Juan")
                        .apellido("Pérez")
                        .email("juan.perez@flota.com")
                        .telefono("3513334444")
                        .domicilio("Av. Los Transportes 101")
                        .keycloakId("transp-juan")
                        .ciudad("Córdoba")
                        .build());

        Transportista sofia = transportistaRepository.save(
                Transportista.builder()
                        .dni(39999111)
                        .nombre("Sofía")
                        .apellido("Ruiz")
                        .email("sofia.ruiz@flota.com")
                        .telefono("1122233344")
                        .domicilio("Ruta 8 Km 12")
                        .keycloakId("transp-sofia")
                        .ciudad("Rosario")
                        .build());

        // --- Administradores ---
        administradorRepository.saveAll(List.of(
                Administrador.builder()
                        .dni(32111222)
                        .nombre("Carlos")
                        .apellido("López")
                        .email("carlos.lopez@admin.com")
                        .telefono("3511112222")
                        .domicilio("Av. Central 500")
                        .keycloakId("admin-carlos")
                        .ciudad("Córdoba")
                        .build(),
                Administrador.builder()
                        .dni(31222999)
                        .nombre("Laura")
                        .apellido("Martínez")
                        .email("laura.martinez@admin.com")
                        .telefono("1122445566")
                        .domicilio("Calle Mitre 123")
                        .keycloakId("admin-laura")
                        .ciudad("Buenos Aires")
                        .build()));

        // --- Camiones ---
        camionRepository.saveAll(List.of(
                Camion.builder()
                        .dominio("AA123BB")
                        .capacidadPeso(12000.0)
                        .capacidadVolumen(25.0)
                        .disponibilidad(true)
                        .costoKmBase(80.0)
                        .consumoLitroKm(0.3)
                        .transportista(juan)
                        .build(),
                Camion.builder()
                        .dominio("AB987CD")
                        .capacidadPeso(9000.0)
                        .capacidadVolumen(20.0)
                        .disponibilidad(true)
                        .costoKmBase(70.0)
                        .consumoLitroKm(0.25)
                        .transportista(juan)
                        .build(),
                Camion.builder()
                        .dominio("AC555EE")
                        .capacidadPeso(15000.0)
                        .capacidadVolumen(30.0)
                        .disponibilidad(false)
                        .costoKmBase(100.0)
                        .consumoLitroKm(0.35)
                        .transportista(sofia)
                        .build()));

        // --- Depósitos ---
        depositoRepository.saveAll(List.of(
                Deposito.builder()
                        .nombre("Depósito Córdoba")
                        .direccion("Av. Circunvalación 3500")
                        .latitud(-31.4201)
                        .longitud(-64.1888)
                        .costoEstadiaDiaria(2500.0)
                        .estado(true)
                        .build(),
                Deposito.builder()
                        .nombre("Depósito Rosario")
                        .direccion("Bv. Oroño 2500")
                        .latitud(-32.9442)
                        .longitud(-60.6505)
                        .costoEstadiaDiaria(2300.0)
                        .estado(true)
                        .build(),
                Deposito.builder()
                        .nombre("Depósito Buenos Aires")
                        .direccion("Panamericana Km 45")
                        .latitud(-34.6037)
                        .longitud(-58.3816)
                        .costoEstadiaDiaria(2700.0)
                        .estado(true)
                        .build()));

        // --- Tarifas por rango de volumen y peso ---
        tarifaRepository.saveAll(List.of(
                TarifaRango.builder()
                        .volumenMin(0.0)
                        .volumenMax(10.0)
                        .pesoMin(0.0)
                        .pesoMax(5000.0)
                        .costoKmBase(70.0)
                        .build(),
                TarifaRango.builder()
                        .volumenMin(10.01)
                        .volumenMax(20.0)
                        .pesoMin(5000.01)
                        .pesoMax(10000.0)
                        .costoKmBase(90.0)
                        .build(),
                TarifaRango.builder()
                        .volumenMin(20.01)
                        .volumenMax(40.0)
                        .pesoMin(10000.01)
                        .pesoMax(20000.0)
                        .costoKmBase(110.0)
                        .build()));

        // --- Parámetros globales de tarifa ---
        parametrosRepository.save(
                ParametrosTarifa.builder()
                        .precioLitroCombustible(1300.0)
                        .cargoFijoTramo(2500.0)
                        .build());

        System.out.println("✅ DataInitializer (ms-flota): datos de prueba cargados correctamente.");
    }
}

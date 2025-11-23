package backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations;

import backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FlotaApiClient {

    private final RestClient.Builder builder;

    @Value("${flota.base.url}")
    private String flotaBase;

    @Value("${flota.camiones.path}")
    private String camionesPath;

    @Value("${flota.tarifas.path}")
    private String tarifasPath;

    @Value("${flota.parametros.path}")
    private String parametrosPath;

    @Value("${flota.depositos.path:/api/depositos}")
    private String depositosPath;

    // --- Obtener un camión por ID ---
    public CamionDTO getCamion(Long camionId) {
        String url = flotaBase + camionesPath + "/" + camionId;
        try {
            ResponseEntity<CamionDTO> resp = builder.build()
                    .get().uri(url)
                    .retrieve()
                    .toEntity(CamionDTO.class);
            return resp.getBody();
        } catch (Exception e) {
            System.err.println("❌ Error consultando camión: " + e.getMessage());
            return null;
        }
    }

    // --- Obtener parámetros globales ---
    public ParametrosTarifaDTO getParametros() {
        String url = flotaBase + parametrosPath;
        try {
            ResponseEntity<ParametrosTarifaDTO[]> resp = builder.build()
                    .get().uri(url)
                    .retrieve()
                    .toEntity(ParametrosTarifaDTO[].class);
            return resp.getBody() != null && resp.getBody().length > 0 ? resp.getBody()[0] : null;
        } catch (Exception e) {
            System.err.println("❌ Error consultando parámetros: " + e.getMessage());
            return null;
        }
    }

    // --- Obtener tarifas por rango ---
    public List<TarifaRangoDTO> getTarifas() {
        String url = flotaBase + tarifasPath;
        try {
            ResponseEntity<TarifaRangoDTO[]> resp = builder.build()
                    .get().uri(url)
                    .retrieve()
                    .toEntity(TarifaRangoDTO[].class);
            return Arrays.asList(resp.getBody());
        } catch (Exception e) {
            System.err.println("❌ Error consultando tarifas: " + e.getMessage());
            return List.of();
        }
    }

    // --- Obtener todos los depósitos ---
    public List<DepositoDTO> getDepositos() {
        String url = flotaBase + depositosPath;
        try {
            ResponseEntity<DepositoDTO[]> resp = builder.build()
                    .get().uri(url)
                    .retrieve()
                    .toEntity(DepositoDTO[].class);
            return resp.getBody() != null ? Arrays.asList(resp.getBody()) : List.of();
        } catch (Exception e) {
            System.err.println("❌ Error consultando depósitos: " + e.getMessage());
            return List.of();
        }
    }

    // --- Obtener un depósito por ID ---
    public DepositoDTO getDeposito(Long depositoId) {
        String url = flotaBase + depositosPath + "/" + depositoId;
        try {
            ResponseEntity<DepositoDTO> resp = builder.build()
                    .get().uri(url)
                    .retrieve()
                    .toEntity(DepositoDTO.class);
            return resp.getBody();
        } catch (Exception e) {
            System.err.println("❌ Error consultando depósito: " + e.getMessage());
            return null;
        }
    }

    // --- Obtener camiones disponibles con filtros de capacidad ---
    public List<CamionDTO> getCamionesDisponibles(Double pesoMaximo, Double volumenMaximo) {
        StringBuilder url = new StringBuilder(flotaBase + camionesPath + "/disponibles?");
        
        if (pesoMaximo != null) {
            url.append("pesoMaximo=").append(pesoMaximo);
        }
        if (volumenMaximo != null) {
            if (pesoMaximo != null) {
                url.append("&");
            }
            url.append("volumenMaximo=").append(volumenMaximo);
        }
        
        try {
            ResponseEntity<CamionDTO[]> resp = builder.build()
                    .get().uri(url.toString())
                    .retrieve()
                    .toEntity(CamionDTO[].class);
            return resp.getBody() != null ? Arrays.asList(resp.getBody()) : List.of();
        } catch (Exception e) {
            System.err.println("❌ Error consultando camiones disponibles: " + e.getMessage());
            return List.of();
        }
    }

    // --- Marcar disponibilidad de un camión ---
    public void actualizarDisponibilidadCamion(Long camionId, boolean disponible) {
        // Corregido: la URL correcta según CamionController es PUT /api/camiones/disponibilidad?camionId={id}&disponible={true/false}
        String url = flotaBase + camionesPath + "/disponibilidad?camionId=" + camionId + "&disponible=" + disponible;
        try {
            builder.build()
                    .put()
                    .uri(url)
                    .retrieve()
                    .toBodilessEntity();
            System.out.println("✅ Disponibilidad del camión " + camionId + " actualizada a: " + disponible);
        } catch (Exception e) {
            System.err.println("❌ Error actualizando disponibilidad del camión: " + e.getMessage());
        }
    }

    // --- Obtener camiones por transportista ---
    public List<CamionDTO> getCamionesPorTransportista(Long transportistaId) {
        String url = flotaBase + camionesPath + "?transportistaId=" + transportistaId;
        try {
            ResponseEntity<CamionDTO[]> resp = builder.build()
                    .get().uri(url)
                    .retrieve()
                    .toEntity(CamionDTO[].class);
            return resp.getBody() != null ? Arrays.asList(resp.getBody()) : List.of();
        } catch (Exception e) {
            System.err.println("❌ Error consultando camiones por transportista: " + e.getMessage());
            return List.of();
        }
    }
}

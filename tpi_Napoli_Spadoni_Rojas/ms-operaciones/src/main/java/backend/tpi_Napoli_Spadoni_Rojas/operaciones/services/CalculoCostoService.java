package backend.tpi_Napoli_Spadoni_Rojas.operaciones.services;

import backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.*;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.dto.*;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class CalculoCostoService {

    private final FlotaApiClient flotaApiClient;
    private final ClientesApiClient clientesApiClient;

    public CalculoCostoService(FlotaApiClient flotaApiClient, ClientesApiClient clientesApiClient) {
        this.flotaApiClient = flotaApiClient;
        this.clientesApiClient = clientesApiClient;
    }

    public Double calcularCostoTramo(Long camionId, Long contenedorId, Double distanciaKm) {
        if (camionId == null || distanciaKm == null || distanciaKm <= 0)
            return 0.0;

        // 1️⃣ Traer datos de camión
        CamionDTO camion = flotaApiClient.getCamion(camionId);
        if (camion == null)
            return 0.0;

        // 2️⃣ Traer contenedor (peso y volumen)
        ContenedorDTO contenedor = clientesApiClient.getContenedor(contenedorId);
        double peso = contenedor != null ? contenedor.getPeso() : 1000.0;
        double volumen = contenedor != null ? contenedor.getVolumen() : 2.0;

        // 3️⃣ Traer tarifas y parámetros
        ParametrosTarifaDTO parametros = flotaApiClient.getParametros();
        var tarifas = flotaApiClient.getTarifas();

        Optional<TarifaRangoDTO> rangoOpt = tarifas.stream()
                .filter(t -> volumen >= t.getVolumenMin() && volumen <= t.getVolumenMax()
                        && peso >= t.getPesoMin() && peso <= t.getPesoMax())
                .findFirst();

        double costoKmTarifa = rangoOpt.map(TarifaRangoDTO::getCostoKmBase).orElse(0.0);
        double costoKmCamion = camion.getCostoKmBase() != null ? camion.getCostoKmBase() : 0.0;
        double consumo = camion.getConsumoLitroKm() != null ? camion.getConsumoLitroKm() : 0.0;
        double precioCombustible = parametros != null ? parametros.getPrecioLitroCombustible() : 0.0;
        double cargoFijo = parametros != null ? parametros.getCargoFijoTramo() : 0.0;

        // 4️⃣ Cálculo del costo total
        double costoCombustible = distanciaKm * consumo * precioCombustible;
        double costoDistancia = distanciaKm * (costoKmTarifa + costoKmCamion);
        double total = costoDistancia + costoCombustible + cargoFijo;

        return Math.round(total * 100.0) / 100.0;
    }
}

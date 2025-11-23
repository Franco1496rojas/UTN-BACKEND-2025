package backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class GeoApiClient {

    private final RestClient.Builder builder;

    @Value("${geoapi.service.url}")
    private String geoapiUrl;

    public double calcularDistanciaKm(String origen, String destino) {
        try {
            // Construye la URL de forma segura (encode de query params)
            String url = UriComponentsBuilder.fromHttpUrl(geoapiUrl)
                    .queryParam("origen", origen)
                    .queryParam("destino", destino)
                    .toUriString();

            ResponseEntity<String> response = builder.build()
                    .get()
                    .uri(url)
                    .retrieve()
                    .toEntity(String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            // Si el endpoint de ms-geoapi devuelve DistanciaDTO, parseá directo:
            if (root.has("kilometros")) {
                return root.get("kilometros").asDouble();
            }

            // (fallback por si algún día pegás directo a Distance Matrix)
            JsonNode leg = root.path("rows").get(0).path("elements").get(0);
            return leg.path("distance").path("value").asDouble() / 1000.0;
        } catch (Exception ex) {
            // Fallback seguro: 0 km (y que el costo se calcule neutro)
            System.err.println("GeoApiClient error: " + ex.getMessage());
            return 0.0;
        }
    }
}

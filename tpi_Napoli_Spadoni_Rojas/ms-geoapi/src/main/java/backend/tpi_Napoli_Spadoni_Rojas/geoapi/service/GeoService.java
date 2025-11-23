package backend.tpi_Napoli_Spadoni_Rojas.geoapi.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import backend.tpi_Napoli_Spadoni_Rojas.geoapi.model.DistanceDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class GeoService {
        @Value("${google.maps.apikey}")
        private String apiKey;

        private final RestClient.Builder builder;

        private static final Pattern COORD_PATTERN = Pattern
                        .compile("^[-+]?\\d+(\\.\\d+)?\\s*,\\s*[-+]?\\d+(\\.\\d+)?$");

        public DistanceDTO calcularDistancia(String origen, String destino) throws Exception {
                RestClient client = builder.baseUrl("https://maps.googleapis.com/maps/api").build();

                boolean origenCoord = esCoordenada(origen);
                boolean destinoCoord = esCoordenada(destino);

                // 1er intento
                DistanceDTO dto = consultar(client, origen, destino, origenCoord, destinoCoord);
                if (dto.getKilometros() > 0)
                        return dto;

                // Si son coordenadas, no reintentar con país; usar fallback Haversine
                if (origenCoord && destinoCoord) {
                        double kms = fallbackHaversine(true, true, origen, destino);
                        return build(origen, destino, kms, kms > 0 ? "N/A" : "N/A");
                }

                // Reintento agregando país para direcciones
                String o2 = origen + ", Argentina";
                String d2 = destino + ", Argentina";
                DistanceDTO retry = consultar(client, o2, d2, esCoordenada(o2), esCoordenada(d2));
                return retry.getKilometros() > 0 ? retry : dto;
        }

        private boolean esCoordenada(String v) {
                return v != null && COORD_PATTERN.matcher(v.trim()).matches();
        }

        private DistanceDTO consultar(RestClient client,
                        String origen,
                        String destino,
                        boolean origenCoord,
                        boolean destinoCoord) {
                try {
                        String origenParam = origenCoord ? limpiarCoordenada(origen)
                                        : URLEncoder.encode(origen, StandardCharsets.UTF_8);
                        String destinoParam = destinoCoord ? limpiarCoordenada(destino)
                                        : URLEncoder.encode(destino, StandardCharsets.UTF_8);

                        String url = "/distancematrix/json?origins=" + origenParam +
                                        "&destinations=" + destinoParam +
                                        "&units=metric&language=es&region=ar&key=" + apiKey;

                        ResponseEntity<String> response = client.get().uri(url).retrieve().toEntity(String.class);
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode root = mapper.readTree(response.getBody());

                        String status = root.path("status").asText();
                        if (!"OK".equals(status)) {
                                return build(origen, destino, 0.0, "N/A");
                        }

                        JsonNode elem = root.path("rows").get(0).path("elements").get(0);
                        String elemStatus = elem.path("status").asText();
                        if (!"OK".equals(elemStatus)) {
                                return build(origen, destino, 0.0, "N/A");
                        }

                        double kms = elem.path("distance").path("value").asDouble() / 1000.0;
                        String dur = elem.path("duration").path("text").asText();
                        return build(origen, destino, kms, dur);
                } catch (Exception ex) {
                        double kms = fallbackHaversine(origenCoord, destinoCoord, origen, destino);
                        return build(origen, destino, kms, kms > 0 ? "N/A" : "N/A");
                }
        }

        private String limpiarCoordenada(String c) {
                return c.trim().replace(" ", "");
        }

        private double fallbackHaversine(boolean oc, boolean dc, String o, String d) {
                if (!(oc && dc))
                        return 0.0;
                try {
                        String[] p1 = o.split(",");
                        String[] p2 = d.split(",");
                        double lat1 = Double.parseDouble(p1[0].trim());
                        double lon1 = Double.parseDouble(p1[1].trim());
                        double lat2 = Double.parseDouble(p2[0].trim());
                        double lon2 = Double.parseDouble(p2[1].trim());
                        double R = 6371.0;
                        double dLat = Math.toRadians(lat2 - lat1);
                        double dLon = Math.toRadians(lon2 - lon1);
                        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                                        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                                                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
                        double cVal = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                        return Math.round(R * cVal * 100.0) / 100.0;
                } catch (Exception e) {
                        return 0.0;
                }
        }

        private DistanceDTO build(String origen, String destino, double kms, String dur) {
                DistanceDTO dto = new DistanceDTO();
                dto.setOrigen(origen);
                dto.setDestino(destino);
                dto.setKilometros(kms);
                dto.setDuracionTexto(dur);
                return dto;
        }
}
package backend.tpi_Napoli_Spadoni_Rojas.flota.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Configuration
public class TimezoneConfig {

    @PostConstruct
    public void init() {
        // Establecer la zona horaria por defecto a UTC
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        System.out.println("🌍 Timezone configurado a: " + TimeZone.getDefault().getID());
    }
}

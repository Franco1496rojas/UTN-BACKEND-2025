package backend.tpi_Napoli_Spadoni_Rojas.operaciones.repositories;

import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RutaRepository extends JpaRepository<Ruta, Long> {
    List<Ruta> findBySolicitudId(Long solicitudId);

    @Query("select coalesce(sum(r.costoTotal),0) from Ruta r where r.solicitud.id = :solId")
    Double sumCostoBySolicitud(@Param("solId") Long solId);

    @Query("select coalesce(sum(r.distanciaTotalKm),0) from Ruta r where r.solicitud.id = :solId")
    Double sumDistanciaBySolicitud(@Param("solId") Long solId);

}

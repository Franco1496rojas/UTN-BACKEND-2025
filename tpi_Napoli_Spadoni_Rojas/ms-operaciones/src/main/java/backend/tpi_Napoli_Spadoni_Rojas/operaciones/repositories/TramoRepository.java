package backend.tpi_Napoli_Spadoni_Rojas.operaciones.repositories;

import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.EstadoTramo;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.Tramo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TramoRepository extends JpaRepository<Tramo, Long> {
    List<Tramo> findByRutaId(Long rutaId);

    @Query("select coalesce(sum(t.costo),0) from Tramo t where t.ruta.id = :rutaId")
    Double sumCostoByRuta(@Param("rutaId") Long rutaId);

    @Query("select coalesce(sum(t.distanciaKm),0) from Tramo t where t.ruta.id = :rutaId")
    Double sumDistanciaByRuta(@Param("rutaId") Long rutaId);

    List<Tramo> findByCamionIdIn(List<Long> camionIds);

    List<Tramo> findByCamionIdInAndEstado(List<Long> camionIds, EstadoTramo estado);

    
}

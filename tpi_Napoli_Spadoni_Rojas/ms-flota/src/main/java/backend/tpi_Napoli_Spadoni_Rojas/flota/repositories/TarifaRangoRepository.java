package backend.tpi_Napoli_Spadoni_Rojas.flota.repositories;

import backend.tpi_Napoli_Spadoni_Rojas.flota.models.TarifaRango;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TarifaRangoRepository extends JpaRepository<TarifaRango, Long> {
    Optional<TarifaRango> findByVolumenMinLessThanEqualAndVolumenMaxGreaterThanEqualAndPesoMinLessThanEqualAndPesoMaxGreaterThanEqual(
            Double volumen, Double volumen2, Double peso, Double peso2);

    /**
     * Busca tarifas que se solapen con los rangos proporcionados
     * Se considera solapamiento si los rangos se intersectan en volumen Y peso
     */
    @Query("SELECT t FROM TarifaRango t WHERE " +
           "(:id IS NULL OR t.id != :id) AND " +
           "(t.volumenMin < :volumenMax AND t.volumenMax > :volumenMin) AND " +
           "(t.pesoMin < :pesoMax AND t.pesoMax > :pesoMin)")
    List<TarifaRango> findOverlappingRangos(
            @Param("id") Long id,
            @Param("volumenMin") Double volumenMin,
            @Param("volumenMax") Double volumenMax,
            @Param("pesoMin") Double pesoMin,
            @Param("pesoMax") Double pesoMax
    );
}

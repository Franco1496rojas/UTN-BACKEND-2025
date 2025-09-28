package com.frc.isi.museo.entidades;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@NamedQueries({
        @NamedQuery(name = "EstiloArtistico.getAllWithObras", query = "SELECT e FROM EstiloArtistico e LEFT JOIN FETCH e.obras")
})
public class EstiloArtistico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nombre;

    @OneToMany(mappedBy = "estiloArtistico", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ObraArtistica> obras = new HashSet<>();

    public void addObra(ObraArtistica obra) {
        this.obras.add(obra);
        obra.setEstiloArtistico(this);
    }
}

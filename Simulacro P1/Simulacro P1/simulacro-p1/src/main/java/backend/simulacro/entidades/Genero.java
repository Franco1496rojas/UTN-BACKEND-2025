package backend.simulacro.entidades;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "GENEROS")
@EqualsAndHashCode(of = "id")
public class Genero {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "GEN_ID")
  private Integer id;

  @Column(name = "NOMBRE")
  private String nombre;

  // Un genero tiene muchos juegos
  @OneToMany(mappedBy = "genero", fetch = FetchType.LAZY)
  @ToString.Exclude
  private Set<Juego> juegos = new HashSet<>();

  public void añadirJuego(Juego juego) {
    this.juegos.add(juego);
    juego.setGenero(this);
  }

}

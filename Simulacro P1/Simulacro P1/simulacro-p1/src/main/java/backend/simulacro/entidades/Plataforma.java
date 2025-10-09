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
@Table(name = "PLATAFORMAS")
@Entity
@EqualsAndHashCode(of = "id")
public class Plataforma {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "PLAT_ID")
  private Integer id;

  @Column(name = "NOMBRE")
  private String nombre;

  // Una platforma tiene muchos juegos
  @OneToMany(mappedBy = "plataforma", fetch = FetchType.LAZY)
  @ToString.Exclude
  private Set<Juego> juegos = new HashSet<>();

  public void añadirJuego(Juego juego) {
    this.juegos.add(juego);
    juego.setPlataforma(this);
  }
}

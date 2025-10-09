package backend.simulacro.entidades;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "JUEGOS")
@EqualsAndHashCode(of = "id")
public class Juego {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "JUEGO_ID")
  private Integer id;

  @Column(name = "TITULO")
  private String titulo;

  @Column(name = "FECHA_LANZAMIENTO")
  private Long fechaLanzamiento;

  @Column(name = "CLASIFICACION_ESRB")
  // Indica a JPA la clase a usar para convertir de enum a valor en la BD y
  // viceversa
  @Convert(converter = ClasificacionESRBConverter.class)
  private ClasificacionESRB clasificacionESRB;

  @Column(name = "RATING")
  private Double rating;

  @Column(name = "JUEGOS_FINALIZADOS")
  private Integer juegosFinalizados;

  @Column(name = "JUGANDO")
  private Integer jugando;
  // Clob representa un tipo de dato SQL para almacenar grandes cantidades de
  // texto, de mayor tamaño que un VARCHAR estándar.
  @Column(name = "RESUMEN", columnDefinition = "CLOB")
  private String resumen;

  // -------------- RELACIONES --------------
  // Un juego tiene un genero
  @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
  @JoinColumn(name = "GENERO_ID")
  @ToString.Exclude
  private Genero genero;

  // Un juego tiene una plataforma
  @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
  @JoinColumn(name = "PLATAFORMA_ID")
  @ToString.Exclude
  private Plataforma plataforma;

  // Un juego tiene un desarrollador
  @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
  @JoinColumn(name = "DESARROLLADOR_ID")
  @ToString.Exclude
  private Desarrollador desarrollador;
}

package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tarea_programa")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TareaPrograma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK → SubPresupuesto */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subpresupuesto_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_tarea_sub_presupuesto"))
    private SubPresupuesto subPresupuesto;

    /** Opcional: proviene de una Partida */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id",
            foreignKey = @ForeignKey(name = "fk_tarea_partida"))
    private Partida partida;

    private String nombre;

    @Enumerated(EnumType.STRING)
    private TipoTareaPrograma tipo;

    @Column(name = "duracion_dias")
    private BigDecimal duracionDias;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "porcentaje_avance")
    private BigDecimal porcentajeAvance;

    /** Calendario en el que se programa esta tarea */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendario_id",
            foreignKey = @ForeignKey(name = "fk_tarea_calendario"))
    private Calendario calendario;

    private Integer orden;

    @Column(name = "es_ruta_critica")
    private Boolean esRutaCritica = false;
}

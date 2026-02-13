package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "calendario_excepcion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CalendarioExcepcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK → Calendario */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendario_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_calexc_calendario"))
    private Calendario calendario;

    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    private CalendarioExcepcionTipo tipo;

    private String descripcion;
}

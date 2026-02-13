package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tarea_dependencia")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TareaDependencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Predecesora */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predecesora_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_dep_predecesora"))
    private TareaPrograma predecesora;

    /** Sucesora */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucesora_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_dep_sucesora"))
    private TareaPrograma sucesora;

    @Enumerated(EnumType.STRING)
    private TipoDependencia tipo;

    /** Desfase (lead/lag), por ejemplo en días */
    private BigDecimal desfase;
}

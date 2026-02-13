package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "partida_total_categoria")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PartidaTotalCategoria {

    @EmbeddedId
    private PartidaTotalCategoriaId id;

    @ManyToOne(fetch = FetchType.LAZY) @MapsId("partidaId")
    @JoinColumn(name = "partida_id",
            foreignKey = @ForeignKey(name = "fk_ptc_partida"))
    private Partida partida;

    @ManyToOne(fetch = FetchType.LAZY) @MapsId("categoriaCostoId")
    @JoinColumn(name = "categoria_costo_id",
            foreignKey = @ForeignKey(name = "fk_ptc_categoria"))
    private CategoriaCosto categoriaCosto;

    /** por 1 unidad, derivado de líneas */
    @Builder.Default
    @Column(name = "unitario_calc", nullable = false)
    private BigDecimal unitarioCalc = BigDecimal.ZERO;

    /** unitario_calc × metrado */
    @Builder.Default
    @Column(name = "total_calc", nullable = false)
    private BigDecimal totalCalc = BigDecimal.ZERO;

    @Column(name = "unitario_override")
    private BigDecimal unitarioOverride;

    @Builder.Default
    @Column(name = "usar_override", nullable = false)
    private Boolean usarOverride = false;

    @PrePersist
    public void prePersist() {
        if (unitarioCalc == null) unitarioCalc = BigDecimal.ZERO;
        if (totalCalc == null) totalCalc = BigDecimal.ZERO;
        if (usarOverride == null) usarOverride = false;
    }
}

package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "factor_conversion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FactorConversion {

    @EmbeddedId
    private FactorConversionId id;

    @ManyToOne(fetch = FetchType.LAZY) @MapsId("unidadOrigenId")
    @JoinColumn(name = "unidad_origen_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_factorconv_unidad_origen"))
    private Unidad unidadOrigen;

    @ManyToOne(fetch = FetchType.LAZY) @MapsId("unidadDestinoId")
    @JoinColumn(name = "unidad_destino_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_factorconv_unidad_destino"))
    private Unidad unidadDestino;

    @Column(nullable = false)
    private BigDecimal factor;
}

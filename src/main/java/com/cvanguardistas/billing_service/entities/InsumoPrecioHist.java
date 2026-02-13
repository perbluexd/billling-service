package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "insumo_precio_hist",
        indexes = {
                @Index(name = "idx_insumo_hist_insumo_fecha",
                        columnList = "insumo_id, vigente_desde DESC")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InsumoPrecioHist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insumo_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_inspreciohist_insumo"))
    private Insumo insumo;

    @Column(nullable = false)
    private BigDecimal precio;

    @Column(name = "vigente_desde", nullable = false)
    private LocalDateTime vigenteDesde; // puedes cambiar a LocalDate si prefieres solo fecha

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FuentePrecioHist fuente;
}

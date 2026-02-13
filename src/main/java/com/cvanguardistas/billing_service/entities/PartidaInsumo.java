package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "partida_insumo")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Check(constraints =
        "((Subpartida_id IS NULL OR Subpartida_id <> partida_id)) AND " +
                "((depende_de_rendimiento AND cuadrilla_frac IS NOT NULL AND cantidad_fija IS NULL) OR " +
                " (NOT depende_de_rendimiento AND cantidad_fija IS NOT NULL AND cuadrilla_frac IS NULL))")
public class PartidaInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Debe pertenecer a una Partida HOJA (validar en servicio) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pi_partida"))
    private Partida partida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insumo_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pi_insumo"))
    private Insumo insumo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_costo_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pi_categoria_costo"))
    private CategoriaCosto categoriaCosto;

    @Column(name = "depende_de_rendimiento", nullable = false)
    private Boolean dependeDeRendimiento = false;

    @Column(name = "cuadrilla_frac")
    private BigDecimal cuadrillaFrac;

    /** cantidad calculada y/o editable según política */
    private BigDecimal cantidad;

    @Column(name = "cantidad_fija")
    private BigDecimal cantidadFija;

    /** precio unitario efectivo usado */
    private BigDecimal pu;

    @Column(name = "pu_override")
    private BigDecimal puOverride;

    @Column(name = "usar_pu_override", nullable = false)
    private Boolean usarPuOverride = false;

    @Column(name = "pu_congelado")
    private BigDecimal puCongelado;

    /** parcial = cantidad × pu */
    private BigDecimal parcial;

    /** si esta línea referencia otra partida */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Subpartida_id",
            foreignKey = @ForeignKey(name = "fk_pi_Subpartida"))
    private Partida Subpartida;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @PrePersist
    public void prePersist() {
        this.creadoEn = LocalDateTime.now();
        this.actualizadoEn = this.creadoEn;
        if (usarPuOverride == null) usarPuOverride = false;
        if (dependeDeRendimiento == null) dependeDeRendimiento = false;
    }

    @PreUpdate
    public void preUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }
}

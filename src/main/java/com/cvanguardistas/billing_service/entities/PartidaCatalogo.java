package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "partida_catalogo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartidaCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 64, unique = true)
    private String codigo;

    // TITULO / SubTITULO / HOJA
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoPartida tipo;

    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "unidad_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_partida_cat_unidad")
    )
    private Unidad unidad;

    @Column(nullable = false)
    private BigDecimal rendimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "padre_id",
            foreignKey = @ForeignKey(name = "fk_partida_cat_padre")
    )
    private PartidaCatalogo padre;

    @Column(name = "cu_estimado")
    private BigDecimal cuEstimado;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    private Integer orden;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @PrePersist
    public void prePersist() {
        this.creadoEn = LocalDateTime.now();
        this.actualizadoEn = this.creadoEn;

        // Defaults defensivos para evitar inserts con NULL
        if (this.activo == null) {
            this.activo = true;
        }
        if (this.rendimiento == null) {
            this.rendimiento = new BigDecimal("1.00");
        }
        if (this.orden == null) {
            this.orden = 1;
        }
        if (this.tipo == null) {
            this.tipo = TipoPartida.HOJA; // valor por defecto razonable
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.actualizadoEn = LocalDateTime.now();
        // Si en updates llegaran nulos por error, los saneamos igual
        if (this.rendimiento == null) {
            this.rendimiento = new BigDecimal("1.00");
        }
        if (this.orden == null) {
            this.orden = 1;
        }
        if (this.tipo == null) {
            this.tipo = TipoPartida.HOJA;
        }
        if (this.activo == null) {
            this.activo = true;
        }
    }
}

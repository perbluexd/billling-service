package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "insumo")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_insumo_unidad"))
    private Unidad unidad;

    @Column(name = "precio_base")
    private BigDecimal precioBase; // puede ser NULL si solo usas histórico

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_insumo_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_insumo_tipoinsumo"))
    private TipoInsumo tipoInsumo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indice_unificado_id",
            foreignKey = @ForeignKey(name = "fk_insumo_indiceunificado"))
    private IndiceUnificado indiceUnificado; // opcional

    @Column(name = "color_hex")
    private String colorHex;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @PrePersist
    public void prePersist() {
        this.creadoEn = LocalDateTime.now();
        this.actualizadoEn = this.creadoEn;
    }

    @PreUpdate
    public void preUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }
}

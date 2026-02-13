package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "producto_tienda")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductoTienda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK → ProveedorTienda */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_tienda_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_producto_tienda_proveedor"))
    private ProveedorTienda proveedorTienda;

    /** SKU externo que brinda la tienda */
    @Column(name = "sku_externo")
    private String skuExterno;

    @Column(name = "codigo_barra")
    private String codigoBarra; // opcional

    private String titulo;

    @Column(name = "url_producto")
    private String urlProducto;

    /** Unidad como aparece en la tienda (texto libre) */
    @Column(name = "unidad_listada")
    private String unidadListada;

    /** Cantidad por pack (si aplica) */
    @Column(name = "cantidad_por_pack")
    private BigDecimal cantidadPorPack;

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
        if (activo == null) activo = true;
    }

    @PreUpdate
    public void preUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }
}

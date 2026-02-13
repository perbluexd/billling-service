package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "proveedor_tienda",
        uniqueConstraints = @UniqueConstraint(name = "uk_proveedor_tienda_nombre", columnNames = "nombre"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProveedorTienda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre del proveedor (ENUM) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private ProveedorNombre nombre;

    private String pais;

    /** Código de moneda (e.g., PEN, USD) */
    private String moneda;

    @Column(name = "url_base")
    private String urlBase;

    @Column(nullable = false)
    private Boolean activo = true;
}

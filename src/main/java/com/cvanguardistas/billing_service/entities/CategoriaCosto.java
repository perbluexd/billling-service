package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categoria_costo",
        uniqueConstraints = @UniqueConstraint(name = "uk_categoria_costo_codigo", columnNames = "codigo"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CategoriaCosto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** MO, MT, EQ, SC, SP, ... */
    @Column(nullable = false, unique = true)
    private String codigo;

    private String nombre;

    @Column(name = "color_hex")
    private String colorHex;

    private Integer orden;

    /** Si suma en CU */
    @Column(name = "incluye_en_cu", nullable = false)
    private Boolean incluyeEnCu = true;

    @Column(nullable = false)
    private Boolean visible = true;
}

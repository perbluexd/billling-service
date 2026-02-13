package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipo_insumo",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tipo_insumo_codigo", columnNames = "codigo")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TipoInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // MO, MT, EQ, SC, SP...
    @Column(nullable = false, unique = true, length = 16)
    private String codigo;

    // MANO DE OBRA, MATERIAL, EQUIPO, SERVICIO, ...
    @Column(nullable = false)
    private String nombre;

    @Column(name = "color_hex")
    private String colorHex;

    private Integer orden;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @PrePersist
    public void prePersist() {
        if (activo == null) activo = true;
    }
}

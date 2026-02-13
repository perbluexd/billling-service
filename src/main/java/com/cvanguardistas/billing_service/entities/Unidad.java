package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "unidad",
        uniqueConstraints = @UniqueConstraint(name = "uk_unidad_codigo", columnNames = "codigo"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Unidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo; // HH, KG, M3, UND, ML, P2, GAL, etc.

    private String descripcion;
}

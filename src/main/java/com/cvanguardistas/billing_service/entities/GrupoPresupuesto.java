package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "grupo_presupuesto",
        uniqueConstraints = @UniqueConstraint(name = "uk_grupo_presupuesto_nombre", columnNames = "nombre"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GrupoPresupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;
}

package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "indice_unificado",
        uniqueConstraints = @UniqueConstraint(name = "uk_indice_unificado_codigo", columnNames = "codigo"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IndiceUnificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo; // p.ej., "47"

    private String descripcion; // p.ej., "MANO DE OBRA"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_insumo_id")
    private TipoInsumo tipoInsumo; // opcional
}

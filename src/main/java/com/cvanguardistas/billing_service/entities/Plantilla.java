package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "plantilla",
        uniqueConstraints = @UniqueConstraint(name = "uk_plantilla_codigo", columnNames = "codigo"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Plantilla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    private String nombre;

    private String descripcion;

    @Column(name = "moneda_por_defecto")
    private String monedaPorDefecto;

    private String version;

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

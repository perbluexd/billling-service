package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "presupuesto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Presupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Grupo (opcional) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "grupo_id",
            foreignKey = @ForeignKey(name = "fk_presupuesto_grupo")
    )
    private GrupoPresupuesto grupo;

    private String nombre;
    private String cliente;
    private String direccion;
    private String distrito;
    private String provincia;
    private String departamento;

    /** Para resolver precio vigente */
    @Column(name = "fecha_base", nullable = false)
    private LocalDate fechaBase;

    /** Default 8 (aplícalo en servicio si quieres) */
    @Column(name = "jornada_horas")
    private BigDecimal jornadaHoras;

    private String moneda;

    @Enumerated(EnumType.STRING)
    private EstadoPresupuesto estado = EstadoPresupuesto.BORRADOR;

    private String version;

    /** Usuario creador */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "creado_por",
            foreignKey = @ForeignKey(name = "fk_presupuesto_creado_por")
    )
    private Usuario creadoPor;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    /** Relación con SubPresupuesto (hijos) */
    @Builder.Default
    @OneToMany(
            mappedBy = "presupuesto",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Setter(AccessLevel.NONE) // importante: no permitir reemplazar la colección
    private List<SubPresupuesto> subPresupuestos = new ArrayList<>();

    /** Relación con snapshots (hijos) */
    @Builder.Default
    @OneToMany(
            mappedBy = "presupuesto",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("creadoEn DESC")
    @Setter(AccessLevel.NONE) // importante: no permitir reemplazar la colección
    private List<PresupuestoSnapshot> snapshots = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.creadoEn = LocalDateTime.now();
        this.actualizadoEn = this.creadoEn;
        if (estado == null) estado = EstadoPresupuesto.BORRADOR;
    }

    @PreUpdate
    public void preUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }

    /* --------- Helpers para mantener la relación en memoria --------- */
    public void addSubPresupuesto(SubPresupuesto sp) {
        if (sp == null) return;
        this.subPresupuestos.add(sp);
        sp.setPresupuesto(this);
    }

    public void removeSubPresupuesto(SubPresupuesto sp) {
        if (sp == null) return;
        this.subPresupuestos.remove(sp);
        sp.setPresupuesto(null);
    }

    public void addSnapshot(PresupuestoSnapshot snap) {
        if (snap == null) return;
        this.snapshots.add(snap);
        snap.setPresupuesto(this);
    }

    public void removeSnapshot(PresupuestoSnapshot snap) {
        if (snap == null) return;
        this.snapshots.remove(snap);
        snap.setPresupuesto(null);
    }
}

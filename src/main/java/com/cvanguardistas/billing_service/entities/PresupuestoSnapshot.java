package com.cvanguardistas.billing_service.entities;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "presupuesto_snapshot")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PresupuestoSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK → Presupuesto */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "presupuesto_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_ps_presupuesto")
    )
    private Presupuesto presupuesto;

    private String version;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    /** JSON con el snapshot (en Postgres usa jsonb) */
    @JdbcTypeCode(SqlTypes.JSON) // Hibernate 6: mapea JsonNode ↔ jsonb
    @Column(name = "json_snapshot", columnDefinition = "jsonb", nullable = false)
    private JsonNode jsonSnapshot;

    @PrePersist
    public void prePersist() {
        if (this.creadoEn == null) this.creadoEn = LocalDateTime.now();
    }
}

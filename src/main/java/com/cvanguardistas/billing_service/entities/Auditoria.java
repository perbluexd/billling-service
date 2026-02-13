package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "auditoria",
        indexes = {
                @Index(name = "idx_aud_usuario_fecha", columnList = "usuario_id, creado_en"),
                @Index(name = "idx_aud_entidad", columnList = "entidad, entidad_id")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Quién hizo el cambio */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_auditoria_usuario"))
    private Usuario usuario;

    /** Nombre lógico de la entidad (p.ej., "Presupuesto", "Partida", etc.) */
    @Column(nullable = false)
    private String entidad;

    /** ID de la entidad afectada (UUID o INT, lo guardamos como texto) */
    @Column(name = "entidad_id", nullable = false)
    private String entidadId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccionAuditoria accion;

    /** JSON previo y nuevo (usa columnDefinition para Postgres jsonb; si usas otro DB, quítalo) */
    @Column(name = "payload_anterior", columnDefinition = "jsonb")
    private String payloadAnterior;

    @Column(name = "payload_nuevo", columnDefinition = "jsonb")
    private String payloadNuevo;

    private String ip;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    /** Opcionales */
    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "razon_cambio")
    private String razonCambio;

    @PrePersist
    public void prePersist() {
        this.creadoEn = LocalDateTime.now();
    }
}

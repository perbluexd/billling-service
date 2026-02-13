package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "usuario",
        indexes = {
                @Index(name = "ix_usuario_email", columnList = "email", unique = true)
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Check(constraints = "(fecha_baja IS NULL OR activo = FALSE)")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombres;
    private String apellidos;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "hash_password")
    private String hashPassword; // puede ser NULL si SSO

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "primer_login", nullable = false)
    private Boolean primerLogin = true;

    @Column(name = "email_verificado", nullable = false)
    private Boolean emailVerificado = false;

    // auditoría / actividad
    private LocalDateTime lastLoginFecha;
    private LocalDateTime lastSeenFecha;
    private LocalDateTime lastLogoutFecha;
    private LocalDateTime creadoFecha;
    private LocalDateTime actualizadoFecha;
    private LocalDateTime fechaBaja;

    /** 🔸 NUEVO: último cambio de contraseña (TIMESTAMPTZ en Postgres) */
    @Column(name = "pwd_changed_at")
    private OffsetDateTime pwdChangedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (creadoFecha == null) creadoFecha = now;
        if (actualizadoFecha == null) actualizadoFecha = now;
        if (activo == null) activo = true;
        if (primerLogin == null) primerLogin = true;
        if (emailVerificado == null) emailVerificado = false;
    }

    @PreUpdate
    public void preUpdate() {
        this.actualizadoFecha = LocalDateTime.now();
    }
}

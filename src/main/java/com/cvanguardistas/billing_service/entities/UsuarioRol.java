package com.cvanguardistas.billing_service.entities;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "usuario_rol",
        uniqueConstraints = @UniqueConstraint(name = "uk_usuario_rol", columnNames = {"usuario_id", "rol_id"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UsuarioRol {

    @EmbeddedId
    private UsuarioRolId id;

    @ManyToOne(fetch = FetchType.LAZY) @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY) @MapsId("rolId")
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    private LocalDateTime asignadoFecha;

    @PrePersist
    public void prePersist() {
        if (asignadoFecha == null) asignadoFecha = LocalDateTime.now();
    }
}

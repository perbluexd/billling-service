package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rol", indexes = {
        @Index(name = "ix_rol_nombre", columnList = "nombre", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    private String descripcion;

    private LocalDateTime creado_Fecha;

    @PrePersist
    public void prePersist() {
        this.creado_Fecha = LocalDateTime.now();
    }
}

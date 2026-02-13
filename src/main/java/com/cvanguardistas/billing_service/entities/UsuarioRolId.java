package com.cvanguardistas.billing_service.entities;


import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class UsuarioRolId implements Serializable {
    private Long usuarioId;
    private Long rolId;
}

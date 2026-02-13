// src/main/java/com/cvanguardistas/billing_service/entities/RefreshToken.java
package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "refresh_token",
        indexes = {
                @Index(name = "idx_refresh_usuario_expira", columnList = "usuario_id, expira_en"),
                @Index(name = "idx_refresh_expira", columnList = "expira_en")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "token_hash", nullable = false, unique = true, length = 256)
    private String tokenHash;

    @Column(name = "expira_en", nullable = false /*, columnDefinition = "timestamptz"*/)
    private OffsetDateTime expiraEn;

    @Column(name = "revocado", nullable = false)
    private Boolean revocado = false;

    @Column(name = "creado_en" /*, columnDefinition = "timestamptz"*/)
    private OffsetDateTime creadoEn;

    private String ip;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @PrePersist
    public void prePersist() {
        if (creadoEn == null) creadoEn = OffsetDateTime.now();
        if (revocado == null) revocado = false;
    }
}

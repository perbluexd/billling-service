package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tipo_cambio")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TipoCambio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;

    @Column(name = "moneda_origen")
    private String monedaOrigen;

    @Column(name = "moneda_destino")
    private String monedaDestino;

    private BigDecimal tasa;
}

package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "calendario")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Calendario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(name = "horas_por_dia")
    private BigDecimal horasPorDia;

    @Column(name = "zona_horaria")
    private String zonaHoraria;
}

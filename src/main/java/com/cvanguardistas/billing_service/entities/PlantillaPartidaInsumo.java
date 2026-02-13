package com.cvanguardistas.billing_service.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;

@Entity
@Table(name = "plantilla_partida_insumo")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Check(constraints =
        "((depende_de_rendimiento AND cuadrilla_frac IS NOT NULL AND cantidad_base IS NULL) OR " +
                " (NOT depende_de_rendimiento AND cantidad_base IS NOT NULL AND cuadrilla_frac IS NULL))")
public class PlantillaPartidaInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK → PlantillaPartida */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plantilla_partida_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ppi_partida"))
    private PlantillaPartida plantillaPartida;

    /** FK → Insumo */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insumo_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ppi_insumo"))
    private Insumo insumo;

    /** FK → CategoriaCosto */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_costo_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ppi_categoria_costo"))
    private CategoriaCosto categoriaCosto;

    /** Si TRUE, depende de rendimiento; si FALSE, es cantidad fija */
    @Column(name = "depende_de_rendimiento", nullable = false)
    private Boolean dependeDeRendimiento = false;

    @Column(name = "cuadrilla_frac")
    private BigDecimal cuadrillaFrac; // si depende_de_rendimiento = TRUE

    @Column(name = "cantidad_base")
    private BigDecimal cantidadBase;  // si depende_de_rendimiento = FALSE

    @Lob
    private String observacion;

    /** Subpartida referenciada */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Subpartida_plantilla_id",
            foreignKey = @ForeignKey(name = "fk_ppi_Subpartida"))
    private PlantillaPartida SubpartidaPlantilla;
}

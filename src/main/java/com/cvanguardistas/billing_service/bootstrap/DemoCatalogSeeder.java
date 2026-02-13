package com.cvanguardistas.billing_service.bootstrap;

import com.cvanguardistas.billing_service.entities.*;
import com.cvanguardistas.billing_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DemoCatalogSeeder implements CommandLineRunner {

    private final UnidadRepository unidadRepo;
    private final TipoInsumoRepository tipoRepo;
    private final CategoriaCostoRepository categoriaRepo;
    private final InsumoRepository insumoRepo;
    private final InsumoPrecioHistRepository precioHistRepo;

    // ✅ tu repo real
    private final CatalogoPartidaRepository partidaCatRepo;

    // ✅ ya lo tienes
    private final PartidaCatalogoInsumoRepository partidaCatInsRepo;

    @Override
    @Transactional
    public void run(String... args) {
        // no sembrar si ya hay catálogo
        if (partidaCatRepo.count() > 0) return;

        // Refs
        Unidad HH  = unidadRepo.findByCodigo("HH").orElseThrow();
        Unidad M2  = unidadRepo.findByCodigo("M2").orElseThrow();
        Unidad GLB = unidadRepo.findByCodigo("GLB").orElseThrow();

        TipoInsumo MO = tipoRepo.findByCodigo("MO").orElseThrow();
        TipoInsumo MT = tipoRepo.findByCodigo("MT").orElseThrow();
        TipoInsumo SC = tipoRepo.findByCodigo("SC").orElseThrow();

        CategoriaCosto catMO = categoriaRepo.findByCodigo("MO").orElseThrow();
        CategoriaCosto catMT = categoriaRepo.findByCodigo("MT").orElseThrow();
        CategoriaCosto catSC = categoriaRepo.findByCodigo("SC").orElseThrow();

        // Insumos mínimos
        Insumo guardian = upsertInsumo("MO-GUARD", "GUARDIAN", HH, MO, new BigDecimal("9.60"));
        Insumo almacen  = upsertInsumo("MT-ALMACEN", "ALMACEN", M2, MT, new BigDecimal("15.00"));
        Insumo caseta   = upsertInsumo("MT-CASETA-SEDAPAL", "CASETA ADICIONAL TECHADA SEDAPAL", M2, MT, new BigDecimal("25.00"));
        Insumo sshh     = upsertInsumo("SC-ALQ-SSH", "ALQUILER DE SS.HH.", GLB, SC, new BigDecimal("1188.00"));

        // Partida catálogo
        PartidaCatalogo pc = partidaCatRepo.findByCodigo("PC-CASETA-GUARD")
                .orElseGet(() -> {
                    PartidaCatalogo p = new PartidaCatalogo();
                    p.setCodigo("PC-CASETA-GUARD");
                    p.setNombre("CASETA P/GUARDIANIA Y/O DEPOSITO");
                    p.setUnidad(GLB);
                    return partidaCatRepo.save(p);
                });

        addLinea(pc, guardian, catMO, new BigDecimal("1.00"),  new BigDecimal("9.60"));
        addLinea(pc, almacen,  catMT, new BigDecimal("25.00"), new BigDecimal("15.00"));
        addLinea(pc, caseta,   catMT, new BigDecimal("15.00"), new BigDecimal("25.00"));
        addLinea(pc, sshh,     catSC, new BigDecimal("2.00"),  new BigDecimal("1188.00"));
    }

    private Insumo upsertInsumo(String codigo, String nombre, Unidad unidad, TipoInsumo tipo, BigDecimal puVigente) {
        Insumo i = insumoRepo.findByCodigo(codigo).orElseGet(() -> {
            Insumo n = new Insumo();
            n.setCodigo(codigo);
            n.setNombre(nombre);
            n.setUnidad(unidad);
            n.setTipoInsumo(tipo);
            return insumoRepo.save(n);
        });

        Optional<InsumoPrecioHist> lastOpt = precioHistRepo.findTopByInsumo_IdOrderByVigenteDesdeDesc(i.getId());
        if (lastOpt.isEmpty() || lastOpt.get().getPrecio().compareTo(puVigente) != 0) {
            InsumoPrecioHist ph = InsumoPrecioHist.builder()
                    .insumo(i)
                    .precio(puVigente)
                    .vigenteDesde(LocalDateTime.now())
                    .fuente(FuentePrecioHist.MANUAL) // ajusta si tu enum difiere
                    .build();
            precioHistRepo.save(ph);
        }
        return i;
    }

    private void addLinea(PartidaCatalogo p, Insumo ins, CategoriaCosto cat,
                          BigDecimal cantidad, BigDecimal pu) {
        // evita duplicados
        boolean exists = partidaCatInsRepo
                .existsByPartidaCatalogo_IdAndInsumo_IdAndCategoriaCosto_Id(
                        p.getId(), ins.getId(), cat.getId());
        if (exists) return;

        PartidaCatalogoInsumo li = new PartidaCatalogoInsumo();
        li.setPartidaCatalogo(p);
        li.setInsumo(ins);
        li.setCategoriaCosto(cat);

        // === Tus campos reales ===
        // para esta demo: cantidad fija (no depende del rendimiento)
        li.setDependeDeRendimiento(false);
        li.setCantidadFija(cantidad);

        // usar PU manual = el que pasamos en el seeder
        li.setUsarPuOverride(true);
        li.setPuOverride(pu);

        partidaCatInsRepo.save(li);
    }

}

// src/main/java/com/cvanguardistas/billing_service/service/impl/SnapshotServiceImpl.java
package com.cvanguardistas.billing_service.service.impl;

import com.cvanguardistas.billing_service.dto.SnapshotCompareResponse;
import com.cvanguardistas.billing_service.dto.SnapshotDiffRowDto;
import com.cvanguardistas.billing_service.dto.SnapshotSummaryDto;
import com.cvanguardistas.billing_service.entities.PresupuestoSnapshot;
import com.cvanguardistas.billing_service.exception.DomainException;
import com.cvanguardistas.billing_service.repository.PresupuestoSnapshotRepository;
import com.cvanguardistas.billing_service.service.SnapshotService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SnapshotServiceImpl implements SnapshotService {

    private final PresupuestoSnapshotRepository snapshotRepo;

    @Override
    public List<SnapshotSummaryDto> listar(Long presupuestoId) {
        return snapshotRepo
                .findByPresupuesto_IdOrderByCreadoEnDesc(presupuestoId)
                .stream()
                .map(s -> new SnapshotSummaryDto(s.getId(), s.getVersion(), s.getCreadoEn()))
                .toList();
    }

    @Override
    public SnapshotCompareResponse comparar(Long presupuestoId, String version1, String version2) {
        PresupuestoSnapshot s1 = snapshotRepo
                .findByPresupuesto_IdAndVersion(presupuestoId, version1)
                .orElseThrow(() -> new DomainException("No existe snapshot v1=" + version1));

        PresupuestoSnapshot s2 = snapshotRepo
                .findByPresupuesto_IdAndVersion(presupuestoId, version2)
                .orElseThrow(() -> new DomainException("No existe snapshot v2=" + version2));

        // getJsonSnapshot() ahora devuelve JsonNode
        Map<Long, PartidaSnap> p1 = indexarPartidas(s1.getJsonSnapshot());
        Map<Long, PartidaSnap> p2 = indexarPartidas(s2.getJsonSnapshot());

        Set<Long> allIds = new HashSet<>();
        allIds.addAll(p1.keySet());
        allIds.addAll(p2.keySet());

        List<SnapshotDiffRowDto> rows = new ArrayList<>();
        BigDecimal totalV1 = BigDecimal.ZERO;
        BigDecimal totalV2 = BigDecimal.ZERO;

        for (Long id : allIds) {
            PartidaSnap a = p1.get(id);
            PartidaSnap b = p2.get(id);

            String codigo = a != null ? a.codigo : (b != null ? b.codigo : null);
            String nombre = a != null ? a.nombre : (b != null ? b.nombre : null);

            BigDecimal met1 = a != null ? nz(a.metrado) : BigDecimal.ZERO;
            BigDecimal met2 = b != null ? nz(b.metrado) : BigDecimal.ZERO;
            BigDecimal dMet = met2.subtract(met1);

            BigDecimal cu1 = a != null ? nz(a.cu) : BigDecimal.ZERO;
            BigDecimal cu2 = b != null ? nz(b.cu) : BigDecimal.ZERO;
            BigDecimal dCu = cu2.subtract(cu1);

            BigDecimal par1 = a != null ? nz(a.parcial) : BigDecimal.ZERO;
            BigDecimal par2 = b != null ? nz(b.parcial) : BigDecimal.ZERO;
            BigDecimal dPar = par2.subtract(par1);

            totalV1 = totalV1.add(par1);
            totalV2 = totalV2.add(par2);

            rows.add(new SnapshotDiffRowDto(
                    id, codigo, nombre,
                    met1, met2, dMet,
                    cu1, cu2, dCu,
                    par1, par2, dPar
            ));
        }

        BigDecimal deltaTotal = totalV2.subtract(totalV1);

        // Ordenar por mayor impacto absoluto en parcial (desc)
        rows = rows.stream()
                .sorted(Comparator.comparing(r -> r.deltaParcial().abs(), Comparator.reverseOrder()))
                .collect(Collectors.toList());

        return new SnapshotCompareResponse(
                presupuestoId, version1, version2,
                rows, totalV1, totalV2, deltaTotal
        );
    }

    // ===== helpers =====

    private static BigDecimal nz(BigDecimal v) { return v != null ? v : BigDecimal.ZERO; }

    // Ahora recibe JsonNode (NO String) y no parsea de nuevo
    private Map<Long, PartidaSnap> indexarPartidas(JsonNode root) {
        try {
            Map<Long, PartidaSnap> map = new HashMap<>();
            if (root == null) return map;

            JsonNode sps = root.get("SubPresupuestos");
            if (sps == null || !sps.isArray()) return map;

            for (JsonNode sp : sps) {
                JsonNode partidas = sp.get("partidas");
                if (partidas == null || !partidas.isArray()) continue;

                for (JsonNode p : partidas) {
                    Long id = p.hasNonNull("id") ? p.get("id").asLong() : null;
                    if (id == null) continue;

                    PartidaSnap ps = new PartidaSnap();
                    ps.id      = id;
                    ps.codigo  = p.hasNonNull("codigo")  ? p.get("codigo").asText()  : null;
                    ps.nombre  = p.hasNonNull("nombre")  ? p.get("nombre").asText()  : null;
                    ps.metrado = p.hasNonNull("metrado") ? new BigDecimal(p.get("metrado").asText()) : null;
                    ps.cu      = p.hasNonNull("cu")      ? new BigDecimal(p.get("cu").asText())      : null;
                    ps.parcial = p.hasNonNull("parcial") ? new BigDecimal(p.get("parcial").asText()) : null;

                    map.put(id, ps);
                }
            }
            return map;
        } catch (Exception e) {
            throw new DomainException("Error parseando snapshot JSON: " + e.getMessage());
        }
    }

    private static class PartidaSnap {
        Long id;
        String codigo;
        String nombre;
        BigDecimal metrado;
        BigDecimal cu;
        BigDecimal parcial;
    }
}

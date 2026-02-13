package com.cvanguardistas.billing_service.repository.specs;

import com.cvanguardistas.billing_service.entities.Insumo;
import com.cvanguardistas.billing_service.entities.TipoInsumo;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

public final class InsumoSpecs {

    private InsumoSpecs() {}

    /** Filtro por texto libre (codigo o nombre) – case-insensitive. */
    public static Specification<Insumo> textoContains(String qLower) {
        if (qLower == null || qLower.isBlank()) return null;
        final String like = "%" + qLower.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("codigo")), like),
                cb.like(cb.lower(root.get("nombre")), like)
        );
    }

    /** Filtro por tipo de insumo (t.codigo = :tipo) – case-insensitive. */
    public static Specification<Insumo> tipoCodigoEquals(String tipoLower) {
        if (tipoLower == null || tipoLower.isBlank()) return null;
        return (root, query, cb) -> {
            // LEFT JOIN para ser null-safe y escalable
            Join<Insumo, TipoInsumo> join = root.join("tipoInsumo", JoinType.LEFT);
            return cb.equal(cb.lower(join.get("codigo")), tipoLower.toLowerCase());
        };
    }

    /** (Opcional) Filtro por activo/inactivo si lo quisieras luego. */
    public static Specification<Insumo> activoEquals(Boolean activo) {
        if (activo == null) return null;
        return (root, query, cb) -> cb.equal(root.get("activo"), activo);
    }
}

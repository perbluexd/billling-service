package com.cvanguardistas.billing_service.security;

import com.cvanguardistas.billing_service.entities.Presupuesto;
import com.cvanguardistas.billing_service.entities.SubPresupuesto;
import com.cvanguardistas.billing_service.repository.PresupuestoRepository;
import com.cvanguardistas.billing_service.repository.SubPresupuestoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("permits")
@RequiredArgsConstructor
public class Permits {

    private final PresupuestoRepository presupuestoRepo;
    private final SubPresupuestoRepository SubPresRepo;

    public boolean esOwnerDePresupuesto(Long presupuestoId, Authentication auth) {
        System.out.println("[PERMITS] esOwnerDePresupuesto pid=" + presupuestoId
                + " auth=" + (auth == null ? "null" : auth.getClass().getName())
                + " principal=" + (auth == null ? "null" : String.valueOf(auth.getPrincipal()))
                + " principalClass=" + (auth == null ? "null" : auth.getPrincipal().getClass().getName()));

        Long me = SecurityUtils.currentUserIdOrNull();
        if (me == null || presupuestoId == null) return false;
        return presupuestoRepo.findOwnerIdById(presupuestoId)
                .map(me::equals)
                .orElse(false);
    }

    public boolean esOwnerDeSubPresupuesto(Long spId, Authentication authentication) {
        System.out.println("[PERMITS] esOwnerDeSubPresupuesto spId=" + spId
                + " auth=" + (authentication == null ? "null" : authentication.getClass().getName())
                + " principal=" + (authentication == null ? "null" : String.valueOf(authentication.getPrincipal()))
                + " principalClass=" + (authentication == null ? "null" : authentication.getPrincipal().getClass().getName()));

        if (authentication == null || authentication.getPrincipal() == null) return false;
        Long userId = tryParseLong(authentication.getPrincipal());
        if (userId == null) return false;

        return SubPresRepo.findById(spId)
                .map(SubPresupuesto::getPresupuesto)
                .map(Presupuesto::getId)
                .flatMap(presupuestoRepo::findOwnerIdById)
                .map(ownerId -> ownerId.equals(userId))
                .orElse(false);
    }

    private static Long tryParseLong(Object principal) {
        try { return Long.valueOf(String.valueOf(principal)); }
        catch (Exception e) { return null; }
    }
}

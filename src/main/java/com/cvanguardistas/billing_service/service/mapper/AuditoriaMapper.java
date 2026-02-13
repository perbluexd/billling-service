// src/main/java/com/cvanguardistas/billing_service/service/mapper/AuditoriaMapper.java
package com.cvanguardistas.billing_service.service.mapper;

import com.cvanguardistas.billing_service.dto.AuditoriaDto;
import com.cvanguardistas.billing_service.entities.Auditoria;
import com.cvanguardistas.billing_service.entities.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuditoriaMapper {

    @Mapping(source = "usuario.id", target = "usuarioId")
    @Mapping(target = "usuarioNombre", expression = "java(displayName(a.getUsuario()))")
    AuditoriaDto toDto(Auditoria a);

    // Construye un nombre visible usando lo que haya disponible
    default String displayName(Usuario u) {
        if (u == null) return null;

        String nombres = safe(u.getNombres());
        String apellidos = safe(u.getApellidos());

        if (nombres != null && apellidos != null) return (nombres + " " + apellidos).trim();
        if (nombres != null) return nombres;
        if (apellidos != null) return apellidos;

        // Fallbacks razonables
        String email = safe(u.getEmail());
        if (email != null) return email;

        return u.getId() != null ? String.valueOf(u.getId()) : null;
    }

    private static String safe(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}

// com.cvanguardistas.billing_service.dto.CatalogoPartidaDto
package com.cvanguardistas.billing_service.dto;

import com.cvanguardistas.billing_service.entities.TipoPartida;
public record CatalogoPartidaDto(Long id, String codigo, String nombre, TipoPartida tipo) { }

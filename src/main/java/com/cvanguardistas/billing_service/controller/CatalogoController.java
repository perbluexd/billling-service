// com.cvanguardistas.billing_service.controller.CatalogoController
package com.cvanguardistas.billing_service.controller;

import com.cvanguardistas.billing_service.dto.CatalogoPartidaDto;
import com.cvanguardistas.billing_service.entities.TipoPartida;
import com.cvanguardistas.billing_service.service.CatalogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/catalogo")
@RequiredArgsConstructor
public class CatalogoController {

    private final CatalogoService catalogoService;

    // GET /catalogo/partidas?q=&tipo=&page=&size=
    @GetMapping("/partidas")
    public ResponseEntity<Page<CatalogoPartidaDto>> buscarPartidas(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) TipoPartida tipo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(catalogoService.buscarPartidas(q, tipo, pageable));
    }
}

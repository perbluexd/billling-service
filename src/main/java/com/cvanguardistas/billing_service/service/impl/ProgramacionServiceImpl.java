package com.cvanguardistas.billing_service.service.impl;

import com.cvanguardistas.billing_service.dto.*;
import com.cvanguardistas.billing_service.entities.*;
import com.cvanguardistas.billing_service.exception.DomainException;
import com.cvanguardistas.billing_service.repository.*;
import com.cvanguardistas.billing_service.service.ProgramacionService;
import com.cvanguardistas.billing_service.service.mapper.ProgramacionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgramacionServiceImpl implements ProgramacionService {

    private final CalendarioRepository calendarioRepo;
    private final CalendarioExcepcionRepository calExcRepo;
    private final TareaProgramaRepository tareaRepo;
    private final TareaDependenciaRepository depRepo;
    private final SubPresupuestoRepository spRepo;
    private final PartidaRepository partidaRepo;

    private final ProgramacionMapper mapper;

    // ===== Calendarios =====

    @Override
    @Transactional(readOnly = true)
    public List<CalendarioDto> listarCalendarios() {
        return calendarioRepo.findAll().stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional
    public CalendarioDto crearCalendario(CrearCalendarioRequest r) {
        Calendario e = mapper.toEntity(r);
        e = calendarioRepo.save(e);
        return mapper.toDto(e);
    }

    @Override
    @Transactional
    public void eliminarCalendario(Long id) {
        if (!calendarioRepo.existsById(id)) return;

        // 1) Desasociar calendario de todas las tareas que lo usen (evita violación de FK)
        List<TareaPrograma> tareas = tareaRepo.findByCalendarioId(id);
        if (!tareas.isEmpty()) {
            for (TareaPrograma t : tareas) {
                t.setCalendario(null);
            }
            tareaRepo.saveAll(tareas);
        }

        // 2) Eliminar excepciones del calendario
        calExcRepo.deleteByCalendarioId(id);

        // 3) Eliminar calendario
        calendarioRepo.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarioExcepcionDto> listarExcepciones(Long calendarioId) {
        return calExcRepo.findByCalendarioIdOrderByFechaAsc(calendarioId)
                .stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional
    public CalendarioExcepcionDto agregarExcepcion(UpsertCalendarioExcepcionRequest r) {
        Calendario cal = calendarioRepo.findById(r.calendarioId())
                .orElseThrow(() -> new DomainException("Calendario no encontrado: " + r.calendarioId()));
        CalendarioExcepcion e = new CalendarioExcepcion();
        e.setCalendario(cal);
        e.setFecha(r.fecha());
        e.setTipo(r.tipo());
        e.setDescripcion(r.descripcion());
        e = calExcRepo.save(e);
        return mapper.toDto(e);
    }

    @Override
    @Transactional
    public void eliminarExcepcion(Long excepcionId) {
        calExcRepo.deleteById(excepcionId);
    }

    // ===== Tareas =====

    @Override
    @Transactional(readOnly = true)
    public ProgramacionTareasDto listarTareas(Long SubPresupuestoId) {
        List<TareaPrograma> tareas = tareaRepo.findBySubPresupuestoIdOrderByOrdenAsc(SubPresupuestoId);
        List<TareaProgramaDto> tareasDto = tareas.stream().map(mapper::toDto).toList();

        List<TareaDependenciaDto> depsDto = depRepo.findBySucesora_SubPresupuesto_Id(SubPresupuestoId)
                .stream().map(mapper::toDto).toList();

        return new ProgramacionTareasDto(tareasDto, depsDto);
    }

    @Override
    @Transactional
    public TareaProgramaDto crearTarea(CrearTareaRequest r) {
        SubPresupuesto sp = spRepo.findById(r.subPresupuestoId())
                .orElseThrow(() -> new DomainException("SubPresupuesto no encontrado: " + r.subPresupuestoId()));

        Partida partida = null;
        if (r.partidaId() != null) {
            partida = partidaRepo.findById(r.partidaId())
                    .orElseThrow(() -> new DomainException("Partida no encontrada: " + r.partidaId()));
            if (!Objects.equals(partida.getSubPresupuesto().getId(), sp.getId())) {
                throw new DomainException("La partida no pertenece al SubPresupuesto");
            }
        }

        Calendario cal = null;
        if (r.calendarioId() != null) {
            cal = calendarioRepo.findById(r.calendarioId())
                    .orElseThrow(() -> new DomainException("Calendario no encontrado: " + r.calendarioId()));
        }

        TareaPrograma t = new TareaPrograma();
        t.setSubPresupuesto(sp);
        t.setPartida(partida);
        t.setNombre(r.nombre());
        t.setTipo(r.tipo());
        t.setDuracionDias(nullSafe(r.duracionDias()));
        t.setFechaInicio(r.fechaInicio());
        t.setFechaFin(calcFechaFin(r.fechaInicio(), r.duracionDias()));
        t.setPorcentajeAvance(BigDecimal.ZERO);
        t.setCalendario(cal);
        t.setOrden(r.orden() != null ? r.orden() : siguienteOrden(sp.getId()));
        t.setEsRutaCritica(Boolean.FALSE);

        t = tareaRepo.save(t);
        return mapper.toDto(t);
    }

    @Override
    @Transactional
    public TareaProgramaDto actualizarTarea(Long tareaId, UpdateTareaRequest r) {
        TareaPrograma t = tareaRepo.findById(tareaId)
                .orElseThrow(() -> new DomainException("Tarea no encontrada: " + tareaId));

        if (r.nombre() != null) t.setNombre(r.nombre());
        if (r.tipo() != null) t.setTipo(r.tipo());
        if (r.duracionDias() != null) t.setDuracionDias(r.duracionDias());
        if (r.fechaInicio() != null) t.setFechaInicio(r.fechaInicio());
        if (r.fechaFin() != null) {
            t.setFechaFin(r.fechaFin());
        } else if (r.fechaInicio() != null || r.duracionDias() != null) {
            t.setFechaFin(calcFechaFin(t.getFechaInicio(), t.getDuracionDias()));
        }
        if (r.porcentajeAvance() != null) t.setPorcentajeAvance(r.porcentajeAvance());
        if (r.calendarioId() != null) {
            Calendario cal = calendarioRepo.findById(r.calendarioId())
                    .orElseThrow(() -> new DomainException("Calendario no encontrado: " + r.calendarioId()));
            t.setCalendario(cal);
        }
        if (r.orden() != null) t.setOrden(r.orden());
        if (r.esRutaCritica() != null) t.setEsRutaCritica(r.esRutaCritica());

        t = tareaRepo.save(t);
        return mapper.toDto(t);
    }

    @Override
    @Transactional
    public void eliminarTarea(Long tareaId) {
        if (!tareaRepo.existsById(tareaId)) return;
        depRepo.deleteByPredecesoraIdOrSucesoraId(tareaId, tareaId);
        tareaRepo.deleteById(tareaId);
    }

    // ===== Dependencias =====

    @Override
    @Transactional
    public TareaDependenciaDto crearDependencia(CrearDependenciaRequest r) {
        TareaPrograma pred = tareaRepo.findById(r.predecesoraId())
                .orElseThrow(() -> new DomainException("Tarea predecesora no encontrada: " + r.predecesoraId()));
        TareaPrograma suc = tareaRepo.findById(r.sucesoraId())
                .orElseThrow(() -> new DomainException("Tarea sucesora no encontrada: " + r.sucesoraId()));

        if (Objects.equals(pred.getId(), suc.getId())) {
            throw new DomainException("Una tarea no puede depender de sí misma");
        }
        if (!Objects.equals(pred.getSubPresupuesto().getId(), suc.getSubPresupuesto().getId())) {
            throw new DomainException("Las tareas deben pertenecer al mismo SubPresupuesto");
        }

        // Evitar duplicados simples (misma pareja pred→suc)
        for (TareaDependencia d : depRepo.findByPredecesoraId(pred.getId())) {
            if (Objects.equals(d.getSucesora().getId(), suc.getId())) {
                throw new DomainException("La dependencia ya existe para estas tareas");
            }
        }

        // ANTICICLO: si ya existe un camino suc ⇒ ... ⇒ pred, agregar pred→suc crea ciclo
        if (existsPath(suc.getId(), pred.getId())) {
            throw new DomainException("La dependencia solicitada crearía un ciclo en la red de tareas");
        }

        TareaDependencia d = new TareaDependencia();
        d.setPredecesora(pred);
        d.setSucesora(suc);
        d.setTipo(r.tipo());
        d.setDesfase(nullSafe(r.desfase()));

        d = depRepo.save(d);
        return mapper.toDto(d);
    }

    @Override
    @Transactional
    public void eliminarDependencia(Long dependenciaId) {
        depRepo.deleteById(dependenciaId);
    }

    // ===== Export CSV =====

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarCsv(Long SubPresupuestoId) {
        List<TareaPrograma> tareas = tareaRepo.findBySubPresupuestoIdOrderByOrdenAsc(SubPresupuestoId);
        Map<Long, TareaPrograma> idx = tareas.stream()
                .collect(Collectors.toMap(TareaPrograma::getId, x -> x));

        List<TareaDependencia> deps = depRepo.findBySucesora_SubPresupuesto_Id(SubPresupuestoId);

        // Construye columna Predecessors estilo MS Project: "123FS+2;124SS"
        Map<Long, String> predecessors = new HashMap<>();
        for (TareaDependencia d : deps) {
            String token = d.getPredecesora().getId() + d.getTipo().name() +
                    (d.getDesfase()!=null && d.getDesfase().compareTo(BigDecimal.ZERO)!=0
                            ? (d.getDesfase().signum()>=0 ? "+" : "") + d.getDesfase().stripTrailingZeros().toPlainString()
                            : "");
            predecessors.merge(d.getSucesora().getId(), token, (a,b) -> a + ";" + b);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Id,Name,Start,Finish,Duration,Predecessors\n");
        for (TareaPrograma t : tareas) {
            sb.append(t.getId()).append(",");
            sb.append(csv(t.getNombre())).append(",");
            sb.append(dateCsv(t.getFechaInicio())).append(",");
            sb.append(dateCsv(t.getFechaFin())).append(",");
            sb.append(durationCsv(t.getDuracionDias())).append(",");
            sb.append(csv(predecessors.getOrDefault(t.getId(), ""))).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ===== helpers =====

    private Integer siguienteOrden(Long spId) {
        return tareaRepo.findBySubPresupuestoIdOrderByOrdenAsc(spId).size() + 1;
    }

    private static BigDecimal nullSafe(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private static LocalDate calcFechaFin(LocalDate inicio, BigDecimal durDias) {
        if (inicio == null || durDias == null) return null;
        int d = durDias.setScale(0, java.math.RoundingMode.CEILING).intValue();
        if (d <= 0) d = 1;
        return inicio.plusDays(d - 1L);
    }

    private static String csv(String s) {
        if (s == null) return "";
        String esc = s.replace("\"", "\"\"");
        return "\"" + esc + "\"";
    }

    private static String dateCsv(LocalDate d) {
        return d != null ? d.toString() : "";
    }

    private static String durationCsv(BigDecimal d) {
        return (d != null) ? d.stripTrailingZeros().toPlainString() : "";
    }

    /**
     * Devuelve true si existe un camino start ⇒ ... ⇒ target usando las dependencias actuales (predecesora→sucesora).
     */
    private boolean existsPath(Long startId, Long targetId) {
        if (Objects.equals(startId, targetId)) return true;
        Set<Long> visitados = new HashSet<>();
        ArrayDeque<Long> q = new ArrayDeque<>();
        q.add(startId);
        visitados.add(startId);

        while (!q.isEmpty()) {
            Long actual = q.poll();
            // sucesores: todas las aristas actual -> X
            List<TareaDependencia> salientes = depRepo.findByPredecesoraId(actual);
            for (TareaDependencia d : salientes) {
                Long next = d.getSucesora().getId();
                if (Objects.equals(next, targetId)) return true;
                if (visitados.add(next)) {
                    q.add(next);
                }
            }
        }
        return false;
    }
}

// src/main/java/com/cvanguardistas/billing_service/service/impl/ReporteServiceImpl.java
package com.cvanguardistas.billing_service.service.impl;

import com.cvanguardistas.billing_service.dto.SpInsumoAgregadoDto;
import com.cvanguardistas.billing_service.entities.Partida;
import com.cvanguardistas.billing_service.entities.PartidaInsumo;
import com.cvanguardistas.billing_service.entities.TipoPartida;
import com.cvanguardistas.billing_service.exception.DomainException;
import com.cvanguardistas.billing_service.repository.PartidaInsumoRepository;
import com.cvanguardistas.billing_service.repository.PartidaRepository;
import com.cvanguardistas.billing_service.repository.SubPresupuestoRepository;
import com.cvanguardistas.billing_service.service.PartidaInsumoService;
import com.cvanguardistas.billing_service.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {

    private final SubPresupuestoRepository spRepo;
    private final PartidaRepository partidaRepo;
    private final PartidaInsumoRepository piRepo;

    // Para el XLSX de insumos agregados
    private final PartidaInsumoService partidaInsumoService;

    // ================================
    // EXISTENTE: Desagregado por línea
    // ================================
    @Override
    @Transactional(readOnly = true) // recomendado para evitar N+1 y garantizar sesión
    public byte[] exportarDesagregadoXlsx(Long subPresupuestoId) {
        // Sanity check
        spRepo.findById(subPresupuestoId)
                .orElseThrow(() -> new DomainException("SubPresupuesto no encontrado: " + subPresupuestoId));

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("Desagregado");

            // Estilos
            CellStyle head = wb.createCellStyle();
            Font fBold = wb.createFont();
            fBold.setBold(true);
            head.setFont(fBold);
            head.setBorderBottom(BorderStyle.THIN);

            // Header
            int r = 0;
            Row h = sh.createRow(r++);
            String[] cols = {
                    "PartidaID","Tipo","Código","Nombre","Unidad","Metrado","CU","Parcial",
                    "Categoría","InsumoID","Insumo","Depende","Cantidad","PU","ParcialLinea"
            };
            for (int i = 0; i < cols.length; i++) {
                Cell c = h.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(head);
            }

            // ⬇️ CAMBIO 1: usar el repo con fetch de UNIDAD
            List<Partida> arbol = partidaRepo.findArbolWithUnidadBySubPresupuesto(subPresupuestoId);

            for (Partida p : arbol) {
                // Fila resumen de la partida
                Row row = sh.createRow(r++);
                fill(row, 0, p.getId());
                fill(row, 1, p.getTipo() != null ? p.getTipo().name() : null);
                fill(row, 2, p.getCodigo());
                fill(row, 3, p.getNombre());
                fill(row, 4, p.getUnidad() != null ? p.getUnidad().getCodigo() : null); // unidad ya inicializada
                fill(row, 5, p.getMetrado());
                fill(row, 6, p.getCu());
                fill(row, 7, p.getParcial());

                if (p.getTipo() == TipoPartida.HOJA) {
                    // ⬇️ CAMBIO 2: repo con fetch de INSUMO/UNIDAD/CATEGORIA
                    List<PartidaInsumo> lineas = piRepo.findByPartidaIdFetchAll(p.getId());

                    for (PartidaInsumo li : lineas) {
                        Row lr = sh.createRow(r++);
                        // Repito info clave de la partida para facilitar pivot
                        fill(lr, 0, p.getId());
                        fill(lr, 1, "LINEA");
                        fill(lr, 2, p.getCodigo());
                        fill(lr, 3, p.getNombre());
                        fill(lr, 4, p.getUnidad() != null ? p.getUnidad().getCodigo() : null);
                        fill(lr, 5, p.getMetrado());
                        fill(lr, 6, p.getCu());
                        fill(lr, 7, p.getParcial());

                        // li.getInsumo(), li.getCategoriaCosto() y li.getInsumo().getUnidad() ya vienen inicializados
                        fill(lr, 8,  li.getCategoriaCosto() != null ? li.getCategoriaCosto().getCodigo() : null);
                        fill(lr, 9,  li.getInsumo() != null ? li.getInsumo().getId() : null);
                        fill(lr, 10, li.getInsumo() != null ? li.getInsumo().getNombre() : null);
                        fill(lr, 11, Boolean.TRUE.equals(li.getDependeDeRendimiento()) ? "S" : "N");
                        fill(lr, 12, li.getCantidad());
                        fill(lr, 13, li.getPu());
                        fill(lr, 14, li.getParcial());
                    }
                }
            }

            // Autosize
            for (int i = 0; i < cols.length; i++) sh.autoSizeColumn(i);

            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new DomainException("Error generando Excel: " + e.getMessage());
        }
    }

    // =========================
    // NUEVO: Insumos (agregado)
    // =========================
    @Override
    public byte[] exportarInsumosXlsx(Long subPresupuestoId) {
        List<SpInsumoAgregadoDto> data =
                partidaInsumoService.listarAgregadoPorSubPresupuesto(subPresupuestoId);

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("Insumos SP");

            // Estilos básicos
            CellStyle header = wb.createCellStyle();
            var font = wb.createFont();
            font.setBold(true);
            header.setFont(font);
            header.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            header.setBorderBottom(BorderStyle.THIN);
            header.setBorderTop(BorderStyle.THIN);
            header.setBorderLeft(BorderStyle.THIN);
            header.setBorderRight(BorderStyle.THIN);

            CellStyle numStyle = wb.createCellStyle();
            DataFormat fmt = wb.createDataFormat();
            numStyle.setDataFormat(fmt.getFormat("#,##0.########")); // cantidades

            CellStyle moneyStyle = wb.createCellStyle();
            moneyStyle.setDataFormat(fmt.getFormat("#,##0.00")); // costo total

            // Header
            int r = 0;
            Row h = sh.createRow(r++);
            int c = 0;
            create(h, c++, "ID Insumo", header);
            create(h, c++, "Código", header);
            create(h, c++, "Nombre", header);
            create(h, c++, "Unidad", header);
            create(h, c++, "Cantidad total", header);
            create(h, c++, "Costo total", header);

            // Rows
            for (SpInsumoAgregadoDto it : data) {
                Row row = sh.createRow(r++);
                int col = 0;

                create(row, col++, it.insumoId(), null);
                create(row, col++, safe(it.codigoInsumo()), null);
                create(row, col++, safe(it.nombreInsumo()), null);
                create(row, col++, safe(it.unidadCodigo()), null);

                Cell q = row.createCell(col++);
                q.setCellValue(it.cantidadTotal() != null ? it.cantidadTotal().doubleValue() : 0d);
                q.setCellStyle(numStyle);

                Cell ct = row.createCell(col++);
                ct.setCellValue(it.costoTotal() != null ? it.costoTotal().doubleValue() : 0d);
                ct.setCellStyle(moneyStyle);
            }

            // Autosize
            for (int i = 0; i < 6; i++) sh.autoSizeColumn(i);

            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Error generando insumos.xlsx para SP " + subPresupuestoId, ex);
        }
    }

    // ===== helpers (reusables) =====
    private static void fill(Row row, int idx, String v) {
        Cell c = row.createCell(idx);
        c.setCellValue(v != null ? v : "");
    }
    private static void fill(Row row, int idx, Long v) {
        Cell c = row.createCell(idx);
        if (v != null) c.setCellValue(v);
        else c.setBlank();
    }
    private static void fill(Row row, int idx, BigDecimal v) {
        Cell c = row.createCell(idx);
        if (v != null) c.setCellValue(v.doubleValue());
        else c.setBlank();
    }

    private static void create(Row row, int col, String val, CellStyle st) {
        Cell cell = row.createCell(col);
        cell.setCellValue(val);
        if (st != null) cell.setCellStyle(st);
    }
    private static void create(Row row, int col, Long val, CellStyle st) {
        Cell cell = row.createCell(col);
        if (val != null) cell.setCellValue(val.doubleValue());
        if (st != null) cell.setCellStyle(st);
    }
    private static String safe(String s) { return s == null ? "" : s; }
}

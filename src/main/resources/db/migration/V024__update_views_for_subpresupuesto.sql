-- V024__update_views_for_subpresupuesto.sql

-- 1) Lista de insumos por subpresupuesto
CREATE OR REPLACE VIEW vw_lista_insumos_por_subpresupuesto AS
SELECT
    p.subpresupuesto_id,
    pi.insumo_id,
    SUM(pi.cantidad) AS cantidad_total,
    CASE
        WHEN SUM(pi.cantidad) = 0 THEN NULL
        ELSE SUM(pi.parcial) / NULLIF(SUM(pi.cantidad), 0)
    END AS pu_ponderado,
    SUM(pi.parcial) AS parcial_total
FROM partida_insumo pi
JOIN partida p ON p.id = pi.partida_id
GROUP BY p.subpresupuesto_id, pi.insumo_id;

-- 2) Resumen de chips por partida
CREATE OR REPLACE VIEW vw_resumen_chips_por_partida AS
SELECT
    ptc.partida_id,
    ptc.categoria_costo_id,
    ptc.unitario_calc,
    ptc.total_calc,
    ptc.unitario_override,
    ptc.usar_override,
    CASE WHEN ptc.usar_override THEN ptc.unitario_override ELSE ptc.unitario_calc END AS unitario_final,
    (CASE WHEN ptc.usar_override THEN ptc.unitario_override ELSE ptc.unitario_calc END)
      * COALESCE(p.metrado, 0) AS total_final
FROM partida_total_categoria ptc
JOIN partida p ON p.id = ptc.partida_id;

-- 3) Presupuesto desagregado (Partida ↔ PartidaInsumo)
CREATE OR REPLACE VIEW vw_presupuesto_desagregado AS
SELECT
    p.subpresupuesto_id,
    p.id          AS partida_id,
    p.codigo      AS partida_codigo,
    p.nombre      AS partida_nombre,
    p.tipo        AS partida_tipo,
    p.unidad_id   AS partida_unidad_id,
    p.rendimiento AS partida_rendimiento,
    p.metrado     AS partida_metrado,
    p.cu          AS partida_cu,
    p.parcial     AS partida_parcial,

    pi.id                 AS partida_insumo_id,
    pi.insumo_id,
    pi.categoria_costo_id,
    pi.depende_de_rendimiento,
    pi.cuadrilla_frac,
    pi.cantidad,
    pi.cantidad_fija,
    pi.pu,
    pi.pu_override,
    pi.usar_pu_override,
    pi.pu_congelado,
    pi.parcial            AS insumo_parcial,
    pi.subpartida_id
FROM partida p
LEFT JOIN partida_insumo pi ON pi.partida_id = p.id;

-- Orden 471: el insumo de roca fosforica quedo sin enlace a la formulacion y su M.P. Usada se calculaba en cero (idempotente, ya aplicado el 2026-08-25).

UPDATE xpr_insumo SET idinsumoformula = 6 WHERE idinsumo = 1517 AND idproduccion = 471;

UPDATE xpr_produccion p SET p.totalmp = (
    SELECT ROUND(SUM(i.cantidad), 2) FROM xpr_insumo i
    JOIN xpr_insumoformula f ON f.idinsumoformula = i.idinsumoformula AND f.defecto = 1
    WHERE i.idproduccion = p.idproduccion)
WHERE p.idproduccion = 471;

-- Template GENERAL del reporte diario: las zonas productivas pasan a ser opcionales por linea (las lineas BARITINA ya existentes las usan).

ALTER TABLE xpr_linea ADD COLUMN usa_zonas INT DEFAULT 0;

UPDATE xpr_linea SET usa_zonas = 1 WHERE report_template_code = 'BARITINA';

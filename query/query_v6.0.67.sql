-- Reporte de Inventario Extendido
INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
VALUES(447, 'EXTENDED_INVENTORY_REPORT', 5, 1, 'menu.warehouse.report.extendedInventoryReport', 1);

-- Eliminar reporte de inventario por subgrupo (absorbido por reporte agrupado)
DELETE FROM funcionalidad WHERE codigo = 'SUBGROUP_INVENTORY_REPORT';

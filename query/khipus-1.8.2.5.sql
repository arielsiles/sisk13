ALTER TABLE inv_articulos ADD cu DECIMAL(16,6) NULL AFTER costo_uni;
ALTER TABLE inv_articulos ADD ct DECIMAL(16,6) NULL AFTER cu;

ALTER TABLE articulos_pedido ADD cu DECIMAL(16,6) NULL AFTER importe;

UPDATE inv_articulos SET cu = 0, ct = 0;
UPDATE articulos_pedido SET cu = 0;


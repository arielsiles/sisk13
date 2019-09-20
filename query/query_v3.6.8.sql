/** 20.09.2019 **/
ALTER TABLE articulos_pedido ADD COLUMN costo_uni DECIMAL(16,6) AFTER importe;
UPDATE articulos_pedido  SET costo_uni = 0;
/** 20.09.2019 **/
alter table articulos_pedido add column costo_uni decimal(16,6) after importe;
update articulos_pedido  set costo_uni = 0;
/** 11.02.2020 **/
alter table movimiento add column id_tmpdet bigint(20) after nro_autorizacion;
alter table movimiento add foreign key (idpedidos) references pedidos (idpedidos);

update movimiento m set m.`IDVENTADIRECTA` = null, m.`TIPOPAGO` = "(contado)" 
where m.`IDMOVIMIENTO` in (2476, 2489, 2486, 2485, 2483, 2482, 2480, 2479, 2477, 2490); -- PARA ILVA
alter table movimiento add foreign key (idventadirecta) references ventadirecta (idventadirecta);
alter table movimiento add foreign key (id_tmpdet) references sf_tmpdet (id_tmpdet);
alter table documentocompra add column id_tmpdet bigint(20) after idtmpenc;



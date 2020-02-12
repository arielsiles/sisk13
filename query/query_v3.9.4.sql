/** 10.02.2020 **/
-- EJECUTADO EN ILVA TODO
alter table pedidos add foreign key (idcliente) references personacliente (idpersonacliente);
alter table pedidos add foreign key (idtipopedido) references tipopedido (idtipopedido);
alter table pedidos add foreign key (idusuario) references usuario (idusuario);

-- PAra ILVA
update pedidos p set p.`IDMOVIMIENTO` = null where p.`IDPEDIDOS` in (
13503	,14822	,14821	,13625	,13626	,13689	,13695	,13852	,13853	,13985	,14058	,14096	,14097	,14101	,14134	,14136	,
14165	,14200	,14233	,14389	,14390	,14419	,14456	,14543	,14587	,14588	,14626	,14702	,14768	,14858	);

alter table pedidos add foreign key (idmovimiento) references movimiento(idmovimiento);

alter table pedidos add foreign key (id_tmpenc) references sf_tmpenc(id_tmpenc);
alter table ventadirecta add foreign key (id_tmpenc) references sf_tmpenc(id_tmpenc);


-- -----------------------------------------------------------------------------------------

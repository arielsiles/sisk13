/** 10.02.2020 **/
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
select p.`IDPEDIDOS`, p.`IDMOVIMIENTO`, m.`IDMOVIMIENTO`
from pedidos p
left join movimiento m on p.`IDMOVIMIENTO` = m.`IDMOVIMIENTO`
where p.`IDMOVIMIENTO` is not null
;

select p.`IDPEDIDOS`, p.`IDMOVIMIENTO`, m.`IDMOVIMIENTO`
from pedidos p
left join movimiento m on p.`IDMOVIMIENTO` = m.`IDMOVIMIENTO`
where p.`IDMOVIMIENTO` is not null
;

select p.`IDPEDIDOS`, p.`id_tmpenc`, e.`id_tmpenc`
from pedidos p
left join sf_tmpenc e on p.`id_tmpenc` = e.`id_tmpenc`
where p.`id_tmpenc` is not null
;

select v.`IDVENTADIRECTA`, v.`id_tmpenc`, e.`id_tmpenc`
from ventadirecta v 
left join sf_tmpenc e on v.`id_tmpenc` = e.`id_tmpenc`
where v.`id_tmpenc` is not null
;


select *
from movimiento m
where m.`IDMOVIMIENTO` in (

);

select *
from sf_tmpenc e
where e.`id_tmpenc` in (

);

select *
from ventadirecta v
update ventadirecta v set v.`id_tmpenc` = null
where v.`IDVENTADIRECTA` in (
);
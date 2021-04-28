-- 27.04.2021
alter table categoriacliente add column idtipocaja bigint(20)after tipo;
alter table categoriacliente add foreign key (idtipocaja) references tipocaja (idtipocaja);

alter table tipocaja add column idcategoriacliente bigint(20)after no_cia;
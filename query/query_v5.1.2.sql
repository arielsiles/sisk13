-- Query v5.1.2

alter table caja add primary key (idcaja);
alter table tipocaja add primary key (idtipocaja);


alter table cajausuario modify column 

alter table cajausuario add foreign key (idcaja) references caja (idcaja);
alter table cajausuario add foreign key (idusuario) references usuario (idusuario);


-- 17.10.2020
insert into funcionalidad values (267, 'NULLIFYSALE', null, 1, 1, 'menu.customers.configuration.nullifySale', 1); -- Por confirmar

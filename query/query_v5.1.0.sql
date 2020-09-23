/** Query for v 5.1.0 **/
--
-- 23.09.2020
alter table garante drop foreign key garante_ibfk_1;
alter table garante drop column idsocio;

alter table garante add column idpersona bigint(20) not null after idgarante;
alter table garante add foreign key (idpersona) references persona (idpersona);

alter table garante add column idcredito bigint(20) not null after idpersona;
alter table garante add foreign key (idcredito) references credito (idcredito);
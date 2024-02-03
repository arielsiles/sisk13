CREATE TABLE `plantillacontablepredefinida` (
  `idplantillacontablepredefinida` bigint(20),
  `nombre` varchar(150) ,
  `version` bigint ,
  `idcompania` bigint ,
  PRIMARY KEY (`idplantillacontablepredefinida`)
) ;

alter table plantillacontablepredefinida add foreign key (idcompania) references compania (idcompania);

create table tipoplantillacontablepredefinida
(
    idtipoplantillacontablepredefinida   bigint(20) not null primary key,
    cuenta            varchar(20) not null,
    idplantillacontablepredefinida bigint(20) not null,
    no_cia             varchar(2)     not null,
    version            bigint         not null,
    idcompania         bigint         not null,
    constraint tipoplantillacontablepredefinida_ibfk_2 foreign key (idcompania) references compania (idcompania)
);

alter table tipoplantillacontablepredefinida add foreign key (idplantillacontablepredefinida) references plantillacontablepredefinida (idplantillacontablepredefinida);

INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
VALUES (445, 'ENABLE_EDIT_VOUCHER', null, 5, 1, 'Voucher.report.editing.approved', 1);

create table pagoparcialacopiomp
(
    idpagoparcialacopiomp     bigint(20)        not null
        primary key,
    descripcion             varchar(100)   null,
    monto                   decimal(12, 2) null,
    fecha            timestamp default CURRENT_TIMESTAMP not null,
    idpagoacopiomp          bigint(20)         null,
    version                 bigint(20)         null,
    idcompania              bigint(20)         null,
    constraint pagoparcialacopiomp_ibfk_1
        foreign key (idcompania) references compania (idcompania),
    constraint pagoparcialacopiomp_ibfk_2
        foreign key (idpagoacopiomp) references pagoacopiomp (idpagoacopiomp)

);


alter table pagoacopiomp
    add montoparcial decimal(12, 2) null after montodescuento;

update funcionalidad set nombrerecurso = 'Functionality.finances.accounting.enableAccountingEntry' where idfuncionalidad = 445;
/** tarea: contabilizar acopios**/
alter table acopiomp add conta int null after estado;

update acopiomp a set a.conta = 0 where a.conta is null;
update acopiomp a set a.estado = 'APR' where a.estado = 'PEN';
update acopiomp a set a.estado = 'APR' where a.estado = 'LIQ';
update acopiomp a set a.estado = 'APR' where a.estado = 'PAY';

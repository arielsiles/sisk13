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

INSERT INTO terdemol.funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
VALUES (445, 'ENABLE_EDIT_VOUCHER', null, 5, 1, 'Voucher.report.editing.approved', 1);

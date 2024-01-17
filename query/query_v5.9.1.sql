CREATE TABLE `plantillacontablepredefinida` (
  `idplantillacontablepredefinida` bigint NOT NULL DEFAULT '0',
  `nombre` varchar(150) ,
  `version` bigint ,
  `idcompania` bigint ,
  PRIMARY KEY (`idplantillacontablepredefinida`)
) ;

create table tipoplantillacontablepredefinida
(
    idtipoplantillacontablepredefinida   bigint         not null
        primary key,
    cuenta            varchar(20)     collate utf8mb3_bin default '' not null,
    idplantillacontablepredefinida bigint         not null,
    no_cia             varchar(2)     not null,
    version            bigint         not null,
    idcompania         bigint         not null,
    constraint tipoplantillacontablepredefinida_ibfk_1
        foreign key (cuenta) references arcgms (cuenta),
    constraint tipoplantillacontablepredefinida_ibfk_2
        foreign key (idcompania) references compania (idcompania)
);

create index cuenta
    on tipoplantillacontablepredefinida (cuenta);

create index idcompania
    on tipoplantillacontablepredefinida (idcompania);
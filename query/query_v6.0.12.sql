-- 23.04.2024
drop table xpr_manoobra;

CREATE TABLE `xpr_manoobra` (
    `idmanoobra` bigint(20) NOT NULL,
    `horas` decimal(10,2) DEFAULT NULL,
    `idempleado` bigint(20) DEFAULT NULL,
    `idproduccion` bigint(20) DEFAULT NULL,
    `version` bigint(20) DEFAULT NULL,
    `idcompania` bigint(20) DEFAULT NULL,
    PRIMARY KEY (`idmanoobra`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

alter table xpr_manoobra add foreign key (idempleado) references empleado(idempleado);
alter table xpr_manoobra add foreign key (idproduccion) references xpr_produccion(idproduccion);
alter table xpr_manoobra add foreign key (idcompania) references compania(idcompania);

alter table xpr_produccion add column fechafin date after descripcion;

alter table xpr_manoobra add column costoxhora decimal(10,2) after horas;
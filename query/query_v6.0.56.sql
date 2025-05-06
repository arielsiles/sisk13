-- 05.05.2025

CREATE TABLE `xpr_grupo` (
    `idgrupo` bigint NOT NULL,
    `nombre` varchar(255) NOT NULL,
    `codigo` varchar(50) NOT NULL,
    `version` bigint NOT NULL,
    `idcompania` bigint NOT NULL,
    PRIMARY KEY (`idgrupo`)
);

-- insertar datos xpr_turno, 3 registros
insert into xpr_grupo (idgrupo, nombre, codigo, version, idcompania) values (1, 'Grupo 1', 'G1', 1, 1);
insert into xpr_grupo (idgrupo, nombre, codigo, version, idcompania) values (2, 'Grupo 2', 'G2', 1, 1);
insert into xpr_grupo (idgrupo, nombre, codigo, version, idcompania) values (3, 'Grupo 3', 'G3', 1, 1);

-- adicionar en xpr_produccion idturno, despues de idlinea
alter table xpr_produccion add column idgrupo bigint after idlinea;
alter table xpr_produccion add foreign key (idgrupo) references xpr_grupo (idgrupo);

update xpr_produccion pr set pr.idgrupo = pr.idturno where pr.idgrupo is null;
update xpr_produccion pr set pr.idturno = null where pr.idturno is not null;

-- eliminar la llave foranea idturno de xpr_produccion
-- alter table xpr_produccion drop foreign key xpr_produccion_ibfk_6; revisar cual es

-- eliminar la columna idturno de xpr_produccion
alter table xpr_produccion drop column idturno;

-- eliminar la tabla xpr_turno
drop table if exists xpr_turno;

--
update xpr_produccion pr set pr.tipoturno = 'D' where pr.tipoturno = 'DAY';
update xpr_produccion pr set pr.tipoturno = 'N' where pr.tipoturno = 'NIGHT';
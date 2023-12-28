-- 03.12.2023
ALTER TABLE detallepagoacopiomp DROP COLUMN peso;
ALTER TABLE detallepagoacopiomp DROP COLUMN precio;
ALTER TABLE detallepagoacopiomp DROP COLUMN monto;

ALTER TABLE pagoacopiomp ADD COLUMN cuentabanco VARCHAR(20) AFTER estado;
ALTER TABLE pagoacopiomp ADD COLUMN cuentacaja VARCHAR(20) AFTER estado;
ALTER TABLE pagoacopiomp ADD COLUMN glosapago VARCHAR(1000) AFTER glosa;

-- 14.12.2023

CREATE TABLE tipodescuentoacopiomp (
    `idtipodescuentoacopiomp` bigint(20) NOT NULL,
    `nombre` varchar(100),
    `tipo` varchar(100),
    PRIMARY KEY (`idtipodescuentoacopiomp`)
);

CREATE TABLE descuentoacopiomp (
    `iddescuentoacopiomp` bigint(20) NOT NULL,
    `descripcion` varchar(100),
    `tipo` varchar(100),
    `monto` decimal(12, 2),
    `idpagoacopiomp` bigint(20),
    `idtipodescuentoacopiomp` bigint(20),
    `version` bigint(20),
    `idcompania` bigint(20),
    PRIMARY KEY (`iddescuentoacopiomp`),
    FOREIGN KEY (`idcompania`)      REFERENCES `compania` (`idcompania`),
    FOREIGN KEY (`idpagoacopiomp`)  REFERENCES `pagoacopiomp` (`idpagoacopiomp`),
    FOREIGN KEY (`idtipodescuentoacopiomp`) references tipodescuentoacopiomp (idtipodescuentoacopiomp)
);

-- 20.12.2023
alter table pagoacopiomp add column montoliquido   decimal(12, 2) after montopago;
alter table pagoacopiomp add column montodescuento decimal(12, 2) after montopago;

ALTER TABLE descuentoacopiomp DROP COLUMN tipo;

-- 26.12.2023
-- alter table tipodescuentoacopiomp add column ctadcto;

INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
VALUES(288, 'PAYMENT_RAWMATERIALPRODUCER', 6, 1, 'Functionality.production.paymentRawMaterialProducer', 1);

-- 18.09.2023
INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
VALUES(270, 'ORDER_RELATIONSHIP', 1, 1, 'Functionality.customers.relationshipVoucher', 1);

INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
VALUES(271, 'DESTINATION', 5, 15, 'Functionality.warehouse.destinationVoucher', 1);

CREATE TABLE `inv_destino` (
    `iddestino` bigint(20) NOT NULL,
    `codigo` varbinary(50) DEFAULT NULL,
    `nombre` varchar(255) DEFAULT NULL,
    `version` bigint(20) DEFAULT NULL,
    `idcompania` bigint(20) DEFAULT NULL,
    PRIMARY KEY (`iddestino`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8


/** 22-10-2018 **/
/*
CREATE TABLE tipocuenta (
	idtipocuenta BIGINT(20) NOT NULL,
	nombre VARCHAR(100) NOT NULL,
	interes DECIMAL(4,2) NOT NULL,
	activo INT(1) NULL,
	VERSION BIGINT(20) NULL,
	idcompania BIGINT(20) NULL,
	PRIMARY KEY (idtipocuenta)
);

-- ALTER TABLE cuenta ADD COLUMN idtipocuenta BIGINT(20) NOT NULL AFTER idsocio; -- REVISAR
ALTER TABLE cuenta ADD FOREIGN KEY (idtipocuenta) REFERENCES tipocuenta(idtipocuenta);

ALTER TABLE transaccioncuenta CHANGE tipo tipotrans VARCHAR(32) NULL;
ALTER TABLE cuenta ADD COLUMN saldo DECIMAL(13,2) NOT NULL AFTER estado;

INSERT INTO tipocuenta (idtipocuenta, nombre, interes, activo, VERSION, idcompania) VALUES (1, "CAJA DE AHORRO", 3, 1 , 0, 1);
*/

/** 20-11-2018 **/
ALTER TABLE sf_tmpdet ADD COLUMN cod_art VARCHAR(6) NULL;
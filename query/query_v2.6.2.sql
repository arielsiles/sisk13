/** 22-10-2018 **/
CREATE TABLE tipocuenta (
	idtipocuenta BIGINT(20) NOT NULL,
	nombre VARCHAR(100) NOT NULL,
	interes DECIMAL(4,2) NOT NULL,
	activo INT(1) NULL,
	VERSION BIGINT(20) NULL,
	idcompania BIGINT(20) NULL,
	PRIMARY KEY (idtipocuenta)
);

ALTER TABLE cuenta ADD COLUMN idtipocuenta BIGINT(20) NOT NULL AFTER idsocio;
ALTER TABLE cuenta ADD FOREIGN KEY (idtipocuenta) REFERENCES tipocuenta(idtipocuenta);

/** 23/10/2018 **/
ALTER TABLE transaccioncuenta CHANGE tipo tipotrans VARCHAR(32) NULL;

ALTER TABLE cuenta ADD COLUMN saldo DECIMAL(13,2) NOT NULL AFTER estado;
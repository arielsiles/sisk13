/** 17122016 **/
ALTER TABLE transaccioncredito ADD COLUMN glosa VARCHAR(255) AFTER importe;

/** 18122016 **/
ALTER TABLE transaccioncredito ADD PRIMARY KEY(idtransaccioncredito);
ALTER TABLE transaccioncredito MODIFY COLUMN idcredito BIGINT NOT NULL;

ALTER TABLE transaccioncredito ADD FOREIGN KEY (idcredito) REFERENCES credito (idcredito);

ALTER TABLE persona ADD FOREIGN KEY (idpersona)   REFERENCES entidad (identidad);


ALTER TABLE empleado ADD FOREIGN KEY (idempleado) REFERENCES persona (idpersona);
ALTER TABLE socio    ADD FOREIGN KEY (idsocio)    REFERENCES persona (idpersona);

-- INSERTAR PERSONA 510, 511 OK

SELECT e.idempleado, p.idpersona
FROM empleado e
LEFT JOIN persona p ON e.idempleado = p.idpersona
;

/** 21122016 **/
CREATE TABLE cuenta (
  idcuenta 	BIGINT(20) NOT NULL,
  nocuenta 	VARCHAR(20) NOT NULL,
  moneda 	VARCHAR(20) NOT NULL,
  estado 	VARCHAR(32) NOT NULL,
  idsocio	BIGINT(20) NOT NULL,
  VERSION 	BIGINT(20) NOT NULL,
  idcompania	BIGINT(20) NOT NULL,
  PRIMARY KEY (idcuenta)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

ALTER TABLE cuenta ADD FOREIGN KEY (idsocio) 	REFERENCES socio (idsocio);
ALTER TABLE cuenta ADD FOREIGN KEY (idcompania) REFERENCES compania (idcompania);

CREATE TABLE transaccioncuenta (
  idtransaccioncuenta 	BIGINT(20) NOT NULL,
  importe 		DECIMAL(13,2) NOT NULL,
  glosa 		VARCHAR(255) NOT NULL,
  fechatransaccion	DATETIME NOT NULL,
  tipotrans 		VARCHAR(32) NOT NULL,
  idcuenta 		BIGINT(20) NOT NULL,
  VERSION 	BIGINT(20) NOT NULL,
  idcompania	BIGINT(20) NOT NULL,
  PRIMARY KEY (idtransaccioncuenta)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

ALTER TABLE transaccioncuenta ADD FOREIGN KEY (idcuenta) REFERENCES cuenta (idcuenta);
ALTER TABLE transaccioncuenta ADD FOREIGN KEY (idcompania) REFERENCES compania (idcompania);




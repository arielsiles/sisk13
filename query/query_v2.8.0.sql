-- GRANT ALL PRIVILEGES ON khipus.* TO 'admin'@'localhost' IDENTIFIED BY 'Cisc.13';
/** 11-12-2018 **/
UPDATE tipocuenta t SET t.`ctap_mn` = '2120110200', t.`ctap_me` = '2120130200' WHERE t.`idtipocuenta` = 1;

/** 14-12-2018 **/
INSERT INTO _sequence VALUES ('CE', 0);
INSERT INTO tipodoc VALUES (12, 'CE', 'COMPROBANTE DE EGRESO');

ALTER TABLE sf_tmpdet ADD COLUMN idcredito BIGINT(20) NULL AFTER idcuenta;

/** 18-12-2018 **/

-- DEFINIR PRECIO DE VENTA EN INV_ARTICULOS
-- DEFINIR COSTO DEL ARTICULO
-- Asiganar corectamente la cuenta de almacen libretas

ALTER TABLE sf_tmpdet ADD COLUMN idsocio BIGINT(20) NULL AFTER cod_art;
ALTER TABLE sf_tmpdet ADD COLUMN cant_art BIGINT(20) NULL AFTER cod_art;

ALTER TABLE transaccioncredito ADD COLUMN id_tmpenc BIGINT(20) AFTER idcredito;
ALTER TABLE transaccioncredito ADD COLUMN VERSION BIGINT(20) NULL; 
UPDATE transaccioncredito t SET t.`version` = 0;

/** 01-01-2018 **/
-- alter table documentocontable modify column nombre varchar(100);  do it

-- NE, PED 

SELECT *
FROM sf_tmpenc e
WHERE e.`tipo_doc` = 'NE'
AND e.`fecha` > '2018-12-01'
;

/** 03-01-2019 **/
CREATE TABLE tipocredito (
	idtipocredito BIGINT(20) NOT NULL,
	nombre VARCHAR(255),
	moneda VARCHAR(20),
	ctavig VARCHAR(20),
	ctaven VARCHAR(20),
	ctaeje VARCHAR(20),
	VERSION BIGINT(20),
	PRIMARY KEY (idtipocredito)
);

/** 05-01-2018 **/
ALTER TABLE credito ADD COLUMN idtipocredito BIGINT(20);
ALTER TABLE credito ADD FOREIGN KEY (idtipocredito) REFERENCES tipocredito(idtipocredito);

/** 06-01-2018 **/
ALTER TABLE tipocredito ADD COLUMN idmoneda BIGINT(20);
ALTER TABLE moneda ADD PRIMARY KEY (idmoneda);
ALTER TABLE tipocredito ADD FOREIGN KEY (idmoneda) REFERENCES moneda(idmoneda);

ALTER TABLE tipocredito ADD COLUMN ictavig VARCHAR(20) AFTER ctaeje;
ALTER TABLE tipocredito ADD COLUMN ictaven VARCHAR(20) AFTER ictavig;

/** 10-01-2018 **/
ALTER TABLE credito ADD COLUMN ultimopago DATE AFTER fechacreacion;

/** 15-01-2018 **/
CREATE TABLE estadoahorro (
	id  		BIGINT(20) NOT NULL,
	cuenta 		VARCHAR(10),
	descr 		VARCHAR(100), 
	nsocio		VARCHAR(100), 
	nombrecomp	VARCHAR(100), 	
	nombres 	VARCHAR(100), 
	ap 		VARCHAR(100), 
	am 		VARCHAR(100), 
	saldocuenta 	DECIMAL(16,2) ,	
	interes 	DECIMAL(16,2), 
	saldoaporte 	DECIMAL(16,2),	
	unidad 		VARCHAR(10),
	PRIMARY KEY(id)
);

CREATE TABLE estadocartera (
	id  		BIGINT(20) NOT NULL,
	cuenta 		VARCHAR(10),
	descr 		VARCHAR(100), 
	ngab 		VARCHAR(10), 
	nrocred		VARCHAR(10), 
	Moneda		VARCHAR(10), 
	gabsocio	VARCHAR(10), 
	nombrecomp	VARCHAR(100), 	
	nombres 	VARCHAR(100), 
	ap 		VARCHAR(100), 
	am 		VARCHAR(100), 
	fapertura	DATE,
	ianual 		DECIMAL(16,2) , 
	garantia	VARCHAR(10), 
	objetivo	VARCHAR(20), 
	saldoactual	DECIMAL(16,2) , 
	ultimopago	DATE,
	diasmora	INT,
	interes		DECIMAL(16,2) , 
	dias 		INT,
	unidad 		VARCHAR(10),
	PRIMARY KEY(id)
);


/** 30-01-2019 **/
INSERT INTO funcionalidad (idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania) VALUES (250, 'CREDITTRANSFER', 1, 1, 'Functionality.credit.transfer', 1);

ALTER TABLE tipocredito ADD COLUMN ictaeje VARCHAR(20) AFTER ictaven;
ALTER TABLE credito ADD COLUMN ipenal DECIMAL(5, 2) AFTER anual;
UPDATE credito c SET c.`ipenal` = 7;

-- actualizar secuencia idsocio
-- actualizar secuencia nosocio




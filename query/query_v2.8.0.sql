-- GRANT ALL PRIVILEGES ON khipus.* TO 'admin'@'localhost' IDENTIFIED BY 'Cisc.13';
/** 11-12-2018 **/
UPDATE tipocuenta t SET t.`ctap_mn` = '2120110200', t.`ctap_me` = '2120130200' WHERE t.`idtipocuenta` = 1;
-- DELETE FROM arcgms;

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



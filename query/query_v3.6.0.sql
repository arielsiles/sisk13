/** 25.07.2019 **/
CREATE TABLE pr_prodafectado(
	idprodafectado BIGINT(20) NOT NULL,
	idinsumo BIGINT(20) NOT NULL,
	idproducto BIGINT(20) NOT NULL,
	VERSION BIGINT(20) NOT NULL,
	idcompania BIGINT(20) NOT NULL,
	PRIMARY KEY(idprodafectado)	
);

ALTER TABLE pr_prodafectado ADD FOREIGN KEY (idinsumo) REFERENCES pr_insumo (idinsumo);
ALTER TABLE pr_prodafectado ADD FOREIGN KEY (idproducto) REFERENCES pr_producto (idproducto);
ALTER TABLE pr_prodafectado ADD FOREIGN KEY (idcompania) REFERENCES compania (idcompania);

ALTER TABLE sf_tmpenc ADD COLUMN ndoc VARCHAR(20) NULL;

--

UPDATE socio s SET s.`nombres` = TRIM(s.`nombres`), s.`apellidopaterno` = TRIM(s.`apellidopaterno`), s.`apellidomaterno` = TRIM(s.`apellidomaterno`)
UPDATE credito c SET c.`codigoant` = TRIM(c.`codigoant`)

/* Solo para F. LECHERO
update credito set codigoant = 	'51-025-03'	 where idcredito = 	76	;
update credito set codigoant = 	'49-073-04'	 where idcredito = 	75	;
update credito set codigoant = 	'49-069-04'	 where idcredito = 	74	;
update credito set codigoant = 	'24-017-02'	 where idcredito = 	29	;
update credito set codigoant = 	'24-015-04'	 where idcredito = 	28	;
update credito set codigoant = 	'20-053-01'	 where idcredito = 	26	;
update credito set codigoant = 	'19-007-01'	 where idcredito = 	318	;
update credito set codigoant = 	'15-041-03'	 where idcredito = 	138	;
update credito set codigoant = 	'15-041-02'	 where idcredito = 	137	;
update credito set codigoant = 	'13-010-02'	 where idcredito = 	270	;
update credito set codigoant = 	'12-090-02'	 where idcredito = 	124	;
update credito set codigoant = 	'12-061-02'	 where idcredito = 	10	;
update credito set codigoant = 	'12-045-02'	 where idcredito = 	9	;
update credito set codigoant = 	'12-032-01'	 where idcredito = 	8	;
update credito set codigoant = 	'12-013-02'	 where idcredito = 	7	;
update credito set codigoant = 	'05-122-05'	 where idcredito = 	102	;
update credito set codigoant = 	'05-100-03'	 where idcredito = 	122	;
update credito set codigoant = 	'05-100-02'	 where idcredito = 	3	;
update credito set codigoant = 	'04-099-07'	 where idcredito = 	283	;
*/

--
-- Actualizando Glosa de desmbolso credito
UPDATE sf_tmpenc e
JOIN sf_tmpdet d ON e.`id_tmpenc` = d.`id_tmpenc`
JOIN credito c ON d.`idcredito` = c.`idcredito`
JOIN socio s ON c.`idsocio` = s.`idsocio`
SET e.`glosa` = CONCAT(s.`nombres`,' ', s.`apellidopaterno`, ' ', s.`apellidomaterno`, ' ', c.`codigoant`, ', ', e.`glosa`)
WHERE e.`tipo_doc` = 'CE'
;
-- Actualizando fecha de asientos , transaccion credito
UPDATE sf_tmpenc e 
JOIN transaccioncredito t ON e.`id_tmpenc` = t.`id_tmpenc`
SET e.`fecha` = t.`fechatransaccion`
;


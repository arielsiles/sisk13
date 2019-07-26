/** 25.07.2019 **/
create table pr_prodafectado(
	idprodafectado bigint(20) not null,
	idinsumo bigint(20) not null,
	idproducto bigint(20) not null,
	VERSION bigint(20) not null,
	idcompania bigint(20) not null,
	primary key(idprodafectado)	
);

alter table pr_prodafectado add foreign key (idinsumo) references pr_insumo (idinsumo);
alter table pr_prodafectado add foreign key (idproducto) references pr_producto (idproducto);
alter table pr_prodafectado add foreign key (idcompania) references compania (idcompania);

alter table sf_tmpenc add column ndoc varchar(20) null;

--

update socio s set s.`nombres` = TRIM(s.`nombres`), s.`apellidopaterno` = TRIM(s.`apellidopaterno`), s.`apellidomaterno` = TRIM(s.`apellidomaterno`);
update credito c set c.`codigoant` = TRIM(c.`codigoant`);

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
update sf_tmpenc e
join sf_tmpdet d on e.`id_tmpenc` = d.`id_tmpenc`
join credito c on d.`idcredito` = c.`idcredito`
join socio s on c.`idsocio` = s.`idsocio`
set e.`glosa` = CONCAT(s.`nombres`,' ', s.`apellidopaterno`, ' ', s.`apellidomaterno`, ' ', c.`codigoant`, ', ', e.`glosa`)
where e.`tipo_doc` = 'CE'
;
-- Actualizando fecha de asientos , transaccion credito
update sf_tmpenc e 
join transaccioncredito t on e.`id_tmpenc` = t.`id_tmpenc`
set e.`fecha` = t.`fechatransaccion`
;


/** 20.09.2019 **/
alter table articulos_pedido add column costo_uni decimal(16,6) after importe;
update articulos_pedido  set costo_uni = 0;

/** 25.09.2019 **/
alter table sf_tmpdet add foreign key (idpersonacliente) references personacliente (idpersonacliente);
alter table sf_tmpdet add foreign key (idsocio) references socio (idsocio);
alter table sf_tmpdet add foreign key (idcuenta) references cuenta (idcuenta);
alter table sf_tmpdet add foreign key (idcredito) references credito (idcredito);


alter table socio add column idpersona bigint(20) after idsocio;

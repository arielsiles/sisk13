/*:03102009 insert default company*/
insert into compania(idcompania,codigo) value (1,'encens');

/*:03102009 insert default values to modulo table*/
insert into modulo(idmodulo,descripcion,nombrerecurso,idcompania) values (1,NULL,'customers',1);
insert into modulo(idmodulo,descripcion,nombrerecurso,idcompania) values (2,NULL,'admin',1);
insert into modulo(idmodulo,descripcion,nombrerecurso,idcompania) values (3,NULL,'contacts',1);
insert into modulo(idmodulo,descripcion,nombrerecurso,idcompania) values (4,NULL,'employees',1);
insert into modulo(idmodulo,descripcion,nombrerecurso,idcompania) values (5,NULL,'finances',1);
insert into modulo(idmodulo,descripcion,nombrerecurso,idcompania) values (6,NULL,'products',1);

/*:03102009 insert default values to funcionalidad table*/
insert into funcionalidad(idfuncionalidad,permiso,codigo,descripcion,idmodulo,nombrerecurso,idcompania) values (1,3,'CASHBOX',NULL,1,'customers.cashBox',1);
insert into funcionalidad(idfuncionalidad,permiso,codigo,descripcion,idmodulo,nombrerecurso,idcompania) values (2,15,'ROLE',NULL,2,'admin.role',1);
insert into funcionalidad(idfuncionalidad,permiso,codigo,descripcion,idmodulo,nombrerecurso,idcompania) values (3,15,'USER',NULL,2,'admin.user',1);
insert into funcionalidad(idfuncionalidad,permiso,codigo,descripcion,idmodulo,nombrerecurso,idcompania) values (4,15,'BUSINESS',NULL,2,'admin.business',1);
insert into funcionalidad(idfuncionalidad,permiso,codigo,descripcion,idmodulo,nombrerecurso,idcompania) values (5,15,'BUSINESSUNIT',NULL,2,'admin.businessUnit',1);
insert into funcionalidad(idfuncionalidad,permiso,codigo,descripcion,idmodulo,nombrerecurso,idcompania) values (6,15,'BUSINESSUNITTYPE',NULL,2,'admin.businessUnitType',1);
insert into funcionalidad(idfuncionalidad,permiso,codigo,descripcion,idmodulo,nombrerecurso,idcompania) values (7,15,'EMPLOYEE',NULL,4,'employees.employee',1);
insert into funcionalidad(idfuncionalidad,permiso,codigo,descripcion,idmodulo,nombrerecurso,idcompania) values (8,15,'PRODUCT',NULL,6,'products.product',1);
insert into funcionalidad(idfuncionalidad,permiso,codigo,descripcion,idmodulo,nombrerecurso,idcompania) values (9,15,'PRODUCTTYPE',NULL,6,'products.producType',1);
insert into funcionalidad(idfuncionalidad,permiso,codigo,descripcion,idmodulo,nombrerecurso,idcompania) values (10,15,'PRODUCTSTATE',NULL,6,'products.producState',1);
insert into funcionalidad(idfuncionalidad,permiso,codigo,descripcion,idmodulo,nombrerecurso,idcompania) values (11,15,'CASHBOXTYPE',NULL,5,'finances.cashBoxType',1);
insert into funcionalidad(idfuncionalidad,permiso,codigo,descripcion,idmodulo,nombrerecurso,idcompania) values (12,15,'CASHBOXSTATETYPE',NULL,5,'finances.cashBoxStateType',1);
insert into funcionalidad(idfuncionalidad,permiso,codigo,descripcion,idmodulo,nombrerecurso,idcompania) values (13,15,'TAXPERCENTAGETYPE',NULL,5,'finances.taxPercentageType',1);
insert into funcionalidad(idfuncionalidad,permiso,codigo,descripcion,idmodulo,nombrerecurso,idcompania) values (14,15,'DOSAGETYPE',NULL,5,'finances.dosageType',1);
insert into funcionalidad(idfuncionalidad,permiso,codigo,descripcion,idmodulo,nombrerecurso,idcompania) values (15,15,'PREBOOKENTRYPERIODTYPE',NULL,5,'finances.preBookEntryPeriodType',1);
insert into funcionalidad(idfuncionalidad,permiso,codigo,descripcion,idmodulo,nombrerecurso,idcompania) values (16,15,'ACCOUNTINGSTATETYPE',NULL,5,'finances.accountingStateType',1);
insert into funcionalidad(idfuncionalidad,permiso,codigo,descripcion,idmodulo,nombrerecurso,idcompania) values (17,5,'CASHBOXTRANSACTION',NULL,1,'customers.cashBoxTransaction',1);
insert into funcionalidad(idfuncionalidad,permiso,codigo,descripcion,idmodulo,nombrerecurso,idcompania) values (18,1,'CASHBOXADMIN',NULL,1,'customers.cashBoxAdmin',1);

/*:03102009 insert default values to rol table*/
insert into rol(idrol,nombre,descripcion,version,idcompania) values (1,'Administrador',NULL,1,1);

/*:03102009 insert default values to derechoacceso table*/
insert into derechoacceso(idfuncionalidad,idrol,permiso,idcompania) values (1,1,3,1);
insert into derechoacceso(idfuncionalidad,idrol,permiso,idcompania) values (2,1,15,1);
insert into derechoacceso(idfuncionalidad,idrol,permiso,idcompania) values (3,1,15,1);
insert into derechoacceso(idfuncionalidad,idrol,permiso,idcompania) values (4,1,15,1);
insert into derechoacceso(idfuncionalidad,idrol,permiso,idcompania) values (5,1,15,1);
insert into derechoacceso(idfuncionalidad,idrol,permiso,idcompania) values (6,1,15,1);
insert into derechoacceso(idfuncionalidad,idrol,permiso,idcompania) values (7,1,15,1);
insert into derechoacceso(idfuncionalidad,idrol,permiso,idcompania) values (8,1,15,1);
insert into derechoacceso(idfuncionalidad,idrol,permiso,idcompania) values (9,1,15,1);
insert into derechoacceso(idfuncionalidad,idrol,permiso,idcompania) values (10,1,15,1);
insert into derechoacceso(idfuncionalidad,idrol,permiso,idcompania) values (11,1,15,1);
insert into derechoacceso(idfuncionalidad,idrol,permiso,idcompania) values (12,1,15,1);
insert into derechoacceso(idfuncionalidad,idrol,permiso,idcompania) values (13,1,15,1);
insert into derechoacceso(idfuncionalidad,idrol,permiso,idcompania) values (14,1,15,1);
insert into derechoacceso(idfuncionalidad,idrol,permiso,idcompania) values (15,1,15,1);
insert into derechoacceso(idfuncionalidad,idrol,permiso,idcompania) values (16,1,15,1);
insert into derechoacceso(idfuncionalidad,idrol,permiso,idcompania) values (17,1,5,1);
insert into derechoacceso(idfuncionalidad,idrol,permiso,idcompania) values (18,1,1,1);

/*:03102009 insert values for default user*/

insert into tipodocumento(idtipodocumento, nombre, version, idcompania) values(1,'CI',1,1);
insert into entidad (identidad, noidentificacion, version, idtipodocumento, idcompania) values (1,'123456789', 0, 1, 1);
insert into persona (idpersona, apellidopaterno, apellidomaterno, nombres, fechanacimiento, profesion, idcompania) values (1, 'Siles', 'Encinas', 'Ariel', NULL, NULL, 1);

insert into empleado (idempleado, fechaingreso, fechasalida, salario, idcompania, flagafp, flagcontrol, flagjubilado, codigomarcacion, tipodepago, flagret) values (1, NULL, NULL, NULL, 1, 0, 0, 0, '123456789', 'PAYMENT_WITH_CHECK', 0);
insert into usuario (idusuario, usuario, clave, email, fechacreacion, idcompania) values (1, 'root', '8a8b61dff6b4f92b0cec80be671d61cd23e6391d', NULL, '2008-08-31 22:06:52', 1);

insert into usuariorol(idusuario,idrol) values (1,1);

insert into secuencia (tabla, valor) values ('compania', 1);
insert into secuencia (tabla, valor) values ('tipoentidad', 1);
insert into secuencia (tabla, valor) values ('tipodocumento', 1);
insert into secuencia (tabla, valor) values ('entidad', 1);
insert into secuencia (tabla, valor) values ('modulo', 6);
insert into secuencia (tabla, valor) values ('funcionalidad', 18);
insert into secuencia (tabla, valor) values ('rol', 1);

/*
delete from usuariorol;
delete from derechoacceso;
delete from rol;
delete from funcionalidad;
delete from modulo;
delete from usuario;
delete from empleado;
delete from persona;
delete from entidad;
delete from tipodocumento;
delete from tipoentidad;
delete from compania;
delete from secuencia;*/

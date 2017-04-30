/* Defining the default test company */
insert into compania (idcompania, codigo) values (1, 'efestia');

/* User loaded by default for test purposes */
insert into tipodocumento (idtipodocumento, nombre, version, idcompania) values (1, 'CI', 1, 1);
/* Employee 1 */
insert into entidad (noidentificacion, version, identidad, extension, idcompania, idtipodocumento) values ('111111', 1, 1, 'CB', 1, 1);
insert into persona (apellidopaterno, apellidomaterno, nombres, fechanacimiento, profesion, idpersona, idcompania) values ('Siles', 'Encinas', 'Ariel', NULL, NULL, 1, 1);
insert into empleado (idempleado, idcompania) values (1,1);
/* Employee 2 */
insert into entidad (noidentificacion, version, identidad, extension, idcompania, idtipodocumento) values ('222222', 1, 2, 'CB', 1, 1);
insert into persona (apellidopaterno, apellidomaterno, nombres, fechanacimiento, profesion, idpersona, idcompania) values ('Perez', 'Lopez', 'Juan', NULL, NULL, 2, 1);
insert into empleado (idempleado, idcompania) values (2,1);

/** User 1. Default user inserted, username root*/
insert into usuario (usuario, clave, fechacreacion, idusuario, idcompania, version) values ('root', '8a8b61dff6b4f92b0cec80be671d61cd23e6391d',  '2008-08-31 22:06:52', 1,1, 1);
/* Rol by default */
insert into rol (idrol, nombre, version, idcompania) values (1, 'Admin', 1, 1);
insert into secuencia (tabla, valor) values ('entidad', 2);
insert into secuencia (tabla, valor) values ('tipodocumento', 1);
insert into secuencia (tabla, valor) values ('rol', 1);

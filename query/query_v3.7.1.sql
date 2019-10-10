/** 08.10.2019 **/
alter table cuentabancaria add primary key(idcuentabancaria);
alter table cuentabancaria add foreign key (idempleado) references empleado (idempleado);
--

/** 10.10.2019 **/
alter table cuentabancaria add constraint empcuenta unique (identidadbancaria, idempleado, numerocuenta);

alter table persona add column telcelular varchar(20) after profesion;
alter table productormateriaprima add column numerocuenta varchar(50) after esresponsable;

insert into funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania) values(255, 'PRODUCERBANKACCOUNT', 6, 5, 'Functionality.production.producerBankAccount', 1);
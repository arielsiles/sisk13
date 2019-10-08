/** 08.10.2019 **/
alter table cuentabancaria add primary key(idcuentabancaria);
alter table cuentabancaria add foreign key (idempleado) references empleado (idempleado);
--

alter table cuentabancaria add column idproductor bigint(20);
alter table cuentabancaria add foreign key (idproductor) references productormateriaprima (idproductormateriaprima);
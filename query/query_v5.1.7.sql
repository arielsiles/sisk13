-- 19.04.2021
alter table inv_articulos modify column saldo_mon decimal(20,6) null;
alter table inv_articulos modify column ct decimal(20,6) null;

-- 21.04.2021
alter table tipocaja add column cuentacontingencia varchar(20) null after cuentaxcobrar;
update tipocaja set cuentacontingencia = '2570110000' where idtipocaja in (1,2,3);
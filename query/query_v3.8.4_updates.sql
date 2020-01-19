/** FCISC 16.01.2019 **/
update sf_tmpdet d set d.`cuenta` = '1110110204' where d.`id_tmpdet` = 612247;
delete from arcgms where cuenta = '1110110202';
update arcgms a set a.`cuenta` = '1110110202', a.`descri` = 'Caja Chica Dpto. Administrativo' where a.`cuenta` = '1110110204';
update sf_tmpdet d set d.`cuenta` = '1110110202' where d.`id_tmpdet` in (6544, 612247);
update arcgms a set a.`descri` = 'RC IVA retenido a Clientes - DPF' where a.`cuenta` = '2420310100';

update sf_tmpdet d set d.`cuenta` = '2420610600' where d.`id_tmpdet` = 3312;
update arcgms a set a.`descri` = 'Provivienda - Ap. Patronal' where a.`cuenta` = '2420610600';
delete from arcgms where cuenta = '2420610601';

update sf_tmpdet d set d.`cuenta` = '1320510100' where d.`id_tmpdet` in (569658,574748,574971,575123,575288,575459);
update sf_tmpdet d set d.`cuenta` = '5140510100' where d.`id_tmpdet` in (569659,574749,574972,575124,575289,575460);

update transaccioncredito t set t.`capital` = 847.09, t.`interes` = 263.91 where t.`idtransaccioncredito` = 259;
update credito c set c.`saldo` =  15407.56 where c.`idcredito` = 64;

update arcgms a set a.`descri` = 'Gastos Notariales y Judiciales' where a.`cuenta` = '4590100000';

select *
from sf_tmpdet d
where d.`cuenta` = '4590100000';

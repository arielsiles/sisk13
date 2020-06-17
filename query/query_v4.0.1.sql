/** 16.06.2020 **/
alter table tipocuenta add column posicion int after nombre;

/** Solo para FLech **/
insert into tipocuenta values (7, 'Depositos Plazo Fijo 180 Dias 4%', null, 180, 4, 0, 1, 'DPF', '2130410000', '2130420000', '2120130200', '2180310000', '2180320000', 0, 1);
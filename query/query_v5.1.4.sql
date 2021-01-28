-- v5.1.4
-- 23.01.2021
-- Para diferimiento
alter table credito add column montodif decimal(13,2) not null default 0 after importe;
alter table credito add column cuotadif decimal(13,2) not null default 0 after cuota;
alter table transaccioncredito add column cuotadif decimal(13,2)  default 0 after interes;

-- 27.01.2021
alter table credito add column traspaso int(1) not null default 0 after entregado;
alter table credito add column idcreditoorig bigint(20) after traspaso;
alter table credito add foreign key (idcreditoorig) references credito(idcredito);

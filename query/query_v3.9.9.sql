/** 17.03.2020 **/
alter table tipocuenta add column ctacf_mn varchar(20) after ctap_mv;
alter table tipocuenta add column ctacf_me varchar(20) after ctacf_mn;
alter table cuenta add column capital decimal(13,2) after moneda;
update cuenta set capital = 0;

-- For FLech
update tipocuenta t set t.`ctacf_mn` = 2180310000 where t.`idtipocuenta` in (2, 3, 4, 6);
update tipocuenta t set t.`ctacf_me` = 2180320000 where t.`idtipocuenta` in (2, 3, 4, 6);

/** 18.03.2020 **/
alter table cuenta add column beneficio1 varchar(100) after saldo;
alter table cuenta add column beneficio2 varchar(100) after beneficio1;
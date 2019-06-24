/** 24.06.2019 **/
alter table transaccioncredito add column traspaso int(1) after tipo;
update transaccioncredito t set t.`traspaso` = 0;

update _sequence s set s.`seq_val` = 2 where s.`seq_name` = 'CT';
/** 20.07.2019 **/
insert into gensecuencia values(19, 'SAVINGS_ACCOUNT_NUMBER', 1000000100, 1);
update secuencia set valor = (select MAX(g.`idgensecuencia`)+1 from gensecuencia g) where tabla='gensecuencia';


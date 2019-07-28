/** 28.07.2019 **/
alter table pr_produccion add column id_tmpenc bigint(20) after idplan;

update gensecuencia g set g.`valor` = 675 where g.`nombre` = 'PRODUCTION_CODE';
set @rank:=675; update pr_produccion set codigo=@rank:=@rank+1;
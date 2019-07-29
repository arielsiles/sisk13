/** 28.07.2019 **/
alter table pr_produccion add column id_tmpenc bigint(20) after idplan;

update gensecuencia g set g.`valor` = 675 where g.`nombre` = 'PRODUCTION_CODE';
set @rank:=675; update pr_produccion set codigo=@rank:=@rank+1;

-- 
update sf_tmpdet d set d.`cuenta` = '1110110100'
where d.`cuenta` = '1110110101';

update sf_tmpenc e set e.`tipo_doc` = 'CI'
where e.`tipo_doc` = 'RI';

update _sequence s set s.`seq_val` = (select max(cast(e.`no_doc` as unsigned)) from sf_tmpenc e where e.`tipo_doc` = 'CI')
where s.`seq_name` = 'CI';

alter table configuracion add column titulo varchar(100) after no_cia; 
alter table configuracion add column subtitulo varchar(100) after titulo;

update configuracion set titulo = 'COOPERATIVA INTEGRAL DE SERVICIOS CBBA LTDA';
update configuracion set subtitulo = 'INDUSTRIAS LACTEAS DEL VALLE ALTO';

update configuracion set titulo = 'COOPERATIVA AGROPECUARIA INTEGRAL DE SERVICIOS CBBA';
update configuracion set subtitulo = '';

--

select *
from sf_tmpdet d
where d.`cuenta` = '1110110101'
;

select *
from sf_tmpenc e 
where e.`tipo_doc` = 'RI'
;


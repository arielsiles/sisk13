-- 27.02.2024
update secuencia set valor = (select max(a.idmaquina)+1 from xpr_maquina a) where tabla = 'xpr_maquina';
update secuencia set valor = (select max(a.idproceso)+1 from xpr_proceso a) where tabla = 'xpr_proceso';
update secuencia set valor = (select max(a.idprocesomaquina)+1 from xpr_procesomaquina a) where tabla = 'xpr_procesomaquina';

alter table xpr_proceso add column codigo varchar(20) after idproceso;
alter table xpr_proceso add column estado varchar(20) after nombre;

update xpr_proceso set codigo = idproceso where codigo is null ;
update xpr_proceso set estado = 'PEN' where estado is null ;

INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
VALUES(296, 'XMACHINE_PRODUCTION', 11, 15, 'menu.xproduction.configuration.machineProduction', 1);

INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
VALUES(297, 'XPROCESS_PRODUCTION', 11, 15, 'menu.xproduction.configuration.processRegistration', 1);

ALTER TABLE xpr_maquina MODIFY COLUMN codigo VARCHAR(50) UNIQUE;
alter table xpr_proceso modify column codigo varchar(20) unique;
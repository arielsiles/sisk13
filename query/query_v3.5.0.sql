/** 01.07.2019 **/
alter table planillafiscalporcategoria add column afplab_individual decimal(13,2) after retencionafp;
alter table planillafiscalporcategoria add column afplab_riesgocomun decimal(13,2) after afplab_individual;
alter table planillafiscalporcategoria add column afplab_solidario decimal(13,2) after afplab_riesgocomun;
alter table planillafiscalporcategoria add column afplab_comision decimal(13,2) after afplab_solidario;

update planillafiscalporcategoria set afplab_individual = 0;
update planillafiscalporcategoria set afplab_riesgocomun = 0;
update planillafiscalporcategoria set afplab_solidario = 0;
update planillafiscalporcategoria set afplab_comision = 0;

/** 02.07.2019 **/
alter table planillatributariaporcategoria add column afplab_individual decimal(13,2) after retencionafp;
alter table planillatributariaporcategoria add column afplab_riesgocomun decimal(13,2) after afplab_individual;
alter table planillatributariaporcategoria add column afplab_solidario decimal(13,2) after afplab_riesgocomun;
alter table planillatributariaporcategoria add column afplab_comision decimal(13,2) after afplab_solidario;

update planillatributariaporcategoria set afplab_individual = 0;
update planillatributariaporcategoria set afplab_riesgocomun = 0;
update planillatributariaporcategoria set afplab_solidario = 0;
update planillatributariaporcategoria set afplab_comision = 0;

alter table ciclogeneracionplanilla add column idtasaafplabindividual bigint(20);
alter table ciclogeneracionplanilla add column idtasaafplabriesgocomun bigint(20);
alter table ciclogeneracionplanilla add column idtasaafplabsolidario bigint(20);
alter table ciclogeneracionplanilla add column idtasaafplabcomision  bigint(20);

update ciclogeneracionplanilla  c set c.`idtasaafplabindividual` = 5;
update ciclogeneracionplanilla  c set c.`idtasaafplabriesgocomun` = 6;
update ciclogeneracionplanilla  c set c.`idtasaafplabsolidario` = 7;
update ciclogeneracionplanilla  c set c.`idtasaafplabcomision` = 8;


alter table planillatributariaporcategoria add column bononocturno decimal(13,2) after bonodominical;
alter table planillatributariaporcategoria add column bonopasaje decimal(13,2) after bononocturno;
alter table planillatributariaporcategoria add column bonorefrigerio decimal(13,2) after bonopasaje;

update planillatributariaporcategoria set bononocturno = 0;
update planillatributariaporcategoria set bonopasaje = 0;
update planillatributariaporcategoria set bonorefrigerio = 0;

alter table planillafiscalporcategoria add column bononocturno decimal(13,2) after bonodominical;
alter table planillafiscalporcategoria add column bonopasaje decimal(13,2) after bononocturno;
alter table planillafiscalporcategoria add column bonorefrigerio decimal(13,2) after bonopasaje;


update planillafiscalporcategoria set bononocturno = 0;
update planillafiscalporcategoria set bonopasaje = 0;
update planillafiscalporcategoria set bonorefrigerio = 0;

/** 03.07.2019 **/
alter table planillafiscalporcategoria add column afpsolidario decimal(13,2) after afplab_comision; 
update planillafiscalporcategoria set afpsolidario = 0;

alter table planillafiscalporcategoria add column ext varchar(6) after ci;
update planillafiscalporcategoria set ext = 'CB';

update entidad e set e.idexttipodocumento = 2 where e.idexttipodocumento is null;

/** 04.07.2019 **/
-- Añadir config AFP Laboral
insert  into `tasaafp`(`idtasaafp`,`activo`,`tipotasaafp`,`tasa`,`version`,`idcompania`) values 
(5,1,'LABOR_INDIVIDUAL','10.00',0,1),
(6,1,'LABOR_COMMON_RISK','1.71',0,1),
(7,1,'LABOR_SOLIDARY_CONTRIBUTION','0.50',0,1),
(8,1,'LABOR_LABOR_COMISSION','0.50',0,1);
update secuencia s set s.`valor` = 9 where s.`tabla` = 'TasaAFP';

-- Añadir bonos
insert into `bono` (`idbono`, `activo`, `monto`, `tipobono`, `nombre`, `version`, `idcompania`, `iddescripcion`, `idtasasmn`) values('13','1','0.00','PRODUCTION_BONUS','Bono de produccion Ok','0','1',null,null);
insert into `bono` (`idbono`, `activo`, `monto`, `tipobono`, `nombre`, `version`, `idcompania`, `iddescripcion`, `idtasasmn`) values('14','1','0.00','NIGHTWORK_BONUS','Bono recargo nocturno Ok','0','1',null,null);
insert into `bono` (`idbono`, `activo`, `monto`, `tipobono`, `nombre`, `version`, `idcompania`, `iddescripcion`, `idtasasmn`) values('15','1','0.00','TRANSRETURN_BONUS','Bono devolucion de pasajes Ok','0','1',null,null);
insert into `bono` (`idbono`, `activo`, `monto`, `tipobono`, `nombre`, `version`, `idcompania`, `iddescripcion`, `idtasasmn`) values('16','1','0.00','REFRESHMENT_BONUS','Bono de refrigerio Ok','0','1',null,null);
update secuencia s set s.`valor` = 17 where s.`tabla` = 'Bono';

-- Bug, Corregir gensecuencia en SECUENCIA
update secuencia s set s.`valor` = 19 where s.`tabla` = 'gensecuencia';

update regladescuento r set r.`tipounidaddescuento` = 'PERCENT' where r.`idregladescuento` = 2;

insert into `rangoregladescuento` (`idrangoregladescuento`, `monto`, `rangofinal`, `rangoinicial`, `nombre`, `secuencia`, `version`, `idcompania`, `idregladescuento`) 
values('1','1.00',null,'13000','AFP SOLIDARIO 1%','1','0','1','2');
insert into secuencia (tabla, valor) values('RANGOREGLADESCUENTO', 2);


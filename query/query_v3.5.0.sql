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

-- update ciclogeneracionplanilla  c set c.`idtasaafplabindividual` = 5;
-- update ciclogeneracionplanilla  c set c.`idtasaafplabriesgocomun` = 6;
-- update ciclogeneracionplanilla  c set c.`idtasaafplabsolidario` = 7;
-- update ciclogeneracionplanilla  c set c.`idtasaafplabcomision` = 8;


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





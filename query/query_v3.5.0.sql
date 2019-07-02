/** 01.07.2019 **/
alter table planillafiscalporcategoria add column afplab_individual decimal(13,2) after retencionafp;
alter table planillafiscalporcategoria add column afplab_riesgocomun decimal(13,2) after afplab_individual;
alter table planillafiscalporcategoria add column afplab_solidario decimal(13,2) after afplab_riesgocomun;
alter table planillafiscalporcategoria add column afplab_comision decimal(13,2) after afplab_solidario;

update planillafiscalporcategoria set afplab_individual = 0;
update planillafiscalporcategoria set afplab_riesgocomun = 0;
update planillafiscalporcategoria set afplab_solidario = 0;
update planillafiscalporcategoria set afplab_comision = 0;

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



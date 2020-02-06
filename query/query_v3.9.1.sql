/** 05.02.2020 **/
alter table planillagenerada add primary key (idplanillagenerada);
alter table reportecontrol add foreign key (idplanillagenerada) references planillagenerada (idplanillagenerada);

alter table `planillaadministrativos` add foreign key (idempleado) references empleado (idempleado);
alter table `planillaadministrativos` add foreign key (idcategoriapuesto) references categoriapuesto (idcategoriapuesto);
alter table `planillaadministrativos` add foreign key (idcompania) references compania (idcompania);
alter table `planillaadministrativos` add foreign key (idcargo) references cargo (idcargo);
alter table `planillaadministrativos` add foreign key (idunidadnegocio) references unidadnegocio (idunidadnegocio);

alter table planillaadministrativos add primary key (idplanillaadministrativos);
alter table planillaadministrativos add foreign key (idplanillagenerada) references planillagenerada (idplanillagenerada);

alter table `ciclogeneracionplanilla` add primary key (idciclogeneracionplanilla);
alter table planillagenerada add foreign key (idciclogeneracionplanilla) references ciclogeneracionplanilla(idciclogeneracionplanilla);

alter table `gestionplanilla` add primary key (idgestionplanilla);

delete from gestionplanilla where idgestionplanilla >= 1 and idgestionplanilla <= 14;

alter table `gestionplanilla` add foreign key (idciclogeneracionplanilla) references ciclogeneracionplanilla (idciclogeneracionplanilla);
alter table `gestionplanilla` add foreign key (idcategoriapuesto) references  categoriapuesto (idcategoriapuesto);

alter table `planillatributaria` add primary key (idplanillatributaria);
alter table planillatributaria add foreign key (idciclogeneracionplanilla) references ciclogeneracionplanilla (idciclogeneracionplanilla);
alter table planillatributaria add foreign key (idempleado) references empleado (idempleado);

alter table `planillafiscal` add primary key (idplanillafiscal);
alter table planillafiscal add foreign key (idciclogeneracionplanilla) references ciclogeneracionplanilla (idciclogeneracionplanilla);
alter table planillafiscal add foreign key (idempleado) references empleado (idempleado);
alter table contratopuesto add primary key (idcontratopuesto);
alter table planillafiscal add foreign key (idcontratopuesto) references contratopuesto (idcontratopuesto);
alter table planillafiscal add foreign key (idplanillatributaria) references planillatributaria (idplanillatributaria);


alter table `planillafiscalporcategoria` add primary key (idplanillafiscalporcategoria);
alter table planillafiscalporcategoria add foreign key (idplanillagenerada) references planillagenerada (idplanillagenerada);
alter table planillafiscalporcategoria add foreign key (idcontratopuesto) references contratopuesto (idcontratopuesto);
alter table planillafiscalporcategoria add foreign key (idempleado) references empleado (idempleado);
alter table planillatributariaporcategoria add primary key (idplanillatributariaporcat);
alter table planillafiscalporcategoria add foreign key (idplanillatributariaporcat) references planillatributariaporcategoria (idplanillatributariaporcat);


-- eliminando planillas generadas
delete from reportecontrol 		 	where idplanillagenerada in (320);
delete from `planillafiscalporcategoria` 	where idplanillagenerada in (320);
delete from `planillatributariaporcategoria` 	where idplanillagenerada in (320);
delete from `planillaadministrativos` 		where idplanillagenerada in (320);
delete from `planillagenerada` 			where idplanillagenerada in (320);

-- 

select *
from planillagenerada




select g.`idgestionplanilla`, g.`idciclogeneracionplanilla`, c.`idciclogeneracionplanilla`
from gestionplanilla g
left join ciclogeneracionplanilla c on g.`idciclogeneracionplanilla` = c.`idciclogeneracionplanilla`
;

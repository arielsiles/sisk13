-- 06.02.2025
alter table `configuracion` add column oc_pagodefault varchar(20);
update configuracion set `oc_pagodefault` = '1120050100'; -- Terdemol

alter table inv_vales add column created_at datetime;
alter table inv_vales add column created_by varchar(100);
alter table inv_vales add column updated_at datetime;
alter table inv_vales add column updated_by varchar(100);

CREATE TABLE xpr_linea (
    idlinea bigint(20) not null,
    nombre varchar(255),
    version bigint,
    idcompania bigint(20),
    primary key (idlinea),
    FOREIGN KEY (idcompania) REFERENCES compania(idcompania)
);

alter table `xpr_proceso` add column idlinea bigint(20) after estado;
alter table `xpr_proceso` add foreign key (idlinea) references `xpr_linea`(idlinea);
alter table `xpr_proceso` add column posicion int(10) after codigo;

-- 
INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
VALUES(308, 'PRODUCTION_LINE', 11, 15, 'Functionality.xproduction.productionLines', 1);

alter table xpr_linea add column codigo varchar(100) after idlinea;

-- 15.02.2024
alter table xpr_maquina add column codigo varchar(50) after idmaquina;

alter table xpr_maquina add foreign key (idcompania) references compania (idcompania);
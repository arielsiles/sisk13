-- 19.02.2024
alter table pagoordencompra add foreign key (idordencompra) references com_encoc (id_com_encoc);
update funcionalidad set nombrerecurso = 'Functionality.rotatoryFund.documentType' where codigo = 'ROTATORYFUNDDOCUMENTTYPE';

-- 21.02.2024
alter table com_encoc add column cta_def int(1) after idcondicionpago;
alter table com_encoc add column cod_prov_aux varchar(6) after cta_def;
alter table com_encoc add column cuentapago varchar(20) after cod_prov_aux;

update com_encoc set cta_def = 1 where cta_def is null;

alter table condicionpago add column tipo varchar(50) after nombre;

update condicionpago set tipo = 'CASH' where nombre = 'CONTADO';
update condicionpago set tipo = 'CREDIT' where nombre = 'CREDITO';


--
select * from funcionalidad f
where f.codigo like '%ROTATORYFUND%';
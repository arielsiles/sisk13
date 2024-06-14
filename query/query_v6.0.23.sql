-- 13.06.2024
alter table inv_grupos add column cta_baja varchar(31) after cta_gasto;
update inv_grupos set cta_baja = '5540010100';

alter table inv_vales add column baja int after cta_gasto;
update inv_vales set baja = 0;
-- 21.06.2025

-- Para terdemol
UPDATE inv_grupos set cta_baja = '5310011001' where cod_gru = 1;  -- Materias Primas
UPDATE inv_grupos set cta_baja = '5310030100' where cod_gru = 14; -- PRODUCTOS MINERALES
UPDATE inv_grupos set cta_baja = '5540010100' where cod_gru = 15; -- MATERIALES DE CORTE Y SOLDADURA

-- 09.11.2025
update xpr_insumoformula x set x.defecto = 1 where x.idinsumoformula = 3;
update xpr_insumoformula x set x.defecto = 0 where x.idinsumoformula = 4;
update xpr_insumoformula x set x.defecto = 1 where x.idinsumoformula = 7;


update xpr_produccion x set x.idlinea = 1 where x.idformula = 1;
update xpr_produccion x set x.idlinea = 2 where x.idformula = 4;

select i.no_trans, i.fecha, i.cod_doc, i.estado, i.oper, i.idtmpenc, m.descri
from inv_vales i
left join inv_mov m on i.no_trans = m.no_trans
where i.fecha between '2021-01-01' and '2021-12-31'
and i.cod_doc = 'BAJ'
and i.estado <> 'ANL'
-- and i.no_vale in ()
-- and m.descri like '%VENTA%'
;

UPDATE inv_mov i        SET i.`estado` = 'APR' WHERE i.`no_trans` IN (26163,26203,26318,26385,26436,26488,26511,26591,26788,26752,26789,26792,26815,26817,26830,26890,26914,26925,26939,26969,26994,27059,27097,27130,27153,27192,27217,27328,27331,27436,27454,27491,27570,27686,27874,28351,28548,28550,28615,28617,28952,28996,29004,29016,29221,29336,29337,29339,29341,29435,29436,29439,29624,29659,29660,29726,29741,29755,29765,29801,29843,29865,29930,29990,30036,30131,30133,30189,30241,30251,30309,30356,30392,30418,30602,30622,30670,30671,30733,30789,30812,30816,30948,30993,31029,31310,31311,31339,31340,31365,31369,31381,31382);
UPDATE inv_movdet i     SET i.`estado` = 'APR' WHERE i.`no_trans` IN (26163,26203,26318,26385,26436,26488,26511,26591,26788,26752,26789,26792,26815,26817,26830,26890,26914,26925,26939,26969,26994,27059,27097,27130,27153,27192,27217,27328,27331,27436,27454,27491,27570,27686,27874,28351,28548,28550,28615,28617,28952,28996,29004,29016,29221,29336,29337,29339,29341,29435,29436,29439,29624,29659,29660,29726,29741,29755,29765,29801,29843,29865,29930,29990,30036,30131,30133,30189,30241,30251,30309,30356,30392,30418,30602,30622,30670,30671,30733,30789,30812,30816,30948,30993,31029,31310,31311,31339,31340,31365,31369,31381,31382);
UPDATE `inv_vales` i    SET i.`estado` = 'APR' WHERE i.`no_trans` IN (26163,26203,26318,26385,26436,26488,26511,26591,26788,26752,26789,26792,26815,26817,26830,26890,26914,26925,26939,26969,26994,27059,27097,27130,27153,27192,27217,27328,27331,27436,27454,27491,27570,27686,27874,28351,28548,28550,28615,28617,28952,28996,29004,29016,29221,29336,29337,29339,29341,29435,29436,29439,29624,29659,29660,29726,29741,29755,29765,29801,29843,29865,29930,29990,30036,30131,30133,30189,30241,30251,30309,30356,30392,30418,30602,30622,30670,30671,30733,30789,30812,30816,30948,30993,31029,31310,31311,31339,31340,31365,31369,31381,31382);

SELECT * from sf_tmpenc
-- update  sf_tmpenc set estado = 'APR'
where id_tmpenc in (

);

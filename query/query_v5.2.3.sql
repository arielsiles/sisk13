ALTER TABLE pedidos ADD COLUMN flagct INT(11) DEFAULT 0 AFTER id_tmpenc;

-- 
SELECT *
FROM ventadirecta v
-- where v.`FECHA_PEDIDO` between '2021-04-01' and '2021-04-30'
-- update ventadirecta v set v.`flagct` = 0
WHERE v.`FECHA_PEDIDO` >= '2021-04-01'
AND v.`IDUSUARIO` = 408
;


SELECT *
FROM pedidos p
-- update pedidos p set p.`flagct` = 0
WHERE p.`FECHA_ENTREGA` BETWEEN '2021-04-01' AND '2021-04-30'
AND p.`tipoventa` = 'CASH'
AND p.`IDUSUARIO` = 408
;

-- 
SELECT SUM(p.`TOTALIMPORTE`)
FROM pedidos p
WHERE p.`FECHA_ENTREGA` BETWEEN '2021-04-01' AND '2021-04-30'
AND p.`tipoventa` = 'CASH'
AND p.`ESTADO` <> 'ANULADO'
AND p.`IDUSUARIO` = 408
;

SELECT SUM(v.`TOTALIMPORTE`) AS importe
FROM ventadirecta v
WHERE v.`FECHA_PEDIDO` BETWEEN '2021-04-01' AND '2021-04-30'
AND v.`ESTADO` <> 'ANULADO'
AND v.`IDUSUARIO` = 408
;


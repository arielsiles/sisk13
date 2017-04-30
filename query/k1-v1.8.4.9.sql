-- v1.8.4.9

-- 1. Actualizar tabla configuracion
ALTER TABLE configuracion ADD ctaCostPT VARCHAR(20);
UPDATE configuracion SET ctaCostPT = '4420110201';

ALTER TABLE configuracion ADD ctaAlmPT VARCHAR(20);
UPDATE configuracion SET ctaAlmPT = '1510110201';

ALTER TABLE configuracion ADD ctaCostPV VARCHAR(20);
UPDATE configuracion SET ctaCostPV = '4430110900';

ALTER TABLE configuracion ADD ctaAlmPV VARCHAR(20);
UPDATE configuracion SET ctaAlmPV = '1520110100';

ALTER TABLE configuracion ADD ctaMerma VARCHAR(20);
UPDATE configuracion SET ctaMerma = '4470510300';

ALTER TABLE configuracion ADD ctaProm VARCHAR(20);
UPDATE configuracion SET ctaProm = '4470310400';

-- 2.  
ALTER TABLE ventadirecta ADD CV INT(11) NOT NULL DEFAULT 0;
 
-- 3.
ALTER TABLE pedidos ADD CV INT(11) NOT NULL DEFAULT 0;

-- 4.
-- Para Pedidos con factura
INSERT INTO sf_confdet (id_sf_confdet, cuenta, tipomovimiento, id_sf_confenc) VALUES (43, '4470510300', 'DEBE', 4);
INSERT INTO sf_confdet (id_sf_confdet, cuenta, tipomovimiento, id_sf_confenc) VALUES (44, '4470310400', 'DEBE', 4);
INSERT INTO sf_confdet (id_sf_confdet, cuenta, tipomovimiento, id_sf_confenc) VALUES (45, '1510110201', 'HABER', 4);

-- Para Pedidos con factura comision
INSERT INTO sf_confdet (id_sf_confdet, cuenta, tipomovimiento, id_sf_confenc) VALUES (46, '4470510300', 'DEBE', 8);
INSERT INTO sf_confdet (id_sf_confdet, cuenta, tipomovimiento, id_sf_confenc) VALUES (47, '4470310400', 'DEBE', 8);
INSERT INTO sf_confdet (id_sf_confdet, cuenta, tipomovimiento, id_sf_confenc) VALUES (48, '1510110201', 'HABER', 8);

UPDATE id_gen SET GEN_VAL = 10 WHERE GEN_NAME = 'SfConfenc_Gen';
UPDATE id_gen SET GEN_VAL = 49 WHERE GEN_NAME = 'SfConfdet_Gen';

INSERT INTO operaciones (IDOPERACIONES, descripcion, NOMBRE) VALUES (9, 'Degustaciones, refrigerios o reposiciones', 'DEG_REF_REP');
INSERT INTO sf_confenc (id_sf_confenc, cuenta, descripcion, tipo_doc, glosa, operacion, idusuario) VALUES (10, NULL, 'DEGUSTACION/REFRIGERIO/REPOSICION', 'NE', 'Degustaciones, refrigerios o reposiciones', 'DEG_REF_REP', 0);

INSERT INTO sf_confdet (id_sf_confdet, cuenta, tipomovimiento, id_sf_confenc) VALUES (49, '4470610218', 'DEBE', 10); -- Degustacion
INSERT INTO sf_confdet (id_sf_confdet, cuenta, tipomovimiento, id_sf_confenc) VALUES (50, '4460310104', 'DEBE', 10); -- Refrigerios
INSERT INTO sf_confdet (id_sf_confdet, cuenta, tipomovimiento, id_sf_confenc) VALUES (51, '4470510300', 'DEBE', 10); -- Reposiciones (Mermas)
INSERT INTO sf_confdet (id_sf_confdet, cuenta, tipomovimiento, id_sf_confenc) VALUES (52, '1510110201', 'HABER', 10); -- Cta Almacen

-- Para Pedidos sin factura
INSERT INTO sf_confdet (id_sf_confdet, cuenta, tipomovimiento, id_sf_confenc) VALUES (53, '4470510300', 'DEBE', 3);
INSERT INTO sf_confdet (id_sf_confdet, cuenta, tipomovimiento, id_sf_confenc) VALUES (54, '4470310400', 'DEBE', 3);
INSERT INTO sf_confdet (id_sf_confdet, cuenta, tipomovimiento, id_sf_confenc) VALUES (55, '1510110201', 'HABER', 3);



-- aux
-- 
-- Actualizar cu en articulos_pedidos PEDIDOS
SELECT 	*
FROM articulos_pedido a
-- update articulos_pedido a 
LEFT JOIN pedidos p ON a.idpedidos = p.`IDPEDIDOS`
LEFT JOIN inv_articulos ar ON a.`cod_art` = ar.`cod_art`
SET a.`cu` = ar.cu
WHERE p.`FECHA_ENTREGA` BETWEEN '2016-11-01' AND '2016-11-30'
;

-- Actualizar cu en articulos_pedidos CONTADO
SELECT 	*
FROM articulos_pedido a
-- update articulos_pedido a
LEFT JOIN ventadirecta v ON a.`IDVENTADIRECTA` = v.`IDVENTADIRECTA`
LEFT JOIN inv_articulos ar ON a.`cod_art` = ar.`cod_art`
SET a.`cu` = ar.cu
WHERE v.`FECHA_PEDIDO` BETWEEN '2016-11-01' AND '2016-11-30'
AND v.`IDUSUARIO` = 6
;



SELECT *
FROM articulos_pedido a WHERE a.`IDVENTADIRECTA` IN (9441,8991,9315);

SELECT *
FROM movimiento m WHERE m.`IDMOVIMIENTO` IN (13167,12444,12979);

-- 
SELECT *
FROM personacliente p WHERE p.`IDPERSONACLIENTE` = 615;
-- 

SELECT *
FROM ventadirecta v WHERE v.`IDVENTADIRECTA` IN (9441,8991,9315);

SELECT *
FROM sf_tmpenc e WHERE e.`id_tmpenc` IN (40511,39125,39823);

SELECT *
FROM sf_tmpdet d WHERE d.`id_tmpenc` IN (40511,39125,39823);


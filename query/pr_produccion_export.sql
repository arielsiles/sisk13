-- Para exportar produccion
-- 1. Plan
SELECT *
FROM pr_plan
WHERE `fecha` BETWEEN '2021-08-01' AND '2021-08-31'
;

-- 2. Produccion
SELECT *
FROM pr_produccion
WHERE `idplan` IN (
	SELECT p.`idplan`
	FROM pr_plan p
	WHERE p.`fecha` BETWEEN '2021-08-01' AND '2021-08-31'
);

-- 3. Productos
SELECT *
FROM pr_producto
WHERE `idplan` IN (
	SELECT p.`idplan`
	FROM pr_plan p
	WHERE p.`fecha` BETWEEN '2021-08-01' AND '2021-08-31'
);

-- 4. Insumos
SELECT *
FROM pr_insumo
WHERE `idproduccion` IN (
	SELECT pr.`idproduccion`
	FROM pr_produccion pr
	WHERE pr.`idplan` IN (
		SELECT p.`idplan`
		FROM pr_plan p
		WHERE p.`fecha` BETWEEN '2021-08-01' AND '2021-08-31'
	)
);

-- 5. periodocostoindirecto
SELECT * FROM periodocostoindirecto
WHERE idgestion = 9 AND mes = 8;

-- 6.
SELECT *
FROM costosindirectos
WHERE idperiodocostoindirecto = 65
;

-- Eliminar produccion
-- 1. Insumos
-- delete FROM pr_insumo
WHERE `idproduccion` IN (
	SELECT pr.`idproduccion`
	FROM pr_produccion pr
	WHERE pr.`idplan` IN (
		SELECT p.`idplan`
		FROM pr_plan p
		WHERE p.`fecha` BETWEEN '2021-07-01' AND '2021-07-31'
	)
);

-- 2. Plan
-- delete FROM pr_producto
WHERE `idplan` IN (
	SELECT p.`idplan`
	FROM pr_plan p
	WHERE p.`fecha` BETWEEN '2021-07-01' AND '2021-07-31'
);

-- 3. Produccion
-- DELETE FROM pr_produccion
WHERE `idplan` IN (
	SELECT p.`idplan`
	FROM pr_plan p
	WHERE p.`fecha` BETWEEN '2021-07-01' AND '2021-07-31'
);

-- 4. Plan
-- DELETE FROM pr_plan
WHERE `fecha` BETWEEN '2021-07-01' AND '2021-07-31'
;

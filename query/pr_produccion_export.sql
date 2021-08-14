-- Plan
SELECT *
FROM pr_plan
WHERE `fecha` BETWEEN '2021-07-01' AND '2021-07-31'
;

-- Produccion
SELECT *
FROM pr_produccion
WHERE `idplan` IN (
	SELECT p.`idplan`
	FROM pr_plan p
	WHERE p.`fecha` BETWEEN '2021-07-01' AND '2021-07-31'
);

-- Productos
SELECT *
FROM pr_producto
WHERE `idplan` IN (
	SELECT p.`idplan`
	FROM pr_plan p
	WHERE p.`fecha` BETWEEN '2021-07-01' AND '2021-07-31'
);

-- Insumos
SELECT *
FROM pr_insumo
WHERE `idproduccion` IN (
	SELECT pr.`idproduccion`
	FROM pr_produccion pr
	WHERE pr.`idplan` IN (
		SELECT p.`idplan`
		FROM pr_plan p
		WHERE p.`fecha` BETWEEN '2021-07-01' AND '2021-07-31'
	)
);



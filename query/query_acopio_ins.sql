SELECT *
FROM `planillaacopio`
WHERE `fecha` IN ('2021-07-04', '2021-07-11', '2021-07-18', '2021-07-25')
;
--

SELECT *
FROM `sesionacopio`
WHERE `fecha` IN ('2021-07-04', '2021-07-11', '2021-07-18', '2021-07-25')
;

-- Para exportar e insertar Pesaje de Acopio
SELECT * FROM `registroacopio`
WHERE `idplanillaacopio` IN (
	SELECT p.`idplanillaacopio`
	FROM `planillaacopio` p
	WHERE p.`fecha` IN ('2021-07-04', '2021-07-11', '2021-07-18', '2021-07-25')
)
;

-- Para exportar e insertar Sesiones de Acopio
SELECT * FROM acopiomateriaprima 
WHERE idsesionacopio IN (
	SELECT idsesionacopio
	FROM `sesionacopio`
	WHERE `fecha` IN ('2021-07-04', '2021-07-11', '2021-07-18', '2021-07-25')
);
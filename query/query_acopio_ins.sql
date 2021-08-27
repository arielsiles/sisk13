-- PARA EXPORTAR, ELIMINAR ACOPIO
SELECT * FROM `planillaacopio`
-- delete FROM `planillaacopio`
WHERE `fecha` IN ('2021-07-18', '2021-07-25')
;
--

SELECT *
FROM `sesionacopio`
-- delete FROM `sesionacopio`
WHERE `fecha` IN ('2021-07-18', '2021-07-25')
;

-- Para exportar e insertar Pesaje de Acopio
SELECT * FROM `registroacopio`
-- delete FROM `registroacopio`
WHERE `idplanillaacopio` IN (
	SELECT p.`idplanillaacopio`
	FROM `planillaacopio` p
	WHERE p.`fecha` IN ('2021-07-18', '2021-07-25')
)
;

-- Para exportar e insertar Sesiones de Acopio
SELECT * FROM acopiomateriaprima 
-- delete FROM acopiomateriaprima 
WHERE idsesionacopio IN (
	SELECT idsesionacopio
	FROM `sesionacopio`
	WHERE `fecha` IN ('2021-07-18', '2021-07-25')
);
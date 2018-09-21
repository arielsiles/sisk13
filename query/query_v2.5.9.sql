/** 19/09/2018 Eliminar registros de acopio de Zonas Productivas CISC **/
-- si existiera
-- Planilla de acopio
SELECT *
FROM registroacopio r
WHERE r.`idzonaproductiva` IN (
	SELECT z.`idzonaproductiva`
	FROM zonaproductiva z
	WHERE grupo = 'CISC'
)	
;

DELETE FROM registroacopio 
WHERE idregistroacopio IN (

);

-- Sesion de acopio
-- si existiera
SELECT *
FROM sesionacopio s
WHERE s.`idzonaproductiva` IN (
	SELECT z.`idzonaproductiva`
	FROM zonaproductiva z
	WHERE grupo = 'CISC'
);

-- -----------------------------------------------------
/** 12.02.2019 ACTUALIZAR PARA LECHERIA **/

-- DEBE
UPDATE sf_tmpdet d SET d.`debeme` = (d.`debe` / d.tc), d.`haberme` = 0
WHERE d.`cuenta` IN (1110220000);

-- HABER
UPDATE sf_tmpdet d SET d.`haberme` = (d.`haber` / d.tc), d.`debeme` = 0
WHERE d.`cuenta` IN (2130420000,2130520000,2130720000);

-- DEBE
UPDATE sf_tmpdet d SET d.`debeme` = (d.`debe` / d.tc), d.`haberme` = 0
WHERE d.`cuenta` IN (1330520600);

-- HABER
UPDATE sf_tmpdet d SET d.`haberme` = (d.`haber` / d.tc), d.`debeme` = 0
WHERE d.`cuenta` IN (1390120000);

-- HABER
UPDATE sf_tmpdet d SET d.`haberme` = (d.`haber` / d.tc), d.`debeme` = 0
WHERE d.`cuenta` IN (1390220000);

-- HABER
UPDATE sf_tmpdet d SET d.`haberme` = (d.`haber` / d.tc), d.`debeme` = 0
WHERE d.`cuenta` IN (1390320000);

-- HABER
UPDATE sf_tmpdet d SET d.`haberme` = (d.`haber` / d.tc), d.`debeme` = 0
WHERE d.`cuenta` IN (1390320000);

-- HABER
UPDATE sf_tmpdet d SET d.`haberme` = (d.`haber` / d.tc), d.`debeme` = 0
WHERE d.`cuenta` IN (1390420000);

-- DEBE
UPDATE sf_tmpdet d SET d.`debeme` = (d.`debe` / d.tc), d.`haberme` = 0
WHERE d.`cuenta` IN (1420320700);

-- DEBE
UPDATE sf_tmpdet d SET d.`debeme` = (d.`debe` / d.tc), d.`haberme` = 0
WHERE d.`cuenta` IN (1671020100);

-- HABER
UPDATE sf_tmpdet d SET d.`haberme` = (d.`haber` / d.tc), d.`debeme` = 0
WHERE d.`cuenta` IN (2180320000);

/** 14.02.2019 PARA ACTUALIZAR LECHERIA **/


UPDATE sf_tmpdet d SET d.`moneda` = 'P', d.`debeme` = 0, d.`haberme` = 0, d.`tc` = 1
WHERE d.`moneda` IS NULL;

UPDATE sf_tmpdet d SET d.`moneda` = 'P', d.`debeme` = 0, d.`haberme` = 0, d.`tc` = 1
WHERE d.`moneda` = 'P';

SELECT *
FROM sf_tmpdet d
WHERE d.`moneda` = 'P'
;

--
--
SELECT d.`id_tmpdet`, d.`cuenta`, d.`debe`, d.`haber`, d.`debeme`, d.`haber` / d.tc , d.`haber`, d.`moneda`, d.`tc`, d.`id_tmpenc`
FROM sf_tmpdet d
WHERE d.`cuenta` IN ();

SELECT d.`id_tmpdet`, d.`cuenta`, a.`descri`, d.`debe`, d.`haber`, d.`debeme`, d.`haberme`, d.`moneda`, d.`tc`, d.`id_tmpenc`
FROM sf_tmpdet d
JOIN arcgms a ON d.`cuenta` = a.`cuenta`
-- WHERE d.`cuenta` IN (2180320000);
WHERE d.`moneda` = 'D'
;
--
-- DEBE
UPDATE sf_tmpdet d SET d.`debeme` = (d.`debe` / d.tc), d.`haberme` = 0
WHERE d.`cuenta` IN ();

-- HABER
UPDATE sf_tmpdet d SET d.`haberme` = (d.`haber` / d.tc), d.`debeme` = 0
WHERE d.`cuenta` IN ();


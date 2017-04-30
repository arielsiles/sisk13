--
ALTER TABLE arcgms ADD cta_raiz VARCHAR(20) NULL AFTER descri;


--
-- 1. Update Activo cta_raiz
UPDATE arcgms a SET a.`cta_raiz` = '1100000000' WHERE a.`cuenta` >= '1100000000' AND a.`cuenta` < '1200000000';
UPDATE arcgms a SET a.`cta_raiz` = '1200000000' WHERE a.`cuenta` >= '1200000000' AND a.`cuenta` < '1300000000';
UPDATE arcgms a SET a.`cta_raiz` = '1400000000' WHERE a.`cuenta` >= '1400000000' AND a.`cuenta` < '1500000000';
UPDATE arcgms a SET a.`cta_raiz` = '1500000000' WHERE a.`cuenta` >= '1500000000' AND a.`cuenta` < '1600000000';
UPDATE arcgms a SET a.`cta_raiz` = '1600000000' WHERE a.`cuenta` >= '1600000000' AND a.`cuenta` < '1700000000';
UPDATE arcgms a SET a.`cta_raiz` = '1700000000' WHERE a.`cuenta` >= '1700000000' AND a.`cuenta` < '1800000000';
UPDATE arcgms a SET a.`cta_raiz` = '1800000000' WHERE a.`cuenta` >= '1800000000' AND a.`cuenta` < '1900000000';
UPDATE arcgms a SET a.`cta_raiz` = '1000000000' WHERE a.`cuenta` = '1000000000';

-- 2. Update Pasivo cta_raiz
UPDATE arcgms a SET a.`cta_raiz` = '2100000000' WHERE a.`cuenta` >= '2100000000' AND a.`cuenta` < '2200000000';
UPDATE arcgms a SET a.`cta_raiz` = '2200000000' WHERE a.`cuenta` >= '2200000000' AND a.`cuenta` < '2300000000';
UPDATE arcgms a SET a.`cta_raiz` = '2400000000' WHERE a.`cuenta` >= '2400000000' AND a.`cuenta` < '2500000000';
UPDATE arcgms a SET a.`cta_raiz` = '2500000000' WHERE a.`cuenta` >= '2500000000' AND a.`cuenta` < '2600000000';
UPDATE arcgms a SET a.`cta_raiz` = '2800000000' WHERE a.`cuenta` >= '2800000000' AND a.`cuenta` < '2900000000';
UPDATE arcgms a SET a.`cta_raiz` = '2000000000' WHERE a.`cuenta` = '2000000000';

-- 3. Update Patrimonio cta_raiz
UPDATE arcgms a SET a.`cta_raiz` = '3100000000' WHERE a.`cuenta` >= '3100000000' AND a.`cuenta` < '3200000000';
UPDATE arcgms a SET a.`cta_raiz` = '3200000000' WHERE a.`cuenta` >= '3200000000' AND a.`cuenta` < '3300000000';
UPDATE arcgms a SET a.`cta_raiz` = '3300000000' WHERE a.`cuenta` >= '3300000000' AND a.`cuenta` < '3400000000';
UPDATE arcgms a SET a.`cta_raiz` = '3400000000' WHERE a.`cuenta` >= '3400000000' AND a.`cuenta` < '3500000000';
UPDATE arcgms a SET a.`cta_raiz` = '3500000000' WHERE a.`cuenta` >= '3500000000' AND a.`cuenta` < '3600000000';
UPDATE arcgms a SET a.`cta_raiz` = '3000000000' WHERE a.`cuenta` = '3000000000';



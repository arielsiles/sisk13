/** 13.06.2019 **/
ALTER TABLE periodocostoindirecto ADD COLUMN contab INT(1) AFTER procesado;
UPDATE periodocostoindirecto p SET p.`contab` = 1;

UPDATE periodocostoindirecto p SET p.`contab` = 0 
WHERE p.`mes` = 5 AND p.`idgestion` = 6;

UPDATE periodocostoindirecto p SET p.`procesado` = 1;
UPDATE periodocostoindirecto p SET p.`procesado` = 0 
WHERE p.`mes` = 5 AND p.`idgestion` = 6;

--

UPDATE pr_producto p SET p.`costo` = 0 WHERE p.`costo` IS NULL;

UPDATE pr_producto p SET p.`costo_a` = 0 WHERE p.`costo_a` IS NULL;
UPDATE pr_producto p SET p.`costo_b` = 0 WHERE p.`costo_b` IS NULL;
UPDATE pr_producto p SET p.`costo_c` = 0 WHERE p.`costo_c` IS NULL;
UPDATE pr_producto p SET p.`costouni` = 0 WHERE p.`costouni` IS NULL;
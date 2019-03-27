SELECT *
FROM inv_movdet d
LEFT JOIN inv_mov m   ON d.`no_trans` = m.`no_trans`
LEFT JOIN inv_vales v ON m.`no_trans` = v.`no_trans`
WHERE v.`fecha` BETWEEN '2019-01-01' AND '2019-03-31'
AND v.`cod_alm` = 2
AND v.`id_com_encoc` IS NULL
;

-- 1.
ALTER TABLE inv_vales ADD COLUMN orig VARCHAR(100) AFTER estado;
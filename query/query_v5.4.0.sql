-- 09.11.2021
ALTER TABLE inv_articulos ADD COLUMN codsin VARCHAR(10) AFTER cod_art;
ALTER TABLE inv_articulos ADD COLUMN cod_meds VARCHAR(10) AFTER cod_med;
ALTER TABLE dosificacion ADD COLUMN caeb VARCHAR(20) AFTER IDDOSIFICACION;

-- 11.11.2021
ALTER TABLE tipodocumento ADD COLUMN codsin VARCHAR(10) AFTER nombre;
ALTER TABLE personacliente ADD COLUMN idtipodocfac BIGINT(20) AFTER telefono;
ALTER TABLE sin_actividad ADD COLUMN activo TINYINT(1);
UPDATE sin_actividad SET activo = 1;
UPDATE sin_actividad SET activo = 0 where codigocaeb in ('640001','105000','477730', '640000');

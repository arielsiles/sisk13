ALTER TABLE configuracion ADD iue_ret VARCHAR(20) NULL;

UPDATE configuracion SET iue_ret = '2420310400' WHERE no_cia = 01;

ALTER TABLE configuracion ADD it_ret VARCHAR(20) NULL;

UPDATE configuracion SET it_ret = '2420310500' WHERE no_cia = 01;
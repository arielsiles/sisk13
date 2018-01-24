/** 22.01.2018 **/
DELETE FROM funcionalidad WHERE codigo = 'ESTIMATIONSTOCKREPORT';
DELETE FROM derechoacceso WHERE idfuncionalidad = 155;

ALTER TABLE inv_inicio ADD no_cia VARCHAR(2) NULL;
UPDATE inv_inicio SET `no_cia` = '01';

DELETE FROM inv_inicio WHERE `idinvinicio` >= 601
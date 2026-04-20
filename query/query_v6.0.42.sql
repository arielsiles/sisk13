-- v6.0.42 Correccion de valores TIME invalidos en fechaespecial
-- horafin '24:00:00' no es un valor TIME valido para JDBC, maximo permitido es '23:59:59'
UPDATE fechaespecial SET horafin = '23:59:59' WHERE horafin = '24:00:00';
UPDATE fechaespecial SET horainicio = '00:00:00' WHERE horainicio = '24:00:00';

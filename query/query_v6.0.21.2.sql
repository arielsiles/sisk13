-- 22.03.2025
-- No se usa
CREATE TABLE sin_tipopuntoventa (
    idtipopuntoventa bigint PRIMARY KEY,
    codigo_clasificador int,
    descripcion VARCHAR(255) NOT NULL
);

INSERT INTO sin_tipopuntoventa (idtipopuntoventa, codigo_clasificador, descripcion) VALUES
(1, 1, 'PUNTO VENTA COMISIONISTA'),
(2, 2, 'PUNTO VENTA VENTANILLA DE COBRANZA'),
(3, 3, 'PUNTO DE VENTA MOVILES'),
(4, 6, 'PUNTO DE VENTA CONJUNTA'),
(5, 4, 'PUNTO DE VENTA YPFB'),
(6, 5, 'PUNTO DE VENTA CAJEROS');

--
ALTER TABLE configuracion ADD COLUMN url_point_of_sale_types VARCHAR(300) AFTER url_reversion_cancel_bill;
UPDATE configuracion SET url_point_of_sale_types='http://10.0.0.106:8080/api/sync/point-of-sale-types' where no_cia = '01';

ALTER TABLE configuracion ADD COLUMN url_register_pos VARCHAR(300) AFTER url_point_of_sale_types;
UPDATE configuracion SET url_register_pos ='http://10.0.0.106:8080/api/billing-operations/register-pos' where no_cia = '01';

ALTER TABLE configuracion ADD COLUMN url_close_pos VARCHAR(300) AFTER url_register_pos;
UPDATE configuracion SET url_close_pos ='http://10.0.0.106:8080/api/billing-operations/close-pos' where no_cia = '01';

-- añadir columna tipo_pos int en tabla sucursal despues de docsector
ALTER TABLE sucursal ADD COLUMN tipo_pos int AFTER docsector;
ALTER TABLE sucursal ADD COLUMN desc_tipopos varchar(100) AFTER tipo_pos;
ALTER TABLE sucursal ADD COLUMN pos_activo int after tipo_pos; -- Actualizar manualmente


insert into sin_motivoanulacion (idmotivoanulacion, codigo, descripcion) values (4, 4, 'FACTURA O NOTA DE CREDITO-DEBITO DEVUELTA');

alter table pedidos add column idmotivoanulacion bigint after idmovimiento;
--
alter table pedidos add column envio int after idmotivoanulacion;
update pedidos set envio = 0 where envio is null;
--
alter table dosificacion add foreign key (idsucursal) references sucursal (idsucursal);

INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
VALUES(408, 'SYNC_BILLING_SFE', 2, 1, 'Functionality.admin.syncCatalogsBillingSFE', 1);

-- ------------------------------------------
-- AUX
update personacliente p set p.email = 'ariel.siles@gmail.com' where p.idpersonacliente >= 1;

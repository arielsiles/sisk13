-- ============================================================================
-- query_v6.0.108_terdemol.sql
-- ============================================================================
-- Permisos propios para el menu "Atencion al cliente" (customers).
--
-- Problema corregido:
--   Un unico permiso 'SALES'/VIEW (funcionalidad id 264) gobernaba de golpe
--   varias opciones del menu: "Ventas" (caja), "Lista de ventas", "Clientes"
--   y los 5 reportes de ventas. Al conceder SALES a un rol se habilitaban
--   todos juntos, sin poder controlarlos por separado. Ademas el dropdown
--   "Reportes" no tenia gate propio y aparecia aunque el rol no viera ninguno.
--
-- Solucion:
--   Se registran 8 funcionalidades propias (una por opcion). El menu ahora
--   gatea cada item con su permiso y el dropdown "Reportes" solo aparece si
--   el rol tiene al menos uno de los reportes (o CREDIT, para los de credito
--   que ya existian). 'SALES' (264) se conserva por si otro punto lo usa.
--
--   Bitmask permiso: VIEW=1, CREATE=2, UPDATE=4, DELETE=8. CRUD completo = 15.
--   idmodulo = 1 (customers, igual que 'SALES' y las Ordenes de Venta).
--   Estas funcionalidades son de solo visualizacion de menu: VIEW(1).
--   Columnas (orden fisico): (idfuncionalidad, codigo, descripcion,
--                             idmodulo, permiso, nombrerecurso, idcompania).
--
-- NOTA (migracion): por decision, NO se auto-asignan estos permisos a los
--       roles que hoy tienen SALES. Tras aplicar este script, hay que
--       habilitarlos manualmente por rol desde el panel de permisos; hasta
--       entonces estas opciones quedaran ocultas para todos los roles.
-- ----------------------------------------------------------------------------

-- Opciones de nivel superior (antes SALES)
insert into funcionalidad values (494, 'SALESBOX',   'Ventas (Caja)',    1, 1, 'Functionality.customers.salesBox',   1);
insert into funcionalidad values (495, 'SALESLIST',  'Lista de ventas',  1, 1, 'Functionality.customers.salesList',  1);
-- (496 'CLIENTLIST' se descarto: el menu "Clientes" usa el permiso EXISTENTE
--  'CLIENT' (id 262), que es el que protege la pantalla clientList y el que ya
--  tienen los roles. No se crea un permiso nuevo. Limpieza por si se inserto antes:)
-- delete from derechoacceso where idfuncionalidad = 496;
-- delete from funcionalidad  where idfuncionalidad = 496 and codigo = 'CLIENTLIST';

-- Sub-items del dropdown Reportes (antes SALES)
insert into funcionalidad values (497, 'SALESREPORTCUSTOMER',        'Reporte Ventas por Cliente',              1, 1, 'Functionality.customers.report.salesCustomer',        1);
insert into funcionalidad values (498, 'SALESREPORTPRODUCT',         'Reporte Ventas por Producto',             1, 1, 'Functionality.customers.report.salesProduct',         1);
insert into funcionalidad values (499, 'SALESREPORTCUSTOMERPRODUCT', 'Reporte Ventas por Cliente y Producto',   1, 1, 'Functionality.customers.report.salesCustomerProduct', 1);
insert into funcionalidad values (500, 'CUSTOMERKARDEXREPORT',       'Reporte Kardex de Clientes',              1, 1, 'Functionality.customers.report.customerKardex',       1);
insert into funcionalidad values (501, 'SUMMARYCLIENTSTATEREPORT',   'Reporte Resumen Estado de Clientes',      1, 1, 'Functionality.customers.report.summaryClientState',   1);

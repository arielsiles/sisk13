-- query_v6.0.121_terdemol.sql
-- Provision mensual de intereses por pagar sobre DPF + CRUD de tipos de cambio.
--
-- Contenido:
--   1) Dos columnas nuevas en `configuracion` para las cuentas de GASTO de la
--      provision de DPF (MN y ME), con su FK hacia arcgms como el resto de las
--      cuentas de la pantalla Preferencias de compania (ver query_v6.0.113).
--   2) La funcionalidad FINANCESEXCHANGERATE (CRUD de arcgtc).
--   3) La funcionalidad PROVISIONDPF (pantalla de provision).
--
-- La contrapartida del asiento (pasivo "cargos financieros por pagar") NO se
-- configura aca: sale de CTACF_MN / CTACF_ME de `tipocuenta`, que ya estan
-- cargadas y son las que usa la renovacion de DPF.
--
-- Bitmask permiso: VIEW=1, CREATE=2, UPDATE=4, DELETE=8.
-- OJO derechoacceso.idmodulo: la named query AccessRight.findByUser hace join
-- contra modulocompania por (idcompania, idmodulo) y filtra por active. Si
-- idmodulo va NULL el join no matchea y el permiso se ignora SIN error.
-- ----------------------------------------------------------------------------


-- 1) Cuentas de gasto de la provision de DPF -------------------------------

ALTER TABLE configuracion
    ADD COLUMN i_ppag_dpf_mn varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT 'Gasto intereses provision DPF MN',
    ADD COLUMN i_ppag_dpf_me varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT 'Gasto intereses provision DPF ME';

ALTER TABLE configuracion
    ADD CONSTRAINT fk_configuracion_i_ppag_dpf_mn FOREIGN KEY (i_ppag_dpf_mn) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_i_ppag_dpf_me FOREIGN KEY (i_ppag_dpf_me) REFERENCES arcgms (cuenta);

-- Valores segun los asientos que se venian haciendo a mano. Verificar que las
-- dos cuentas existan en arcgms antes de correr el update (la FK lo rechaza si no).
--   4110310000  Intereses por Depositos a Plazo Fijo MN
--   4110320000  Intereses por Depositos a Plazo Fijo ME
-- select cuenta, descri from arcgms where cuenta in ('4110310000','4110320000');

update configuracion
   set i_ppag_dpf_mn = '4110310000',
       i_ppag_dpf_me = '4110320000';


-- 2) Verificacion de las cuentas de pasivo en tipocuenta ---------------------
--    Todo tipo de cuenta DPF con certificados vivos tiene que tener cargada su
--    cuenta de cargos financieros, si no la provision se corta con mensaje.
--    Esta consulta debe devolver 0 filas:
--
--   select tc.idtipocuenta, tc.nombre, tc.CTACF_MN, tc.CTACF_ME
--     from tipocuenta tc
--    where tc.tipo = 'DPF'
--      and (   (tc.CTACF_MN is null or tc.CTACF_MN = '')
--           or (tc.CTACF_ME is null or tc.CTACF_ME = ''));


-- 3) Funcionalidades ---------------------------------------------------------
--    Columnas de funcionalidad (orden fisico):
--      (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
--    Ultima sembrada: 503 (COMPANYSETTING, query_v6.0.110).
--    idmodulo: 5 = finances, 1 = customers.
SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
insert into funcionalidad values (@nuevo_id, 'FINANCESEXCHANGERATE', 'Tipos de cambio de contabilidad (arcgtc)', 5, 15, 'Functionality.finances.financesExchangeRate', 1);

-- La pantalla de provision no tiene edicion ni baja: solo consultar/calcular
-- (VIEW=1) y generar el asiento (CREATE=2). Por eso permiso = 3.
SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
insert into funcionalidad values (@nuevo_id, 'PROVISIONDPF', 'Provision de intereses por pagar sobre DPF', 1, 3, 'Functionality.customers.provisionDPF', 1);


-- 4) Otorgar los permisos al rol Administrador (idrol = 1) -------------------

-- insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo) values (514, 1, 15, 1, 5);
-- insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo) values (515, 1, 3, 1, 1);


-- 5) Tipos de cambio a cargar para poder verificar/generar -------------------
--    La provision exige el tipo de cambio del dolar en la fecha EXACTA del
--    cierre de mes. Sin esa fila no se puede ni verificar un mes pasado.
--    Que cierres faltan (clase_cambio del dolar segun cg_moneda):
--
--   select d.fecha
--     from (select last_day(concat(y.y,'-',lpad(m.m,2,'0'),'-01')) fecha
--             from (select 2025 y union select 2026) y,
--                  (select 1 m union select 2 union select 3 union select 4
--                    union select 5 union select 6 union select 7 union select 8
--                    union select 9 union select 10 union select 11 union select 12) m) d
--    where d.fecha <= curdate()
--      and not exists (select 1 from arcgtc t
--                       where t.fecha = d.fecha
--                         and t.clase_cambio = (select clase_cambio from cg_moneda where cod_mon = 'D'))
--    order by d.fecha;
--
--    Cargarlos desde Finanzas > Configuracion > Tipos de cambio (contabilidad).

-- query_v6.0.121_update_DPF.sql
-- Conciliacion de datos de DPF, complemento de query_v6.0.121_terdemol.sql
--
-- Contenido:
--   1) Diagnostico previo (solo SELECT)
--   2) Estado ANULADO para los certificados fantasma (sin ningun movimiento contable)
--   3) Conciliacion de `cuenta.capital` contra el mayor del propio certificado
--   4) Codigos de DPF duplicados: informe para decision
--   5) Verificacion posterior
--
-- ============================================================================
-- ORDEN DE APLICACION - LEER ANTES DE EJECUTAR
-- ============================================================================
-- La seccion 2 escribe el valor 'ANNULLED' en cuenta.estado. Ese valor tiene que
-- existir ANTES en el enum AccountState, si no Hibernate revienta al leer esas
-- filas con "No enum constant ...AccountState.ANNULLED".
--   => Desplegar primero el codigo que agrega el estado, recien despues correr esto.
--
-- Si prefieren el literal en castellano ('ANULADO') hay que cambiarlo en los DOS
-- lados a la vez: el enum y este script. La columna es varchar(32), entra cualquiera.
-- Se usa ANNULLED por consistencia con los valores que ya estan (ACTIVE, INACTIVE).
--
-- ============================================================================
-- POR QUE `codigo` NO SIRVE COMO CLAVE
-- ============================================================================
-- Hay 5 codigos de DPF repetidos en dos filas distintas (ME00122, ME0062, ME0082,
-- ME0096, ME0098). Todo este script opera por `idcuenta`, que es la PK real.
-- Un UPDATE por `codigo` tocaria dos certificados de socios diferentes.
-- ----------------------------------------------------------------------------


-- ============================================================================
-- 1) DIAGNOSTICO PREVIO (solo lectura, correr y guardar el resultado)
-- ============================================================================

-- 1.a Resumen: cuantos DPF tienen el capital desalineado de su propia contabilidad.
--     El capital del certificado es la suma de sus creditos: dentro de un mismo
--     certificado el unico movimiento deudor es la cancelacion (verificado).
select case when k.creditos is null then 'sin movimientos (a anular)'
            when c.capital = k.creditos then 'coincide'
            when c.capital = 0         then 'capital en CERO'
            else 'DIFIERE' end resultado,
       count(*) dpf
  from cuenta c
  join tipocuenta t on t.idtipocuenta = c.idtipocuenta
  left join (select d.idcuenta,
                    round(sum(case when cu.moneda = 'D' then d.haberme else d.haber end), 2) creditos
               from sf_tmpdet d
               join cuenta cu on cu.idcuenta = d.idcuenta
              where d.idcuenta is not null
              group by d.idcuenta) k on k.idcuenta = c.idcuenta
 where t.tipo = 'DPF'
 group by 1
 order by 2 desc;

-- 1.b Detalle de los que se van a conciliar, con el valor actual y el derivado.
select c.idcuenta, c.codigo, c.nocuenta, c.moneda, c.estado,
       c.fechaapertura, c.fechavence,
       c.capital capital_actual,
       k.creditos capital_segun_mayor,
       round(k.creditos - c.capital, 2) diferencia
  from cuenta c
  join tipocuenta t on t.idtipocuenta = c.idtipocuenta
  join (select d.idcuenta,
               round(sum(case when cu.moneda = 'D' then d.haberme else d.haber end), 2) creditos
          from sf_tmpdet d
          join cuenta cu on cu.idcuenta = d.idcuenta
         where d.idcuenta is not null
         group by d.idcuenta) k on k.idcuenta = c.idcuenta
 where t.tipo = 'DPF'
   and c.capital <> k.creditos
 order by c.fechaapertura desc;

-- 1.c Los que quedan vivos en 2025-2026: son los que bloquean la provision.
select c.idcuenta, c.codigo, c.moneda, c.capital capital_actual, k.creditos capital_segun_mayor,
       c.fechaapertura, c.fechavence
  from cuenta c
  join tipocuenta t on t.idtipocuenta = c.idtipocuenta
  join (select d.idcuenta,
               round(sum(case when cu.moneda = 'D' then d.haberme else d.haber end), 2) creditos
          from sf_tmpdet d
          join cuenta cu on cu.idcuenta = d.idcuenta
         where d.idcuenta is not null
         group by d.idcuenta) k on k.idcuenta = c.idcuenta
 where t.tipo = 'DPF'
   and c.capital <> k.creditos
   and c.fechaapertura <= '2026-12-31'
   and c.fechavence   >= '2025-01-01'
 order by c.fechaapertura;
-- Esperado hoy: ME00148 (0 -> 26422), ME00158 (5241 -> 7241),
--               ME00159 (3779 -> 5779), ME00162 (57191 -> 58191)


-- ============================================================================
-- 2) ESTADO ANULADO PARA LOS CERTIFICADOS FANTASMA
-- ============================================================================
-- Son filas de `cuenta` que nunca tuvieron un solo movimiento contable: se dieron
-- de alta y el desembolso nunca se registro, o se cargo dos veces y quedo la copia.
-- Hoy son exactamente 2 en todo el sistema, las dos DPF, y las dos son el hermano
-- huerfano de un codigo duplicado:
--
--   idcuenta 112 (ME00122, socio 483, ACTIVE)  <- huerfano; el bueno es 113 (socio 352)
--   idcuenta  45 (ME0062,  socio 236, INACTIVE) <- huerfano; el bueno es 46 (mismo socio)
--
-- OJO con idcuenta 112: esta en ACTIVE y vencio el 01/08/2023. Es el unico ACTIVE
-- sin movimientos, y por eso aparecia en listados operativos como si fuera un DPF vivo.

-- 2.a Verificacion: listar lo que se va a anular. Revisar ANTES de ejecutar el update.
select c.idcuenta, c.codigo, c.nocuenta, c.idsocio, c.capital, c.estado,
       c.fechaapertura, c.fechavence
  from cuenta c
  join tipocuenta t on t.idtipocuenta = c.idtipocuenta
 where t.tipo = 'DPF'
   and not exists (select 1 from sf_tmpdet d where d.idcuenta = c.idcuenta)
 order by c.idcuenta;

-- 2.b Respaldo antes de tocar nada.
create table respaldo_cuenta_20260803 as
select * from cuenta
 where idcuenta in (
   select idcuenta from (
     select c.idcuenta
       from cuenta c
       join tipocuenta t on t.idtipocuenta = c.idtipocuenta
      where t.tipo = 'DPF'
        and not exists (select 1 from sf_tmpdet d where d.idcuenta = c.idcuenta)
   ) x);

-- 2.c Anular. Se listan los idcuenta de forma explicita a proposito: un WHERE
--     dinamico sobre "sin movimientos" podria barrer filas nuevas si se corre
--     mas adelante en otro momento.
update cuenta
   set estado = 'ANNULLED'
 where idcuenta in (112, 45);

-- 2.d Control: debe devolver 2 filas, ambas en ANNULLED.
select idcuenta, codigo, nocuenta, estado from cuenta where idcuenta in (112, 45);


-- ============================================================================
-- 3) CONCILIACION DE `cuenta.capital` CONTRA EL MAYOR
-- ============================================================================
-- El capital del certificado se deriva de la suma de sus creditos contables.
-- Cubre los tres casos que hoy estan desalineados:
--   - capital en 0 por un alta que no lo grabo
--   - aumentos posteriores ("DEPOSITO PARA AUMENTAR AL DPF") que nunca llegaron al campo
--   - diferencias sueltas de captura
--
-- Los certificados anulados en la seccion 2 quedan fuera: no tienen de donde derivar.

-- 3.a Respaldo de todos los que van a cambiar.
create table respaldo_capital_dpf_20260803 as
select c.idcuenta, c.codigo, c.nocuenta, c.moneda, c.estado, c.capital capital_anterior
  from cuenta c
  join tipocuenta t on t.idtipocuenta = c.idtipocuenta
  join (select d.idcuenta,
               round(sum(case when cu.moneda = 'D' then d.haberme else d.haber end), 2) creditos
          from sf_tmpdet d
          join cuenta cu on cu.idcuenta = d.idcuenta
         where d.idcuenta is not null
         group by d.idcuenta) k on k.idcuenta = c.idcuenta
 where t.tipo = 'DPF'
   and c.capital <> k.creditos;

-- 3.b Conciliar.
update cuenta c
  join tipocuenta t on t.idtipocuenta = c.idtipocuenta
  join (select d.idcuenta,
               round(sum(case when cu.moneda = 'D' then d.haberme else d.haber end), 2) creditos
          from sf_tmpdet d
          join cuenta cu on cu.idcuenta = d.idcuenta
         where d.idcuenta is not null
         group by d.idcuenta) k on k.idcuenta = c.idcuenta
   set c.capital = k.creditos
 where t.tipo = 'DPF'
   and c.capital <> k.creditos;

-- 3.c Si prefieren conciliar SOLO lo que bloquea la provision y dejar el resto
--     del historico como esta, usar este update en lugar del 3.b:
--
-- update cuenta set capital = 26422.00 where idcuenta = 140;  -- ME00148
-- update cuenta set capital =  7241.00 where idcuenta = (select idcuenta from (select idcuenta from cuenta where codigo='ME00158') z);
-- update cuenta set capital =  5779.00 where idcuenta = (select idcuenta from (select idcuenta from cuenta where codigo='ME00159') z);
-- update cuenta set capital = 58191.00 where idcuenta = (select idcuenta from (select idcuenta from cuenta where codigo='ME00162') z);
--     (confirmar antes los idcuenta con la consulta 1.c; esos 3 codigos no estan
--      duplicados hoy, pero se resuelven por subconsulta por las dudas)


-- ============================================================================
-- 4) CODIGOS DE DPF DUPLICADOS - INFORME, NO SE TOCA
-- ============================================================================
-- Quedan 3 codigos repetidos donde AMBAS filas son certificados legitimos, con
-- movimientos y capital propio, de socios distintos:
--
--   ME0082 -> idcuenta 68 (socio 291, 19.319) y 69 (socio 231, 10.000)
--   ME0096 -> idcuenta 83 (socio 234, 12.765) y 84 (socio 238,  1.250)
--   ME0098 -> idcuenta 86 (socio 238, 52.778) y 87 (socio 353,  2.334)
--
-- No se anulan ni se fusionan: son dos DPF reales que comparten codigo por un
-- error de captura. La clave operativa es `nocuenta` / `idcuenta`, que si son
-- unicos, asi que el calculo no se ve afectado. Renumerar el codigo de uno de
-- cada par es decision de finanzas, y conviene hacerlo desde la pantalla para
-- que quede el rastro, no por SQL.

select c.codigo, c.idcuenta, c.nocuenta, c.idsocio, c.capital, c.estado,
       (select count(*) from sf_tmpdet d where d.idcuenta = c.idcuenta) movimientos
  from cuenta c
  join tipocuenta t on t.idtipocuenta = c.idtipocuenta
 where t.tipo = 'DPF'
   and c.codigo in (select codigo from (select codigo from cuenta cc
                                          join tipocuenta tt on tt.idtipocuenta = cc.idtipocuenta
                                         where tt.tipo = 'DPF'
                                         group by codigo having count(*) > 1) z)
 order by c.codigo, c.idcuenta;


-- ============================================================================
-- 5) VERIFICACION POSTERIOR - LAS TRES DEBEN DEVOLVER 0 FILAS
-- ============================================================================

-- 5.a Ningun DPF con capital distinto de su mayor (salvo los anulados).
select c.idcuenta, c.codigo, c.capital, k.creditos
  from cuenta c
  join tipocuenta t on t.idtipocuenta = c.idtipocuenta
  join (select d.idcuenta,
               round(sum(case when cu.moneda = 'D' then d.haberme else d.haber end), 2) creditos
          from sf_tmpdet d
          join cuenta cu on cu.idcuenta = d.idcuenta
         where d.idcuenta is not null
         group by d.idcuenta) k on k.idcuenta = c.idcuenta
 where t.tipo = 'DPF'
   and c.estado <> 'ANNULLED'
   and c.capital <> k.creditos;

-- 5.b Ningun DPF sin movimientos que no este anulado.
select c.idcuenta, c.codigo, c.estado
  from cuenta c
  join tipocuenta t on t.idtipocuenta = c.idtipocuenta
 where t.tipo = 'DPF'
   and c.estado <> 'ANNULLED'
   and not exists (select 1 from sf_tmpdet d where d.idcuenta = c.idcuenta);

-- 5.c Ningun DPF vivo en 2025-2026 con capital en cero.
select c.idcuenta, c.codigo, c.capital, c.fechaapertura, c.fechavence
  from cuenta c
  join tipocuenta t on t.idtipocuenta = c.idtipocuenta
 where t.tipo = 'DPF'
   and c.estado <> 'ANNULLED'
   and (c.capital is null or c.capital <= 0)
   and c.fechaapertura <= '2026-12-31'
   and c.fechavence   >= '2025-01-01';


-- ============================================================================
-- ROLLBACK
-- ============================================================================
-- update cuenta c join respaldo_capital_dpf_20260803 r on r.idcuenta = c.idcuenta
--    set c.capital = r.capital_anterior;
-- update cuenta c join respaldo_cuenta_20260803 r on r.idcuenta = c.idcuenta
--    set c.estado = r.estado;
-- drop table respaldo_capital_dpf_20260803;
-- drop table respaldo_cuenta_20260803;

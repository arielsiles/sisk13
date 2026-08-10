-- ============================================================================
-- fix_cuentas_configuracion_inexistentes.sql
-- ============================================================================
-- Deja en NULL las columnas de cuenta de `configuracion` que Hibernate no puede
-- resolver contra el plan de cuentas `arcgms`.
--
-- Motivo: CashAccount tiene CLAVE COMPUESTA (no_cia, cuenta). Un valor que no
-- resuelve por ESA clave hace que el proxy @ManyToOne LAZY explote al renderizar
-- /admin/companySetting.xhtml con:
--   javax.persistence.EntityNotFoundException: Unable to find CashAccount with id ...
--
-- OJO: verificar solo por `cuenta` NO alcanza. La cuenta puede existir en arcgms
-- con OTRO no_cia (tipico al traer datos de otro cliente): matchea por codigo
-- pero Hibernate la busca por (no_cia, cuenta) y no la encuentra. Por eso aqui
-- todo se verifica por (no_cia, cuenta), igual que Hibernate.
--
-- Cubre las 51 columnas de cuenta. Idempotente. NULL es compatible con las FK.
--
-- ADVERTENCIAS:
--   * NULL BORRA esa configuracion; no adivina la cuenta correcta. Si el problema
--     es el no_cia (la cuenta SI existe con otra compania), quiza convenga
--     corregir el no_cia en vez de anular -- eso es decision del negocio.
--   * Si una columna es NOT NULL en la BD, su UPDATE fallara: poner un codigo
--     valido, o antes: ALTER TABLE configuracion MODIFY <col> VARCHAR(20) NULL;
-- ----------------------------------------------------------------------------

-- 1) DIAGNOSTICO AMPLIADO (solo lectura). Correr PRIMERO.
--    match_codigo      = existe la cuenta por codigo (ignorando no_cia)
--    match_cia_cuenta  = existe por (no_cia, cuenta)  <- como busca Hibernate
--    match_binario     = existe por codigo comparando BINARY (detecta espacios/mayus)
--    len               = largo del codigo (un largo raro delata espacios)
--  Lo que rompe la pantalla son las filas con match_cia_cuenta = 0.

SELECT t.columna, t.no_cia, t.codigo, LENGTH(t.codigo) AS len,
       (SELECT COUNT(*) FROM arcgms a WHERE a.cuenta = t.codigo) AS match_codigo,
       (SELECT COUNT(*) FROM arcgms a WHERE a.no_cia = t.no_cia AND a.cuenta = t.codigo) AS match_cia_cuenta,
       (SELECT COUNT(*) FROM arcgms a WHERE BINARY a.cuenta = BINARY t.codigo) AS match_binario
  FROM (
    SELECT 'ctadiftipcam' AS columna, no_cia, `ctadiftipcam` AS codigo FROM configuracion
    UNION ALL SELECT 'iue_ret' AS columna, no_cia, `iue_ret` AS codigo FROM configuracion
    UNION ALL SELECT 'it_ret' AS columna, no_cia, `it_ret` AS codigo FROM configuracion
    UNION ALL SELECT 'ctacostpt' AS columna, no_cia, `ctacostpt` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaalmpt' AS columna, no_cia, `ctaalmpt` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaalmptag' AS columna, no_cia, `ctaalmptag` AS codigo FROM configuracion
    UNION ALL SELECT 'ctacostpv' AS columna, no_cia, `ctacostpv` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaalmpv' AS columna, no_cia, `ctaalmpv` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaMerma' AS columna, no_cia, `ctaMerma` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaProm' AS columna, no_cia, `ctaProm` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaantprovme' AS columna, no_cia, `ctaantprovme` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaantprovmn' AS columna, no_cia, `ctaantprovmn` AS codigo FROM configuracion
    UNION ALL SELECT 'ctadeptrame' AS columna, no_cia, `ctadeptrame` AS codigo FROM configuracion
    UNION ALL SELECT 'ctadeptramn' AS columna, no_cia, `ctadeptramn` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaalmme' AS columna, no_cia, `ctaalmme` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaalmmn' AS columna, no_cia, `ctaalmmn` AS codigo FROM configuracion
    UNION ALL SELECT 'ctatransalmme' AS columna, no_cia, `ctatransalmme` AS codigo FROM configuracion
    UNION ALL SELECT 'ctatransalmmn' AS columna, no_cia, `ctatransalmmn` AS codigo FROM configuracion
    UNION ALL SELECT 'ctatransalm1mn' AS columna, no_cia, `ctatransalm1mn` AS codigo FROM configuracion
    UNION ALL SELECT 'ctatransalm2mn' AS columna, no_cia, `ctatransalm2mn` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaaitb' AS columna, no_cia, `ctaaitb` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaivacrefime' AS columna, no_cia, `ctaivacrefime` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaivacrefimn' AS columna, no_cia, `ctaivacrefimn` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaivacrefitrmn' AS columna, no_cia, `ctaivacrefitrmn` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaprovobu' AS columna, no_cia, `ctaprovobu` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaafet' AS columna, no_cia, `ctaafet` AS codigo FROM configuracion
    UNION ALL SELECT 'ctamermabaj' AS columna, no_cia, `ctamermabaj` AS codigo FROM configuracion
    UNION ALL SELECT 'ctareproc' AS columna, no_cia, `ctareproc` AS codigo FROM configuracion
    UNION ALL SELECT 'ct_cajaahorro' AS columna, no_cia, `ct_cajaahorro` AS codigo FROM configuracion
    UNION ALL SELECT 'ct_cajaveter' AS columna, no_cia, `ct_cajaveter` AS codigo FROM configuracion
    UNION ALL SELECT 'CAJAgRAL1MN' AS columna, no_cia, `CAJAgRAL1MN` AS codigo FROM configuracion
    UNION ALL SELECT 'i_pvig_pf_mn' AS columna, no_cia, `i_pvig_pf_mn` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaprovaf' AS columna, no_cia, `ctaprovaf` AS codigo FROM configuracion
    UNION ALL SELECT 'ctag_it' AS columna, no_cia, `ctag_it` AS codigo FROM configuracion
    UNION ALL SELECT 'ctap_debfisiva' AS columna, no_cia, `ctap_debfisiva` AS codigo FROM configuracion
    UNION ALL SELECT 'ctap_itxpagar' AS columna, no_cia, `ctap_itxpagar` AS codigo FROM configuracion
    UNION ALL SELECT 'ctai_ventapri' AS columna, no_cia, `ctai_ventapri` AS codigo FROM configuracion
    UNION ALL SELECT 'ctai_ventasec' AS columna, no_cia, `ctai_ventasec` AS codigo FROM configuracion
    UNION ALL SELECT 'ctacomision' AS columna, no_cia, `ctacomision` AS codigo FROM configuracion
    UNION ALL SELECT 'cxp_provmn' AS columna, no_cia, `cxp_provmn` AS codigo FROM configuracion
    UNION ALL SELECT 'cta_pat01' AS columna, no_cia, `cta_pat01` AS codigo FROM configuracion
    UNION ALL SELECT 'cta_pat02' AS columna, no_cia, `cta_pat02` AS codigo FROM configuracion
    UNION ALL SELECT 'cta_pat03' AS columna, no_cia, `cta_pat03` AS codigo FROM configuracion
    UNION ALL SELECT 'cta_pat04' AS columna, no_cia, `cta_pat04` AS codigo FROM configuracion
    UNION ALL SELECT 'cta_pat05' AS columna, no_cia, `cta_pat05` AS codigo FROM configuracion
    UNION ALL SELECT 'cxp_iva' AS columna, no_cia, `cxp_iva` AS codigo FROM configuracion
    UNION ALL SELECT 'cxp_regalia' AS columna, no_cia, `cxp_regalia` AS codigo FROM configuracion
    UNION ALL SELECT 'cxp_cns' AS columna, no_cia, `cxp_cns` AS codigo FROM configuracion
    UNION ALL SELECT 'res_perdida' AS columna, no_cia, `res_perdida` AS codigo FROM configuracion
    UNION ALL SELECT 'res_utilidad' AS columna, no_cia, `res_utilidad` AS codigo FROM configuracion
    UNION ALL SELECT 'oc_pagodefault' AS columna, no_cia, `oc_pagodefault` AS codigo FROM configuracion
  ) t
 WHERE t.codigo IS NOT NULL AND TRIM(t.codigo) <> ''
 ORDER BY match_cia_cuenta ASC, t.columna;


-- 2) CORRECCION: deja en NULL las cuentas que no resuelven por (no_cia, cuenta).

UPDATE configuracion c SET c.`ctadiftipcam` = NULL
  WHERE c.`ctadiftipcam` IS NOT NULL AND TRIM(c.`ctadiftipcam`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctadiftipcam`);
UPDATE configuracion c SET c.`iue_ret` = NULL
  WHERE c.`iue_ret` IS NOT NULL AND TRIM(c.`iue_ret`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`iue_ret`);
UPDATE configuracion c SET c.`it_ret` = NULL
  WHERE c.`it_ret` IS NOT NULL AND TRIM(c.`it_ret`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`it_ret`);
UPDATE configuracion c SET c.`ctacostpt` = NULL
  WHERE c.`ctacostpt` IS NOT NULL AND TRIM(c.`ctacostpt`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctacostpt`);
UPDATE configuracion c SET c.`ctaalmpt` = NULL
  WHERE c.`ctaalmpt` IS NOT NULL AND TRIM(c.`ctaalmpt`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctaalmpt`);
UPDATE configuracion c SET c.`ctaalmptag` = NULL
  WHERE c.`ctaalmptag` IS NOT NULL AND TRIM(c.`ctaalmptag`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctaalmptag`);
UPDATE configuracion c SET c.`ctacostpv` = NULL
  WHERE c.`ctacostpv` IS NOT NULL AND TRIM(c.`ctacostpv`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctacostpv`);
UPDATE configuracion c SET c.`ctaalmpv` = NULL
  WHERE c.`ctaalmpv` IS NOT NULL AND TRIM(c.`ctaalmpv`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctaalmpv`);
UPDATE configuracion c SET c.`ctaMerma` = NULL
  WHERE c.`ctaMerma` IS NOT NULL AND TRIM(c.`ctaMerma`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctaMerma`);
UPDATE configuracion c SET c.`ctaProm` = NULL
  WHERE c.`ctaProm` IS NOT NULL AND TRIM(c.`ctaProm`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctaProm`);
UPDATE configuracion c SET c.`ctaantprovme` = NULL
  WHERE c.`ctaantprovme` IS NOT NULL AND TRIM(c.`ctaantprovme`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctaantprovme`);
UPDATE configuracion c SET c.`ctaantprovmn` = NULL
  WHERE c.`ctaantprovmn` IS NOT NULL AND TRIM(c.`ctaantprovmn`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctaantprovmn`);
UPDATE configuracion c SET c.`ctadeptrame` = NULL
  WHERE c.`ctadeptrame` IS NOT NULL AND TRIM(c.`ctadeptrame`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctadeptrame`);
UPDATE configuracion c SET c.`ctadeptramn` = NULL
  WHERE c.`ctadeptramn` IS NOT NULL AND TRIM(c.`ctadeptramn`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctadeptramn`);
UPDATE configuracion c SET c.`ctaalmme` = NULL
  WHERE c.`ctaalmme` IS NOT NULL AND TRIM(c.`ctaalmme`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctaalmme`);
UPDATE configuracion c SET c.`ctaalmmn` = NULL
  WHERE c.`ctaalmmn` IS NOT NULL AND TRIM(c.`ctaalmmn`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctaalmmn`);
UPDATE configuracion c SET c.`ctatransalmme` = NULL
  WHERE c.`ctatransalmme` IS NOT NULL AND TRIM(c.`ctatransalmme`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctatransalmme`);
UPDATE configuracion c SET c.`ctatransalmmn` = NULL
  WHERE c.`ctatransalmmn` IS NOT NULL AND TRIM(c.`ctatransalmmn`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctatransalmmn`);
UPDATE configuracion c SET c.`ctatransalm1mn` = NULL
  WHERE c.`ctatransalm1mn` IS NOT NULL AND TRIM(c.`ctatransalm1mn`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctatransalm1mn`);
UPDATE configuracion c SET c.`ctatransalm2mn` = NULL
  WHERE c.`ctatransalm2mn` IS NOT NULL AND TRIM(c.`ctatransalm2mn`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctatransalm2mn`);
UPDATE configuracion c SET c.`ctaaitb` = NULL
  WHERE c.`ctaaitb` IS NOT NULL AND TRIM(c.`ctaaitb`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctaaitb`);
UPDATE configuracion c SET c.`ctaivacrefime` = NULL
  WHERE c.`ctaivacrefime` IS NOT NULL AND TRIM(c.`ctaivacrefime`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctaivacrefime`);
UPDATE configuracion c SET c.`ctaivacrefimn` = NULL
  WHERE c.`ctaivacrefimn` IS NOT NULL AND TRIM(c.`ctaivacrefimn`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctaivacrefimn`);
UPDATE configuracion c SET c.`ctaivacrefitrmn` = NULL
  WHERE c.`ctaivacrefitrmn` IS NOT NULL AND TRIM(c.`ctaivacrefitrmn`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctaivacrefitrmn`);
UPDATE configuracion c SET c.`ctaprovobu` = NULL
  WHERE c.`ctaprovobu` IS NOT NULL AND TRIM(c.`ctaprovobu`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctaprovobu`);
UPDATE configuracion c SET c.`ctaafet` = NULL
  WHERE c.`ctaafet` IS NOT NULL AND TRIM(c.`ctaafet`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctaafet`);
UPDATE configuracion c SET c.`ctamermabaj` = NULL
  WHERE c.`ctamermabaj` IS NOT NULL AND TRIM(c.`ctamermabaj`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctamermabaj`);
UPDATE configuracion c SET c.`ctareproc` = NULL
  WHERE c.`ctareproc` IS NOT NULL AND TRIM(c.`ctareproc`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctareproc`);
UPDATE configuracion c SET c.`ct_cajaahorro` = NULL
  WHERE c.`ct_cajaahorro` IS NOT NULL AND TRIM(c.`ct_cajaahorro`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ct_cajaahorro`);
UPDATE configuracion c SET c.`ct_cajaveter` = NULL
  WHERE c.`ct_cajaveter` IS NOT NULL AND TRIM(c.`ct_cajaveter`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ct_cajaveter`);
UPDATE configuracion c SET c.`CAJAgRAL1MN` = NULL
  WHERE c.`CAJAgRAL1MN` IS NOT NULL AND TRIM(c.`CAJAgRAL1MN`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`CAJAgRAL1MN`);
UPDATE configuracion c SET c.`i_pvig_pf_mn` = NULL
  WHERE c.`i_pvig_pf_mn` IS NOT NULL AND TRIM(c.`i_pvig_pf_mn`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`i_pvig_pf_mn`);
UPDATE configuracion c SET c.`ctaprovaf` = NULL
  WHERE c.`ctaprovaf` IS NOT NULL AND TRIM(c.`ctaprovaf`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctaprovaf`);
UPDATE configuracion c SET c.`ctag_it` = NULL
  WHERE c.`ctag_it` IS NOT NULL AND TRIM(c.`ctag_it`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctag_it`);
UPDATE configuracion c SET c.`ctap_debfisiva` = NULL
  WHERE c.`ctap_debfisiva` IS NOT NULL AND TRIM(c.`ctap_debfisiva`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctap_debfisiva`);
UPDATE configuracion c SET c.`ctap_itxpagar` = NULL
  WHERE c.`ctap_itxpagar` IS NOT NULL AND TRIM(c.`ctap_itxpagar`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctap_itxpagar`);
UPDATE configuracion c SET c.`ctai_ventapri` = NULL
  WHERE c.`ctai_ventapri` IS NOT NULL AND TRIM(c.`ctai_ventapri`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctai_ventapri`);
UPDATE configuracion c SET c.`ctai_ventasec` = NULL
  WHERE c.`ctai_ventasec` IS NOT NULL AND TRIM(c.`ctai_ventasec`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctai_ventasec`);
UPDATE configuracion c SET c.`ctacomision` = NULL
  WHERE c.`ctacomision` IS NOT NULL AND TRIM(c.`ctacomision`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`ctacomision`);
UPDATE configuracion c SET c.`cxp_provmn` = NULL
  WHERE c.`cxp_provmn` IS NOT NULL AND TRIM(c.`cxp_provmn`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`cxp_provmn`);
UPDATE configuracion c SET c.`cta_pat01` = NULL
  WHERE c.`cta_pat01` IS NOT NULL AND TRIM(c.`cta_pat01`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`cta_pat01`);
UPDATE configuracion c SET c.`cta_pat02` = NULL
  WHERE c.`cta_pat02` IS NOT NULL AND TRIM(c.`cta_pat02`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`cta_pat02`);
UPDATE configuracion c SET c.`cta_pat03` = NULL
  WHERE c.`cta_pat03` IS NOT NULL AND TRIM(c.`cta_pat03`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`cta_pat03`);
UPDATE configuracion c SET c.`cta_pat04` = NULL
  WHERE c.`cta_pat04` IS NOT NULL AND TRIM(c.`cta_pat04`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`cta_pat04`);
UPDATE configuracion c SET c.`cta_pat05` = NULL
  WHERE c.`cta_pat05` IS NOT NULL AND TRIM(c.`cta_pat05`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`cta_pat05`);
UPDATE configuracion c SET c.`cxp_iva` = NULL
  WHERE c.`cxp_iva` IS NOT NULL AND TRIM(c.`cxp_iva`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`cxp_iva`);
UPDATE configuracion c SET c.`cxp_regalia` = NULL
  WHERE c.`cxp_regalia` IS NOT NULL AND TRIM(c.`cxp_regalia`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`cxp_regalia`);
UPDATE configuracion c SET c.`cxp_cns` = NULL
  WHERE c.`cxp_cns` IS NOT NULL AND TRIM(c.`cxp_cns`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`cxp_cns`);
UPDATE configuracion c SET c.`res_perdida` = NULL
  WHERE c.`res_perdida` IS NOT NULL AND TRIM(c.`res_perdida`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`res_perdida`);
UPDATE configuracion c SET c.`res_utilidad` = NULL
  WHERE c.`res_utilidad` IS NOT NULL AND TRIM(c.`res_utilidad`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`res_utilidad`);
UPDATE configuracion c SET c.`oc_pagodefault` = NULL
  WHERE c.`oc_pagodefault` IS NOT NULL AND TRIM(c.`oc_pagodefault`) <> ''
    AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = c.no_cia AND a.cuenta = c.`oc_pagodefault`);

-- 3) VERIFICACION: debe devolver 0 filas.

SELECT t.columna, t.no_cia, t.codigo
  FROM (
    SELECT 'ctadiftipcam' AS columna, no_cia, `ctadiftipcam` AS codigo FROM configuracion
    UNION ALL SELECT 'iue_ret' AS columna, no_cia, `iue_ret` AS codigo FROM configuracion
    UNION ALL SELECT 'it_ret' AS columna, no_cia, `it_ret` AS codigo FROM configuracion
    UNION ALL SELECT 'ctacostpt' AS columna, no_cia, `ctacostpt` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaalmpt' AS columna, no_cia, `ctaalmpt` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaalmptag' AS columna, no_cia, `ctaalmptag` AS codigo FROM configuracion
    UNION ALL SELECT 'ctacostpv' AS columna, no_cia, `ctacostpv` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaalmpv' AS columna, no_cia, `ctaalmpv` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaMerma' AS columna, no_cia, `ctaMerma` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaProm' AS columna, no_cia, `ctaProm` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaantprovme' AS columna, no_cia, `ctaantprovme` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaantprovmn' AS columna, no_cia, `ctaantprovmn` AS codigo FROM configuracion
    UNION ALL SELECT 'ctadeptrame' AS columna, no_cia, `ctadeptrame` AS codigo FROM configuracion
    UNION ALL SELECT 'ctadeptramn' AS columna, no_cia, `ctadeptramn` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaalmme' AS columna, no_cia, `ctaalmme` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaalmmn' AS columna, no_cia, `ctaalmmn` AS codigo FROM configuracion
    UNION ALL SELECT 'ctatransalmme' AS columna, no_cia, `ctatransalmme` AS codigo FROM configuracion
    UNION ALL SELECT 'ctatransalmmn' AS columna, no_cia, `ctatransalmmn` AS codigo FROM configuracion
    UNION ALL SELECT 'ctatransalm1mn' AS columna, no_cia, `ctatransalm1mn` AS codigo FROM configuracion
    UNION ALL SELECT 'ctatransalm2mn' AS columna, no_cia, `ctatransalm2mn` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaaitb' AS columna, no_cia, `ctaaitb` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaivacrefime' AS columna, no_cia, `ctaivacrefime` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaivacrefimn' AS columna, no_cia, `ctaivacrefimn` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaivacrefitrmn' AS columna, no_cia, `ctaivacrefitrmn` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaprovobu' AS columna, no_cia, `ctaprovobu` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaafet' AS columna, no_cia, `ctaafet` AS codigo FROM configuracion
    UNION ALL SELECT 'ctamermabaj' AS columna, no_cia, `ctamermabaj` AS codigo FROM configuracion
    UNION ALL SELECT 'ctareproc' AS columna, no_cia, `ctareproc` AS codigo FROM configuracion
    UNION ALL SELECT 'ct_cajaahorro' AS columna, no_cia, `ct_cajaahorro` AS codigo FROM configuracion
    UNION ALL SELECT 'ct_cajaveter' AS columna, no_cia, `ct_cajaveter` AS codigo FROM configuracion
    UNION ALL SELECT 'CAJAgRAL1MN' AS columna, no_cia, `CAJAgRAL1MN` AS codigo FROM configuracion
    UNION ALL SELECT 'i_pvig_pf_mn' AS columna, no_cia, `i_pvig_pf_mn` AS codigo FROM configuracion
    UNION ALL SELECT 'ctaprovaf' AS columna, no_cia, `ctaprovaf` AS codigo FROM configuracion
    UNION ALL SELECT 'ctag_it' AS columna, no_cia, `ctag_it` AS codigo FROM configuracion
    UNION ALL SELECT 'ctap_debfisiva' AS columna, no_cia, `ctap_debfisiva` AS codigo FROM configuracion
    UNION ALL SELECT 'ctap_itxpagar' AS columna, no_cia, `ctap_itxpagar` AS codigo FROM configuracion
    UNION ALL SELECT 'ctai_ventapri' AS columna, no_cia, `ctai_ventapri` AS codigo FROM configuracion
    UNION ALL SELECT 'ctai_ventasec' AS columna, no_cia, `ctai_ventasec` AS codigo FROM configuracion
    UNION ALL SELECT 'ctacomision' AS columna, no_cia, `ctacomision` AS codigo FROM configuracion
    UNION ALL SELECT 'cxp_provmn' AS columna, no_cia, `cxp_provmn` AS codigo FROM configuracion
    UNION ALL SELECT 'cta_pat01' AS columna, no_cia, `cta_pat01` AS codigo FROM configuracion
    UNION ALL SELECT 'cta_pat02' AS columna, no_cia, `cta_pat02` AS codigo FROM configuracion
    UNION ALL SELECT 'cta_pat03' AS columna, no_cia, `cta_pat03` AS codigo FROM configuracion
    UNION ALL SELECT 'cta_pat04' AS columna, no_cia, `cta_pat04` AS codigo FROM configuracion
    UNION ALL SELECT 'cta_pat05' AS columna, no_cia, `cta_pat05` AS codigo FROM configuracion
    UNION ALL SELECT 'cxp_iva' AS columna, no_cia, `cxp_iva` AS codigo FROM configuracion
    UNION ALL SELECT 'cxp_regalia' AS columna, no_cia, `cxp_regalia` AS codigo FROM configuracion
    UNION ALL SELECT 'cxp_cns' AS columna, no_cia, `cxp_cns` AS codigo FROM configuracion
    UNION ALL SELECT 'res_perdida' AS columna, no_cia, `res_perdida` AS codigo FROM configuracion
    UNION ALL SELECT 'res_utilidad' AS columna, no_cia, `res_utilidad` AS codigo FROM configuracion
    UNION ALL SELECT 'oc_pagodefault' AS columna, no_cia, `oc_pagodefault` AS codigo FROM configuracion
  ) t
 WHERE t.codigo IS NOT NULL AND TRIM(t.codigo) <> ''
   AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia = t.no_cia AND a.cuenta = t.codigo);


SELECT b.cta_bco, b.descri, b.no_cia, b.cuenta AS cuenta_contable, b.estado
FROM ck_ctas_bco b
WHERE b.estado = 'VIG'
  AND b.cuenta IS NOT NULL AND TRIM(b.cuenta) <> ''
  AND NOT EXISTS (SELECT 1 FROM arcgms a
                  WHERE a.no_cia = b.no_cia AND a.cuenta = b.cuenta);

delete from ck_ctas_bco;
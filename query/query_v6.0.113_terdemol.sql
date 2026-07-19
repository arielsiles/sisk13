-- query_v6.0.113_terdemol.sql
-- Integridad referencial (etapa 2): FK de las 51 cuentas contables de
-- `configuracion` (pantalla Preferencias de compania) hacia arcgms.cuenta.
-- Sin cascada: ON DELETE RESTRICT / ON UPDATE NO ACTION por defecto.
-- Previene de raiz el bug de cuentas colgadas que documenta query_v6.0.110.

-- 1) Verificacion previa: debe devolver 0 filas. La FK acepta NULL, pero no
--    cadenas vacias ni codigos que no existan en arcgms.
--   select * from (
--     select 'ctaaitb' col, ctaaitb val from configuracion where ctaaitb <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctaaitb)
--     union all
--     select 'ctaantprovme' col, ctaantprovme val from configuracion where ctaantprovme <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctaantprovme)
--     union all
--     select 'ctaantprovmn' col, ctaantprovmn val from configuracion where ctaantprovmn <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctaantprovmn)
--     union all
--     select 'ctadiftipcam' col, ctadiftipcam val from configuracion where ctadiftipcam <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctadiftipcam)
--     union all
--     select 'ctadeptrame' col, ctadeptrame val from configuracion where ctadeptrame <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctadeptrame)
--     union all
--     select 'ctadeptramn' col, ctadeptramn val from configuracion where ctadeptramn <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctadeptramn)
--     union all
--     select 'ctaafet' col, ctaafet val from configuracion where ctaafet <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctaafet)
--     union all
--     select 'ctaivacrefime' col, ctaivacrefime val from configuracion where ctaivacrefime <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctaivacrefime)
--     union all
--     select 'ctaivacrefimn' col, ctaivacrefimn val from configuracion where ctaivacrefimn <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctaivacrefimn)
--     union all
--     select 'ctaivacrefitrmn' col, ctaivacrefitrmn val from configuracion where ctaivacrefitrmn <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctaivacrefitrmn)
--     union all
--     select 'ctaprovobu' col, ctaprovobu val from configuracion where ctaprovobu <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctaprovobu)
--     union all
--     select 'ctaalmme' col, ctaalmme val from configuracion where ctaalmme <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctaalmme)
--     union all
--     select 'ctatransalmme' col, ctatransalmme val from configuracion where ctatransalmme <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctatransalmme)
--     union all
--     select 'ctaalmmn' col, ctaalmmn val from configuracion where ctaalmmn <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctaalmmn)
--     union all
--     select 'ctatransalm1mn' col, ctatransalm1mn val from configuracion where ctatransalm1mn <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctatransalm1mn)
--     union all
--     select 'ctatransalm2mn' col, ctatransalm2mn val from configuracion where ctatransalm2mn <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctatransalm2mn)
--     union all
--     select 'ctatransalmmn' col, ctatransalmmn val from configuracion where ctatransalmmn <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctatransalmmn)
--     union all
--     select 'iue_ret' col, iue_ret val from configuracion where iue_ret <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.iue_ret)
--     union all
--     select 'it_ret' col, it_ret val from configuracion where it_ret <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.it_ret)
--     union all
--     select 'ctaCostPT' col, ctaCostPT val from configuracion where ctaCostPT <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctaCostPT)
--     union all
--     select 'ctaAlmPT' col, ctaAlmPT val from configuracion where ctaAlmPT <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctaAlmPT)
--     union all
--     select 'ctaAlmPTAG' col, ctaAlmPTAG val from configuracion where ctaAlmPTAG <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctaAlmPTAG)
--     union all
--     select 'ctaCostPV' col, ctaCostPV val from configuracion where ctaCostPV <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctaCostPV)
--     union all
--     select 'ctaAlmPV' col, ctaAlmPV val from configuracion where ctaAlmPV <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctaAlmPV)
--     union all
--     select 'ctaMerma' col, ctaMerma val from configuracion where ctaMerma <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctaMerma)
--     union all
--     select 'ctaProm' col, ctaProm val from configuracion where ctaProm <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctaProm)
--     union all
--     select 'ctaMermaBaj' col, ctaMermaBaj val from configuracion where ctaMermaBaj <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctaMermaBaj)
--     union all
--     select 'ctaReproc' col, ctaReproc val from configuracion where ctaReproc <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctaReproc)
--     union all
--     select 'ct_cajaahorro' col, ct_cajaahorro val from configuracion where ct_cajaahorro <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ct_cajaahorro)
--     union all
--     select 'ct_cajaveter' col, ct_cajaveter val from configuracion where ct_cajaveter <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ct_cajaveter)
--     union all
--     select 'cajagral1mn' col, cajagral1mn val from configuracion where cajagral1mn <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.cajagral1mn)
--     union all
--     select 'i_pvig_pf_mn' col, i_pvig_pf_mn val from configuracion where i_pvig_pf_mn <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.i_pvig_pf_mn)
--     union all
--     select 'ctaprovaf' col, ctaprovaf val from configuracion where ctaprovaf <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctaprovaf)
--     union all
--     select 'ctaG_it' col, ctaG_it val from configuracion where ctaG_it <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctaG_it)
--     union all
--     select 'ctaP_debFisIva' col, ctaP_debFisIva val from configuracion where ctaP_debFisIva <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctaP_debFisIva)
--     union all
--     select 'ctaP_itxpagar' col, ctaP_itxpagar val from configuracion where ctaP_itxpagar <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctaP_itxpagar)
--     union all
--     select 'ctaI_ventapri' col, ctaI_ventapri val from configuracion where ctaI_ventapri <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctaI_ventapri)
--     union all
--     select 'ctaI_ventasec' col, ctaI_ventasec val from configuracion where ctaI_ventasec <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctaI_ventasec)
--     union all
--     select 'ctacomision' col, ctacomision val from configuracion where ctacomision <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.ctacomision)
--     union all
--     select 'cxp_provmn' col, cxp_provmn val from configuracion where cxp_provmn <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.cxp_provmn)
--     union all
--     select 'cta_pat01' col, cta_pat01 val from configuracion where cta_pat01 <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.cta_pat01)
--     union all
--     select 'cta_pat02' col, cta_pat02 val from configuracion where cta_pat02 <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.cta_pat02)
--     union all
--     select 'cta_pat03' col, cta_pat03 val from configuracion where cta_pat03 <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.cta_pat03)
--     union all
--     select 'cta_pat04' col, cta_pat04 val from configuracion where cta_pat04 <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.cta_pat04)
--     union all
--     select 'cta_pat05' col, cta_pat05 val from configuracion where cta_pat05 <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.cta_pat05)
--     union all
--     select 'cxp_iva' col, cxp_iva val from configuracion where cxp_iva <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.cxp_iva)
--     union all
--     select 'cxp_regalia' col, cxp_regalia val from configuracion where cxp_regalia <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.cxp_regalia)
--     union all
--     select 'cxp_cns' col, cxp_cns val from configuracion where cxp_cns <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.cxp_cns)
--     union all
--     select 'res_perdida' col, res_perdida val from configuracion where res_perdida <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.res_perdida)
--     union all
--     select 'res_utilidad' col, res_utilidad val from configuracion where res_utilidad <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.res_utilidad)
--     union all
--     select 'oc_pagodefault' col, oc_pagodefault val from configuracion where oc_pagodefault <> '' and not exists (select 1 from arcgms p where p.cuenta = configuracion.oc_pagodefault)
--   ) t where val is not null;

-- 2) Alinear collation con arcgms.cuenta (utf8mb3_bin): estas 34 heredan
--    utf8mb3_general_ci de la tabla; las otras 17 ya estan en utf8mb3_bin.

ALTER TABLE configuracion MODIFY cajagral1mn varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY ct_cajaahorro varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY ct_cajaveter varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY cta_pat01 varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY cta_pat02 varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY cta_pat03 varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY cta_pat04 varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY cta_pat05 varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY ctaAlmPT varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY ctaAlmPTAG varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY ctaAlmPV varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY ctacomision varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY ctaCostPT varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY ctaCostPV varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY ctaG_it varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY ctaI_ventapri varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY ctaI_ventasec varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY ctaMerma varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY ctaMermaBaj varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY ctaP_debFisIva varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY ctaP_itxpagar varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY ctaProm varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY ctaprovaf varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY ctaReproc varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY cxp_cns varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY cxp_iva varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY cxp_provmn varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY cxp_regalia varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY i_pvig_pf_mn varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY it_ret varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY iue_ret varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY oc_pagodefault varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY res_perdida varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE configuracion MODIFY res_utilidad varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;

-- 3) Crear las FK en un solo ALTER (una sola reconstruccion de la tabla).

ALTER TABLE configuracion
    ADD CONSTRAINT fk_configuracion_ctaaitb FOREIGN KEY (ctaaitb) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctaantprovme FOREIGN KEY (ctaantprovme) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctaantprovmn FOREIGN KEY (ctaantprovmn) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctadiftipcam FOREIGN KEY (ctadiftipcam) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctadeptrame FOREIGN KEY (ctadeptrame) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctadeptramn FOREIGN KEY (ctadeptramn) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctaafet FOREIGN KEY (ctaafet) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctaivacrefime FOREIGN KEY (ctaivacrefime) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctaivacrefimn FOREIGN KEY (ctaivacrefimn) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctaivacrefitrmn FOREIGN KEY (ctaivacrefitrmn) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctaprovobu FOREIGN KEY (ctaprovobu) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctaalmme FOREIGN KEY (ctaalmme) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctatransalmme FOREIGN KEY (ctatransalmme) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctaalmmn FOREIGN KEY (ctaalmmn) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctatransalm1mn FOREIGN KEY (ctatransalm1mn) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctatransalm2mn FOREIGN KEY (ctatransalm2mn) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctatransalmmn FOREIGN KEY (ctatransalmmn) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_iue_ret FOREIGN KEY (iue_ret) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_it_ret FOREIGN KEY (it_ret) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctacostpt FOREIGN KEY (ctaCostPT) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctaalmpt FOREIGN KEY (ctaAlmPT) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctaalmptag FOREIGN KEY (ctaAlmPTAG) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctacostpv FOREIGN KEY (ctaCostPV) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctaalmpv FOREIGN KEY (ctaAlmPV) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctamerma FOREIGN KEY (ctaMerma) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctaprom FOREIGN KEY (ctaProm) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctamermabaj FOREIGN KEY (ctaMermaBaj) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctareproc FOREIGN KEY (ctaReproc) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ct_cajaahorro FOREIGN KEY (ct_cajaahorro) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ct_cajaveter FOREIGN KEY (ct_cajaveter) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_cajagral1mn FOREIGN KEY (cajagral1mn) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_i_pvig_pf_mn FOREIGN KEY (i_pvig_pf_mn) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctaprovaf FOREIGN KEY (ctaprovaf) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctag_it FOREIGN KEY (ctaG_it) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctap_debfisiva FOREIGN KEY (ctaP_debFisIva) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctap_itxpagar FOREIGN KEY (ctaP_itxpagar) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctai_ventapri FOREIGN KEY (ctaI_ventapri) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctai_ventasec FOREIGN KEY (ctaI_ventasec) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_ctacomision FOREIGN KEY (ctacomision) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_cxp_provmn FOREIGN KEY (cxp_provmn) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_cta_pat01 FOREIGN KEY (cta_pat01) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_cta_pat02 FOREIGN KEY (cta_pat02) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_cta_pat03 FOREIGN KEY (cta_pat03) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_cta_pat04 FOREIGN KEY (cta_pat04) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_cta_pat05 FOREIGN KEY (cta_pat05) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_cxp_iva FOREIGN KEY (cxp_iva) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_cxp_regalia FOREIGN KEY (cxp_regalia) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_cxp_cns FOREIGN KEY (cxp_cns) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_res_perdida FOREIGN KEY (res_perdida) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_res_utilidad FOREIGN KEY (res_utilidad) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_configuracion_oc_pagodefault FOREIGN KEY (oc_pagodefault) REFERENCES arcgms (cuenta);

-- Verificacion: deben quedar 51 constraints.
--   select count(*) from information_schema.key_column_usage
--    where table_schema = database() and table_name = 'configuracion'
--      and referenced_table_name = 'arcgms';

-- ----------------------------------------------------------------------------
-- Permiso propio CASHACCOUNTRENAME para el boton "Renumerar" de
-- /finances/cashAccount.xhtml (cambia el codigo de cuenta, que es la PK).
-- Se separa de ACCOUNTINGPLAN/UPDATE: editar una cuenta no deberia habilitar
-- tambien renumerarla. El grant a roles se hace desde Administracion > Roles.
-- ----------------------------------------------------------------------------

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
INSERT INTO funcionalidad VALUES
    (@nuevo_id, 'CASHACCOUNTRENAME', 'Renumerar cuenta contable (cambia el codigo)', 5, 4, 'Functionality.finances.cashAccountRename', 1);

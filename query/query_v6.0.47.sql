-- 30.01.2025
alter table configuracion add column res_perdida varchar(20);
alter table configuracion add column res_utilidad varchar(20);

-- Solo Para ILVA
update configuracion set res_perdida  = '3530100000';
update configuracion set res_utilidad = '3510100000';
--

-- Solo Para TERDEMOL, una vez
-- DELETE FROM arcgms 
WHERE cuenta IN ('3800000000', '3810000000', '3820000000', '3830000000', '3830100000');

INSERT INTO arcgms (cuenta, descri, cta_raiz, cta_niv3, cn_nivel, tipo, activa, no_cia, permiso_inv, exije_cc, moneda)
VALUES
('3800000000', 'RESULTADOS ACUMULADOS', '3800000000', NULL, 2, 'C', 'N', '01', 'N', 'N', 'P'),
('3810000000', 'Utilidades Acumuladas', '3800000000', '3810000000', 3, 'C', 'N', '01', 'N', 'N', 'P'),
('3810100000', 'Utilidades Acumuladas', '3800000000', '3810000000', 4, 'C', 'N', '01', 'N', 'N', 'P'),
('3820000000', 'UTILIDADES DEL PERIODO O GESTION', '3800000000', '3820000000', 3, 'C', 'N', '01', 'N', 'N', 'P'),
('3820100000', 'Utilidades del periodo o gestion', '3800000000', '3820000000', 4, 'C', 'N', '01', 'N', 'N', 'P'),
('3830000000', '(PERDIDAS ACUMULADAS)', '3800000000', '3830000000', 3, 'C', 'N', '01', 'N', 'N', 'P'),
('3830100000', '(PERDIDAS ACUMULADAS)', '3800000000', '3830000000', 4, 'C', 'N', '01', 'N', 'N', 'P'),
('3840000000', '(PERDIDAS DEL PERIODO O GESTION)', '3800000000', '3840000000', 3, 'C', 'N', '01', 'N', 'N', 'P'),
('3840100000', '(Perdidas del periodo o gestion)', '3800000000', '3840000000', 4, 'C', 'N', '01', 'N', 'N', 'P');

update configuracion set res_perdida  = '3830100000';
update configuracion set res_utilidad = '3810100000';
-- 

update arcgms set permite_iva = 'N' where permite_iva is null;
update arcgms set ind_mov = 'N' where ind_mov is null;

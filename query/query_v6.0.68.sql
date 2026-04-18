--
-- Mayor Contable agrupado por Tipo de Cuenta
INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
VALUES(448, 'MAJOR_ACCOUNTING_GROUPED', 5, 1, 'Functionality.accounting.majorAccountingGrouped', 1);

-- Flag de cuenta regularizadora (para calculo de saldo en Mayor Contable)
-- Usa convencion 'S'/'N' del StringBooleanUserType registrado en package-info.java
alter table arcgms add column ind_regulariz varchar(1) not null default 'N';

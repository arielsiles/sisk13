-- 14.03.2025
ALTER TABLE configuracion ADD COLUMN url_reversion_cancel_bill VARCHAR(300) AFTER url_ping;
UPDATE configuracion SET url_reversion_cancel_bill='http://10.0.0.106:8080/api/billing/reversion-cancel-bill' where no_cia = '01';

-- 15.03.2025
INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
VALUES(409 , 'REVERSION_CANCEL_BILL_SFE', 1, 1, 'Functionality.customers.reversionCancelBillSFE', 1);
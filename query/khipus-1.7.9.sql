-- 24062015: Ariel
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
VALUES (197, 'PURCHASEDOCUMENT', NULL, 5, 15, 'PurchaseDocument.button.add', 1);

INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
VALUES (198, 'DISCOUNTCOMMENT', NULL, 5, 15, 'DiscountComment.newDiscountComment', 1);

INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
VALUES (199, 'WAREHOUSEPURCHASEORDERLIQUIDATE', NULL, 5, 15, 'PurchaseOrder.liquidate', 1);

INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
VALUES (200, 'REMAKEPURCHASEORDERPAYMENT', NULL, 5, 15, 'REMAKEPURCHASEORDERPAYMENT', 1);
	      
ALTER TABLE cxp_lcompras MODIFY COLUMN no_auto VARCHAR(18);


gensecuencia:
SF_TMPENC_ANPRO_TIP_DOC
SF_TMPENC_AF_PAY_TIP_DOC

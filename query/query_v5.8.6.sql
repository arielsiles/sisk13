-- 29.11.2023
alter table acopiomp add column idempleado bigint(20) after idmetaproductoproduccion;
ALTER TABLE acopiomp ADD FOREIGN KEY (idempleado) REFERENCES empleado(idempleado);

INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
VALUES(285, 'RAWMATERIALPRODUCER_PRODUCTIVEZONE', 6, 1, 'Functionality.production.rawMaterialProducer.productiveZone', 1);

INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
VALUES(286, 'RAWMATERIALPRODUCER_BANKACCOUNT', 6, 1, 'Functionality.production.rawMaterialProducer.bankAccount', 1);

INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
VALUES(287, 'RAWMATERIALPRODUCER_PRODUCERTAXES', 6, 1, 'Functionality.production.rawMaterialProducer.producerTaxes', 1);
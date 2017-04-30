-- 20062015: Ariel, modificaciones para ordenes de compra

-- modify sf_entidades
ALTER TABLE sf_entidades MODIFY COLUMN cod_enti BIGINT(20) NOT NULL;
ALTER TABLE sf_entidades ADD PRIMARY KEY (cod_enti);
ALTER TABLE documentocobro MODIFY COLUMN identidad BIGINT(20);
ALTER TABLE documentocompra MODIFY COLUMN identidad BIGINT(20);
ALTER TABLE documentodescargo MODIFY COLUMN identidad BIGINT(20);
ALTER TABLE sf_tmpenc MODIFY COLUMN cod_enti BIGINT(20);

-- Inserts LUGARRECEPCION
INSERT INTO lugarrecepcion (idlugarrecepcion, codigo, nombre, VERSION, idunidadnegocio, idcompania)
VALUES (1, '1', 'ILVA PUNATA', 0, 470, 1);

INSERT INTO lugarrecepcion (idlugarrecepcion, codigo, nombre, VERSION, idunidadnegocio, idcompania)
VALUES (2, '2', 'ILVA CBBA', 0, 470, 1);

INSERT INTO lugarrecepcion (idlugarrecepcion, codigo, nombre, VERSION, idunidadnegocio, idcompania)
VALUES (3, '3', 'CISC PUNATA', 0, 470, 1);

-- Inserts condicionpago
INSERT INTO condicionpago (idcondicionpago, codigo, nombre, VERSION, idcompania) VALUES(1,'1','CONTADO','0','1');
INSERT INTO condicionpago (idcondicionpago, codigo, nombre, VERSION, idcompania) VALUES(2,'2','DEPOSITO','0','1');
INSERT INTO condicionpago (idcondicionpago, codigo, nombre, VERSION, idcompania) VALUES(3,'3','CREDITO','0','1');

-- Insert Insumos de Produccion
INSERT INTO inv_inventario (cod_art, no_cia, cod_alm, saldo_uni, VERSION )
SELECT cod_art, no_cia, 1, 0.00, 0
FROM inv_articulos
WHERE cod_gru = 2 
;

-- Insert Materiales de Produccion
INSERT INTO inv_inventario (cod_art, no_cia, cod_alm, saldo_uni, VERSION )
SELECT cod_art, no_cia, 3, 0.00, 0
FROM inv_articulos
WHERE cod_gru = 3
;

INSERT INTO gensecuencia (idgensecuencia, nombre, valor, idcompania) VALUES (3, 'WAREHOUSE_PRODUCT_ITEM_SEQUENCE', 183, 1);

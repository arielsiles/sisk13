ALTER TABLE registropagomateriaprima ADD ga DECIMAL(16,2) NULL AFTER descuentoreserva;

UPDATE registropagomateriaprima SET ga = 0;

ALTER TABLE planillapagomateriaprima ADD totalga DECIMAL(16,2) NULL AFTER totaldescuentoreserva;

UPDATE planillapagomateriaprima SET totalga = 0;


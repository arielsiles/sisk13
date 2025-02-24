-- 23.02.2025
CREATE TABLE detalleanalitica (
    iddetalleanalitica BIGINT NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    iddestino BIGINT NOT NULL,
    idcompania BIGINT NOT NULL,
    version BIGINT NOT NULL,
    PRIMARY KEY (iddetalleanalitica),
    FOREIGN KEY (iddestino) REFERENCES inv_destino(iddestino),
    FOREIGN KEY (idcompania) REFERENCES compania(idcompania)
) ENGINE=InnoDB;

ALTER TABLE inv_vales ADD COLUMN iddetalleanalitica BIGINT AFTER cod_prod;
ALTER TABLE inv_vales ADD FOREIGN KEY (iddetalleanalitica) REFERENCES detalleanalitica(iddetalleanalitica);
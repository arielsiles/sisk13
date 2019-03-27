/** 27.03.2019 **/
CREATE TABLE inv_periodo (
	id_inv_periodo BIGINT(20) NOT NULL,
	codart VARCHAR(6) NOT NULL,
	saldofis DECIMAL(12,2) NOT NULL,
	saldoval DECIMAL(12,2) NOT NULL,
	costouni DECIMAL(12,2) NOT NULL,
	mes INT(11) NOT NULL,
	gestion INT(11) NOT NULL,
	PRIMARY KEY (idinvperiodo)
);

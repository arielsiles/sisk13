/** 01.02.2018 **/
DELETE FROM inv_inventario WHERE cod_art = 115 AND cod_alm = 1;
DELETE FROM inv_inventario WHERE cod_art = 105 AND cod_alm = 1;
DELETE FROM inv_inventario WHERE cod_art = 112 AND cod_alm = 1;
DELETE FROM inv_inventario WHERE cod_art = 79 AND cod_alm = 1;
DELETE FROM inv_inventario WHERE cod_art = 45 AND cod_alm = 3;
DELETE FROM inv_inventario WHERE cod_art = 31 AND cod_alm = 3;
DELETE FROM inv_inventario WHERE cod_art = 34 AND cod_alm = 3;

DELETE FROM inv_inventario_detalle WHERE cod_art = 115 AND cod_alm = 1;
DELETE FROM inv_inventario_detalle WHERE cod_art = 105 AND cod_alm = 1;
DELETE FROM inv_inventario_detalle WHERE cod_art = 112 AND cod_alm = 1;
DELETE FROM inv_inventario_detalle WHERE cod_art = 79 AND cod_alm = 1;
DELETE FROM inv_inventario_detalle WHERE cod_art = 45 AND cod_alm = 3;
DELETE FROM inv_inventario_detalle WHERE cod_art = 31 AND cod_alm = 3;
DELETE FROM inv_inventario_detalle WHERE cod_art = 34 AND cod_alm = 3;


-- SET @folio = 591;
-- insert into inv_inicio
SELECT (@folio := @folio + 1), i.`cod_art`, a.`descri`, 0, NULL, 3, 0, 2017, '01'
FROM inv_inventario i
JOIN inv_articulos a ON i.`cod_art` = a.`cod_art`
WHERE i.`cod_alm` = 3
AND i.`cod_art` NOT IN (
	SELECT i.`cod_art`
	FROM inv_inicio i
	WHERE i.`alm` = 3
);

-- SET @folio = 609;
-- INSERT INTO inv_inicio
SELECT (@folio := @folio + 1), i.`cod_art`, a.`descri`, 0, NULL, 1, 0, 2017, '01'
FROM inv_inventario i
JOIN inv_articulos a ON i.`cod_art` = a.`cod_art`
WHERE i.`cod_alm` = 1
AND i.`cod_art` NOT IN (
	SELECT i.`cod_art`
	FROM inv_inicio i
	WHERE i.`alm` = 1
);

UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	57	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	58	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	59	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	333.00	 , costo_uni = 	29.319173	 WHERE cod_art = 	60	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	61	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	62	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	63	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	840.00	 , costo_uni = 	29.264519	 WHERE cod_art = 	64	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	1420.00	 , costo_uni = 	29.490000	 WHERE cod_art = 	65	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	150.00	 , costo_uni = 	26.470000	 WHERE cod_art = 	66	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	67	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	28.00	 , costo_uni = 	37.170000	 WHERE cod_art = 	68	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	1498.68	 , costo_uni = 	29.319000	 WHERE cod_art = 	69	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	943.50	 , costo_uni = 	29.319000	 WHERE cod_art = 	70	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	33.150000	 WHERE cod_art = 	71	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	869.00	 , costo_uni = 	39.830000	 WHERE cod_art = 	72	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	1940.44	 , costo_uni = 	29.145000	 WHERE cod_art = 	73	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	1862.00	 , costo_uni = 	32.360000	 WHERE cod_art = 	74	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	600.00	 , costo_uni = 	32.390000	 WHERE cod_art = 	75	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	700.00	 , costo_uni = 	30.160000	 WHERE cod_art = 	76	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	40.00	 , costo_uni = 	32.360000	 WHERE cod_art = 	77	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	149000.00	 , costo_uni = 	0.988833	 WHERE cod_art = 	78	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	1887.19	 , costo_uni = 	29.319000	 WHERE cod_art = 	79	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	175.00	 , costo_uni = 	26.100000	 WHERE cod_art = 	80	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	396.55	 , costo_uni = 	19.966392	 WHERE cod_art = 	81	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	82	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	6545.00	 , costo_uni = 	0.904800	 WHERE cod_art = 	83	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	84	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	42.630000	 WHERE cod_art = 	85	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	24000.00	 , costo_uni = 	0.417500	 WHERE cod_art = 	86	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	2000.00	 , costo_uni = 	0.365542	 WHERE cod_art = 	87	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	36000.00	 , costo_uni = 	0.361276	 WHERE cod_art = 	88	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	21000.00	 , costo_uni = 	0.366195	 WHERE cod_art = 	89	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	25000.00	 , costo_uni = 	0.362692	 WHERE cod_art = 	90	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	91	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	1000.00	 , costo_uni = 	1.318485	 WHERE cod_art = 	92	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.374100	 WHERE cod_art = 	93	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	16000.00	 , costo_uni = 	0.259526	 WHERE cod_art = 	94	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	26589.00	 , costo_uni = 	0.106333	 WHERE cod_art = 	95	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	3736.00	 , costo_uni = 	1.279944	 WHERE cod_art = 	96	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	157759.00	 , costo_uni = 	0.530000	 WHERE cod_art = 	97	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	76860.00	 , costo_uni = 	1.710000	 WHERE cod_art = 	98	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	42.00	 , costo_uni = 	34.136823	 WHERE cod_art = 	99	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	100	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	101	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	102	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	9600.00	 , costo_uni = 	0.281663	 WHERE cod_art = 	103	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.170000	 WHERE cod_art = 	104	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	10.529896	 WHERE cod_art = 	105	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	106	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	107	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	2000.00	 , costo_uni = 	0.174000	 WHERE cod_art = 	108	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	210.00	 , costo_uni = 	7.210000	 WHERE cod_art = 	109	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	110	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	111	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	38.00	 , costo_uni = 	26.970000	 WHERE cod_art = 	112	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	1.790000	 WHERE cod_art = 	113	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	114	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	425.00	 , costo_uni = 	9.005550	 WHERE cod_art = 	115	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	780.00	 , costo_uni = 	9.181846	 WHERE cod_art = 	116	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	76.00	 , costo_uni = 	276.280000	 WHERE cod_art = 	117	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	188	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	209.30	 , costo_uni = 	20.010000	 WHERE cod_art = 	479	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	59.58	 , costo_uni = 	28.832687	 WHERE cod_art = 	480	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	1100.00	 , costo_uni = 	28.710000	 WHERE cod_art = 	481	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	29.145000	 WHERE cod_art = 	482	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	433.00	 , costo_uni = 	32.202400	 WHERE cod_art = 	485	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	851.97	 , costo_uni = 	29.319000	 WHERE cod_art = 	526	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	4627.00	 , costo_uni = 	0.205597	 WHERE cod_art = 	529	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	703.00	 , costo_uni = 	1.638500	 WHERE cod_art = 	530	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.374100	 WHERE cod_art = 	531	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	1.305000	 WHERE cod_art = 	552	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.279063	 WHERE cod_art = 	553	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	1600.00	 , costo_uni = 	1.365900	 WHERE cod_art = 	560	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.312751	 WHERE cod_art = 	568	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	240.00	 , costo_uni = 	1.058500	 WHERE cod_art = 	572	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	1.620375	 WHERE cod_art = 	573	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.281663	 WHERE cod_art = 	574	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	418.122000	 WHERE cod_art = 	580	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	24.523560	 WHERE cod_art = 	581	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	24.523560	 WHERE cod_art = 	582	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	24.523560	 WHERE cod_art = 	583	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	25.129080	 WHERE cod_art = 	584	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	1392.000000	 WHERE cod_art = 	587	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	225.330000	 WHERE cod_art = 	596	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	38.044333	 WHERE cod_art = 	599	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	65.214286	 WHERE cod_art = 	600	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.957000	 WHERE cod_art = 	613	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	2.088870	 WHERE cod_art = 	645	 AND gestion = 2017;




UPDATE inv_inicio SET cantidad = 	65.94	 , costo_uni = 	11.699502	 WHERE cod_art = 	2	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	244.210000	 WHERE cod_art = 	3	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.081780	 WHERE cod_art = 	4	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.090000	 WHERE cod_art = 	5	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.110000	 WHERE cod_art = 	6	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	7	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.168345	 WHERE cod_art = 	8	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.280000	 WHERE cod_art = 	9	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	10	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	5000.00	 , costo_uni = 	0.120000	 WHERE cod_art = 	11	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	40000.00	 , costo_uni = 	0.102985	 WHERE cod_art = 	12	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	10000.00	 , costo_uni = 	0.176742	 WHERE cod_art = 	13	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	20000.00	 , costo_uni = 	0.138355	 WHERE cod_art = 	14	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	5000.00	 , costo_uni = 	0.126150	 WHERE cod_art = 	15	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	20000.00	 , costo_uni = 	0.170000	 WHERE cod_art = 	16	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.147900	 WHERE cod_art = 	17	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	16.26	 , costo_uni = 	0.414037	 WHERE cod_art = 	18	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	19	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	27.28	 , costo_uni = 	417.922215	 WHERE cod_art = 	20	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	74.00	 , costo_uni = 	61.740541	 WHERE cod_art = 	21	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	880.00	 , costo_uni = 	6.000285	 WHERE cod_art = 	22	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	23	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	14.355000	 WHERE cod_art = 	24	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	25	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	26	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	27	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	4607.44	 , costo_uni = 	0.537324	 WHERE cod_art = 	28	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	90.00	 , costo_uni = 	13.434279	 WHERE cod_art = 	29	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	23.40	 , costo_uni = 	200.000000	 WHERE cod_art = 	30	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	215.01	 , costo_uni = 	45.240000	 WHERE cod_art = 	31	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	32.625000	 WHERE cod_art = 	32	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	525.90	 , costo_uni = 	8.535558	 WHERE cod_art = 	33	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	38000.00	 , costo_uni = 	0.007830	 WHERE cod_art = 	34	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	3.782716	 WHERE cod_art = 	35	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	250.00	 , costo_uni = 	77.203800	 WHERE cod_art = 	36	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	37	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	108.00	 , costo_uni = 	102.660000	 WHERE cod_art = 	38	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	25.00	 , costo_uni = 	87.800000	 WHERE cod_art = 	39	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	99.17	 , costo_uni = 	63.814873	 WHERE cod_art = 	40	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	97998.00	 , costo_uni = 	0.021283	 WHERE cod_art = 	41	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	1000.00	 , costo_uni = 	1.618218	 WHERE cod_art = 	42	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	1.00	 , costo_uni = 	1574.700000	 WHERE cod_art = 	43	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	1000.00	 , costo_uni = 	2.407115	 WHERE cod_art = 	44	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	43.00	 , costo_uni = 	27.342075	 WHERE cod_art = 	45	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	61.20	 , costo_uni = 	39.590000	 WHERE cod_art = 	46	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	47	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	48	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	6000.00	 , costo_uni = 	0.067920	 WHERE cod_art = 	49	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.200000	 WHERE cod_art = 	50	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	20000.00	 , costo_uni = 	0.139571	 WHERE cod_art = 	51	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	10000.00	 , costo_uni = 	0.143550	 WHERE cod_art = 	52	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	500.00	 , costo_uni = 	0.130000	 WHERE cod_art = 	53	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.100000	 WHERE cod_art = 	54	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	1.30	 , costo_uni = 	669.124204	 WHERE cod_art = 	55	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	70.76	 , costo_uni = 	87.000000	 WHERE cod_art = 	56	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	152	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	0.000000	 WHERE cod_art = 	162	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	1176.240000	 WHERE cod_art = 	525	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	125.00	 , costo_uni = 	68.643000	 WHERE cod_art = 	546	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	133.110000	 WHERE cod_art = 	547	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	1.604280	 WHERE cod_art = 	576	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	314.870400	 WHERE cod_art = 	604	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	31.320000	 WHERE cod_art = 	605	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	133.458000	 WHERE cod_art = 	614	 AND gestion = 2017;
UPDATE inv_inicio SET cantidad = 	0.00	 , costo_uni = 	11.310000	 WHERE cod_art = 	635	 AND gestion = 2017;

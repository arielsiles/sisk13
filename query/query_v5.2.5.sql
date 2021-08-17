-- 15.08.2021
-- EJEC EN ILVA HASTA ...
UPDATE pedidos p SET p.`IDTIPOPEDIDO` = 8
WHERE p.`FECHA_ENTREGA` BETWEEN '2021-04-01' AND '2021-07-31'
AND p.`IDCLIENTE` = 2238
AND p.`IDTIPOPEDIDO` = 1
;

ALTER TABLE configuracion ADD COLUMN ctaAlmPTAG VARCHAR(20) AFTER ctaAlmPT;
UPDATE configuracion SET ctaAlmPTAG = '1510110203';

ALTER TABLE inv_vales ADD COLUMN idpedidostr BIGINT(20);
ALTER TABLE inv_vales ADD FOREIGN KEY (idpedidostr) REFERENCES pedidos(IDPEDIDOS);

-- Uniendo dos pedidos 4598 -> 4537
UPDATE articulos_pedido SET idpedidos = 	62471	 WHERE IDARTICULOSPEDIDO = 	468044	;
UPDATE articulos_pedido SET idpedidos = 	62471	 WHERE IDARTICULOSPEDIDO = 	468045	;
UPDATE articulos_pedido SET idpedidos = 	62471	 WHERE IDARTICULOSPEDIDO = 	468046	;
UPDATE articulos_pedido SET idpedidos = 	62471	 WHERE IDARTICULOSPEDIDO = 	468047	;

UPDATE pedidos p SET p.`ESTADO` = 'ANULADO' WHERE p.`IDPEDIDOS` = 62561;
UPDATE pedidos SET totalimporte = 1690.50, impuesto = 219.77 WHERE idpedidos = 62471;

UPDATE inv_vales SET idpedidostr = 	62230	 WHERE no_trans = 	28554	;
UPDATE inv_vales SET idpedidostr = 	62409	 WHERE no_trans = 	28555	;
UPDATE inv_vales SET idpedidostr = 	62410	 WHERE no_trans = 	28556	;
UPDATE inv_vales SET idpedidostr = 	62471	 WHERE no_trans = 	28557	;
UPDATE inv_vales SET idpedidostr = 	62674	 WHERE no_trans = 	28558	;
UPDATE inv_vales SET idpedidostr = 	62744	 WHERE no_trans = 	28559	;
UPDATE inv_vales SET idpedidostr = 	62766	 WHERE no_trans = 	28560	;
UPDATE inv_vales SET idpedidostr = 	62956	 WHERE no_trans = 	28561	;
UPDATE inv_vales SET idpedidostr = 	63053	 WHERE no_trans = 	28562	;
UPDATE inv_vales SET idpedidostr = 	63083	 WHERE no_trans = 	28563	;
UPDATE inv_vales SET idpedidostr = 	63439	 WHERE no_trans = 	28564	;
UPDATE inv_vales SET idpedidostr = 	63503	 WHERE no_trans = 	28565	;
UPDATE inv_vales SET idpedidostr = 	63516	 WHERE no_trans = 	28566	;
UPDATE inv_vales SET idpedidostr = 	63525	 WHERE no_trans = 	28567	;
UPDATE inv_vales SET idpedidostr = 	63530	 WHERE no_trans = 	28568	;
UPDATE inv_vales SET idpedidostr = 	63603	 WHERE no_trans = 	28569	;
UPDATE inv_vales SET idpedidostr = 	63703	 WHERE no_trans = 	28570	;
UPDATE inv_vales SET idpedidostr = 	63870	 WHERE no_trans = 	28571	;
UPDATE inv_vales SET idpedidostr = 	63878	 WHERE no_trans = 	28572	;
UPDATE inv_vales SET idpedidostr = 	63948	 WHERE no_trans = 	28573	;
UPDATE inv_vales SET idpedidostr = 	64011	 WHERE no_trans = 	28574	;
UPDATE inv_vales SET idpedidostr = 	64045	 WHERE no_trans = 	28575	;
UPDATE inv_vales SET idpedidostr = 	64090	 WHERE no_trans = 	28576	;
UPDATE inv_vales SET idpedidostr = 	64102	 WHERE no_trans = 	28577	;
UPDATE inv_vales SET idpedidostr = 	64193	 WHERE no_trans = 	28578	;
UPDATE inv_vales SET idpedidostr = 	64386	 WHERE no_trans = 	28579	;
UPDATE inv_vales SET idpedidostr = 	64465	 WHERE no_trans = 	28580	;
UPDATE inv_vales SET idpedidostr = 	64478	 WHERE no_trans = 	28581	;
UPDATE inv_vales SET idpedidostr = 	64631	 WHERE no_trans = 	28582	;
UPDATE inv_vales SET idpedidostr = 	64707	 WHERE no_trans = 	28583	;
UPDATE inv_vales SET idpedidostr = 	64872	 WHERE no_trans = 	28584	;
UPDATE inv_vales SET idpedidostr = 	64964	 WHERE no_trans = 	28585	;
UPDATE inv_vales SET idpedidostr = 	65017	 WHERE no_trans = 	28586	;
UPDATE inv_vales SET idpedidostr = 	65082	 WHERE no_trans = 	28587	;
UPDATE inv_vales SET idpedidostr = 	65255	 WHERE no_trans = 	28588	;
UPDATE inv_vales SET idpedidostr = 	65276	 WHERE no_trans = 	28589	;
UPDATE inv_vales SET idpedidostr = 	65278	 WHERE no_trans = 	28590	;
UPDATE inv_vales SET idpedidostr = 	65427	 WHERE no_trans = 	28591	;
UPDATE inv_vales SET idpedidostr = 	65593	 WHERE no_trans = 	28592	;
UPDATE inv_vales SET idpedidostr = 	65658	 WHERE no_trans = 	28593	;
UPDATE inv_vales SET idpedidostr = 	65757	 WHERE no_trans = 	28594	;
UPDATE inv_vales SET idpedidostr = 	65799	 WHERE no_trans = 	28595	;
UPDATE inv_vales SET idpedidostr = 	65803	 WHERE no_trans = 	28596	;
UPDATE inv_vales SET idpedidostr = 	65957	 WHERE no_trans = 	28597	;
UPDATE inv_vales SET idpedidostr = 	66028	 WHERE no_trans = 	28598	;
UPDATE inv_vales SET idpedidostr = 	66055	 WHERE no_trans = 	28599	;
UPDATE inv_vales SET idpedidostr = 	66187	 WHERE no_trans = 	28600	;
UPDATE inv_vales SET idpedidostr = 	66189	 WHERE no_trans = 	28601	;
UPDATE inv_vales SET idpedidostr = 	66390	 WHERE no_trans = 	28602	;
UPDATE inv_vales SET idpedidostr = 	66432	 WHERE no_trans = 	28603	;
UPDATE inv_vales SET idpedidostr = 	66521	 WHERE no_trans = 	28604	;
UPDATE inv_vales SET idpedidostr = 	66522	 WHERE no_trans = 	28605	;
UPDATE inv_vales SET idpedidostr = 	66534	 WHERE no_trans = 	28606	;
UPDATE inv_vales SET idpedidostr = 	66546	 WHERE no_trans = 	28607	;
UPDATE inv_vales SET idpedidostr = 	66715	 WHERE no_trans = 	28608	;
UPDATE inv_vales SET idpedidostr = 	66747	 WHERE no_trans = 	28609	;
UPDATE inv_vales SET idpedidostr = 	66803	 WHERE no_trans = 	28610	;
UPDATE inv_vales SET idpedidostr = 	66977	 WHERE no_trans = 	28611	;
UPDATE inv_vales SET idpedidostr = 	67159	 WHERE no_trans = 	28612	;
UPDATE inv_vales SET idpedidostr = 	67161	 WHERE no_trans = 	28613	;
UPDATE inv_vales SET idpedidostr = 	67364	 WHERE no_trans = 	28618	;
UPDATE inv_vales SET idpedidostr = 	67503	 WHERE no_trans = 	28664	;
UPDATE inv_vales SET idpedidostr = 	67598	 WHERE no_trans = 	28683	;
UPDATE inv_vales SET idpedidostr = 	67618	 WHERE no_trans = 	28691	;
UPDATE inv_vales SET idpedidostr = 	67771	 WHERE no_trans = 	28723	;
UPDATE inv_vales SET idpedidostr = 	68026	 WHERE no_trans = 	28795	;
UPDATE inv_vales SET idpedidostr = 	68138	 WHERE no_trans = 	28826	;
UPDATE inv_vales SET idpedidostr = 	68144	 WHERE no_trans = 	28827	;
UPDATE inv_vales SET idpedidostr = 	68170	 WHERE no_trans = 	28828	;
UPDATE inv_vales SET idpedidostr = 	68278	 WHERE no_trans = 	28869	;
UPDATE inv_vales SET idpedidostr = 	68422	 WHERE no_trans = 	28921	;
UPDATE inv_vales SET idpedidostr = 	68462	 WHERE no_trans = 	28936	;
UPDATE inv_vales SET idpedidostr = 	68575	 WHERE no_trans = 	28982	;
UPDATE inv_vales SET idpedidostr = 	68682	 WHERE no_trans = 	29010	;
UPDATE inv_vales SET idpedidostr = 	68752	 WHERE no_trans = 	29022	;
UPDATE inv_vales SET idpedidostr = 	68959	 WHERE no_trans = 	29093	;
UPDATE inv_vales SET idpedidostr = 	69144	 WHERE no_trans = 	29124	;

-- 
ALTER TABLE inv_articulos ADD COLUMN codeq VARCHAR(6) AFTER cod_art;

UPDATE inv_articulos SET codeq = 	1400	 WHERE cod_art = 	667	;
UPDATE inv_articulos SET codeq = 	1399	 WHERE cod_art = 	119	;
UPDATE inv_articulos SET codeq = 	1401	 WHERE cod_art = 	589	;
UPDATE inv_articulos SET codeq = 	1402	 WHERE cod_art = 	588	;
UPDATE inv_articulos SET codeq = 	1403	 WHERE cod_art = 	606	;
UPDATE inv_articulos SET codeq = 	1404	 WHERE cod_art = 	594	;
UPDATE inv_articulos SET codeq = 	1398	 WHERE cod_art = 	118	;
UPDATE inv_articulos SET codeq = 	1421	 WHERE cod_art = 	125	;
UPDATE inv_articulos SET codeq = 	1422	 WHERE cod_art = 	854	;
UPDATE inv_articulos SET codeq = 	1416	 WHERE cod_art = 	148	;
UPDATE inv_articulos SET codeq = 	1417	 WHERE cod_art = 	150	;
UPDATE inv_articulos SET codeq = 	1433	 WHERE cod_art = 	151	;
UPDATE inv_articulos SET codeq = 	1420	 WHERE cod_art = 	705	;
UPDATE inv_articulos SET codeq = 	1419	 WHERE cod_art = 	704	;
UPDATE inv_articulos SET codeq = 	1418	 WHERE cod_art = 	703	;
UPDATE inv_articulos SET codeq = 	1414	 WHERE cod_art = 	808	;
UPDATE inv_articulos SET codeq = 	1415	 WHERE cod_art = 	135	;
UPDATE inv_articulos SET codeq = 	1409	 WHERE cod_art = 	128	;
UPDATE inv_articulos SET codeq = 	1407	 WHERE cod_art = 	129	;
UPDATE inv_articulos SET codeq = 	1406	 WHERE cod_art = 	130	;
UPDATE inv_articulos SET codeq = 	1405	 WHERE cod_art = 	131	;
UPDATE inv_articulos SET codeq = 	1408	 WHERE cod_art = 	132	;
UPDATE inv_articulos SET codeq = 	1412	 WHERE cod_art = 	136	;
UPDATE inv_articulos SET codeq = 	1411	 WHERE cod_art = 	138	;
UPDATE inv_articulos SET codeq = 	1410	 WHERE cod_art = 	143	;

-- Actualizar vista 'VENTAS'
-- UPDATE inv_almacenes SET tipo = 'DAIRYAGENCY' WHERE COD_ALM = 8;

-- EJEC EN ILVA HASTA ACA
ALTER TABLE inv_almacenes ADD COLUMN ctacosto VARCHAR(20) AFTER cuenta;
UPDATE inv_almacenes i SET i.`ctacosto` = '4420110201' WHERE i.`cod_alm` = 2;
UPDATE inv_almacenes i SET i.`ctacosto` = '4420110201' WHERE i.`cod_alm` = 8;





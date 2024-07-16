-- 16.07.2024
--
delete from arcgms where cuenta = '3000000000';
delete from arcgms where cuenta = '3110050000';
delete from arcgms where cuenta = '3110050100';

update arcgms set cuenta = 	'3000000000'	 where cuenta = 	'3100000000';
update arcgms set cuenta = 	'3100000000'	 where cuenta = 	'3110000000';
update arcgms set cuenta = 	'3110000000'	 where cuenta = 	'3110010000';
update arcgms set cuenta = 	'3110100000'	 where cuenta = 	'3110010100';
update arcgms set cuenta = 	'3200000000'	 where cuenta = 	'3110020000';
update arcgms set cuenta = 	'3210000000'	 where cuenta = 	'3110020100';
update arcgms set cuenta = 	'3300000000'	 where cuenta = 	'3110030000';
update arcgms set cuenta = 	'3310000000'	 where cuenta = 	'3110030100';
update arcgms set cuenta = 	'3400000000'	 where cuenta = 	'3110040000';
update arcgms set cuenta = 	'3410000000'	 where cuenta = 	'3110040100';
update arcgms set cuenta = 	'3500000000'	 where cuenta = 	'3110040200';
update arcgms set cuenta = 	'3510000000'	 where cuenta = 	'3110040201';
update arcgms set cuenta = 	'3600000000'	 where cuenta = 	'3110060000';
update arcgms set cuenta = 	'3610000000'	 where cuenta = 	'3110060100';
update arcgms set cuenta = 	'3700000000'	 where cuenta = 	'3110070000';
update arcgms set cuenta = 	'3710000000'	 where cuenta = 	'3110070100';
update arcgms set cuenta = 	'3800000000'	 where cuenta = 	'3110080000';
update arcgms set cuenta = 	'3810000000'	 where cuenta = 	'3110080100';
update arcgms set cuenta = 	'3820000000'	 where cuenta = 	'3110080101';
update arcgms set cuenta = 	'3830000000'	 where cuenta = 	'3110080200';
update arcgms set cuenta = 	'3830100000'	 where cuenta = 	'3110080201';

update arcgms set descri = 	'PATRIMONIO'	 where cuenta = 	'3000000000';
update arcgms set descri = 	'CAPITAL SOCIAL'	 where cuenta = 	'3100000000';
update arcgms set descri = 	'CAPITAL SOCIAL PAGADO'	 where cuenta = 	'3110000000';
update arcgms set descri = 	'CAPITAL SOCIAL PAGADO'	 where cuenta = 	'3110100000';
update arcgms set descri = 	'APORTES POR CAPITALIZAR'	 where cuenta = 	'3200000000';
update arcgms set descri = 	'APORTES POR CAPITALIZAR'	 where cuenta = 	'3210000000';
update arcgms set descri = 	'RESERVAS POR REVALUOS TECNICOS'	 where cuenta = 	'3300000000';
update arcgms set descri = 	'RESERVAS POR REVALUOS TECNICOS'	 where cuenta = 	'3310000000';
update arcgms set descri = 	'RESERVAS'	 where cuenta = 	'3400000000';
update arcgms set descri = 	'RESERVA LEGAL'	 where cuenta = 	'3410000000';
update arcgms set descri = 	'AJUSTES AL PATRIMONIO'	 where cuenta = 	'3500000000';
update arcgms set descri = 	'AJUSTES AL PATRIMONIO'	 where cuenta = 	'3510000000';
update arcgms set descri = 	'AJUSTE DE CAPITAL'	 where cuenta = 	'3600000000';
update arcgms set descri = 	'AJUSTE DE CAPITAL'	 where cuenta = 	'3610000000';
update arcgms set descri = 	'APORTES NO CAPITALIZABLES'	 where cuenta = 	'3700000000';
update arcgms set descri = 	'APORTES NO CAPITALIZABLES'	 where cuenta = 	'3710000000';
update arcgms set descri = 	'RESULTADOS ACUMULADOS'	 where cuenta = 	'3800000000';
update arcgms set descri = 	'RESULTADOS ACUMULADOS'	 where cuenta = 	'3810000000';
update arcgms set descri = 	'RESULTADOS ACUMULADOS NO DISTRIBUIDAS '	 where cuenta = 	'3820000000';
update arcgms set descri = 	'RESULTADO DE LA GESTION'	 where cuenta = 	'3830000000';
update arcgms set descri = 	'RESULTADO DE LA GESTION'	 where cuenta = 	'3830100000';


update configuracion set cta_pat01 = '3100000000' where no_cia = '01';
update configuracion set cta_pat02 = '3200000000' where no_cia = '01';
update configuracion set cta_pat03 = '3300000000' where no_cia = '01';
update configuracion set cta_pat04 = '3400000000' where no_cia = '01';
update configuracion set cta_pat05 = '3500000000' where no_cia = '01';

update sf_tmpdet set cuenta = '3210000000' where id_tmpdet = 10980;
update sf_tmpdet set cuenta = '3210000000' where id_tmpdet = 10984;
update sf_tmpdet set cuenta = '3210000000' where id_tmpdet = 11192;

update arcgms set cta_raiz = 	'3000000000'	 where cuenta = 	'3000000000';
update arcgms set cta_raiz = 	'3100000000'	 where cuenta = 	'3100000000';
update arcgms set cta_raiz = 	'3100000000'	 where cuenta = 	'3110000000';
update arcgms set cta_raiz = 	'3100000000'	 where cuenta = 	'3110100000';
update arcgms set cta_raiz = 	'3200000000'	 where cuenta = 	'3200000000';
update arcgms set cta_raiz = 	'3200000000'	 where cuenta = 	'3210000000';
update arcgms set cta_raiz = 	'3300000000'	 where cuenta = 	'3300000000';
update arcgms set cta_raiz = 	'3300000000'	 where cuenta = 	'3310000000';
update arcgms set cta_raiz = 	'3400000000'	 where cuenta = 	'3400000000';
update arcgms set cta_raiz = 	'3400000000'	 where cuenta = 	'3410000000';
update arcgms set cta_raiz = 	'3500000000'	 where cuenta = 	'3500000000';
update arcgms set cta_raiz = 	'3500000000'	 where cuenta = 	'3510000000';
update arcgms set cta_raiz = 	'3600000000'	 where cuenta = 	'3600000000';
update arcgms set cta_raiz = 	'3600000000'	 where cuenta = 	'3610000000';
update arcgms set cta_raiz = 	'3700000000'	 where cuenta = 	'3700000000';
update arcgms set cta_raiz = 	'3700000000'	 where cuenta = 	'3710000000';
update arcgms set cta_raiz = 	'3800000000'	 where cuenta = 	'3800000000';
update arcgms set cta_raiz = 	'3800000000'	 where cuenta = 	'3810000000';
update arcgms set cta_raiz = 	'3800000000'	 where cuenta = 	'3820000000';
update arcgms set cta_raiz = 	'3800000000'	 where cuenta = 	'3830000000';
update arcgms set cta_raiz = 	'3800000000'	 where cuenta = 	'3830100000';

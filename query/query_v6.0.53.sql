-- 01.04.2025
alter table sf_tmpenc add column open int after descri;
alter table sf_tmpenc add column close int after open;

update sf_tmpenc set open = 0 where open is null;
update sf_tmpenc set close = 0 where close is null;

--
-- 14.04.2025
delete from costosindirectosconf where idcompania = 1;
update secuencia set  valor = 1 where tabla = 'costosindirectosconf';

update arcgms set cta_niv3 = 	'3110000000'	 , cn_nivel = 	5	 where cuenta = 	'3110100100';
update arcgms set cta_niv3 = 	'4110000000'	 , cn_nivel = 	5	 where cuenta = 	'4110010400';
update arcgms set cta_niv3 = 	'4150000000'	 , cn_nivel = 	5	 where cuenta = 	'4150040300';
update arcgms set cta_niv3 = 	'5330000000'	 , cn_nivel = 	5	 where cuenta = 	'5330020500';
update arcgms set cta_niv3 = 	'5340000000'	 , cn_nivel = 	5	 where cuenta = 	'5340030500';
update arcgms set cta_niv3 = 	'5420000000'	 , cn_nivel = 	4	 where cuenta = 	'5420170000';
update arcgms set cta_niv3 = 	'5420000000'	 , cn_nivel = 	5	 where cuenta = 	'5420170100';
update arcgms set cta_niv3 = 	'5510000000'	 , cn_nivel = 	5	 where cuenta = 	'5510011300';
update arcgms set cta_niv3 = 	'5510000000'	 , cn_nivel = 	5	 where cuenta = 	'5510030200';
update arcgms set cta_niv3 = 	'5510000000'	 , cn_nivel = 	5	 where cuenta = 	'5510090200';
update arcgms set cta_niv3 = 	'5510000000'	 , cn_nivel = 	4	 where cuenta = 	'5510470000';
update arcgms set cta_niv3 = 	'5510000000'	 , cn_nivel = 	5	 where cuenta = 	'5510470100';
update arcgms set cta_niv3 = 	'5530000000'	 , cn_nivel = 	4	 where cuenta = 	'5530020000';
update arcgms set cta_niv3 = 	'5530000000'	 , cn_nivel = 	5	 where cuenta = 	'5530020100';
update arcgms set cta_niv3 = 	'5560000000'	 , cn_nivel = 	4	 where cuenta = 	'5560100000';
update arcgms set cta_niv3 = 	'5560000000'	 , cn_nivel = 	5	 where cuenta = 	'5560100100';

update arcgms set cta_raiz = 	'4100000000'			 where cuenta = 	'4150050200';
update arcgms set cta_raiz = 	'4200000000'			 where cuenta = 	'4210010100';
update arcgms set cta_raiz = 	'5300000000'			 where cuenta = 	'5330020500';
update arcgms set cta_raiz = 	'5400000000'			 where cuenta = 	'5420170100';
update arcgms set cta_raiz = 	'5500000000'			 where cuenta = 	'5510470000';
update arcgms set cta_raiz = 	'5500000000'			 where cuenta = 	'5510470100';
update arcgms set cta_raiz = 	'5500000000'			 where cuenta = 	'5510500100';
update arcgms set cta_raiz = 	'5500000000'			 where cuenta = 	'5510600100';
update arcgms set cta_raiz = 	'5500000000'			 where cuenta = 	'5520050200';
update arcgms set cta_raiz = 	'5500000000'			 where cuenta = 	'5530020100';
update arcgms set cta_raiz = 	'5500000000'			 where cuenta = 	'5560100100';

update arcgms set cta_raiz = 	'5400000000'			 where cuenta = 	'5420170000';
update arcgms set cta_raiz = 	'5500000000'			 where cuenta = 	'5510500000';
update arcgms set cta_raiz = 	'5500000000'			 where cuenta = 	'5510600000';
update arcgms set cta_raiz = 	'5500000000'			 where cuenta = 	'5530020000';
update arcgms set cta_raiz = 	'5500000000'			 where cuenta = 	'5560100000';
update arcgms set cta_raiz = 	'4200000000'			 where cuenta = 	'4210010000';
update arcgms set cta_raiz = 	'4100000000'			 where cuenta = 	'4150040200';
update arcgms set cta_raiz = 	'4100000000'			 where cuenta = 	'4150040300';





/** 17.05.2019 **/
update SECUENCIA set VALOR=(select MAX(E.`idcostosindirectosconf`)+1 from costosindirectosconf E) where TABLA='COSTOSINDIRECTOSCONF';
update SECUENCIA set VALOR=(select MAX(z.`idcostosindirectos`)+1 from costosindirectos z) where TABLA='COSTOSINDIRECTOS';
update SECUENCIA set VALOR=(select MAX(z.`idperiodocostoindirecto`)+1 from periodocostoindirecto z) where TABLA='PERIODOCOSTOINDIRECTO';

/** 21.05.2019 **/
alter table pr_insumoformula add column idform bigint(20) null after idformula;
alter table pr_insumoformula add foreign key (idform) references pr_formula(idformula);
alter table pr_formula add column totaleq decimal(16,2) not null after estado;

/** 22.05.2019 **/
alter table pr_produccion add column costototal decimal(16,2) default 0 after estado;
alter table pr_produccion add column totalmp decimal(16,2) default 0 after costototal;

/** 24.05.2019 **/
alter table pr_producto add column costo decimal(16,2) default 0 after cantidad;

alter table inv_articulos add column med_pr varchar(6) after cod_med;
alter table inv_articulos add column cant_pr decimal(16, 2) after med_pr;

alter table pr_producto add column costo_a decimal(16, 2) default 0 after costo;
alter table pr_producto add column costo_b decimal(16, 2) default 0 after costo_a;
alter table pr_producto add column costo_c decimal(16, 2) default 0 after costo_b;

/** 27.05.2019 **/
alter table pr_producto add column costouni decimal(16, 2) default 0 after costo;

update inv_articulos set med_pr = 	'ML',	 cant_pr = 	120	 where cod_art = 	764	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	120	 where cod_art = 	763	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	160	 where cod_art = 	491	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	160	 where cod_art = 	492	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	100	 where cod_art = 	762	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	100	 where cod_art = 	760	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	100	 where cod_art = 	761	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	150	 where cod_art = 	121	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	160	 where cod_art = 	667	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	1000	 where cod_art = 	119	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	950	 where cod_art = 	161	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	0	 where cod_art = 	433	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	0	 where cod_art = 	541	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	120	 where cod_art = 	589	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	140	 where cod_art = 	144	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	150	 where cod_art = 	478	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	200	 where cod_art = 	189	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	120	 where cod_art = 	588	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	140	 where cod_art = 	146	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	200	 where cod_art = 	190	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	120	 where cod_art = 	606	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	200	 where cod_art = 	608	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	120	 where cod_art = 	594	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	200	 where cod_art = 	595	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	2000	 where cod_art = 	147	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	160	 where cod_art = 	145	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	160	 where cod_art = 	123	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	160	 where cod_art = 	122	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	946	 where cod_art = 	643	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	150	 where cod_art = 	124	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	1000	 where cod_art = 	486	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	950	 where cod_art = 	118	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	160	 where cod_art = 	597	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	160	 where cod_art = 	607	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	160	 where cod_art = 	585	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	160	 where cod_art = 	120	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	120	 where cod_art = 	127	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	150	 where cod_art = 	125	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	160	 where cod_art = 	494	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	1000	 where cod_art = 	126	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	150	 where cod_art = 	728	;
update inv_articulos set med_pr = 	'GR',	 cant_pr = 	500	 where cod_art = 	148	;
update inv_articulos set med_pr = 	'GR',	 cant_pr = 	200	 where cod_art = 	149	;
update inv_articulos set med_pr = 	'GR',	 cant_pr = 	500	 where cod_art = 	150	;
update inv_articulos set med_pr = 	'GR',	 cant_pr = 	500	 where cod_art = 	151	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	1000	 where cod_art = 	705	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	1000	 where cod_art = 	704	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	1000	 where cod_art = 	703	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	1000	 where cod_art = 	134	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	450	 where cod_art = 	135	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	1000	 where cod_art = 	669	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	1000	 where cod_art = 	668	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	1000	 where cod_art = 	642	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	2000	 where cod_art = 	128	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	2000	 where cod_art = 	129	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	2000	 where cod_art = 	130	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	120	 where cod_art = 	551	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	2000	 where cod_art = 	131	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	2000	 where cod_art = 	132	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	2000	 where cod_art = 	133	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	120	 where cod_art = 	157	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	140	 where cod_art = 	158	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	150	 where cod_art = 	159	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	500	 where cod_art = 	160	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	80	 where cod_art = 	136	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	120	 where cod_art = 	154	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	140	 where cod_art = 	156	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	150	 where cod_art = 	155	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	500	 where cod_art = 	137	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	80	 where cod_art = 	138	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	120	 where cod_art = 	139	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	140	 where cod_art = 	140	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	150	 where cod_art = 	141	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	500	 where cod_art = 	142	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	80	 where cod_art = 	143	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	140	 where cod_art = 	191	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	0	 where cod_art = 	182	;
update inv_articulos set med_pr = 	'ML',	 cant_pr = 	0	 where cod_art = 	183	;

/** 28.05.2019 **/
alter table periodocostoindirecto add column dist int(1) after mes;
alter table periodocostoindirecto drop column dia;
update periodocostoindirecto p set p.`dist` = 0;

alter table pr_plan add column estado varchar(5) after fecha;
update pr_plan p set p.`estado` = 'PEN';



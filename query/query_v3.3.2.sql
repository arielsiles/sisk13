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
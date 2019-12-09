/** 09.12.2019 **/
alter table descuentproductmateriaprima add column comision decimal(16,2) after otrosdescuentos;
update descuentproductmateriaprima  set comision = 0;
insert into `tipomovimientoproductor` values (7, 'BS', 'COMISION BANCO', 'A', 1);

alter table planillapagomateriaprima add column totalcomision decimal(16,2) after totadescuentosxgab;
update planillapagomateriaprima set totalcomision = 0;


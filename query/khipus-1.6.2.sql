--Fecha de creacion: 30/10/2014
--descripcion : CRUD RESERVAS PRODUCTORES LECHEROS
--permiso para crear un producto en base a otro
alter table PLANILLAPAGOMATERIAPRIMA add (IUE number(3,2) null, IT number(3,2) null);
alter table WISE.CONFIGURACION add (IUE NUMBER(3,2) NULL,IT NUMBER(3,2) NULL, PRECIOUNITARIOLECHE NUMBER(5,2));
update PLANILLAPAGOMATERIAPRIMA set iue = 5.0, it = 3.0, TASAIMPUESTO = 8.0;
update wise.configuracion set iue = 5.0, it = 3.0, preciounitarioleche = 3.0;
--commit
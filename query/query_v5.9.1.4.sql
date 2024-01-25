-- 25.01.2024
-- To Terdemol - Ejecutado
-- delete from territoriotrabajo where IDTERRITORIOTRABAJO >= 1;
-- delete from tipocliente where idtipocliente >= 1;
-- delete from precioarticulo where idprecioarticulo >= 1;
-- delete from categoriacliente where idcategoriacliente >= 1;
insert into `territoriotrabajo` (`idterritoriotrabajo`, `nombre`, `pais`, `descripcion`, `iddistribuidor`, `iddepartamento`) values('1','Cochabamba','Bolivia',null,null,'1');
insert into `tipocliente` (`idtipocliente`, `nombre`) values('1','Empresa o Institucion');
insert into `tipocliente` (`idtipocliente`, `nombre`) values('2','Gubernamental');
insert into `tipocliente` (`idtipocliente`, `nombre`) values('3','ONG');
insert into `tipocliente` (`idtipocliente`, `nombre`) values('20','Otros');
-- End
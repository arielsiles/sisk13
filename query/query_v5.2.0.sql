-- 27.04.2021
-- alter table categoriacliente add column idtipocaja bigint(20)after tipo;
-- alter table categoriacliente add foreign key (idtipocaja) references tipocaja (idtipocaja);

alter table tipocaja add column idcategoriacliente bigint(20)after no_cia;

-- Configuraciones para release
-- 0. Crear Almacen Agencia y asignar cuenta contable (1510110203 - Productos Agencia Central)
-- 1. Crear Categorias Cliente con los productos y precios para fijar como los mas vendidos
-- 1.1. En categoriacliente fijar tipo segun corresponde:
--	FACTORY_LIST *
--	AGENCY_LIST *
--	VET_LIST

-- 2. Relacionar en tipo de caja (tipocaja) con la categoriacliente q corresponde

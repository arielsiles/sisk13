-- Baja de permisos de los reportes eliminados del menu Procesos Productivos: MP utilizada en produccion, Produccion Total y Balance de produccion.

delete da from derechoacceso da
  join funcionalidad f on f.idfuncionalidad = da.idfuncionalidad
 where f.codigo in ('REPORTMILKRAW', 'REPORTPRODUCTPRODUCED', 'REPORTPRODUCCTIONBALANCE');

delete from funcionalidad
 where codigo in ('REPORTMILKRAW', 'REPORTPRODUCTPRODUCED', 'REPORTPRODUCCTIONBALANCE');

-- Se borra por codigo y no por id: REPORTMILKRAW esta duplicada en algunas bases (ids 163 y 412).

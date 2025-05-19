-- 
select *
from khipus.vmarcado v
where v.marperid = 5151362
and v.marfecha between '2020-01-01' and '2020-12-31'
;

select v.`idrhmarcado` as id, v.`marperid` as tarjeta,  v.`marfecha` as fecha, v.`marhora` as hora, v.`control`
from khipus.vmarcado v
where v.marperid = 5151362
  and v.`marfecha` between '2020-01-01' and '2020-12-31'
;

-- Reporte de marcaciones con reporte de control
SELECT r.`idreportecontrol`, r.`fecha`, bh.`horainicio`, bh.`horafin`, r.`marcinicio`, r.`marcfin`, r.`mindescuento`,
       e.`noidentificacion` as ci, p.`nombres`, p.`apellidopaterno`, p.`apellidomaterno`
FROM reportecontrol r
    LEFT JOIN bandahorariacontrato bc  ON r.`idbandahorariac` = bc.`idbandahorariacontrato`
    left join `bandahoraria` bh        on bc.`idbandahoraria` = bh.`idbandahoraria`
    LEFT JOIN contratopuesto cp        ON bc.`idcontratopuesto` = cp.`idcontratopuesto`
    LEFT JOIN contrato c               ON cp.`idcontrato` = c.`idcontrato`
    LEFT JOIN empleado em              ON c.`idempleado` = em.`idempleado`
    LEFT JOIN persona p                ON em.`idempleado` = p.`idpersona`
    LEFT JOIN entidad e                ON p.`idpersona` = e.`identidad`
    left join `planillagenerada` pg    on r.idplanillagenerada = pg.idplanillagenerada
WHERE r.`fecha` BETWEEN '2020-01-01' AND '2020-01-31'
and e.`noidentificacion` = 5151362
and pg.tipoplanillagen = 'OFFICIAL'
;


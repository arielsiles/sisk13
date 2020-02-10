/** 02.02.2020 **/
alter table sf_tmpenc add column idmovimiento bigint(24) null after fecha;
alter table sf_tmpenc add column fac int(11) null after idmovimiento;
alter table sf_tmpdet add column idmovimiento bigint(24) null after id_tmpenc;
alter table sf_tmpdet add foreign key (idmovimiento) references movimiento (idmovimiento);
--
alter table `com_encoc` add primary key (id_com_encoc);
alter table `com_detoc` add foreign key (id_com_encoc) references com_encoc (id_com_encoc);

alter table `inv_vales` add foreign key (id_com_encoc) references com_encoc (id_com_encoc);
update inv_vales i set i.`idtmpenc` = null where i.`no_trans` in (18295, 18293, 18291, 18258, 18308); -- Para ILVA
alter table `inv_vales` add foreign key (idtmpenc) references sf_tmpenc (id_tmpenc);
-- alter table inv_mov add foreign key (no_trans) references inv_vales (no_trans); -- err
alter table `inv_movdet` add primary key (id_inv_movdet);

alter table documentocompra add primary key (iddocumentocompra);
alter table `documentocompra` add foreign key (idtmpenc) references sf_tmpenc (id_tmpenc);
alter table `documentocompra` add foreign key (idordencompra) references com_encoc (id_com_encoc);

alter table `documentocontable` add primary key (iddocumentocontable);
update documentocompra d set d.`iddocumentocompra` = 1318 where d.`iddocumentocompra` = 5781; -- para ilva

alter table documentocompra add foreign key (iddocumentocompra) references documentocontable (iddocumentocontable);

alter table sf_tmpdet add foreign key (iddocumentocompra) references documentocompra (iddocumentocompra);



-- 


select d.`iddocumentocompra`, do.`iddocumentocontable`
from documentocompra d
left join documentocontable do on d.`iddocumentocompra` = do.`iddocumentocontable`
;

select do.`iddocumentocontable`, d.`iddocumentocompra`
from documentocontable do
left join documentocompra d on do.`iddocumentocontable` = d.`iddocumentocompra` 
;

select d.`iddocumentocompra`, d.`idtmpenc`, e.`id_tmpenc`
from documentocompra d
left join sf_tmpenc e on d.`idtmpenc` = e.`id_tmpenc`
where d.`idtmpenc` is not null
;


--
select i.`no_trans`, i.`idtmpenc`, e.`id_tmpenc`
from inv_vales i
left join sf_tmpenc e on i.`idtmpenc` = e.`id_tmpenc`
where i.`idtmpenc` is not null
;

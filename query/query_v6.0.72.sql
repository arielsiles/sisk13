-- ============================================================================
-- v6.0.72 :: Permisos para registro/aprobacion/anulacion de Documento de
--            Compra desde el modal en la pantalla de Orden de Compra.
-- ============================================================================
--
-- El flujo modal coexiste con el flujo de pagina completa actual y usa
-- permisos propios para que el administrador pueda habilitarlo
-- selectivamente.
-- ----------------------------------------------------------------------------

insert into funcionalidad
    (idfuncionalidad, permiso, codigo, descripcion, idmodulo, nombrerecurso, idcompania)
values
    (449, 1, 'PURCHASEDOCUMENTMODAL_REGISTER', null, 5,
        'menu.warehouse.purchaseDocument.modal.register', 1),
    (450, 1, 'PURCHASEDOCUMENTMODAL_APPROVE',  null, 5,
        'menu.warehouse.purchaseDocument.modal.approve',  1),
    (451, 1, 'PURCHASEDOCUMENTMODAL_NULLIFY',  null, 5,
        'menu.warehouse.purchaseDocument.modal.nullify',  1);

-- Asignacion por defecto al rol Administrador (idrol=1).
-- idmodulo=5 (finances) coincide con la funcionalidad para que el join
-- AccessRight.findByUser por companyModule.active resuelva.
insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo)
values
    (449, 1, 1, 1, 5),
    (450, 1, 1, 1, 5),
    (451, 1, 1, 1, 5);

-- Sincronizar secuencia de funcionalidad al ultimo id usado en el catalogo.
update secuencia
   set valor = (select max(e.idfuncionalidad) + 1 from funcionalidad e)
 where tabla = 'funcionalidad';

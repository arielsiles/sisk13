-- ============================================================================
-- query_v6.0.110_terdemol.sql
-- ============================================================================
-- CRUD de la tabla `configuracion` (pantalla "Preferencias de compania") y
-- logos de compania administrables desde esa pantalla.
--
-- Contexto:
--   La pantalla /admin/companySetting.xhtml ya existia y edita `configuracion`,
--   pero era INACCESIBLE: su render depende de s:hasPermission('COMPANYSETTING',..)
--   y la funcionalidad 'COMPANYSETTING' nunca se sembro en `funcionalidad`.
--   Sin fila en `funcionalidad` no puede haber fila en `derechoacceso`, el mapa
--   de permisos que arma UserServiceBean al login no tiene la clave, y
--   AppIdentity.hasPermission() devuelve false en silencio. Efecto: ni el link
--   del menu ni el boton Guardar se renderizaban, y la funcionalidad tampoco
--   aparecia en la pantalla de Roles, asi que nadie podia otorgarla.
--
-- Detalle:
--   Bitmask permiso: VIEW=1, CREATE=2, UPDATE=4, DELETE=8.
--   La pantalla es de UN solo registro por compania (findByCompany usa
--   getSingleResult) y no tiene alta ni baja: por eso permiso = 5
--   (VIEW=1 + UPDATE=4) y no 15. OJO: 3 seria VIEW+CREATE, no VIEW+UPDATE.
--   idmodulo = 2 (admin), igual que ROLE / USER / BUSINESSUNIT.
--   Columnas de funcionalidad (orden fisico): (idfuncionalidad, codigo,
--       descripcion, idmodulo, permiso, nombrerecurso, idcompania).
--
-- IMPORTANTE (derechoacceso.idmodulo): la named query AccessRight.findByUser
--   hace join contra modulocompania por (idcompania, idmodulo) y filtra por
--   companyModule.active. Si idmodulo va NULL el join no matchea y el permiso
--   se ignora sin error. Por eso el INSERT de abajo setea idmodulo = 2.
-- ----------------------------------------------------------------------------

-- 1) Logos de compania -------------------------------------------------------
--    Se reutiliza la tabla `archivo` (entidad File, BLOB + discriminador `tipo`)
--    que ya usan las fotos de activos fijos, en vez de meter LONGBLOBs en
--    `configuracion`, que se consulta en ~100 lugares.
--    Los logos se reescalan al guardar: cabecera 200x50, login 300x300.
--
--    NOTA: no se crean FKs hacia `archivo` porque `archivo.idarchivo` no tiene
--    PRIMARY KEY ni indice unico en esta base (mismo hueco de integridad que
--    documenta query/fix_integridad_permisos.sql para el modulo de permisos).
--    MySQL rechazaria la FK con error 1822. Si mas adelante se corrige la
--    integridad de `archivo`, agregar aqui las dos FKs.

ALTER TABLE configuracion ADD COLUMN idlogocabecera BIGINT(20) DEFAULT NULL;
ALTER TABLE configuracion ADD COLUMN idlogologin    BIGINT(20) DEFAULT NULL;

-- 2) Funcionalidad del permiso ----------------------------------------------
--    nombrerecurso apunta a la clave i18n ya existente en messages_app.properties
--    ('Functionality.admin.companySetting = Preferencias de compania'), que hasta
--    ahora estaba huerfana.

insert into funcionalidad values (503, 'COMPANYSETTING', 'Preferencias de compania (configuracion)', 2, 5, 'Functionality.admin.companySetting', 1);

-- 3) Otorgar el permiso al rol Administrador (idrol = 1) ---------------------

insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo)
values (503, 1, 5, 1, 2);

-- 4) Limpiar cuentas contables colgadas --------------------------------------
--    Estas 4 columnas apuntan a cuentas que NO existen en el plan de cuentas
--    (arcgms). Verificado sobre terdemol:
--
--      ctaivacrefitrmn = 5340120000  -> no existe
--      ctaG_it         = 5340130000  -> no existe
--      ctaCostPT       = 5100080100  -> no existe
--      ctaCostPV       = 5100080100  -> no existe
--
--    En la familia 53401* existen 5340100000/5340100100/5340110000/5340110100 y
--    luego salta a 5340140000: las cuentas ...12... y ...13... fueron eliminadas
--    o renumeradas del plan y nadie actualizo configuracion. De 5100080* no
--    existe ninguna.
--
--    Como la asociacion es @ManyToOne LAZY, Hibernate devolvia un proxy y recien
--    explotaba al tocarlo, con un EntityNotFoundException que no decia que cuenta
--    era. Eso rompia /admin/companySetting.xhtml al renderizar y, mas grave,
--    tambien los flujos contables que las consumen:
--      VoucherAccoutingServiceBean (costo de ventas), AccountingCreditSaleAction,
--      FinanceAccountingDocumentServiceBean, FixedAssetPurchaseOrderServiceBean,
--      WarehouseAccountEntryServiceBean.
--
--    Se dejan en NULL: es un valor honesto ("no configurado") en vez de un codigo
--    que miente. A partir de ahora, cualquier flujo que necesite una de estas
--    cuentas y la encuentre en NULL (o apuntando a una cuenta inexistente) corta
--    con CompanyAccountNotConfiguredException, y la pantalla muestra que columna
--    exacta hay que configurar en Preferencias de compania, en vez de un
--    NullPointerException.
--
--    Para dejarlas operativas hay que cargarles la cuenta correcta desde
--    Administracion > Preferencias de compania > Cuentas contables.
--
--    NOTA: se quito @NotNull/nullable=false de los 17 codigos de cuenta de la
--    entidad para que NULL sea un estado valido y guardable (ctaivacrefitrmn era
--    uno de ellos: sin ese cambio la pantalla no habria podido guardar).

UPDATE configuracion SET ctaivacrefitrmn = NULL WHERE ctaivacrefitrmn = '5340120000';
UPDATE configuracion SET ctaG_it         = NULL WHERE ctaG_it         = '5340130000';
UPDATE configuracion SET ctaCostPT       = NULL WHERE ctaCostPT       = '5100080100';
UPDATE configuracion SET ctaCostPV       = NULL WHERE ctaCostPV       = '5100080100';

-- Verificacion: no debe devolver ninguna fila.
--   SELECT 'ctaivacrefitrmn' col, c.ctaivacrefitrmn val FROM configuracion c
--    WHERE c.ctaivacrefitrmn IS NOT NULL
--      AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia=c.no_cia AND a.cuenta=c.ctaivacrefitrmn)
--   UNION ALL SELECT 'ctaG_it', c.ctaG_it FROM configuracion c
--    WHERE c.ctaG_it IS NOT NULL
--      AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia=c.no_cia AND a.cuenta=c.ctaG_it)
--   UNION ALL SELECT 'ctaCostPT', c.ctaCostPT FROM configuracion c
--    WHERE c.ctaCostPT IS NOT NULL
--      AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia=c.no_cia AND a.cuenta=c.ctaCostPT)
--   UNION ALL SELECT 'ctaCostPV', c.ctaCostPV FROM configuracion c
--    WHERE c.ctaCostPV IS NOT NULL
--      AND NOT EXISTS (SELECT 1 FROM arcgms a WHERE a.no_cia=c.no_cia AND a.cuenta=c.ctaCostPV);

-- 5) Correccion del permiso: 3 -> 5 ------------------------------------------
--    Los INSERT de los puntos 2 y 3 se sembraron con permiso = 3, que es
--    VIEW(1)+CREATE(2) -- NO VIEW+UPDATE. El bitmask es VIEW=1, CREATE=2,
--    UPDATE=4, DELETE=8, asi que VIEW+UPDATE es 5, no 3.
--
--    Efecto del error: el boton Guardar de la pantalla depende de
--    s:hasPermission('COMPANYSETTING','UPDATE'), y AppIdentity evalua
--    permissionCode == (permissionCode & asignado) -> 4 == (4 & 3) -> 4 == 0 ->
--    false. La pantalla abria en modo consulta, sin forma de guardar. En la
--    pantalla de Roles se ve igual: Ver y Crear con checkbox, Actualizar y
--    Eliminar vacios.
--
--    Los INSERT de arriba ya quedaron corregidos a 5 para bases nuevas; estos
--    UPDATE arreglan las bases donde el script ya se ejecuto con el valor malo.
--    Son condicionales (AND permiso = 3), asi que en una base nueva no hacen nada.
--
--    Tras aplicarlos hay que CERRAR SESION y volver a entrar: el mapa de permisos
--    se arma en el login (UserServiceBean.getPermissions).

UPDATE funcionalidad SET permiso = 5 WHERE codigo = 'COMPANYSETTING' AND permiso = 3;
UPDATE derechoacceso SET permiso = 5 WHERE idfuncionalidad = 503 AND idrol = 1 AND permiso = 3;

-- Verificacion: ambas columnas deben quedar en 5.
--   SELECT f.idfuncionalidad, f.codigo, f.permiso AS disponibles,
--          da.idrol, da.permiso AS otorgado
--     FROM funcionalidad f
--     LEFT JOIN derechoacceso da ON da.idfuncionalidad = f.idfuncionalidad
--    WHERE f.codigo = 'COMPANYSETTING';

-- 6) Email UNISUELDO configurable -------------------------------------------
--    El correo que va en el archivo UNISUELDO v2 (planilla de gerentes) estaba
--    hardcodeado en PayrollBankUnisueldo2ReportAction. Se mueve a `configuracion`
--    para que sea administrable desde la pantalla, tab "Recursos humanos".
--    Se inicializa con el valor que estaba en el codigo para no cambiar el
--    comportamiento de las bases existentes.

ALTER TABLE configuracion ADD COLUMN email_unisueldo VARCHAR(100) DEFAULT NULL AFTER hrsdialaboral;

UPDATE configuracion SET email_unisueldo = 'juana.pozo@ilvabolivia.com' WHERE email_unisueldo IS NULL;

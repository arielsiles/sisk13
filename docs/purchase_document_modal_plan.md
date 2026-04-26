# Mini-plan — Modal de Documento de Compra en pantalla de OC

Rama: `feature/purchase-document-modal` (desde `feature/drop-cu-ct`).

## Objetivo

En la pantalla de Orden de Compra ([warehousePurchaseOrder.xhtml](../view/warehouse/warehousePurchaseOrder.xhtml)) ofrecer una vía rápida (modal) para:

1. **Registrar** un nuevo documento de compra (factura/recibo) sin abandonar la pantalla de la OC.
2. **Revisar / modificar / aprobar** un documento existente en estado `Pendiente` (ícono nuevo en la grilla de documentos, abre el mismo modal con datos cargados).

El flujo actual (botón "Adicionar documento de compra" → página separada `warehousePurchaseDocument.xhtml`) **se conserva intacto y sin cambios**. El modal coexiste con su propio botón, su propia action y sus propios permisos.

## Decisiones cerradas

| # | Decisión | Valor |
|---|---|---|
| 1 | Coexistencia | Botón nuevo + action nueva; el flujo actual queda intocable |
| 2 | Texto del botón nuevo | **"Registrar factura/recibo"** |
| 3 | Campos del modal | Todos los del formulario actual (paridad completa) |
| 4 | Acceso a documentos existentes | Nuevo ícono `ver16.png` en la grilla de documentos de la OC, abre el modal con los datos del documento |
| 5 | Refresh tras registrar/aprobar | Refrescar lista de documentos y montos asociados sin recargar la página |
| 6 | Habilitación | Hasta que la OC esté finalizada (igual que el flujo actual: `!isPurchaseOrderNullified`) |
| 7 | OC al CRÉDITO y CONTADO | Aplica a ambos |
| 8 | Permisos | **Nuevos** (no reusar los de `PURCHASEDOCUMENT.*`) |
| 9 | Validaciones | Mismas reglas que hoy, mantenidas u optimizadas |

## Hallazgos relevantes de la investigación

### Capas del flujo actual

| Capa | Componente | Notas |
|---|---|---|
| Action wrapper | [WarehousePurchaseDocumentAction.java](../src/main/com/encens/khipus/action/warehouse/WarehousePurchaseDocumentAction.java) | `@Scope(CONVERSATION)`, delega en `PurchaseDocumentAction`; métodos `addPurchaseDocument`, `select`, `create`, `update`, `approve`, `nullify`, `cancel` |
| Action base | [PurchaseDocumentAction.java](../src/main/com/encens/khipus/action/purchases/PurchaseDocumentAction.java) | Lógica común; convierte fechas, calcula campos derivados, llama al servicio |
| Servicio | `PurchaseDocumentServiceBean` | `createDocument`, `updateDocument`, `approveDocument`, `nullifyDocument`. Approve genera asiento contable via `FinanceAccountingDocumentService.createAccountingVoucher` |
| UI | [purchaseDocument.xhtml](../view/purchases/purchaseDocument.xhtml) | Form único con ~15 campos; AJAX entre campos para tasas/IVA; **popups internos** para Provider y Cuenta de ajuste |
| Estados | `PurchaseDocumentState` | `PENDING → APPROVED → NULLIFIED` (sin transición inversa) |

### Riesgos técnicos del modal

1. **Forms anidados prohibidos en JSF**: el modal debe contener su propio `<h:form>` y debe estar **fuera** del form principal de la OC. Patrón ya validado en Fase 2/3 (modales `annulVoucherForm`, `purchaseOrderAnnulConfirmation`).
2. **Modales anidados (popups dentro del modal)**: el formulario actual usa `selectPopUp` para Provider y Cuenta de ajuste. RichFaces 3.x maneja mal los modales anidados. **Solución**: declarar esos popups al **nivel raíz** de la pantalla de OC (afuera del modal de documento) y referenciarlos por id. Esto evita la jerarquía problemática.
3. **AJAX dentro del modal**: el formulario actual usa `<a4j:support>` para recalcular campos al cambiar tasas/IVA. Mantener esos `reRender` con paths apuntando a ids dentro del modal funciona, mientras el modal panel no use `<c:if>` (usar `rendered=` en sus componentes hijos).
4. **Refresh de la grilla post-registro/aprobación**: usar `oncomplete="Richfaces.hideModalPanel('purchaseDocumentModal')"` + `reRender="purchaseDocumentListId"` en el botón de submit del modal.

## Plan por fases

### Fase 0 — Permisos y mensajes i18n

- **0.1** Migración SQL `query_v6.0.72.sql`: agregar 3 funcionalidades nuevas en tabla `funcionalidad`:
  - `PURCHASEDOCUMENTMODAL_REGISTER` — registrar/editar desde modal
  - `PURCHASEDOCUMENTMODAL_APPROVE` — aprobar desde modal
  - `PURCHASEDOCUMENTMODAL_NULLIFY` — anular desde modal
  Asignar al rol Administrador (idrol=1) con `idmodulo=5` (finances), `permiso=1` (consistente con la convención del resto de la rama). Bumpear `secuencia.funcionalidad` al último id usado.
- **0.2** Claves i18n en [messages_app.properties](../resources/messages_app.properties):
  - `PurchaseDocument.modal.register` = "Registrar factura/recibo"
  - `PurchaseDocument.modal.title.new` = "Nuevo documento de compra"
  - `PurchaseDocument.modal.title.edit` = "Editar documento de compra"
  - `PurchaseDocument.modal.review.title` = "Revisar documento"
  - `PurchaseDocument.modal.openExisting.tooltip` = "Abrir en modal"
  - `PurchaseDocument.modal.success.created` = "Documento registrado correctamente."
  - `PurchaseDocument.modal.success.approved` = "Documento aprobado correctamente."
  - mensajes de error mapeados a las excepciones del servicio.

### Fase 1 — Action nueva `WarehousePurchaseDocumentModalAction`

- **1.1** Crear `WarehousePurchaseDocumentModalAction` (paquete `action.warehouse`, `@Scope(CONVERSATION)`, `@Name("warehousePurchaseDocumentModalAction")`).
- **1.2** Inyectar `purchaseDocumentService`, `financeAccountingDocumentService`, `financesExchangeRateService`, `warehousePurchaseOrderAction` (referencia a la OC actual).
- **1.3** Métodos:
  - `openForCreate()` — inicializa `PurchaseDocument` vacío, lo asocia con la OC actual, prepara defaults. Llamado por el botón "Registrar factura/recibo".
  - `openForReview(PurchaseDocument)` — carga el documento existente para revisar/editar/aprobar.
  - `save()` — guarda (create o update según el caso). Permiso `PURCHASEDOCUMENTMODAL_REGISTER.VIEW`.
  - `approve()` — aprueba el documento. Permiso `PURCHASEDOCUMENTMODAL_APPROVE.VIEW`. Solo habilitado si `state=PENDING`.
  - `nullify()` — anula. Permiso `PURCHASEDOCUMENTMODAL_NULLIFY.VIEW`. Solo si `state=PENDING`.
  - `closeModal()` — limpia estado interno, prepara para próxima apertura.
- **1.4** Helpers de UI:
  - `isCanRegister()` — true si la OC permite agregar (no anulada).
  - `isCanApprove()` — true si el documento es `PENDING`.
  - `isEditingExisting()` / `isCreatingNew()` para rendering condicional del título.
- **1.5** No reescribir lógica de servicio: delegar a `purchaseDocumentService` directamente. La paridad la da el servicio, no la action.

### Fase 2 — Fragmento del modal (UI)

- **2.1** Crear [view/warehouse/purchaseDocumentModal.xhtml](../view/warehouse/purchaseDocumentModal.xhtml) (fragmento auto-contenido, no página completa).
- **2.2** Estructura:
  ```xml
  <rich:modalPanel id="purchaseDocumentModal" minHeight="500" minWidth="900" autosized="true" zindex="2000">
      <f:facet name="header">…</f:facet>
      <h:form id="purchaseDocumentModalForm">
          <!-- Mismos campos que purchaseDocument.xhtml -->
          <!-- Botones: Guardar (siempre), Aprobar (si PENDING), Anular (si PENDING), Cancelar -->
      </h:form>
  </rich:modalPanel>
  ```
- **2.3** Reutilizar `<ui:include>` cuando sea seguro o copiar+adaptar los campos. Decisión preliminar: **copiar y adaptar** porque el original tiene su propio `<h:form>` y referencias a `action` que apuntan al wrapper viejo. Mejor un fragmento limpio que use `warehousePurchaseDocumentModalAction.*`.
- **2.4** Botones del modal:
  - **Guardar** (registra o actualiza) → `oncomplete="Richfaces.hideModalPanel('purchaseDocumentModal')"` + `reRender="purchaseDocumentListId, totalsPanel"`.
  - **Aprobar** (solo si PENDING) → mismo patrón.
  - **Anular** (solo si PENDING) → mismo patrón.
  - **Cancelar** → solo cierra el modal sin tocar el backend.

### Fase 3 — Modales hijos (Provider, Cuenta de ajuste) al nivel raíz

- **3.1** Mover `financesEntityListModalPanel` y `cashAccountAdjustmentListModalPanel` al nivel raíz de [warehousePurchaseOrder.xhtml](../view/warehouse/warehousePurchaseOrder.xhtml), junto a los demás `<s:decorate>` de modales auxiliares (sección de líneas 571-743 actuales).
- **3.2** El modal de documento referencia esos popups por id; al abrirse, se ven correctamente sobre el modal sin nesting.
- **3.3** Verificar que los popups no se rompen para el flujo viejo: ambos siguen usándose desde la página separada `warehousePurchaseDocument.xhtml` mediante el mismo id, sin cambios.

### Fase 4 — Botones e ícono en `warehousePurchaseOrder.xhtml`

- **4.1** Agregar botón **"Registrar factura/recibo"** junto al actual "Adicionar documento de compra" (líneas 66 y 197). Visible si `canRegister && hasPermission('PURCHASEDOCUMENTMODAL_REGISTER','VIEW')`. Abre el modal vía `rich:componentControl`.
- **4.2** En la grilla de la pestaña "Documentos" (línea ~487-491), agregar un nuevo `<h:commandLink>` con `<h:graphicImage value="/img/ver16.png">`. Action: `warehousePurchaseDocumentModalAction.openForReview(purchaseDocumentItem)`. Visible si tiene permiso `PURCHASEDOCUMENTMODAL_REGISTER.VIEW`. El link abre el modal con los datos del documento.
- **4.3** Incluir el fragmento del modal: `<ui:include src="/warehouse/purchaseDocumentModal.xhtml"/>` cerca del final del archivo, junto a los otros modales sueltos.

### Fase 5 — Validación funcional

Probar:

- **5.1** Crear documento desde modal, verificar refresh de la grilla y montos sin recargar página.
- **5.2** Abrir documento `PENDING` desde el ícono `ver16.png`, modificar y aprobar dentro del modal. Confirmar que el asiento contable (`sf_tmpenc`) se generó.
- **5.3** Anular documento desde el modal. Confirmar `state=NULLIFIED`.
- **5.4** Verificar que el flujo viejo (botón "Adicionar documento de compra") sigue funcionando idéntico.
- **5.5** Verificar que sin los permisos nuevos no aparecen ni el botón ni el ícono (con permiso viejo intacto).
- **5.6** Validar AJAX interno del modal: cambiar IVA / tasas y ver que recalcula sin cerrar el modal.

## Riesgos y mitigaciones

| Riesgo | Mitigación |
|---|---|
| Los popups hijos (Provider, Cuenta) no se ven sobre el modal | Declararlos al nivel raíz de la pantalla, no dentro del modal; usar `zindex` mayor en los popups si se requiere |
| Conversation Seam confusa al abrir modal con datos viejos cacheados | `closeModal()` limpia explícitamente las variables, y `openForCreate`/`openForReview` setean fresco |
| AJAX reRender al guardar no actualiza la grilla | El listado usa `purchaseOrderAction.purchaseDocumentList` (binding); el `reRender="purchaseDocumentListId"` debe forzar re-evaluación. Probar y ajustar el id si fuera necesario. Plan B: refrescar la pestaña entera con `reRender="warehousePurchaseOrderForm"` |
| Errores de validación no se ven dentro del modal | El modal incluye su propio `<rich:messages />` o `<h:messages />` para errores locales |

## Abierto antes de implementar

- ¿El flujo "Aprobar" desde el modal debe permitir que el usuario primero **edite** y después apruebe en un solo botón ("Guardar y Aprobar"), o son dos botones separados ("Guardar" y "Aprobar")? Mi propuesta: **dos botones separados** dentro del modal, igual que la página actual (botón "Aprobar" solo se habilita si el documento ya está persistido y en `PENDING`).
- ¿En la grilla, el ícono `ver16.png` reemplaza al "Seleccionar" de texto que ya existe, o se agrega como segunda acción? Mi propuesta: **se agrega como segunda acción**; el "Seleccionar" actual sigue llevando a la página completa para cualquier usuario que prefiera el flujo viejo.

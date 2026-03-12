# Changelog

Todos los cambios notables de este proyecto se documentan aqui.
Formato basado en [Keep a Changelog](https://keepachangelog.com/es/1.0.0/).

## [Sin publicar]

---

## [6.0.31] - 2026-03-12

### Optimizado
- **Generacion de planillas de acopio de leche**: Reduccion de ~2000+ queries a ~50-70 en el proceso `generateAll()`.
  Tiempo estimado de 5+ min a <30 seg para ~100 productores en ~20 zonas.
  - OPT-1: Eliminada query muerta en `addProrationAlcohol()` (~100 queries)
  - OPT-2: Cache de `hasLicense()` por productor (~1400 lazy loads)
  - OPT-3: Pre-carga batch de `ProducerTax` con JOIN FETCH (~200 queries)
  - OPT-4: Batch `prepareDiscount()` por zona con nueva NamedQuery (~100 queries)
  - OPT-5: Eliminado `findById()+update()` redundante, persist directo de DiscountReserve (~200 queries)
  - OPT-6: Calculo global `totalWeightFortnight` movido fuera del loop de zonas (~19 queries)
  - OPT-7: Consolidacion de 3 iteraciones del mapa de productores en `applyProrations()` (mejora CPU)

### Modificado
- `RawMaterialPayRollServiceBean.java` - Nuevos metodos: `preloadProducerTaxes()`, `hasLicenseFromTax()`, `applyProrations()`
- `RawMaterialPayRollService.java` - Firma de `generatePayroll()` con parametro `totalWeightFortnight`
- `RawMaterialPayRollAction.java` - Inyeccion de `CollectedRawMaterialCalculatorService`, pre-calculo de peso quincenal
- `SalaryMavementProducerServiceBean.java` - Nuevo metodo `prepareDiscountsBatch()`
- `SalaryMovementProducerService.java` - Nueva firma `prepareDiscountsBatch()`
- `SalaryMovementProducer.java` - Nueva NamedQuery `SalaryMovementProducer.getDiscountByZone`

---

## [6.0.30] - (fecha anterior)

(Releases anteriores no documentados en este formato)

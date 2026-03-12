# Analisis Detallado: Generacion de Planillas de Acopio de Leche

## Indice

1. [Resumen General](#1-resumen-general)
2. [Flujo Completo del Proceso](#2-flujo-completo-del-proceso)
3. [Entidades JPA Involucradas](#3-entidades-jpa-involucradas)
4. [Tablas MySQL Involucradas](#4-tablas-mysql-involucradas)
5. [Calculos Detallados](#5-calculos-detallados)
6. [Servicios Involucrados](#6-servicios-involucrados)
7. [Diagrama de Relaciones entre Entidades](#7-diagrama-de-relaciones-entre-entidades)
8. [Constantes del Sistema](#8-constantes-del-sistema)
9. [Flujo de Aprobacion](#9-flujo-de-aprobacion)

---

## 1. Resumen General

La generacion de planillas de acopio de leche es el proceso central del modulo de produccion de SISK13/KHIPUS. Permite calcular los pagos quincenales a los productores de leche, incluyendo:

- Total de litros acopiados por productor
- Monto bruto (litros x precio unitario)
- Descuentos diversos (retenciones fiscales, alcohol, concentrados, credito, veterinario, yogurt, tachos, otros)
- Ajustes por diferencias de peso en zona productiva
- Descuento de reserva
- Calculo del liquido pagable final

**Punto de entrada UI:** `view/production/rawMaterialPayRoll.xhtml` lineas 202-205

```xml
<a4j:commandButton reRender="formPanel,collectedRawMaterialList"
                   rendered="#{!rawMaterialPayRollAction.managed and !rawMaterialPayRollAction.readonly}"
                   value="#{messages['RawMaterialPayRoll.generatePayroll']}"
                   action="#{rawMaterialPayRollAction.generate}"/>
```

---

## 2. Flujo Completo del Proceso

### 2.1 Parametros de Entrada (UI)

El usuario selecciona en la vista:

| Parametro | Componente | Descripcion |
|-----------|-----------|-------------|
| `gestion` | `h:selectOneMenu` | Ano fiscal (ej: 2026) |
| `month` | `h:selectOneMenu` | Mes del enum `Month` |
| `periodo` | `h:selectOneMenu` | Quincena: `FIRSTPERIODO` (1-15) o `SECONDPERIODO` (16-28/29/30/31) |
| `metaProduct` | `h:selectOneMenu` | Producto de acopio (leche) |
| `unitPrice` | `h:inputText` | Precio unitario por litro (default: 4.50 Bs) |
| `productiveZone` | Modal de busqueda | Zona productiva (GAB) - opcional |

### 2.2 Metodo `generate()` - RawMaterialPayRollAction (linea 198)

**Archivo:** `action/production/RawMaterialPayRollAction.java`

```
1. Obtiene la instancia de RawMaterialPayRoll
2. Calcula fechas del periodo:
   - fechaInicio = (gestion.year, month.value, periodo.initDay)
     - FIRSTPERIODO: dia 1
     - SECONDPERIODO: dia 16
   - fechaFin = (gestion.year, month.value, periodo.endDay)
     - FIRSTPERIODO: dia 15
     - SECONDPERIODO: dia 28/29/30/31 (segun mes y bisiesto)
3. Busca DiscountProducer vigente para la fecha fin
4. Valida que no haya DiscountProducers duplicados
5. Valida que fechaInicio <= fechaFin
6. Si hay zona productiva seleccionada:
   a. Valida la planilla (no cruce con planillas existentes)
   b. Limpia registros anteriores
   c. Llama a generatePayroll() para UNA zona
7. Si NO hay zona productiva (caso normal):
   a. Llama a generateAll() -> genera para TODAS las zonas productivas ILVA
```

### 2.3 Metodo `generateAll()` - RawMaterialPayRollAction (linea 292)

```
1. Recalcula fechas del periodo
2. Busca DiscountProducer vigente
3. Obtiene TODAS las zonas productivas que NO tienen planilla en ese periodo
   (query: ProductiveZone.findAllThatDoNotHavePayRollOnDate)
4. Filtra solo zonas con grupo = "ILVA"
5. Para CADA zona productiva:
   a. Crea nueva instancia de RawMaterialPayRoll
   b. Copia datos maestros (fechas, empresa, producto, precio, tasas)
   c. Llama validate() - verifica no cruce con planilla anterior
   d. Llama generatePayroll(payRoll, discountProducer)
   e. Llama createAll(payRoll) - persiste en BD
```

### 2.4 Metodo `generatePayroll()` - RawMaterialPayRollServiceBean (linea 166)

Este es el **metodo central** de todo el proceso. Ejecuta los siguientes pasos:

```
PASO 1: CALCULO DE RESERVA POR GAB
   Si existe discountProducer:
   a. totalWeightFortnight = total litros acopiados en TODA la quincena (todas las GABs)
   b. totalWeightFortnightGAB = total litros acopiados en la quincena para ESTA GAB
   c. percentageReserveGAB = (totalWeightFortnightGAB * 100 / totalWeightFortnight) / 100
   d. totalReservaGAB = round(totalWeightFortnight * discountProducer.reserve, 2) * PRICE_UNIT_MILK * percentageReserveGAB

PASO 2: CALCULO DE DIFERENCIAS DE PESO
   createMapOfDifferencesWeights():
   - Consulta CollectionRecord (registroacopio) para obtener:
     receivedAmount (cantidad recibida por productores) y weightedAmount (cantidad pesada en balanza)
   - Calculo por fecha: diff = weightedAmount * unitPrice - receivedAmount * unitPrice
   - Resultado: Map<Date, Double> con la diferencia monetaria por dia

PASO 3: CREACION DEL MAPA DE PRODUCTORES
   createMapOfProducers():
   a. taxRate = rawMaterialPayRoll.taxRate / 100 (ej: 0.008)
   b. Para cada registro de acopio (productor + fecha + cantidad):
      - earned = amount * unitPrice
      - withholding = 0 si tiene licencia fiscal valida, sino earned * taxRate
      - aux.collectedAmount += amount
      - aux.earnedMoney += earned
      - aux.withholdingTax += withholding
      - aux.discountGA += amount * DISCOUNT_GA (actualmente 0.0)
   c. Calcula porcentaje de participacion de cada productor
   d. Calcula prorrateo de diferencias de peso
   e. Si hay reserva > 0, calcula descuento de reserva proporcional

PASO 4: OBTENCION DE ALCOHOL POR GAB
   getAlcoholBayGAB():
   - Suma todos los movimientos de alcohol (SalaryMovementGAB) para la zona productiva

PASO 5: CREACION DE REGISTROS DE PAGO (por cada productor)
   Para cada Aux en el mapa:
   a. Crea RawMaterialPayRecord
   b. Establece totalAmount, productiveZoneAdjustment, earnedMoney
   c. totalPayCollected = unitPrice * collectedAmount
   d. Verifica licencia fiscal (ProducerTax)
   e. Llama prepareDiscount() para obtener descuentos individuales
   f. Asigna alcohol proporcional: alcoholByGAB * porcentaje
   g. Establece retencion fiscal (withholdingTax)
   h. Establece descuento de reserva y GA
   i. Agrega record a la planilla

PASO 6: AJUSTE DE DIFERENCIA DE ALCOHOL
   Si hay diferencia de redondeo en alcohol, se ajusta en el primer productor

PASO 7: CALCULO DE LIQUIDO PAGABLE
   calculateLiquidPayable()

PASO 8: ASIGNACION DE TOTALES A LA PLANILLA
   Todos los totales se redondean a 2 decimales
```

---

## 3. Entidades JPA Involucradas

### 3.1 Entidades Principales

| # | Clase Java | Tabla MySQL | Descripcion |
|---|-----------|-------------|-------------|
| 1 | `RawMaterialPayRoll` | `planillapagomateriaprima` | Planilla de pago (cabecera) |
| 2 | `RawMaterialPayRecord` | `registropagomateriaprima` | Registro de pago por productor (detalle) |
| 3 | `RawMaterialProducerDiscount` | `descuentproductmateriaprima` | Descuentos asignados a cada productor |
| 4 | `RawMaterialProducer` | `productormateriaprima` | Productor de materia prima (ganadero) |
| 5 | `ProductiveZone` | `zonaproductiva` | Zona productiva / GAB |
| 6 | `MetaProduct` | `metaproductoproduccion` | Producto meta (leche) |
| 7 | `CollectedRawMaterial` | `acopiomateriaprima` | Registro de acopio diario por productor |
| 8 | `RawMaterialCollectionSession` | `sesionacopio` | Sesion de acopio (por dia/zona) |
| 9 | `CollectionForm` | `planillaacopio` | Planilla de acopio (balanza) |
| 10 | `CollectionRecord` | `registroacopio` | Registro de acopio por balanza (pesado vs recibido) |
| 11 | `DiscountProducer` | `descuentoproductor` | Configuracion de descuento de reserva por periodo |
| 12 | `DiscountReserve` | `descuentoreserva` | Descuento de reserva aplicado por productor |
| 13 | `SalaryMovementProducer` | `movimientosalarioproductor` | Movimientos de descuento individual por productor |
| 14 | `SalaryMovementGAB` | `movimientosalariogab` | Movimientos de descuento por GAB (alcohol) |
| 15 | `TypeMovementProducer` | `tipomovimientoproductor` | Catalogo de tipos de movimiento/descuento |
| 16 | `ProducerTax` | *(tabla de impuestos del productor)* | Licencia fiscal del productor |
| 17 | `GestionTax` | *(tabla de gestion fiscal)* | Gestion fiscal asociada a licencia |

### 3.2 Entidades de Soporte

| Clase Java | Tabla MySQL | Descripcion |
|-----------|-------------|-------------|
| `Company` | `compania` | Empresa |
| `Gestion` | `gestion` | Ano fiscal |
| `GestionPayroll` | `gestionplanilla` | Gestion de planilla |

### 3.3 Enums

| Enum | Valores | Descripcion |
|------|---------|-------------|
| `Periodo` | `FIRSTPERIODO(1,15)`, `SECONDPERIODO(16,30)` | Quincena |
| `StatePayRoll` | `PENDING`, `APPROVED` | Estado de planilla |
| `Month` | Enero-Diciembre | Meses del ano |
| `ProductionCollectionState` | `PENDING`, `APPROVED` | Estado de sesion de acopio |

---

## 4. Tablas MySQL Involucradas

### 4.1 Tablas Principales (escritura)

```sql
-- Planilla de pago (cabecera)
planillapagomateriaprima (
  idplanillapagomateriaprima  BIGINT PK,
  fechainicio                 DATE NOT NULL,
  fechafin                    DATE NOT NULL,
  preciounitario              DECIMAL(9,2) NOT NULL,    -- precio por litro
  tasaimpuesto                DECIMAL(3,2) NOT NULL,    -- IT + IUE
  iue                         DECIMAL(3,2) NOT NULL,    -- 0.5%
  it                          DECIMAL(3,2) NOT NULL,    -- 0.3%
  estado                      VARCHAR(50) NOT NULL,     -- PENDING/APPROVED
  idzonaproductiva            BIGINT FK NOT NULL,
  idmetaproductoproduccion    BIGINT FK NOT NULL,
  idcompania                  BIGINT FK NOT NULL,
  totalacopiadoxgab           DECIMAL(16,2),  -- total pesado
  totalpesadoxgab             DECIMAL(16,2),  -- total acopiado (litros)
  totalmontoacopioadoxgab     DECIMAL(16,2),  -- total bruto Bs
  totalretencionesxgab        DECIMAL(16,2),  -- total retenciones
  totalcreditoxgab            DECIMAL(16,2),
  totalveterinarioxgab        DECIMAL(16,2),
  totalalcoholxgab            DECIMAL(16,2),
  totalconcentradosxgab       DECIMAL(16,2),
  totalyogurdxgab             DECIMAL(16,2),
  totaltachosxgab             DECIMAL(16,2),
  totadescuentosxgab          DECIMAL(16,2),  -- total descuentos
  totalotrosdecuentosxgab     DECIMAL(16,2),
  totalajustexgab             DECIMAL(16,2),  -- ajuste por diferencia peso
  totalotrosingresosxgab      DECIMAL(16,2),
  totaliquidoxgab             DECIMAL(16,2),  -- total liquido pagable
  totaldescuentoreserva       DECIMAL(16,2),
  totalcomision               DECIMAL(16,2),
  totalga                     DECIMAL(16,2),
  version                     BIGINT NOT NULL
)

-- Registro de pago por productor (detalle)
registropagomateriaprima (
  idregistropagomateriaprima      BIGINT PK,
  idplanillapagomateriaprima      BIGINT FK NOT NULL,
  iddescuentproductmateriaprima   BIGINT FK NOT NULL,
  cantidadtotal                   DECIMAL(24,2) NOT NULL,  -- litros acopiados
  ajustezonaproductiva            DECIMAL(16,2) NOT NULL,  -- ajuste peso
  descuentoreserva                DECIMAL(16,2) NOT NULL,
  ga                              DECIMAL(16,2) NOT NULL,  -- descuento GA
  totalganado                     DECIMAL(16,2) NOT NULL,  -- earned money
  totalpagoacopio                 DECIMAL(16,2) NOT NULL,  -- pago bruto
  liquidopagable                  DECIMAL(16,2) NOT NULL,  -- liquido final
  licenciaimpuestos               VARCHAR(200),
  fechaexpiralicenciaimpuesto     DATE,
  fechainicialicenciaimpuesto     DATE,
  idcompania                      BIGINT FK,
  version                         BIGINT NOT NULL
)

-- Descuentos por productor
descuentproductmateriaprima (
  iddescuentproductmateriaprima   BIGINT PK,
  idproductormateriaprima         BIGINT FK NOT NULL,
  codigo                          BIGINT NOT NULL,
  yogurt                          DECIMAL(16,2) NOT NULL,
  veterinario                     DECIMAL(16,2) NOT NULL,
  credito                         DECIMAL(16,2) NOT NULL,
  tachos                          DECIMAL(16,2) NOT NULL,
  otrosdescuentos                 DECIMAL(16,2) NOT NULL,
  otrosingresos                   DECIMAL(16,2) NOT NULL,
  retencion                       DECIMAL(16,2) NOT NULL,
  alcohol                         DECIMAL(16,2) NOT NULL,
  concentrados                    DECIMAL(16,2) NOT NULL,
  comision                        DECIMAL(16,2) NOT NULL,
  idcompania                      BIGINT FK,
  version                         BIGINT NOT NULL
)

-- Descuento de reserva por productor
descuentoreserva (
  iddescuentoreserva         BIGINT PK,
  monto                      DECIMAL(16,2) NOT NULL,
  fechaini                   DATE NOT NULL,
  fechafin                   DATE NOT NULL,
  iddescuentoproductor       BIGINT FK NOT NULL,
  idproductormateriaprima    BIGINT FK NOT NULL
)
```

### 4.2 Tablas de Lectura (datos de acopio)

```sql
-- Sesion de acopio diario
sesionacopio (
  idsesionacopio               BIGINT PK,
  fecha                        DATE NOT NULL,
  estado                       VARCHAR NOT NULL,
  idzonaproductiva             BIGINT FK NOT NULL,
  idmetaproductoproduccion     BIGINT FK NOT NULL,
  idcompania                   BIGINT FK
)

-- Acopio por productor (cuanto entrego cada productor)
acopiomateriaprima (
  idacopiomateriaprima         BIGINT PK,
  cantidad                     DECIMAL(16,2) NOT NULL,  -- litros
  idproductormateriaprima      BIGINT FK NOT NULL,
  idsesionacopio               BIGINT FK NOT NULL,
  idcompania                   BIGINT FK
)

-- Planilla de acopio (balanza)
planillaacopio (
  idplanillaacopio             BIGINT PK,
  fecha                        DATE NOT NULL,
  idmetaproductoproduccion     BIGINT FK NOT NULL,
  idcompania                   BIGINT FK
)

-- Registro de acopio (pesado en balanza por GAB)
registroacopio (
  idregistroacopio             BIGINT PK,
  cantidadrecibida             DECIMAL(16,2) NOT NULL,  -- suma productores
  cantidadpesada               DECIMAL(16,2) NOT NULL,  -- pesado en balanza
  cantidadrechazada            DECIMAL(16,2) NOT NULL,
  idzonaproductiva             BIGINT FK NOT NULL,
  idplanillaacopio             BIGINT FK NOT NULL,
  idcompania                   BIGINT FK
)

-- Movimientos de descuento por productor
movimientosalarioproductor (
  idmovimientosalarioproductor BIGINT PK,
  idproductormateriaprima      BIGINT FK NOT NULL,
  idtipomovimientoproductor    BIGINT FK NOT NULL,
  descripcion                  VARCHAR,
  fecha                        DATE NOT NULL,
  valor                        DECIMAL(16,2) NOT NULL,
  estado                       VARCHAR NOT NULL,
  idzonaproductiva             BIGINT FK,
  idcompania                   BIGINT FK
)

-- Movimientos de descuento por GAB (alcohol)
movimientosalariogab (
  -- similar estructura, descuento a nivel GAB
  -- se usa para el descuento de alcohol prorrateado
)

-- Configuracion de reserva
descuentoproductor (
  iddescuentoproductor         BIGINT PK,
  promedioleche                DECIMAL(16,2) NOT NULL,
  reserva                      DECIMAL(8,5) NOT NULL,   -- tasa de reserva
  reservaquicenta              DECIMAL(8,5) NOT NULL,
  fechaini                     DATE NOT NULL,
  fechafin                     DATE NOT NULL,
  montototalmn                 DECIMAL(16,2) NOT NULL,
  montototalme                 DECIMAL(16,2) NOT NULL,
  tc                           DECIMAL(5,2) NOT NULL,
  estado                       VARCHAR(10) NOT NULL
)

-- Zona productiva
zonaproductiva (
  idzonaproductiva             BIGINT PK,
  numero                       VARCHAR(20),
  grupo                        VARCHAR(20),     -- 'ILVA', 'CISC', etc.
  nombre                       VARCHAR(200) NOT NULL,
  tienecns                     INT NOT NULL,
  idciudad                     BIGINT FK,
  idcompania                   BIGINT FK
)

-- Tipos de movimiento
tipomovimientoproductor (
  idtipomovimientoproductor    BIGINT PK,
  -- nombres: CONCENTRADOS, COMISION BANCO, YOGURT, VETERINARIO,
  --          TACHOS, OTROS EGRESOS, OTROS INGRESOS, CREDITO
)

-- Tabla de secuencias
secuencia (
  tabla                        VARCHAR PK,
  valor                        BIGINT
)
```

---

## 5. Calculos Detallados

### 5.1 Valores Iniciales por Defecto

```
IT (Impuesto a las Transacciones) = 0.3%  (0.003)
IUE (Impuesto sobre Utilidades)   = 0.5%  (0.005)
Tasa Impositiva (taxRate)         = IT + IUE = 0.8%  (0.008)
Precio Unitario Leche             = 4.50 Bs/litro (Constants.PRICE_UNIT_MILK)
Descuento GA                      = 0.0 (Constants.DISCOUNT_GA) -- deshabilitado
```

### 5.2 Calculo de Fechas del Periodo

```
fechaInicio = (anio, mes, periodo.initDay)
fechaFin    = (anio, mes, periodo.endDay(mes+1, anio))

Donde endDay para SECONDPERIODO:
  - Meses de 31 dias (ene,mar,may,jul,ago,oct,dic): dia 31
  - Febrero bisiesto: dia 29
  - Febrero normal: dia 28
  - Otros meses: dia 30

Para FIRSTPERIODO: siempre dia 15
```

### 5.3 Calculo de Reserva por GAB

```
totalWeightFortnight    = SUM(litros acopiados en la quincena, TODAS las GABs)
totalWeightFortnightGAB = SUM(litros acopiados en la quincena, ESTA GAB)

percentageReserveGAB = (totalWeightFortnightGAB * 100 / totalWeightFortnight) / 100

totalReservaGAB = ROUND(totalWeightFortnight * discountProducer.reserve, 2)
                  * PRICE_UNIT_MILK
                  * percentageReserveGAB
```

**Ejemplo:** Si la quincena tiene 100,000 litros totales, esta GAB aporto 15,000 litros, y la tasa de reserva es 0.02:
```
percentageReserveGAB = (15000 * 100 / 100000) / 100 = 0.15
totalReservaGAB = ROUND(100000 * 0.02, 2) * 4.50 * 0.15
                = 2000 * 4.50 * 0.15 = 1350.00 Bs
```

### 5.4 Diferencias de Peso (Ajuste Zona Productiva)

La diferencia surge entre lo que los productores reportan entregar (`receivedAmount` - suma de acopio individual) y lo que la balanza de la planta pesa (`weightedAmount`).

```
Para cada dia del periodo:
  diff = weightedAmount * unitPrice - receivedAmount * unitPrice

totalDiffMoney = SUM(diff por todos los dias)
```

**Prorrateo de la diferencia a cada productor:**
```
porcentaje_productor = (earnedMoney_productor * 100 / totalMoneyCollectedGAB) / 100

adjustmentAmount_productor = totalDiffMoney * porcentaje_productor

earnedMoney_productor += adjustmentAmount_productor
```

### 5.5 Calculo por Productor

#### 5.5.1 Litros y Monto Bruto
```
totalAmount     = SUM(litros entregados por el productor en el periodo)
earnedMoney     = totalAmount * unitPrice  (antes de ajustes)
totalPayCollected = unitPrice * totalAmount
```

#### 5.5.2 Retencion Fiscal (Withholding Tax)
```
SI el productor tiene licencia fiscal valida (ProducerTax) para el periodo:
   withholdingTax = 0.0
SINO:
   withholdingTax = SUM(earned_por_dia * taxRate)
   donde taxRate = (IT + IUE) / 100 = 0.008
```

La licencia fiscal es valida si:
- Existe un `ProducerTax` con `formNumber` no vacio
- La `gestionTax` del ProducerTax cubre el rango de fechas del periodo

#### 5.5.3 Descuento de Alcohol (prorrateado por GAB)
```
alcoholByGAB = SUM(movimientos de alcohol en SalaryMovementGAB para esta GAB)

porcentaje_productor = (earnedMoney * 100 / totalMoneyCollectedGAB) / 100

alcohol_productor = ROUND(alcoholByGAB * porcentaje_productor, 2)
```

La diferencia de redondeo se ajusta sumandola al primer productor de la lista.

#### 5.5.4 Descuento de Reserva (proporcional)
```
SI totalReservaGAB > 0:
   porcentaje = (earnedMoney * 100 / totalMoneyCollectedGAB) / 100
   reserveDiscount = ROUND(totalReservaGAB * porcentaje, 2)
   earnedMoney -= reserveDiscount
```

Se crea un registro `DiscountReserve` para cada productor.

#### 5.5.5 Descuento GA
```
discountGA = totalAmount * DISCOUNT_GA  (actualmente 0.0, deshabilitado)
```

#### 5.5.6 Descuentos Individuales (SalaryMovementProducer)

Se obtienen de la tabla `movimientosalarioproductor` segun el tipo:

| Tipo Movimiento | Campo Destino | Descripcion |
|----------------|---------------|-------------|
| CONCENTRADOS | concentrated | Alimento concentrado |
| COMISION BANCO | commission | Comision bancaria |
| YOGURT | yogurt | Yogurt |
| VETERINARIO | veterinary | Servicios veterinarios |
| TACHOS | cans | Recipientes/tachos |
| OTROS EGRESOS | otherDiscount | Otros descuentos |
| OTROS INGRESOS | otherIncoming | Otros ingresos (se suma) |
| CREDITO | credit | Credito |

Cada valor se redondea a 2 decimales con redondeo simetrico.

### 5.6 Calculo del Liquido Pagable

**Metodo:** `calculateLiquidPayable()` (ServiceBean linea 1260)

```
Para cada registro de pago (RawMaterialPayRecord):

   totalDiscount = alcohol
                 + concentrated
                 + withholdingTax
                 + cans
                 + credit
                 + veterinary
                 + yogurt
                 + otherDiscount
                 + commission

   liquidPayable = earnedMoney - totalDiscount + otherIncoming - discountGA

   liquidPayable = ROUND(liquidPayable, 2)
```

**Total de la planilla:**
```
totalLiquidByGAB = ROUND(SUM(liquidPayable de todos los productores), 2)
```

### 5.7 Formula Completa Resumida

```
LIQUIDO PAGABLE (por productor) =
    (litros * precioUnitario + ajustePeso - descuentoReserva)  -- earnedMoney
    - retencionFiscal
    - alcohol
    - concentrados
    - comisionBanco
    - yogurt
    - veterinario
    - tachos
    - credito
    - otrosDescuentos
    - descuentoGA
    + otrosIngresos
```

### 5.8 Totales de la Planilla (GAB)

Todos estos campos se calculan como la suma de los valores individuales de cada productor:

| Campo | Descripcion |
|-------|-------------|
| `totalCollectedByGAB` | Total litros acopiados |
| `totalMountCollectdByGAB` | Total monto bruto Bs |
| `totalRetentionGAB` | Total retenciones fiscales |
| `totalAlcoholByGAB` | Total descuento alcohol |
| `totalConcentratedByGAB` | Total descuento concentrados |
| `totalCreditByGAB` | Total descuento credito |
| `totalVeterinaryByGAB` | Total descuento veterinario |
| `totalYogourdByGAB` | Total descuento yogurt |
| `totalRecipByGAB` | Total descuento tachos |
| `totalOtherDiscountByGAB` | Total otros descuentos |
| `totalAdjustmentByGAB` | Total ajuste por diferencia peso |
| `totalOtherIncomeByGAB` | Total otros ingresos |
| `totalReserveDicount` | Total descuento reserva |
| `totalGA` | Total descuento GA |
| `totalCommission` | Total comisiones |
| `totalLiquidByGAB` | **Total liquido pagable** |

---

## 6. Servicios Involucrados

### 6.1 Servicios Principales

| Servicio | Bean | Responsabilidad |
|----------|------|-----------------|
| `RawMaterialPayRollService` | `RawMaterialPayRollServiceBean` | Generacion, validacion, persistencia de planillas |
| `SalaryMovementProducerService` | `SalaryMavementProducerServiceBean` | Preparacion de descuentos individuales por productor |
| `SalaryMovementGABService` | `SalaryMavementGABServiceBean` | Obtencion de descuento de alcohol por GAB |
| `CollectedRawMaterialCalculatorService` | `CollectedRawMaterialCalculatorServiceBean` | Calculo de litros acopiados totales |
| `ProductiveZoneService` | `ProductiveZoneServiceBean` | Busqueda de zonas productivas sin planilla |
| `RawMaterialProducerService` | *(Bean correspondiente)* | Actualizacion de datos de productor |
| `CompanyConfigurationService` | *(Bean correspondiente)* | Configuracion de empresa (precio, tasas) |
| `GestionService` | *(Bean correspondiente)* | Gestiones fiscales |
| `RawMaterialProducerDiscountService` | *(Bean correspondiente)* | Gestion de descuentos de productores |

### 6.2 Clase Auxiliar Interna: `Aux`

Definida dentro de `RawMaterialPayRollServiceBean` (linea 1195), acumula calculos intermedios por productor:

```java
class Aux {
    RawMaterialProducer producer;
    Double collectedAmount = 0.0;      // litros totales
    Double adjustmentAmount = 0.0;     // ajuste por diferencia de peso
    Double collectedTotalMoney = 0.0;  // monto total acopiado
    Double earnedMoney = 0.0;          // dinero ganado (con ajustes)
    Double withholdingTax = 0.0;       // retencion fiscal
    Double procentaje = 0.0;           // % participacion en la GAB
    Double totaDiffMoney = 0.0;        // total diferencia monetaria
    Double reserveDiscount = 0.0;      // descuento de reserva
    Double discountGA = 0.0;           // descuento GA
}
```

---

## 7. Diagrama de Relaciones entre Entidades

```
                    RawMaterialPayRoll (planillapagomateriaprima)
                    |-- productiveZone --> ProductiveZone (zonaproductiva)
                    |-- metaProduct --> MetaProduct (metaproductoproduccion)
                    |-- company --> Company (compania)
                    |
                    |-- rawMaterialPayRecordList (1:N)
                        |
                        RawMaterialPayRecord (registropagomateriaprima)
                        |-- rawMaterialProducerDiscount (1:1)
                        |   |
                        |   RawMaterialProducerDiscount (descuentproductmateriaprima)
                        |   |-- rawMaterialProducer --> RawMaterialProducer (productormateriaprima)
                        |
                        |-- rawMaterialPayRoll --> (referencia al padre)

DATOS DE ACOPIO (lectura):

    RawMaterialCollectionSession (sesionacopio)
    |-- productiveZone --> ProductiveZone
    |-- metaProduct --> MetaProduct
    |-- collectedRawMaterialList (1:N)
        |
        CollectedRawMaterial (acopiomateriaprima)
        |-- rawMaterialProducer --> RawMaterialProducer
        |-- amount (litros entregados)

    CollectionForm (planillaacopio)
    |-- metaProduct --> MetaProduct
    |-- collectionRecordList (1:N)
        |
        CollectionRecord (registroacopio)
        |-- productiveZone --> ProductiveZone
        |-- receivedAmount (reportado por productores)
        |-- weightedAmount (pesado en balanza)

DESCUENTOS (lectura):

    SalaryMovementProducer (movimientosalarioproductor)
    |-- rawMaterialProducer --> RawMaterialProducer
    |-- typeMovementProducer --> TypeMovementProducer (tipomovimientoproductor)
    |-- productiveZone --> ProductiveZone
    |-- valor, fecha, estado

    SalaryMovementGAB (movimientosalariogab)
    |-- productiveZone --> ProductiveZone
    |-- typeMovementGAB --> TypeMovementGAB
    |-- valor (alcohol por GAB)

RESERVA:

    DiscountProducer (descuentoproductor)
    |-- reserve (tasa)
    |-- startDate, endDate
    |-- state (ENABLE/APPROVED)

    DiscountReserve (descuentoreserva)
    |-- discountProducer --> DiscountProducer
    |-- materialProducer --> RawMaterialProducer
    |-- amount, fechaini, fechafin
```

---

## 8. Constantes del Sistema

**Archivo:** `src/main/com/encens/khipus/util/Constants.java`

```java
PRICE_UNIT_MILK = 4.50       // Precio por litro de leche en Bs
DISCOUNT_GA     = 0.0        // Descuento GA (deshabilitado)
IT              = 0.3        // Impuesto a las Transacciones (%)
IUE             = 0.5        // Impuesto sobre Utilidades (%)
taxRate         = IT + IUE   // = 0.8% total
```

---

## 9. Flujo de Aprobacion

Despues de generar las planillas (estado PENDING), existe un proceso de aprobacion (`approvedPayRoll()`) que:

1. Verifica que existan planillas pendientes para el periodo
2. Aprueba sesiones de acopio (`RawMaterialCollectionSession` -> APPROVED)
3. Aprueba notas de rechazo (`RawMaterialRejectionNote` -> APPROVED)
4. Aprueba descuentos de productores (`SalaryMovementProducer` -> APPROVED)
5. Aprueba descuentos de GAB (`SalaryMovementGAB` -> APPROVED)
6. Aprueba la planilla de pago (`RawMaterialPayRoll` -> APPROVED)
7. Aprueba el descuento de reserva (`DiscountProducer` -> APPROVED)

---

## 10. Queries JPA Clave

### 10.1 Obtener productores con acopio en el periodo
```sql
-- RawMaterialPayRoll.findCollectedAmountByMetaProductBetweenDates
SELECT session.date, producer, collected.amount
FROM RawMaterialProducer producer
JOIN producer.collectedRawMaterialList collected
JOIN collected.rawMaterialCollectionSession session
JOIN session.productiveZone zone
WHERE session.metaProduct = :metaProduct
AND zone = :productiveZone
AND session.date BETWEEN :startDate AND :endDate
ORDER BY session.date
```

### 10.2 Obtener diferencias de peso por dia
```sql
-- RawMaterialPayRoll.differenceRawMaterialBetweenDates
SELECT record.collectionForm.date, record.receivedAmount, record.weightedAmount
FROM CollectionRecord record
JOIN record.collectionForm
WHERE record.collectionForm.date BETWEEN :startDate AND :endDate
AND record.productiveZone = :productiveZone
AND record.collectionForm.metaProduct = :metaProduct
ORDER BY record.collectionForm.date ASC
```

### 10.3 Obtener descuentos de productor
```sql
-- SalaryMovementProducer.getDiscount
SELECT movement.valor, type.typeMovement, type.name
FROM SalaryMovementProducer movement
JOIN movement.typeMovementProducer type
WHERE movement.date BETWEEN :startDate AND :endDate
AND movement.rawMaterialProducer = :rawMaterialProducer
AND movement.rawMaterialProducer.productiveZone = :productiveZone
```

### 10.4 Obtener alcohol por GAB
```sql
-- SalaryMovementGAB.getDiscount
SELECT gab.valor, type.typeMovement, type.name
FROM SalaryMovementGAB gab
JOIN gab.typeMovementGAB type
WHERE gab.date BETWEEN :startDate AND :endDate
AND gab.productiveZone = :productiveZone
```

---

## 11. Archivos Fuente Relevantes

| Archivo | Ruta |
|---------|------|
| Vista XHTML | `view/production/rawMaterialPayRoll.xhtml` |
| Action (controlador) | `src/main/com/encens/khipus/action/production/RawMaterialPayRollAction.java` |
| Servicio principal | `src/main/com/encens/khipus/service/production/RawMaterialPayRollServiceBean.java` |
| Interfaz servicio | `src/main/com/encens/khipus/service/production/RawMaterialPayRollService.java` |
| Entidad planilla | `src/main/com/encens/khipus/model/production/RawMaterialPayRoll.java` |
| Entidad registro pago | `src/main/com/encens/khipus/model/production/RawMaterialPayRecord.java` |
| Entidad descuentos | `src/main/com/encens/khipus/model/production/RawMaterialProducerDiscount.java` |
| Servicio descuentos | `src/main/com/encens/khipus/service/production/SalaryMavementProducerServiceBean.java` |
| Servicio alcohol GAB | `src/main/com/encens/khipus/service/production/SalaryMavementGABServiceBean.java` |
| Entidad sesion acopio | `src/main/com/encens/khipus/model/production/RawMaterialCollectionSession.java` |
| Entidad acopio productor | `src/main/com/encens/khipus/model/production/CollectedRawMaterial.java` |
| Entidad registro balanza | `src/main/com/encens/khipus/model/production/CollectionRecord.java` |
| Entidad planilla acopio | `src/main/com/encens/khipus/model/production/CollectionForm.java` |
| Entidad zona productiva | `src/main/com/encens/khipus/model/production/ProductiveZone.java` |
| Entidad reserva config | `src/main/com/encens/khipus/model/production/DiscountProducer.java` |
| Entidad reserva detalle | `src/main/com/encens/khipus/model/production/DiscountReserve.java` |
| Entidad mov. productor | `src/main/com/encens/khipus/model/production/SalaryMovementProducer.java` |
| Entidad mov. GAB | `src/main/com/encens/khipus/model/production/SalaryMovementGAB.java` |
| Enum Periodo | `src/main/com/encens/khipus/model/production/Periodo.java` |
| Constantes | `src/main/com/encens/khipus/util/Constants.java` |

---

*Documento generado como referencia para mejoras y modificaciones del modulo de planillas de acopio de leche.*
*Ultima actualizacion: 2026-03-11*

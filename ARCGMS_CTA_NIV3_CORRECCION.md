# Corrección de `cta_niv3` en tabla `arcgms`

**Fecha de análisis:** 2025-11-15
**Archivo analizado:** `tmp/arcgms.csv`
**Total de registros:** 875
**Registros nivel 1-2:** 23
**Registros nivel 3+:** 852
**Registros con error:** 15
**Porcentaje de error:** 1.71%

---

## 1. Regla Correcta para `cta_niv3`

La columna `cta_niv3` debe almacenar la cuenta de nivel 3 a la que pertenece cada cuenta.

### Estructura Correcta Según Nivel:

#### **Niveles 1 y 2:**
```
cta_niv3 = '' (vacío)
```
Las cuentas de nivel 1 y 2 NO tienen cuenta de nivel 3 padre.

#### **Niveles 3+:**
```
cta_niv3 = [3 primeros dígitos de 'cuenta'] + "0000000"
```

### Ejemplos:

| cuenta (10 dígitos) | cn_nivel | cta_niv3 CORRECTA | Explicación |
|---------------------|----------|-------------------|-------------|
| `1000000000` | 1 | *(vacío)* | Nivel 1 - No tiene nivel 3 padre |
| `1100000000` | 2 | *(vacío)* | Nivel 2 - No tiene nivel 3 padre |
| `1110000000` | 3 | `1110000000` | Nivel 3 - Primeros 3 dígitos = `111` → `1110000000` |
| `1110010000` | 4 | `1110000000` | Nivel 4 - Primeros 3 dígitos = `111` → `1110000000` |
| `1110010100` | 5 | `1110000000` | Nivel 5 - Primeros 3 dígitos = `111` → `1110000000` |
| `1110010101` | 6 | `1110000000` | Nivel 6 - Primeros 3 dígitos = `111` → `1110000000` |
| `3210000000` | 3 | `3210000000` | Nivel 3 - Primeros 3 dígitos = `321` → `3210000000` |
| `5350150000` | 4 | `5350000000` | Nivel 4 - Primeros 3 dígitos = `535` → `5350000000` |

---

## 2. Tipos de Errores Encontrados

### ✅ Buenas Noticias
**No se encontraron errores en niveles 1 y 2:** Todas las cuentas de nivel 1-2 tienen correctamente `cta_niv3` vacío.

### ❌ Errores en Niveles 3+
**15 registros con valor incorrecto**

Se identificaron 2 tipos de problemas:

1. **Valor incorrecto de nivel 3** (6 registros)
   - Cuentas de nivel 3 con referencia incorrecta a otra cuenta nivel 3
   - Ejemplo: `cuenta = 3210000000` tiene `cta_niv3 = 3110000000` ❌ (debería ser `3210000000`)

2. **Valor vacío cuando debería tener valor** (9 registros)
   - Cuentas de nivel 4-5 sin valor en `cta_niv3`
   - Ejemplo: `cuenta = 5350150000` (nivel 4) tiene `cta_niv3 = ''` ❌ (debería ser `5350000000`)

---

## 3. Listado de Registros Incorrectos

### 3.1 Errores en PATRIMONIO (3XXXXXXXXX) - Nivel 3

**Problema:** Todas estas cuentas tienen `cta_niv3 = 3110000000` cuando deberían apuntar a sí mismas.

| Cuenta | Descripción | cta_niv3 ACTUAL ❌ | cta_niv3 CORRECTA ✅ | Nivel |
|--------|-------------|-------------------|---------------------|-------|
| `3210000000` | APORTES POR CAPITALIZAR | `3110000000` | `3210000000` | 3 |
| `3310000000` | RESERVAS POR REVALUOS TECNICOS | `3110000000` | `3310000000` | 3 |
| `3410000000` | RESERVA LEGAL | `3110000000` | `3410000000` | 3 |
| `3510000000` | AJUSTES AL PATRIMONIO | `3110000000` | `3510000000` | 3 |
| `3610000000` | AJUSTE DE CAPITAL | `3110000000` | `3610000000` | 3 |
| `3710000000` | APORTES NO CAPITALIZABLES | `3110000000` | `3710000000` | 3 |

**Causa:** Todas apuntan incorrectamente a `3110000000` (CAPITAL SOCIAL) cuando deberían apuntar a sí mismas por ser cuentas de nivel 3.

---

### 3.2 Errores en COSTOS (5210XXXXXXX) - Nivel 4

| Cuenta | Descripción | cta_niv3 ACTUAL ❌ | cta_niv3 CORRECTA ✅ | Nivel |
|--------|-------------|-------------------|---------------------|-------|
| `5210060000` | DESCUENTO SOBRE SERVICIOS | *(vacío)* | `5210000000` | 4 |

**Causa:** Falta el valor `cta_niv3` cuando debería ser `5210000000`.

---

### 3.3 Errores en PROYECTOS (535XXXXXXXX) - Niveles 4-5

| Cuenta | Descripción | cta_niv3 ACTUAL ❌ | cta_niv3 CORRECTA ✅ | Nivel |
|--------|-------------|-------------------|---------------------|-------|
| `5350150000` | MATERIALES Y SUMINISTROS | *(vacío)* | `5350000000` | 4 |
| `5350150100` | MATERIALES Y SUMINISTROS | *(vacío)* | `5350000000` | 5 |
| `5350160000` | EQUIPO DE PROTECCION ALPERSONAL | *(vacío)* | `5350000000` | 4 |
| `5350160100` | EQUIPO DE PROTECCION ALPERSONAL | *(vacío)* | `5350000000` | 5 |
| `5350160200` | ROPA DE TRABAJO | *(vacío)* | `5350000000` | 5 |
| `5350170000` | HERRAMIENTAS A CORTO PLAZO | *(vacío)* | `5350000000` | 4 |
| `5350170100` | HERRAMIENTAS A CORTO PLAZO | *(vacío)* | `5350000000` | 5 |

**Causa:** Todas las cuentas `535XXXXXXX` tienen `cta_niv3` vacío cuando deberían tener `5350000000`.

---

### 3.4 Errores en GESTIONES VETAS (558XXXXXXXX) - Nivel 3

| Cuenta | Descripción | cta_niv3 ACTUAL ❌ | cta_niv3 CORRECTA ✅ | Nivel |
|--------|-------------|-------------------|---------------------|-------|
| `5580000000` | GESTIONES VETAS | *(vacío)* | `5580000000` | 3 |

**Causa:** Cuenta nivel 3 sin valor en `cta_niv3` (debería apuntar a sí misma).

---

## 4. Agrupación por Tipo de Corrección

### Resumen de correcciones necesarias:

| Cuenta Nivel 3 | Cantidad de registros | Tipo de Error | Acción |
|----------------|----------------------|---------------|---------|
| `321XXXXXXX` | 1 | Valor incorrecto (`3110000000` → `3210000000`) | UPDATE a sí misma |
| `331XXXXXXX` | 1 | Valor incorrecto (`3110000000` → `3310000000`) | UPDATE a sí misma |
| `341XXXXXXX` | 1 | Valor incorrecto (`3110000000` → `3410000000`) | UPDATE a sí misma |
| `351XXXXXXX` | 1 | Valor incorrecto (`3110000000` → `3510000000`) | UPDATE a sí misma |
| `361XXXXXXX` | 1 | Valor incorrecto (`3110000000` → `3610000000`) | UPDATE a sí misma |
| `371XXXXXXX` | 1 | Valor incorrecto (`3110000000` → `3710000000`) | UPDATE a sí misma |
| `521XXXXXXX` | 1 | Vacío → `5210000000` | UPDATE desde NULL |
| `535XXXXXXX` | 7 | Vacío → `5350000000` | UPDATE desde NULL |
| `558XXXXXXX` | 1 | Vacío → `5580000000` | UPDATE desde NULL |

**Total:** 15 registros

---

## 5. Consultas SQL de Corrección

### 5.1 Corrección Individual (Opción Segura)

```sql
-- ============================================
-- PATRIMONIO (32XXXXXXXX - 37XXXXXXXX) - NIVEL 3
-- Problema: Todas apuntan a 3110000000, deben apuntar a sí mismas
-- ============================================
UPDATE arcgms SET cta_niv3 = '3210000000' WHERE cuenta = '3210000000';
UPDATE arcgms SET cta_niv3 = '3310000000' WHERE cuenta = '3310000000';
UPDATE arcgms SET cta_niv3 = '3410000000' WHERE cuenta = '3410000000';
UPDATE arcgms SET cta_niv3 = '3510000000' WHERE cuenta = '3510000000';
UPDATE arcgms SET cta_niv3 = '3610000000' WHERE cuenta = '3610000000';
UPDATE arcgms SET cta_niv3 = '3710000000' WHERE cuenta = '3710000000';

-- ============================================
-- COSTOS (521XXXXXXXX) - NIVEL 4
-- Problema: cta_niv3 vacío
-- ============================================
UPDATE arcgms SET cta_niv3 = '5210000000' WHERE cuenta = '5210060000';

-- ============================================
-- PROYECTOS (535XXXXXXXX) - NIVELES 4-5
-- Problema: cta_niv3 vacío
-- ============================================
UPDATE arcgms SET cta_niv3 = '5350000000' WHERE cuenta = '5350150000';
UPDATE arcgms SET cta_niv3 = '5350000000' WHERE cuenta = '5350150100';
UPDATE arcgms SET cta_niv3 = '5350000000' WHERE cuenta = '5350160000';
UPDATE arcgms SET cta_niv3 = '5350000000' WHERE cuenta = '5350160100';
UPDATE arcgms SET cta_niv3 = '5350000000' WHERE cuenta = '5350160200';
UPDATE arcgms SET cta_niv3 = '5350000000' WHERE cuenta = '5350170000';
UPDATE arcgms SET cta_niv3 = '5350000000' WHERE cuenta = '5350170100';

-- ============================================
-- GESTIONES VETAS (558XXXXXXXX) - NIVEL 3
-- Problema: cta_niv3 vacío (debe apuntar a sí misma)
-- ============================================
UPDATE arcgms SET cta_niv3 = '5580000000' WHERE cuenta = '5580000000';
```

---

### 5.2 Corrección por Lotes (Opción Optimizada)

```sql
-- Corregir cuentas de patrimonio (nivel 3 que apuntan mal)
UPDATE arcgms
SET cta_niv3 = cuenta  -- Apuntar a sí mismas
WHERE cuenta IN (
    '3210000000', '3310000000', '3410000000',
    '3510000000', '3610000000', '3710000000'
);

-- Corregir cuenta de costos
UPDATE arcgms
SET cta_niv3 = '5210000000'
WHERE cuenta = '5210060000';

-- Corregir cuentas de proyectos (535XXXXXXX)
UPDATE arcgms
SET cta_niv3 = '5350000000'
WHERE cuenta IN (
    '5350150000', '5350150100', '5350160000',
    '5350160100', '5350160200', '5350170000',
    '5350170100'
);

-- Corregir cuenta de gestiones vetas
UPDATE arcgms
SET cta_niv3 = '5580000000'
WHERE cuenta = '5580000000';
```

---

### 5.3 Corrección Global con Fórmula (Opción Automática)

⚠️ **ADVERTENCIA:** Esta consulta corregirá automáticamente TODAS las cuentas. Usar con precaución.

```sql
-- Corregir cta_niv3 para todos los niveles
UPDATE arcgms
SET cta_niv3 = CASE
    WHEN cn_nivel IN (1, 2) THEN ''  -- Niveles 1-2: vacío
    ELSE CONCAT(SUBSTRING(cuenta, 1, 3), '0000000')  -- Nivel 3+: primeros 3 dígitos + 7 ceros
END
WHERE (
    -- Nivel 1-2 con valor cuando debería estar vacío
    (cn_nivel IN (1, 2) AND cta_niv3 != '')
    OR
    -- Nivel 3+ con valor incorrecto o vacío
    (cn_nivel NOT IN (1, 2) AND cta_niv3 != CONCAT(SUBSTRING(cuenta, 1, 3), '0000000'))
);
```

---

### 5.4 Validación Post-Corrección

Después de ejecutar las correcciones, ejecutar estas consultas para verificar:

#### **Validación para niveles 1-2:**
```sql
-- NO deben haber registros con cta_niv3 en niveles 1-2
SELECT
    cuenta,
    descri,
    cta_niv3,
    cn_nivel
FROM arcgms
WHERE cn_nivel IN (1, 2)
  AND cta_niv3 != ''
ORDER BY cuenta;
```
**Resultado esperado:** 0 filas

#### **Validación para niveles 3+:**
```sql
-- NO deben haber registros con cta_niv3 incorrecta en niveles 3+
SELECT
    cuenta,
    descri,
    cta_niv3 AS cta_niv3_actual,
    CONCAT(SUBSTRING(cuenta, 1, 3), '0000000') AS cta_niv3_correcta,
    cn_nivel
FROM arcgms
WHERE cn_nivel NOT IN (1, 2)
  AND cta_niv3 != CONCAT(SUBSTRING(cuenta, 1, 3), '0000000')
ORDER BY cuenta;
```
**Resultado esperado:** 0 filas

#### **Resumen general:**
```sql
-- Validación completa
SELECT
    CASE
        WHEN cn_nivel IN (1, 2) THEN 'NIVEL 1-2'
        ELSE 'NIVEL 3+'
    END AS grupo,
    COUNT(*) AS total,
    SUM(CASE
        WHEN cn_nivel IN (1, 2) AND cta_niv3 = '' THEN 1
        WHEN cn_nivel NOT IN (1, 2) AND cta_niv3 = CONCAT(SUBSTRING(cuenta, 1, 3), '0000000') THEN 1
        ELSE 0
    END) AS correctos,
    SUM(CASE
        WHEN cn_nivel IN (1, 2) AND cta_niv3 != '' THEN 1
        WHEN cn_nivel NOT IN (1, 2) AND cta_niv3 != CONCAT(SUBSTRING(cuenta, 1, 3), '0000000') THEN 1
        ELSE 0
    END) AS incorrectos
FROM arcgms
GROUP BY grupo;
```
**Resultado esperado:** incorrectos = 0 en ambos grupos

---

## 6. Impacto y Consideraciones

### 6.1 Tablas y Sistemas Afectados

La columna `cta_niv3` es utilizada en:

- **Dashboard de Finanzas** (`FinanceDashboardServlet.java`)
  ```sql
  LEFT JOIN arcgms ca3 ON ca.cta_niv3 = ca3.cuenta
  ```
  - Impacto: Los reportes agrupan subcuentas bajo su cuenta nivel 3
  - Errores actuales hacen que algunas cuentas se agrupen incorrectamente

- **Reportes Jerárquicos**
  - Estados financieros con drill-down por niveles
  - Reportes consolidados por grupos de cuentas

### 6.2 Impacto Específico de los Errores

#### **Cuentas de Patrimonio (321-371):**
- **Problema:** Se agrupan incorrectamente bajo `3110000000` (CAPITAL SOCIAL)
- **Impacto:** Reportes de patrimonio mezclan conceptos diferentes
- **Severidad:** ALTA - Afecta estados financieros

#### **Cuentas de Proyectos (535):**
- **Problema:** No se agrupan (cta_niv3 vacío)
- **Impacto:** Gastos de proyectos no aparecen en reportes consolidados
- **Severidad:** MEDIA - Datos existen pero no se agrupan

#### **Cuenta de Gestiones Vetas (558):**
- **Problema:** No se puede navegar jerárquicamente
- **Impacto:** Reportes de exploración incompletos
- **Severidad:** BAJA - Es cuenta nivel 3 sin subcuentas

### 6.3 Recomendaciones

1. **Ejecutar en entorno de desarrollo primero**
2. **Hacer backup de la tabla antes de corregir:**
   ```sql
   CREATE TABLE arcgms_backup_niv3 AS SELECT * FROM arcgms;
   ```
3. **Ejecutar corrección en orden:**
   - Primero: Cuentas de patrimonio (más críticas)
   - Segundo: Cuentas de proyectos
   - Tercero: Otras cuentas
4. **Ejecutar validación post-corrección**
5. **Verificar dashboard de finanzas**
6. **Regenerar reportes contables del período actual**

### 6.4 Prevención de Errores Futuros

Considerar agregar un trigger o constraint:

```sql
-- Trigger para validar cta_niv3 al insertar/actualizar
DELIMITER $$
CREATE TRIGGER arcgms_validate_cta_niv3
BEFORE INSERT ON arcgms
FOR EACH ROW
BEGIN
    DECLARE niv3_correcta VARCHAR(10);

    -- Calcular valor correcto según nivel
    IF NEW.cn_nivel IN (1, 2) THEN
        SET niv3_correcta = '';
    ELSE
        SET niv3_correcta = CONCAT(SUBSTRING(NEW.cuenta, 1, 3), '0000000');
    END IF;

    -- Validar
    IF NEW.cta_niv3 != niv3_correcta THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'cta_niv3 incorrecta: niveles 1-2 vacío, niveles 3+ = 3 dígitos + 7 ceros';
    END IF;
END$$
DELIMITER ;
```

---

## 7. Análisis de Patrones

### 7.1 Patrón de Error en Patrimonio

**Todas las cuentas nivel 3 de patrimonio** (excepto `3110000000`) apuntan incorrectamente a `3110000000`:

```
3110000000 (CAPITAL SOCIAL)          ✅ cta_niv3 = 3110000000 (correcto)
3210000000 (APORTES POR CAPITALIZAR) ❌ cta_niv3 = 3110000000 (debe ser 3210000000)
3310000000 (RESERVAS POR REVALUOS)   ❌ cta_niv3 = 3110000000 (debe ser 3310000000)
3410000000 (RESERVA LEGAL)           ❌ cta_niv3 = 3110000000 (debe ser 3410000000)
...
```

**Causa probable:** Error de copia/pegado o migración de datos inicial.

### 7.2 Patrón de Error en Proyectos

**Todas las subcuentas de `5350000000` (PROYECTOS)** tienen `cta_niv3` vacío:

```
5350000000 (PROYECTOS)                   ✅ cta_niv3 = 5350000000 (correcto)
5350150000 (MATERIALES Y SUMINISTROS)    ❌ cta_niv3 = '' (debe ser 5350000000)
5350160000 (EQUIPO DE PROTECCION)        ❌ cta_niv3 = '' (debe ser 5350000000)
5350170000 (HERRAMIENTAS)                ❌ cta_niv3 = '' (debe ser 5350000000)
```

**Causa probable:** Cuentas añadidas posteriormente sin completar `cta_niv3`.

---

## 8. Archivos Generados

- **`tmp/arcgms.csv`** - Datos originales exportados
- **`tmp/arcgms_errores_niv3.csv`** - Listado de 15 registros con error
- **`tmp/analyze_arcgms_niv3.py`** - Script Python de análisis
- **`ARCGMS_CTA_NIV3_CORRECCION.md`** - Este documento

---

## 9. Relación con Errores de `cta_raiz`

**Nota:** Algunos registros tienen errores en AMBAS columnas (`cta_raiz` Y `cta_niv3`):

| cuenta | Error en cta_raiz | Error en cta_niv3 | Acción |
|--------|-------------------|-------------------|---------|
| `5210060000` | ❌ | ❌ | Corregir ambas |
| `5350150000` | ✅ | ❌ | Solo corregir cta_niv3 |
| `5350160000` | ✅ | ❌ | Solo corregir cta_niv3 |
| `5350170000` | ❌ | ❌ | Corregir ambas |
| `5350170100` | ❌ | ❌ | Corregir ambas |

**Recomendación:** Ejecutar ambas correcciones en una sola transacción o en secuencia inmediata.

---

**Documento generado:** 2025-11-15
**Autor:** Claude Code
**Versión:** 1.0

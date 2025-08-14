# Validador de Plan de Cuentas Contables - SISK13

## 📋 Descripción

Este script automatiza la validación semanal del plan de cuentas contables, verificando que todas las cuentas tengan los niveles correctamente asignados según la estructura jerárquica del sistema KHIPUS.

## 🚀 Uso Rápido

### Validación básica (uso más común):
```bash
python scripts/validar_plan_cuentas.py
```

### Validación con archivo específico:
```bash
python scripts/validar_plan_cuentas.py ruta/al/archivo.xlsx
```

### Generar reporte en archivo:
```bash
python scripts/validar_plan_cuentas.py --reporte reporte_validacion.txt
```

### Modo silencioso (solo resumen):
```bash
python scripts/validar_plan_cuentas.py --silencioso
```

## 📊 Estructura de Niveles

El script valida que las cuentas sigan esta estructura jerárquica:

| Nivel | Formato | Ejemplo | Descripción |
|-------|---------|---------|-------------|
| **1** | `X000000000` | `1000000000` | 1 dígito + 9 ceros |
| **2** | `XX00000000` | `1100000000` | 2 dígitos + 8 ceros |
| **3** | `XXX0000000` | `1110000000` | 3 dígitos + 7 ceros |
| **4** | `XXXXXX0000` | `1110010000` | 6 dígitos + 4 ceros |
| **5** | `XXXXXXXX00` | `1110010100` | 8 dígitos + 2 ceros |
| **6** | `XXXXXXXXXX` | `1110040101` | 10 dígitos completos |

## 📁 Archivos Requeridos

El script espera encontrar un archivo Excel con las siguientes columnas:
- `cuenta`: Código de la cuenta (10 dígitos)
- `cn_nivel`: Nivel asignado a la cuenta (1-6)
- `descri`: Descripción de la cuenta (opcional)

## 🔄 Automatización Semanal

### Opción 1: Programador de Tareas Windows
1. Abrir "Programador de tareas"
2. Crear tarea básica
3. Configurar para ejecutarse semanalmente
4. Acción: Iniciar programa
   - Programa: `python`
   - Argumentos: `scripts/validar_plan_cuentas.py --reporte --silencioso`
   - Directorio: `D:\Intellij\sisk13`

### Opción 2: Script Batch (.bat)
Crear archivo `validacion_semanal.bat`:
```batch
@echo off
cd /d "D:\Intellij\sisk13"
python scripts/validar_plan_cuentas.py --reporte reporte_semanal_%date:~6,4%%date:~3,2%%date:~0,2%.txt
pause
```

### Opción 3: Task Scheduler con PowerShell
```powershell
# Crear tarea programada
$Action = New-ScheduledTaskAction -Execute "python" -Argument "scripts/validar_plan_cuentas.py --reporte" -WorkingDirectory "D:\Intellij\sisk13"
$Trigger = New-ScheduledTaskTrigger -Weekly -WeeksInterval 1 -DaysOfWeek Monday -At 9:00AM
$Settings = New-ScheduledTaskSettingsSet -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries
Register-ScheduledTask -TaskName "Validacion Plan Cuentas" -Action $Action -Trigger $Trigger -Settings $Settings
```

## 📈 Interpretación de Resultados

### ✅ Resultado Exitoso:
```
📊 RESULTADO DE LA VALIDACIÓN
====================================
📅 Fecha: 2025-01-12 14:30:00
📋 Total de cuentas validadas: 860
❌ Cuentas con errores: 0
📊 Precisión: 100.0%

🎉 ¡EXCELENTE! Todas las cuentas tienen niveles correctamente asignados.
```

### ❌ Resultado con Errores:
```
📊 RESULTADO DE LA VALIDACIÓN
====================================
📋 Total de cuentas validadas: 860
❌ Cuentas con errores: 3
📊 Precisión: 99.7%

🚨 SE ENCONTRARON 3 ERRORES:
Fila   Cuenta        Nivel Asig.  Nivel Correcto  Descripción
--------------------------------------------------------------
45     1110050500    4            5               BANCOS CUENTA CORRIENTE...
128    2110020300    3            4               PROVEEDORES VARIOS...
```

## 🛠️ Opciones Avanzadas

### Parámetros del Script:
- `archivo`: Ruta al archivo Excel (opcional, por defecto: `view/plan_ctas/plan_cuentas.xlsx`)
- `--reporte, -r`: Generar reporte en archivo de texto
- `--silencioso, -s`: Mostrar solo resumen sin detalles

### Ejemplos Avanzados:
```bash
# Validar archivo específico con reporte detallado
python scripts/validar_plan_cuentas.py backup/plan_cuentas_enero.xlsx --reporte reporte_enero.txt

# Validación rápida solo con resumen
python scripts/validar_plan_cuentas.py --silencioso

# Generar reporte con timestamp automático
python scripts/validar_plan_cuentas.py --reporte
```

## 🔍 Solución de Problemas

### Error: "No se encontró el archivo"
- Verificar que existe `view/plan_ctas/plan_cuentas.xlsx`
- Especificar la ruta completa del archivo

### Error: "Missing optional dependency 'openpyxl'"
```bash
pip install openpyxl pandas
```

### Error: "El archivo debe contener las columnas"
- Verificar que el Excel tenga las columnas `cuenta` y `cn_nivel`
- Revisar que no haya espacios extra en los nombres de columnas

## 📞 Soporte

Para reportar problemas o sugerencias:
1. Verificar que el archivo Excel esté en el formato correcto
2. Revisar los logs de error del script
3. Contactar al equipo de desarrollo del sistema KHIPUS

---
**Última actualización**: 2025-01-12  
**Versión**: 1.0  
**Autor**: Claude Code
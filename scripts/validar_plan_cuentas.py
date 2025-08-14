#!/usr/bin/env python3
"""
Validador de Plan de Cuentas Contables - SISK13 (KHIPUS)
========================================================

Este script valida que las cuentas contables tengan los niveles correctamente asignados
según la estructura jerárquica del plan de cuentas.

Uso:
    python validar_plan_cuentas.py [ruta_archivo_excel]
    
    Si no se especifica ruta, usa: plan_ctas/plan_cuentas.xlsx

Estructura de niveles:
    - Nivel 1: X000000000 (1 dígito + 9 ceros)
    - Nivel 2: XX00000000 (2 dígitos + 8 ceros)  
    - Nivel 3: XXX0000000 (3 dígitos + 7 ceros)
    - Nivel 4: XXXXXX0000 (6 dígitos + 4 ceros)
    - Nivel 5: XXXXXXXX00 (8 dígitos + 2 ceros)
    - Nivel 6: XXXXXXXXXX (10 dígitos completos)

Autor: Claude Code
Fecha: 2025-01-12
"""

import pandas as pd
import sys
import os
from datetime import datetime
import argparse

class ValidadorPlanCuentas:
    def __init__(self, archivo_excel=None):
        self.archivo_excel = archivo_excel or 'plan_ctas/plan_cuentas.xlsx'
        self.df = None
        self.errores = []
        self.total_cuentas = 0
        
    def cargar_archivo(self):
        """Carga el archivo Excel del plan de cuentas"""
        try:
            if not os.path.exists(self.archivo_excel):
                raise FileNotFoundError(f"No se encontró el archivo: {self.archivo_excel}")
                
            print(f"Cargando archivo: {self.archivo_excel}")
            self.df = pd.read_excel(self.archivo_excel)
            
            # Filtrar solo filas con datos válidos
            self.df = self.df[self.df['cuenta'].notna()].copy()
            
            print(f"Archivo cargado exitosamente - {len(self.df)} cuentas encontradas")
            
            # Verificar columnas requeridas
            if 'cuenta' not in self.df.columns or 'cn_nivel' not in self.df.columns:
                raise ValueError("El archivo debe contener las columnas 'cuenta' y 'cn_nivel'")
                
            return True
            
        except Exception as e:
            print(f"ERROR al cargar el archivo: {e}")
            return False
    
    def calcular_nivel_correcto(self, cuenta_str):
        """Calcula el nivel correcto basado en la estructura de la cuenta"""
        cuenta = str(int(float(cuenta_str))).zfill(10)
        
        # Aplicar las reglas de niveles
        if cuenta[1:] == '000000000':
            return 1  # X000000000
        elif cuenta[2:] == '00000000':
            return 2  # XX00000000
        elif cuenta[3:] == '0000000':
            return 3  # XXX0000000
        elif cuenta[6:] == '0000':
            return 4  # XXXXXX0000
        elif cuenta[8:] == '00':
            return 5  # XXXXXXXX00
        else:
            return 6  # XXXXXXXXXX
    
    def validar_cuentas(self):
        """Valida todas las cuentas del plan"""
        print("\nIniciando validacion de niveles...")
        
        self.errores = []
        self.total_cuentas = 0
        
        for i, row in self.df.iterrows():
            cuenta_num = row['cuenta']
            nivel_asignado = int(row['cn_nivel']) if pd.notna(row['cn_nivel']) else None
            
            if nivel_asignado is None:
                continue
                
            self.total_cuentas += 1
            cuenta_str = str(int(cuenta_num)).zfill(10)
            nivel_correcto = self.calcular_nivel_correcto(cuenta_num)
            
            if nivel_asignado != nivel_correcto:
                self.errores.append({
                    'fila_excel': i + 2,  # +2 porque Excel cuenta desde 1 y hay header
                    'cuenta': cuenta_str,
                    'descripcion': row['descri'] if pd.notna(row['descri']) else 'Sin descripcion',
                    'nivel_asignado': nivel_asignado,
                    'nivel_correcto': nivel_correcto,
                })
        
        print(f"Validacion completada")
        return len(self.errores) == 0
    
    def mostrar_resultados(self, detallado=True):
        """Muestra los resultados de la validación"""
        print(f"\n{'='*60}")
        print(f"RESULTADO DE LA VALIDACION")
        print(f"{'='*60}")
        print(f"Fecha: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        print(f"Archivo: {self.archivo_excel}")
        print(f"Total de cuentas validadas: {self.total_cuentas}")
        print(f"Cuentas con errores: {len(self.errores)}")
        print(f"Cuentas correctas: {self.total_cuentas - len(self.errores)}")
        print(f"Precision: {((self.total_cuentas - len(self.errores)) / self.total_cuentas * 100):.1f}%")
        
        if self.errores:
            print(f"\nSE ENCONTRARON {len(self.errores)} ERRORES:")
            print("="*80)
            print(f"{'Fila':<6} {'Cuenta':<12} {'Nivel Asig.':<12} {'Nivel Correcto':<15} {'Descripcion'}")
            print("-"*80)
            
            for error in self.errores[:20]:  # Mostrar maximo 20 errores
                desc_corta = error['descripcion'][:35] + '...' if len(error['descripcion']) > 35 else error['descripcion']
                print(f"{error['fila_excel']:<6} {error['cuenta']:<12} {error['nivel_asignado']:<12} {error['nivel_correcto']:<15} {desc_corta}")
            
            if len(self.errores) > 20:
                print(f"\n... y {len(self.errores) - 20} errores mas.")
            
            if detallado and len(self.errores) <= 5:
                print(f"\nDETALLES DE LOS ERRORES:")
                for i, error in enumerate(self.errores, 1):
                    print(f"\n{i}. Cuenta {error['cuenta']} (Fila Excel {error['fila_excel']})")
                    print(f"   Descripcion: {error['descripcion']}")
                    print(f"   Nivel asignado: {error['nivel_asignado']} -> Nivel correcto: {error['nivel_correcto']}")
                    print(f"   Razon: Segun la estructura jerarquica deberia ser nivel {error['nivel_correcto']}")
        else:
            print(f"\nEXCELENTE! Todas las cuentas tienen niveles correctamente asignados.")
            print("No se encontraron errores en la asignacion de niveles.")
    
    def generar_sql_actualizacion(self, archivo_sql=None):
        """Genera archivo SQL con las actualizaciones necesarias para cn_nivel"""
        if archivo_sql is None:
            archivo_sql = 'plan_ctas/update_cn_nivel.sql'
        
        try:
            with open(archivo_sql, 'w', encoding='utf-8') as f:
                f.write(f"-- ACTUALIZACION DE CN_NIVEL - PLAN DE CUENTAS CONTABLES\n")
                f.write(f"-- ========================================================\n")
                f.write(f"-- Fecha: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
                f.write(f"-- Archivo origen: {self.archivo_excel}\n")
                f.write(f"-- Tabla destino: arcgms\n")
                f.write(f"-- Total de cuentas validadas: {self.total_cuentas}\n")
                f.write(f"-- Errores encontrados: {len(self.errores)}\n\n")
                
                if self.errores:
                    f.write(f"-- ACTUALIZACIONES NECESARIAS:\n")
                    f.write(f"-- {'-' * 50}\n\n")
                    
                    for error in self.errores:
                        f.write(f"-- Cuenta: {error['cuenta']} | Nivel actual: {error['nivel_asignado']} -> Correcto: {error['nivel_correcto']}\n")
                        f.write(f"UPDATE arcgms SET cn_nivel = {error['nivel_correcto']} WHERE cuenta = {error['cuenta']};\n\n")
                    
                    f.write(f"-- TOTAL DE ACTUALIZACIONES: {len(self.errores)}\n")
                else:
                    f.write(f"-- RESULTADO: No se requieren actualizaciones.\n")
                    f.write(f"-- Todas las cuentas tienen cn_nivel correctamente asignado.\n")
            
            print(f"\nArchivo SQL generado: {archivo_sql}")
            return archivo_sql
            
        except Exception as e:
            print(f"Error al generar archivo SQL: {e}")
            return None
    
    def generar_reporte_archivo(self, archivo_salida=None):
        """Genera un reporte en archivo de texto"""
        if archivo_salida is None:
            timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
            archivo_salida = f'scripts/reporte_validacion_{timestamp}.txt'
        
        try:
            with open(archivo_salida, 'w', encoding='utf-8') as f:
                f.write(f"REPORTE DE VALIDACIÓN - PLAN DE CUENTAS CONTABLES\n")
                f.write(f"={'='*60}\n")
                f.write(f"Fecha: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
                f.write(f"Archivo validado: {self.archivo_excel}\n")
                f.write(f"Total de cuentas: {self.total_cuentas}\n")
                f.write(f"Errores encontrados: {len(self.errores)}\n")
                f.write(f"Precisión: {((self.total_cuentas - len(self.errores)) / self.total_cuentas * 100):.1f}%\n\n")
                
                if self.errores:
                    f.write("CUENTAS CON ERRORES:\n")
                    f.write("-" * 80 + "\n")
                    f.write(f"{'Fila':<6} {'Cuenta':<12} {'Nivel Asig.':<12} {'Nivel Correcto':<15} {'Descripción'}\n")
                    f.write("-" * 80 + "\n")
                    
                    for error in self.errores:
                        f.write(f"{error['fila_excel']:<6} {error['cuenta']:<12} {error['nivel_asignado']:<12} {error['nivel_correcto']:<15} {error['descripcion']}\n")
                else:
                    f.write("RESULTADO: Todas las cuentas estan correctamente asignadas.\n")
            
            print(f"\nReporte guardado en: {archivo_salida}")
            return archivo_salida
            
        except Exception as e:
            print(f"Error al generar reporte: {e}")
            return None

def main():
    """Función principal"""
    parser = argparse.ArgumentParser(
        description='Validador de niveles del plan de cuentas contables',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Ejemplos:
  python validar_plan_cuentas.py
  python validar_plan_cuentas.py plan_ctas/plan_cuentas.xlsx
  python validar_plan_cuentas.py --reporte archivo.txt
        """
    )
    
    parser.add_argument('archivo', nargs='?', 
                       help='Ruta al archivo Excel del plan de cuentas')
    parser.add_argument('--reporte', '-r', 
                       help='Generar reporte en archivo de texto')
    parser.add_argument('--silencioso', '-s', action='store_true',
                       help='Mostrar solo el resumen, sin detalles')
    
    args = parser.parse_args()
    
    print("VALIDADOR DE PLAN DE CUENTAS CONTABLES - SISK13")
    print("=" * 50)
    
    # Crear validador
    validador = ValidadorPlanCuentas(args.archivo)
    
    # Cargar archivo
    if not validador.cargar_archivo():
        sys.exit(1)
    
    # Validar cuentas
    resultado_ok = validador.validar_cuentas()
    
    # Mostrar resultados
    validador.mostrar_resultados(detallado=not args.silencioso)
    
    # Generar archivo SQL con actualizaciones
    validador.generar_sql_actualizacion()
    
    # Generar reporte en archivo si se solicita
    if args.reporte:
        validador.generar_reporte_archivo(args.reporte)
    
    # Código de salida
    sys.exit(0 if resultado_ok else 1)

if __name__ == "__main__":
    main()
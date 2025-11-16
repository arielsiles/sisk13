import csv

errores = []
total = 0
nivel_1_2 = 0

with open('arcgms.csv', 'r', encoding='utf-8-sig') as f:
    reader = csv.DictReader(f, delimiter='|')
    for row in reader:
        total += 1
        cuenta = row['cuenta']
        cta_niv3_actual = row['cta_niv3']
        cn_nivel = row['cn_nivel']
        descri = row['descri']

        # Validar según el nivel
        if cn_nivel in ('1', '2'):
            # Niveles 1 y 2: cta_niv3 DEBE estar vacío
            nivel_1_2 += 1
            if cta_niv3_actual != '':
                errores.append({
                    'cuenta': cuenta,
                    'descri': descri,
                    'cta_niv3_actual': cta_niv3_actual,
                    'cta_niv3_correcta': '',  # Debe estar vacío
                    'cn_nivel': cn_nivel,
                    'tipo_error': 'DEBE_ESTAR_VACIO'
                })

        else:
            # Niveles 3+: cta_niv3 = primeros 3 dígitos + 7 ceros
            if len(cuenta) >= 3:
                cta_niv3_correcta = cuenta[:3] + '0000000'

                if cta_niv3_actual != cta_niv3_correcta:
                    errores.append({
                        'cuenta': cuenta,
                        'descri': descri,
                        'cta_niv3_actual': cta_niv3_actual,
                        'cta_niv3_correcta': cta_niv3_correcta,
                        'cn_nivel': cn_nivel,
                        'tipo_error': 'VALOR_INCORRECTO'
                    })

# Estadísticas
errores_nivel_1_2 = [e for e in errores if e['tipo_error'] == 'DEBE_ESTAR_VACIO']
errores_nivel_3_mas = [e for e in errores if e['tipo_error'] == 'VALOR_INCORRECTO']

print(f'=== ANÁLISIS CTA_NIV3 ===')
print(f'Total registros: {total}')
print(f'Registros nivel 1-2: {nivel_1_2}')
print(f'Registros nivel 3+: {total - nivel_1_2}')
print(f'')
print(f'Total errores encontrados: {len(errores)}')
print(f'  - Nivel 1-2 con valor (debe estar vacío): {len(errores_nivel_1_2)}')
print(f'  - Nivel 3+ con valor incorrecto: {len(errores_nivel_3_mas)}')
print(f'Porcentaje errores: {len(errores)/total*100:.2f}%')
print()

if len(errores_nivel_1_2) > 0:
    print('=== ERRORES NIVEL 1-2 (deben estar vacíos) ===')
    print('cuenta|cta_niv3_actual|cn_nivel|descri')
    for error in errores_nivel_1_2[:10]:
        print(f"{error['cuenta']}|{error['cta_niv3_actual']}|{error['cn_nivel']}|{error['descri'][:50]}")
    if len(errores_nivel_1_2) > 10:
        print(f"... y {len(errores_nivel_1_2) - 10} más")
    print()

if len(errores_nivel_3_mas) > 0:
    print('=== ERRORES NIVEL 3+ (primeros 30) ===')
    print('cuenta|cta_niv3_actual|cta_niv3_correcta|cn_nivel|descri')
    for error in errores_nivel_3_mas[:30]:
        print(f"{error['cuenta']}|{error['cta_niv3_actual']}|{error['cta_niv3_correcta']}|{error['cn_nivel']}|{error['descri'][:50]}")
    if len(errores_nivel_3_mas) > 30:
        print(f"... y {len(errores_nivel_3_mas) - 30} más")

# Guardar todos los errores en CSV
with open('arcgms_errores_niv3.csv', 'w', encoding='utf-8', newline='') as f:
    writer = csv.DictWriter(f, fieldnames=['cuenta', 'descri', 'cta_niv3_actual', 'cta_niv3_correcta', 'cn_nivel', 'tipo_error'])
    writer.writeheader()
    writer.writerows(errores)

print(f"\nErrores guardados en: arcgms_errores_niv3.csv")

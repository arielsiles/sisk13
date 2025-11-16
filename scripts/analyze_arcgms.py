import csv

errores = []
total = 0

with open('arcgms.csv', 'r', encoding='utf-8-sig') as f:
    reader = csv.DictReader(f, delimiter='|')
    for row in reader:
        total += 1
        cuenta = row['cuenta']
        cta_raiz_actual = row['cta_raiz']
        cn_nivel = row['cn_nivel']
        descri = row['descri']

        if len(cuenta) >= 2:
            cta_raiz_correcta = cuenta[:2] + '00000000'

            if cta_raiz_actual != cta_raiz_correcta and cta_raiz_actual != '':
                errores.append({
                    'cuenta': cuenta,
                    'descri': descri,
                    'cta_raiz_actual': cta_raiz_actual,
                    'cta_raiz_correcta': cta_raiz_correcta,
                    'cn_nivel': cn_nivel
                })

print(f'Total registros: {total}')
print(f'Registros con error: {len(errores)}')
print(f'Porcentaje errores: {len(errores)/total*100:.2f}%')
print()
print('Primeros 30 errores:')
print('cuenta|cta_raiz_actual|cta_raiz_correcta|cn_nivel|descri')
for error in errores[:30]:
    print(f"{error['cuenta']}|{error['cta_raiz_actual']}|{error['cta_raiz_correcta']}|{error['cn_nivel']}|{error['descri'][:50]}")

# Guardar todos los errores en CSV
with open('arcgms_errores.csv', 'w', encoding='utf-8', newline='') as f:
    writer = csv.DictWriter(f, fieldnames=['cuenta', 'descri', 'cta_raiz_actual', 'cta_raiz_correcta', 'cn_nivel'])
    writer.writeheader()
    writer.writerows(errores)

print(f"\nErrores guardados en: arcgms_errores.csv")

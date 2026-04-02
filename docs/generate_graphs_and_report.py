#!/usr/bin/env python3
"""
Генерирует графики по CSV-выгрузкам всех модулей и обновляет отчёт report_v777.docx
"""

import os
import csv
import math
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from docx import Document
from docx.shared import Inches, Pt
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml.ns import qn
from docx.oxml import OxmlElement
from docx.text.paragraph import Paragraph

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
RESOURCES = os.path.join(BASE_DIR, '..', 'src', 'test', 'resources')
GRAPHS_DIR = os.path.join(BASE_DIR, 'graphs')
os.makedirs(GRAPHS_DIR, exist_ok=True)

# ============================================================
# Данные CSV
# ============================================================

def load_csv(path):
    xs, ys = [], []
    with open(path) as f:
        reader = csv.DictReader(f)
        for row in reader:
            xs.append(float(row['x']))
            ys.append(float(row['y']))
    return xs, ys


# ============================================================
# Функции построения графиков
# ============================================================

def plot_simple(xs, ys, title, color, filename):
    fig, ax = plt.subplots(figsize=(7, 4))
    ax.plot(xs, ys, color=color, linewidth=2, marker='o', markersize=5)
    ax.axhline(0, color='black', linewidth=0.8)
    ax.axvline(0, color='black', linewidth=0.8)
    ax.set_title(title, fontsize=13, pad=10)
    ax.grid(True, alpha=0.3)
    plt.tight_layout()
    path = os.path.join(GRAPHS_DIR, filename)
    fig.savefig(path, dpi=120, bbox_inches='tight')
    plt.close(fig)
    return path


def plot_multi(datasets, title, filename, log_x=False):
    fig, ax = plt.subplots(figsize=(8, 5))
    for ds in datasets:
        ax.plot(ds['x'], ds['y'], color=ds['color'], linewidth=2,
                marker='o', markersize=4, label=ds['label'])
    ax.axhline(0, color='black', linewidth=0.8)
    ax.axvline(0, color='black', linewidth=0.8)
    if log_x:
        ax.set_xscale('log')
    ax.set_title(title, fontsize=13, pad=10)
    ax.grid(True, alpha=0.3)
    ax.legend()
    plt.tight_layout()
    path = os.path.join(GRAPHS_DIR, filename)
    fig.savefig(path, dpi=120, bbox_inches='tight')
    plt.close(fig)
    return path


def plot_clipped(xs, ys, title, color, filename, ylim=(-20, 20)):
    """График с обрезкой по оси Y для функций с асимптотами"""
    fig, ax = plt.subplots(figsize=(7, 4))
    pts = sorted(zip(xs, ys))
    xs_s = [p[0] for p in pts]
    ys_clipped = [y if ylim[0] <= y <= ylim[1] else None for y in [p[1] for p in pts]]
    # Рисуем отдельные сегменты, разрывая там где None
    seg_x, seg_y, cx, cy = [], [], [], []
    for xv, yv in zip(xs_s, ys_clipped):
        if yv is None:
            if cx:
                seg_x.append(cx); seg_y.append(cy); cx, cy = [], []
        else:
            cx.append(xv); cy.append(yv)
    if cx:
        seg_x.append(cx); seg_y.append(cy)
    for sx, sy in zip(seg_x, seg_y):
        ax.plot(sx, sy, color=color, linewidth=2, marker='o', markersize=5)
    ax.set_ylim(ylim)
    ax.axhline(0, color='black', linewidth=0.8)
    ax.axvline(0, color='black', linewidth=0.8)
    ax.set_title(title, fontsize=13, pad=10)
    ax.grid(True, alpha=0.3)
    plt.tight_layout()
    path = os.path.join(GRAPHS_DIR, filename)
    fig.savefig(path, dpi=120, bbox_inches='tight')
    plt.close(fig)
    return path


def plot_system(xs_lt0, ys_lt0, xs_gt0, ys_gt0, filename):
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(12, 5))
    # x <= 0
    pts = sorted(zip(xs_lt0, ys_lt0))
    xs_s = [p[0] for p in pts]
    ys_m = [y if -30 <= y <= 30 else None for y in [p[1] for p in pts]]
    seg_x, seg_y, cx, cy = [], [], [], []
    for xv, yv in zip(xs_s, ys_m):
        if yv is None:
            if cx: seg_x.append(cx); seg_y.append(cy); cx, cy = [], []
        else:
            cx.append(xv); cy.append(yv)
    if cx: seg_x.append(cx); seg_y.append(cy)
    for sx, sy in zip(seg_x, seg_y):
        ax1.plot(sx, sy, color='#E63946', linewidth=2, marker='o', markersize=5)
    ax1.set_ylim(-30, 30)
    ax1.axhline(0, color='black', linewidth=0.8); ax1.axvline(0, color='black', linewidth=0.8)
    ax1.set_title('Система: x ≤ 0', fontsize=12); ax1.grid(True, alpha=0.3)
    ax1.set_xlabel('x'); ax1.set_ylabel('f(x)')
    # x > 0
    pts2 = sorted(zip(xs_gt0, ys_gt0))
    ax2.plot([p[0] for p in pts2], [p[1] for p in pts2], color='#457B9D', linewidth=2, marker='o', markersize=5)
    ax2.axhline(0, color='black', linewidth=0.8)
    ax2.set_title('Система: x > 0', fontsize=12); ax2.grid(True, alpha=0.3)
    ax2.set_xlabel('x'); ax2.set_ylabel('f(x)')
    plt.tight_layout()
    path = os.path.join(GRAPHS_DIR, filename)
    fig.savefig(path, dpi=120, bbox_inches='tight')
    plt.close(fig)
    return path


# ============================================================
# Генерация всех графиков
# ============================================================

print("Генерирую графики...")

# Уровень 0: sin (аналитические данные — нет CSV)
sin_pts = [(-math.pi, 0), (-math.pi*3/4, -math.sqrt(2)/2), (-math.pi/2, -1),
           (-math.pi/4, -math.sqrt(2)/2), (0, 0), (math.pi/4, math.sqrt(2)/2),
           (math.pi/2, 1), (math.pi*3/4, math.sqrt(2)/2), (math.pi, 0)]
g_sin = plot_simple([p[0] for p in sin_pts], [p[1] for p in sin_pts],
    'sin(x) — базовая функция (ряд Тейлора)', '#E63946', 'sin.png')

# Уровень 0: cos
cos_xs, cos_ys = load_csv(os.path.join(RESOURCES, 'cos.csv'))
g_cos = plot_clipped(cos_xs, cos_ys, 'cos(x) = sin(π/2 − x) — через Sine', '#E07A5F', 'cos.png', ylim=(-2, 2))

# Уровень 0: ln (аналитические данные)
ln_xs = [0.1, 0.25, 0.5, 1, 2, math.e, 5, 10, 20]
g_ln = plot_simple(ln_xs, [math.log(x) for x in ln_xs],
    'ln(x) — базовая функция (ряд Тейлора)', '#2A9D8F', 'ln.png')

# Уровень 0: log2, log10
log2_xs = [0.25, 0.5, 1, 2, 4, 8, 16, 32]
lg_xs = [0.1, 1, 2, 5, 10, 50, 100, 1000]
g_logs_base = plot_multi([
    {'x': log2_xs, 'y': [math.log2(x) for x in log2_xs], 'label': 'log₂(x)', 'color': '#457B9D'},
    {'x': lg_xs, 'y': [math.log10(x) for x in lg_xs], 'label': 'log₁₀(x)', 'color': '#1D3557'},
], 'log₂(x) и log₁₀(x) = ln(x)/ln(base)', 'logs_base.png')

# Уровень 1: sec, csc, tan, cot (module-тесты)
sec_xs, sec_ys = load_csv(os.path.join(RESOURCES, 'sec.csv'))
g_sec = plot_clipped(sec_xs, sec_ys, 'sec(x) = 1/cos(x) — module-тест', '#F4A261', 'sec_module.png')

csc_xs, csc_ys = load_csv(os.path.join(RESOURCES, 'csc.csv'))
g_csc = plot_clipped(csc_xs, csc_ys, 'csc(x) = 1/sin(x) — module-тест', '#E9C46A', 'csc_module.png')

tan_xs, tan_ys = load_csv(os.path.join(RESOURCES, 'tan.csv'))
g_tan = plot_clipped(tan_xs, tan_ys, 'tan(x) = sin(x)/cos(x) — module-тест', '#2A9D8F', 'tan_module.png', ylim=(-10, 10))

cot_xs, cot_ys = load_csv(os.path.join(RESOURCES, 'cot.csv'))
g_cot = plot_clipped(cot_xs, cot_ys, 'cot(x) = cos(x)/sin(x) — module-тест', '#264653', 'cot_module.png', ylim=(-10, 10))

# Уровень 2: интеграционные тесты с моками
sec_it_xs, sec_it_ys = load_csv(os.path.join(RESOURCES, 'integration', 'secIT.csv'))
g_sec_it = plot_clipped(sec_it_xs, sec_it_ys, 'sec(x) — интеграционный тест (мок-Cosine)', '#F4A261', 'sec_it.png')

csc_it_xs, csc_it_ys = load_csv(os.path.join(RESOURCES, 'integration', 'cscIT.csv'))
g_csc_it = plot_clipped(csc_it_xs, csc_it_ys, 'csc(x) — интеграционный тест (мок-Sine)', '#E9C46A', 'csc_it.png')

tan_it_xs, tan_it_ys = load_csv(os.path.join(RESOURCES, 'integration', 'tanIT.csv'))
g_tan_it = plot_clipped(tan_it_xs, tan_it_ys, 'tan(x) — интеграционный тест (мок-Sine/Cosine)', '#2A9D8F', 'tan_it.png', ylim=(-5, 5))

cot_it_xs, cot_it_ys = load_csv(os.path.join(RESOURCES, 'integration', 'cotIT.csv'))
g_cot_it = plot_clipped(cot_it_xs, cot_it_ys, 'cot(x) — интеграционный тест (мок-Sine/Cosine)', '#264653', 'cot_it.png', ylim=(-5, 5))

# Система
sys_xs, sys_ys = load_csv(os.path.join(RESOURCES, 'system.csv'))
sys_it_xs, sys_it_ys = load_csv(os.path.join(RESOURCES, 'integration', 'systemIT.csv'))

xs_lt0 = [x for x in sys_xs if x <= 0]
ys_lt0 = [y for x, y in zip(sys_xs, sys_ys) if x <= 0]
xs_gt0 = [x for x in sys_xs if x > 0]
ys_gt0 = [y for x, y in zip(sys_xs, sys_ys) if x > 0]
g_system = plot_system(xs_lt0, ys_lt0, xs_gt0, ys_gt0, 'system.png')

xs_it_lt0 = [x for x in sys_it_xs if x <= 0]
ys_it_lt0 = [y for x, y in zip(sys_it_xs, sys_it_ys) if x <= 0]
xs_it_gt0 = [x for x in sys_it_xs if x > 0]
ys_it_gt0 = [y for x, y in zip(sys_it_xs, sys_it_ys) if x > 0]
g_system_it = plot_system(xs_it_lt0, ys_it_lt0, xs_it_gt0, ys_it_gt0, 'system_it.png')

print(f"Все графики сгенерированы: {len(os.listdir(GRAPHS_DIR))} файлов")

# ============================================================
# Обновляем отчёт
# ============================================================

print("\nОбновляю отчёт...")
doc = Document(os.path.join(BASE_DIR, 'report_v777.docx'))

# 1. Исправляем описание Cosine (было неправильно написано про "независимый ряд Тейлора")
for p in doc.paragraphs:
    if 'Cosine — базовая функция, ряд Тейлора' in p.text:
        for run in p.runs:
            run.text = ''
        if p.runs:
            p.runs[0].text = '• Cosine — вычисляется через Sine: cos(x) = sin(π/2 − x). Зависит от Sine согласно диаграмме классов задания.'
        break

# 2. Удаляем старые параграфы секции 4 (кроме заголовка) до секции 5
paras_to_remove = []
in_section4 = False
for p in doc.paragraphs:
    if '4. Графики CSV-выгрузок' in p.text:
        in_section4 = True
        continue
    if in_section4:
        if p.text.strip().startswith('5.'):
            break
        paras_to_remove.append(p)

for p in paras_to_remove:
    p._element.getparent().remove(p._element)

# 3. Находим параграф секции 5 (чтобы вставлять перед ним)
section5_para = None
for p in doc.paragraphs:
    if p.text.strip().startswith('5.'):
        section5_para = p
        break

if section5_para is None:
    raise RuntimeError("Параграф '5. Выводы' не найден!")

parent = section5_para._element.getparent()



# Вставляем элементы в прямом порядке.
# Ключевая идея: вставляем каждый новый элемент ПОСЛЕ предыдущего,
# отслеживая текущую позицию вставки через insert_after_elem.

insert_after_elem = None  # None = вставлять после заголовка "4. Графики CSV-выгрузок"

# Находим заголовок секции 4
section4_elem = None
for p in doc.paragraphs:
    if '4. Графики CSV-выгрузок' in p.text:
        section4_elem = p._element
        break

insert_after_elem = section4_elem


def append_elem(new_elem):
    """Вставляет new_elem после insert_after_elem и обновляет указатель"""
    global insert_after_elem
    insert_after_elem.addnext(new_elem)
    insert_after_elem = new_elem


def append_text(text, bold=False, size=None, align=None):
    p_elem = OxmlElement('w:p')
    append_elem(p_elem)
    p_obj = Paragraph(p_elem, doc)
    if align:
        p_obj.alignment = align
    run = p_obj.add_run(text)
    run.bold = bold
    if size:
        run.font.size = Pt(size)


def append_graph(img_path, caption):
    # Картинка
    img_elem = OxmlElement('w:p')
    append_elem(img_elem)
    img_obj = Paragraph(img_elem, doc)
    img_obj.alignment = WD_ALIGN_PARAGRAPH.CENTER
    img_obj.add_run().add_picture(img_path, width=Inches(5.5))
    # Подпись
    cap_elem = OxmlElement('w:p')
    append_elem(cap_elem)
    cap_obj = Paragraph(cap_elem, doc)
    cap_obj.alignment = WD_ALIGN_PARAGRAPH.CENTER
    cap_run = cap_obj.add_run(caption)
    cap_run.italic = True
    cap_run.font.size = Pt(10)


append_text('Ниже представлены графики по CSV-выгрузкам всех модулей, организованные по уровням интеграции bottom-up.')

append_text('4.1. Уровень 0 — базовые функции', bold=True, size=12)
append_graph(g_sin, 'Рис. 2. sin(x) — базовая функция, ряд Тейлора')
append_graph(g_cos, 'Рис. 3. cos(x) = sin(π/2 − x), зависит от Sine')
append_graph(g_ln, 'Рис. 4. ln(x) — базовая функция, ряд Тейлора')
append_graph(g_logs_base, 'Рис. 5. log₂(x) и log₁₀(x) = ln(x)/ln(base)')

append_text('4.2. Уровень 1 — модульные тесты производных функций', bold=True, size=12)
append_graph(g_sec, 'Рис. 6. sec(x) = 1/cos(x) — module-тест')
append_graph(g_csc, 'Рис. 7. csc(x) = 1/sin(x) — module-тест')
append_graph(g_tan, 'Рис. 8. tan(x) = sin(x)/cos(x) — module-тест')
append_graph(g_cot, 'Рис. 9. cot(x) = cos(x)/sin(x) — module-тест')

append_text('4.3. Уровень 2 — интеграционные тесты с мок-заглушками', bold=True, size=12)
append_graph(g_sec_it, 'Рис. 10. sec(x) — интеграционный тест (мок-Cosine)')
append_graph(g_csc_it, 'Рис. 11. csc(x) — интеграционный тест (мок-Sine)')
append_graph(g_tan_it, 'Рис. 12. tan(x) — интеграционный тест (мок-Sine/Cosine)')
append_graph(g_cot_it, 'Рис. 13. cot(x) — интеграционный тест (мок-Sine/Cosine)')

append_text('4.4. Система функций — полная интеграция', bold=True, size=12)
append_graph(g_system, 'Рис. 14. Система (module-тест): x ≤ 0 и x > 0')
append_graph(g_system_it, 'Рис. 15. Система (интеграционный тест с моками)')

out_path = os.path.join(BASE_DIR, 'report_v777.docx')
doc.save(out_path)
print(f"Отчёт сохранён: {out_path}")
print("Готово!")

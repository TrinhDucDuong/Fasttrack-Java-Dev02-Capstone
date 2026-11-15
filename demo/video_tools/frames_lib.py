"""HTML frame templates (1920x1080) for the demo video. Red boxes have no text."""
import base64
import html
import json
import re
from pathlib import Path

W, H = 1920, 1080
RED = "#ff2d2d"

CSS = f"""
* {{ box-sizing: border-box; margin: 0; padding: 0; }}
html, body {{ width: {W}px; height: {H}px; overflow: hidden; background: #eef2f8; color: #14243a;
  font-family: "Segoe UI", Inter, system-ui, sans-serif; }}
.top {{ height: 72px; display: flex; align-items: center; gap: 18px; padding: 0 40px; background: #10213a; color: #fff; }}
.top .sec {{ font-size: 20px; font-weight: 700; letter-spacing: .02em; }}
.top .chip {{ font-size: 15px; padding: 4px 12px; border-radius: 999px; background: #1f3a63; color: #b9d3ff; font-weight: 600; }}
.top .chip.local {{ background: #12452f; color: #9be7c0; }}
.top .chip.cloud {{ background: #4a2a06; color: #ffd08a; }}
.top .num {{ margin-left: auto; font-size: 15px; color: #9fb3cf; }}
.title {{ padding: 20px 40px 0; font-size: 34px; font-weight: 800; }}
.sub {{ padding: 2px 40px 0; font-size: 20px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; color: #4d6078; }}
.stage {{ position: absolute; left: 40px; right: 40px; top: 186px; bottom: 36px; }}
.box {{ position: absolute; border: 5px solid {RED}; border-radius: 10px; box-shadow: 0 0 0 3px #ffffffaa, 0 0 22px #ff2d2d88; pointer-events: none; }}
.panel {{ background: #fff; border: 1px solid #d5deea; border-radius: 16px; box-shadow: 0 10px 30px #10213a14; }}
.code {{ background: #0f1b2d; color: #dbe7ff; border-radius: 16px; font: 21px/1.5 Consolas, "Cascadia Mono", monospace; padding: 18px 0; position: relative; overflow: hidden; }}
.code .file {{ color: #8fb2e6; font: 600 17px "Segoe UI", sans-serif; padding: 0 24px 10px; border-bottom: 1px solid #22385a; margin-bottom: 8px; }}
.code .tx {{ white-space: pre; }}
.code .ln {{ display: flex; white-space: pre; padding: 0 24px; min-height: 1.5em; }}
.code .no {{ width: 3.2em; color: #5b7299; text-align: right; padding-right: 18px; flex: none; }}
.kw {{ color: #ff9ac1; }} .an {{ color: #ffd479; }} .st {{ color: #a5e3a0; }} .cm {{ color: #7c93b8; font-style: italic; }} .ty {{ color: #7cd3ff; }}
.json {{ font: 20px/1.45 Consolas, monospace; white-space: pre; }}
.json .k {{ color: #9a3412; }} .json .s {{ color: #166534; }} .json .n {{ color: #1d4ed8; }} .json .b {{ color: #7c3aed; }}
.json .ln {{ display: block; padding: 0 18px; min-height: 29px; }}
.method {{ display: inline-block; font: 800 20px Consolas, monospace; padding: 6px 14px; border-radius: 8px; color: #fff; }}
.GET {{ background: #15803d; }} .POST {{ background: #b45309; }} .PUT {{ background: #1d4ed8; }} .DELETE {{ background: #b91c1c; }}
.status {{ display: inline-block; font: 800 22px Consolas, monospace; padding: 6px 16px; border-radius: 8px; }}
.s2 {{ background: #dcfce7; color: #166534; }} .s4 {{ background: #fee2e2; color: #991b1b; }}
.lbl {{ font-size: 15px; font-weight: 700; color: #5d6e84; text-transform: uppercase; letter-spacing: .06em; margin-bottom: 8px; }}
table.t {{ width: 100%; border-collapse: collapse; font-size: 21px; }}
table.t th {{ text-align: left; padding: 12px 16px; background: #10213a; color: #fff; font-size: 18px; }}
table.t td {{ padding: 11px 16px; border-bottom: 1px solid #dfe6f0; vertical-align: top; }}
table.t tr:nth-child(even) td {{ background: #f6f8fc; }}
.term {{ background: #0b1220; color: #d6e2ff; border-radius: 16px; font: 20px/1.55 Consolas, monospace; padding: 20px 26px; position: relative; white-space: pre; overflow: hidden; }}
.term .ok {{ color: #4ade80; }} .term .warn {{ color: #fbbf24; }} .term .dim {{ color: #7d8aa5; }} .term .cmd {{ color: #93c5fd; }}
"""


def esc(text):
    return html.escape(str(text), quote=True)


def page(section, num, total, title, sub, body, env=None, boxes_js=""):
    chip = f'<span class="chip {env}">{"LOCAL" if env == "local" else "AWS CLOUD"}</span>' if env else ""
    return f"""<!doctype html><html><head><meta charset="utf-8"><style>{CSS}</style></head><body>
<div class="top"><span class="sec">{esc(section)}</span>{chip}<span class="num">{num:02d} / {total:02d}</span></div>
<div class="title">{esc(title)}</div><div class="sub">{esc(sub)}</div>
{body}
<script>{boxes_js}</script></body></html>"""


# Boxes are computed in the browser from elements marked data-hl="1" (exact element bounds + padding)
BOX_JS = """
document.querySelectorAll('[data-hl]').forEach(el => {
  const r = el.getBoundingClientRect(); const pad = Number(el.dataset.pad || 6);
  const b = document.createElement('div'); b.className = 'box';
  Object.assign(b.style, { left: (r.left - pad) + 'px', top: (r.top - pad) + 'px', width: (r.width + 2 * pad) + 'px', height: (r.height + 2 * pad) + 'px' });
  document.body.appendChild(b);
});
"""

JAVA_KW = r"\b(public|private|protected|final|class|interface|record|return|new|if|throw|void|static|import|package|extends|implements|var|enum|switch|case|default|try|this|null|true|false|boolean|int|long)\b"


SQL_KW = r"\b(CREATE|INDEX|ON|USING|GIN|ALTER|TABLE|ADD|COLUMN|DROP|CONSTRAINT|IF|EXISTS|NOT|DEFAULT|EXTENSION|UPDATE|SET|WHERE)\b"
TOKEN = re.compile(r'(?P<st>"(?:[^"\\]|\\.)*")|(?P<an>@\w+)|(?P<kw>' + JAVA_KW[2:-2].join(["\\b(?:", ")\\b"]) + ')|(?P<sq>' + SQL_KW[2:-2].join(["\\b(?:", ")\\b"]) + ')')


def highlight_java(line):
    if re.match(r"\s*(//|\*|/\*|--|#)", line):
        return f'<span class="cm">{esc(line)}</span>'
    out, pos = [], 0
    for m in TOKEN.finditer(line):
        out.append(esc(line[pos:m.start()]))
        cls = {"st": "st", "an": "an", "kw": "kw", "sq": "kw"}[m.lastgroup]
        out.append(f'<span class="{cls}">{esc(m.group())}</span>')
        pos = m.end()
    out.append(esc(line[pos:]))
    return "".join(out)


def code_block(repo, rel, start, end, marks, style=""):
    """marks: list of (from_line, to_line) absolute line numbers to box."""
    lines = (Path(repo) / rel).read_text(encoding="utf-8").splitlines()
    rows = []
    marked = {}
    for a, b in marks:
        marked[a] = b
    i = start
    while i <= end:
        if i in marked:
            group = []
            for j in range(i, marked[i] + 1):
                group.append(f'<div class="ln"><span class="no">{j}</span><span class="tx">{highlight_java(lines[j - 1])}</span></div>')
            rows.append('<div data-hl="1" data-pad="2">' + "".join(group) + "</div>")
            i = marked[i] + 1
        else:
            rows.append(f'<div class="ln"><span class="no">{i}</span><span class="tx">{highlight_java(lines[i - 1])}</span></div>')
            i += 1
    return f'<div class="code" style="{style}"><div class="file">{esc(rel)}</div>{"".join(rows)}</div>'


def find_line(repo, rel, needle, after=1):
    lines = (Path(repo) / rel).read_text(encoding="utf-8").splitlines()
    for idx in range(after - 1, len(lines)):
        if needle in lines[idx]:
            return idx + 1
    raise ValueError(f"{needle!r} not found in {rel}")


# ---------------------------------------------------------------- JSON rendering
def shorten(value):
    if isinstance(value, str) and value.startswith("eyJ") and len(value) > 40:
        return value[:28] + "…(JWT rút gọn)"
    if isinstance(value, str) and len(value) > 70:
        return value[:67] + "…"
    return value


def json_lines(value, hl_keys, indent=0, key=None, trailing="", out=None, max_items=4, counter=None):
    """Render JSON to lines; lines whose key is in hl_keys get data-hl. hl_keys: {'key': max_count or None}."""
    out = [] if out is None else out
    counter = {} if counter is None else counter
    pad = "  " * indent
    prefix = f'<span class="k">"{esc(key)}"</span>: ' if key is not None else ""
    hl = False
    if key is not None and key in hl_keys:
        counter[key] = counter.get(key, 0) + 1
        limit = hl_keys[key]
        hl = limit is None or counter[key] <= limit
    if isinstance(value, dict):
        if not value:
            out.append((hl, f"{pad}{prefix}{{}}{trailing}"))
            return out
        out.append((False, f"{pad}{prefix}{{"))
        items = list(value.items())
        for n, (k, v) in enumerate(items):
            json_lines(v, hl_keys, indent + 1, k, "," if n < len(items) - 1 else "", out, max_items, counter)
        out.append((False, f"{pad}}}{trailing}"))
    elif isinstance(value, list):
        if not value:
            out.append((hl, f"{pad}{prefix}[]{trailing}"))
            return out
        out.append((False, f"{pad}{prefix}["))
        shown = value[:max_items]
        for n, v in enumerate(shown):
            more = n < len(shown) - 1 or len(value) > max_items
            json_lines(v, hl_keys, indent + 1, None, "," if more else "", out, max_items, counter)
        if len(value) > max_items:
            out.append((False, f'{pad}  <span class="cm" style="color:#8a99ad">… {len(value) - max_items} phần tử nữa</span>'))
        out.append((False, f"{pad}]{trailing}"))
    else:
        v = shorten(value)
        if isinstance(v, str):
            rendered = f'<span class="s">"{esc(v)}"</span>'
        elif isinstance(v, bool) or v is None:
            rendered = f'<span class="b">{json.dumps(v)}</span>'
        else:
            rendered = f'<span class="n">{esc(json.dumps(v))}</span>'
        out.append((hl, f"{pad}{prefix}{rendered}{trailing}"))
    return out


def json_html(value, hl_keys, max_lines=26, max_items=4):
    lines = json_lines(value, hl_keys, max_items=max_items)
    if len(lines) > max_lines:
        hl_idx = [i for i, (hl, _) in enumerate(lines) if hl]
        start = 0
        if hl_idx and hl_idx[-1] >= max_lines - 1:
            start = max(0, min(hl_idx[0] - 2, len(lines) - max_lines))
        lines = lines[start:start + max_lines] + [(False, '<span style="color:#8a99ad">  …</span>')]
    return "".join(
        f'<span class="ln"{" data-hl=1 data-pad=3" if hl else ""}>{text}</span>' for hl, text in lines)


def img_data(path):
    data = Path(path).read_bytes()
    kind = "png" if str(path).lower().endswith(".png") else "jpeg"
    return f"data:image/{kind};base64," + base64.b64encode(data).decode()

"""Build the demo video: HTML frames -> PNG (headless Edge via CDP) -> MP4 (ffmpeg) + timed speech script.

usage: python build_video.py <repo> <workdir>
"""
import json
import math
import subprocess
import sys
from pathlib import Path

from frames_lib import (BOX_JS, H, W, code_block, esc, find_line, img_data, json_html, page)

REPO = Path(sys.argv[1])
WORK = Path(sys.argv[2])
FRAMES = WORK / "frames2"
FRAMES.mkdir(parents=True, exist_ok=True)
LOCAL = json.loads((REPO / "demo/output/local/steps.json").read_text(encoding="utf-8"))
CLOUD = json.loads((REPO / "demo/output/cloud/steps.json").read_text(encoding="utf-8"))
STEP = {"local": {s["id"]: s for s in LOCAL["steps"]}, "cloud": {s["id"]: s for s in CLOUD["steps"]}}
SHOTS = {p.stem: p for d in WORK.glob("shots-*") for p in list(d.glob("*.png")) + list(d.glob("*.jpg"))}
MANIFEST = {}
for d in WORK.glob("shots-*"):
    f = d / "manifest.json"
    if f.exists():
        for m in json.loads(f.read_text(encoding="utf-8")):
            MANIFEST[m["shot"]] = m

frames = []  # each: dict(section, title, sub, body, say, env)


from tts_text import phonetic

CHARS_PER_SECOND = 16.0   # measured: HN - Ngoc Huyen reads ~17-18 phonetic chars/s


def seconds_for(say):
    return max(4.5, round(len(phonetic(say)) / CHARS_PER_SECOND + 1.4, 1))


def add(section, title, sub, body, say, env=None, extra=0.0):
    frames.append({"section": section, "title": title, "sub": sub, "body": body, "say": say, "env": env,
                   "seconds": seconds_for(say) + extra})


def L(rel, needle, after=1):
    return find_line(REPO, rel, needle, after)


# ------------------------------------------------------------------ frame kinds
def stage(inner, style=""):
    return f'<div class="stage" style="{style}">{inner}</div>'


def code_frame(section, title, sub, snippets, say):
    """snippets: list of (rel, start_needle, end_needle, [(mark_from_needle, mark_to_needle)])"""
    cols = []
    for rel, a, b, marks in snippets:
        start, end = L(rel, a), L(rel, b, L(rel, a))
        abs_marks = []
        for ma, mb in marks:
            x = L(rel, ma, start)
            y = L(rel, mb, x) if mb else x
            abs_marks.append((x, y))
        font = "font-size:17px;" if len(snippets) > 1 else ""
        cols.append(code_block(REPO, rel, start, end, abs_marks, style=f"flex:1; min-width:0; {font}"))
    add(section, title, sub, stage(f'<div style="display:flex; gap:24px; height:100%;">{"".join(cols)}</div>'), say)


def table_frame(section, title, sub, headers, rows, say, hl_rows=()):
    head = "".join(f"<th>{esc(h)}</th>" for h in headers)
    body = "".join(
        f'<tr{" data-hl=1 data-pad=2" if i in hl_rows else ""}>' + "".join(f"<td>{c}</td>" for c in row) + "</tr>"
        for i, row in enumerate(rows))
    add(section, title, sub, stage(f'<div class="panel" style="overflow:hidden"><table class="t"><thead><tr>{head}</tr></thead><tbody>{body}</tbody></table></div>'), say)


def api_frame(env, step_id, say, keys=(), hl_body=False, hl_status=True, max_lines=24, max_items=4, title=None):
    s = STEP[env][step_id]
    base = "{{baseUrl}}"
    auth = s.get("auth")
    headers = []
    if s["requestBody"] is not None:
        headers.append(("Content-Type", "application/json"))
    if auth:
        headers.append(("Authorization", "Bearer {{" + ("adminToken" if auth == "admin" else "accessToken") + "}}"))
    head_html = "".join(f'<div style="font:19px Consolas,monospace; margin:3px 0"><b>{esc(k)}:</b> {esc(v)}</div>' for k, v in headers) \
        or '<div style="font:19px Consolas,monospace; color:#8a99ad">(không cần token)</div>'
    req_body = json_html(s["requestBody"], {}, max_lines=12) if s["requestBody"] is not None else \
        '<div style="font:19px Consolas,monospace; color:#8a99ad; padding:0 18px">(không có body)</div>'
    hl_keys = {}
    for k in keys:
        name, _, limit = k.partition("#")
        hl_keys[name] = int(limit) if limit else None
    if s["response"] in (None, ""):
        resp = '<div style="font:20px Consolas,monospace; color:#8a99ad; padding:0 18px">(response không có body)</div>'
    else:
        resp = json_html(s["response"], hl_keys, max_lines=max_lines, max_items=max_items)
    status_cls = "s2" if s["status"] < 400 else "s4"
    status_label = {200: "200 OK", 201: "201 Created", 204: "204 No Content", 400: "400 Bad Request",
                    401: "401 Unauthorized", 403: "403 Forbidden", 404: "404 Not Found", 409: "409 Conflict"}.get(s["status"], str(s["status"]))
    env_url = LOCAL["baseUrl"] if env == "local" else CLOUD["baseUrl"]
    body = stage(f"""
<div style="display:grid; grid-template-columns: 760px 1fr; gap:24px; height:100%;">
  <div class="panel" style="padding:22px; overflow:hidden">
    <div class="lbl">Request</div>
    <div style="display:flex; align-items:center; gap:12px; margin-bottom:6px">
      <span class="method {s['method']}">{s['method']}</span>
      <span style="font:22px Consolas,monospace; word-break:break-all">{esc(base + s['path'])}</span>
    </div>
    <div style="font:16px Consolas,monospace; color:#6b7c93; margin-bottom:18px">{{{{baseUrl}}}} = {esc(env_url)}</div>
    <div class="lbl">Headers</div>{head_html}
    <div class="lbl" style="margin-top:18px">Body</div><div class="json">{req_body}</div>
  </div>
  <div class="panel" style="padding:22px 4px 22px 4px; overflow:hidden">
    <div style="display:flex; align-items:center; gap:16px; padding:0 18px 14px">
      <div class="lbl" style="margin:0">Response</div>
      <span class="status {status_cls}"{' data-hl=1 data-pad=4' if hl_status else ''}>{status_label}</span>
      <span style="color:#6b7c93; font-size:18px">{s['elapsedMs']} ms</span>
    </div>
    <div class="json"{' data-hl=1 data-pad=4' if hl_body else ''}>{resp}</div>
  </div>
</div>""")
    section = "DEMO LOCAL · Postman" if env == "local" else "DEMO AWS CLOUD · API"
    add(section, title or f"{step_id}  {s['title']}", f"{s['method']} {s['path']}", body, say, env=env)


def shot_frame(section, shot, title, sub, say, env=None):
    m = MANIFEST[shot]
    sw, sh = m["width"], m["height"]
    aw, ah = W - 80, H - 186 - 30
    scale = min(aw / sw, ah / sh)
    w, h = int(sw * scale), int(sh * scale)
    left = 40 + (aw - w) // 2
    boxes = "".join(
        f'<div class="box" style="left:{left + int((b["x"] - 6) * scale)}px; top:{186 + int((b["y"] - 6) * scale)}px; '
        f'width:{int((b["w"] + 12) * scale)}px; height:{int((b["h"] + 12) * scale)}px"></div>' for b in m["boxes"])
    body = f'<img src="{img_data(SHOTS[shot])}" style="position:absolute; left:{left}px; top:186px; width:{w}px; height:{h}px; ' \
           f'border-radius:12px; box-shadow:0 12px 40px #10213a33; border:1px solid #c9d4e3">{boxes}'
    add(section, title, sub, body, say, env=env)


def term_frame(section, title, sub, lines, say, hl=()):
    rows = []
    for i, (cls, text) in enumerate(lines):
        rows.append(f'<div{" data-hl=1 data-pad=3" if i in hl else ""}><span class="{cls}">{esc(text)}</span></div>')
    add(section, title, sub, stage(f'<div class="term" style="height:100%">{"".join(rows)}</div>'), say)



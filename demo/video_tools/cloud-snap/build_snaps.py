"""Compose high-resolution copies of the cloud UI states captured from the real site, then shoot them."""
import json
import re
import subprocess
from pathlib import Path

HERE = Path(__file__).parent
REPO = Path(__file__).resolve().parents[3]
index = (REPO / "api-gateway/src/main/resources/static/index.html").read_text(encoding="utf-8")
HEAD = index[:index.index("<body>")]
VIDEO = (REPO / "demo/assets/middle-java-bai-1.mp4").resolve().as_uri()


def header(email, role):
    return f"""<header>
<div class="brand"><span class="logo">MJ</span>Middle Java Store</div>
<span class="health"><span class="dot up"></span><span>Hệ thống hoạt động</span></span>
<div class="who"><span>{email}</span><span class="badge {role}" id="who-role">{role}</span><button class="ghost small">Đăng xuất</button></div>
</header>"""


def customer_tabs(active):
    tabs = [("shop", "Cửa hàng"), ("orders", "Đơn hàng"), ("notifications", "Thông báo")]
    active_attr = ' class="active"'
    return '<nav class="tabs">' + "".join(
        f'<button data-tab="{k}"{active_attr if k == active else ""}>{v}</button>' for k, v in tabs) + "</nav>"


def page(body, extra_script=""):
    return HEAD + "<body>\n" + body + (f"<script>{extra_script}</script>" if extra_script else "") + "\n</body></html>"


snaps = {}
# ---- ADMIN (captured from the real cloud session)
snaps["ui-cloud-admin-orders"] = page((HERE / "admin-orders.body.html").read_text(encoding="utf-8"))
snaps["ui-cloud-admin-upload"] = page((HERE / "admin-upload.body.html").read_text(encoding="utf-8"))

# ---- registration of custemer@gmail.com (password field shows dots only)
snaps["ui-cloud-register"] = page("""<header>
<div class="brand"><span class="logo">MJ</span>Middle Java Store</div>
<span class="health"><span class="dot up"></span><span>Hệ thống hoạt động</span></span>
</header>
<main><section id="auth-view" class="auth card">
<nav><button type="button" class="ghost small">Đăng nhập</button><button type="button" class="small">Đăng ký</button></nav>
<form class="stack" id="auth-form">
<h2>Đăng ký tài khoản khách hàng</h2>
<label>Email <input name="email" type="email" value="custemer@gmail.com"></label>
<label>Mật khẩu <input name="password" type="password" value="xxxxxxxxxx" placeholder="Tối thiểu 10 ký tự"></label>
<button type="submit" id="auth-submit">Đăng ký</button>
<p class="note">Tài khoản mới đăng ký có role <b>CUSTOMER</b>. Tài khoản <b>ADMIN</b> được tạo sẵn khi hệ thống khởi động.</p>
</form></section></main>""")

# ---- shop with cart after sign-up
shop = (HERE / "customer-shop.panel.html").read_text(encoding="utf-8")
snaps["ui-cloud-new-customer-cart"] = page(header("custemer@gmail.com", "CUSTOMER") + "<main><section id=\"customer-view\">"
                                           + customer_tabs("shop") + shop + "</section></main>"
                                           + '<div id="toast" class="show">Đã thêm “Middle Java” vào giỏ</div>')

# ---- order paid + video from S3 (local copy of the same file; the signed S3 URL is not stored)
orders = (HERE / "customer-orders.panel.html").read_text(encoding="utf-8").replace("__VIDEO_SRC__", VIDEO)
snaps["ui-cloud-video-s3"] = page(header("custemer@gmail.com", "CUSTOMER") + "<main><section id=\"customer-view\">"
                                  + customer_tabs("orders") + orders + "</section></main>",
                                  "const v=document.querySelector('video');v.addEventListener('loadeddata',()=>{v.currentTime=4;});")

# ---- notification
snaps["ui-cloud-notifications-new"] = page(header("custemer@gmail.com", "CUSTOMER") + "<main><section id=\"customer-view\">"
                                           + customer_tabs("notifications") + """
<div data-panel="notifications" class="card">
<div class="row" style="justify-content:space-between"><h2>Thông báo</h2><button class="ghost small">Tải lại</button></div>
<div class="list" id="notifications"><div class="list-item"><div>Order #13 has been created. Status: CONFIRMED, payment PAID via VNPAY, total 29.99.</div><div class="muted" style="font-size:.8rem">Đơn #13 · 15:29:13 15/11/2026</div></div></div>
</div></section></main>""")

out = HERE / "html"
out.mkdir(exist_ok=True)
for name, html in snaps.items():
    (out / f"{name}.html").write_text(html, encoding="utf-8")

boxes = {
    "ui-cloud-admin-orders": ['#who-role', '[data-admin-order="12"]'],
    "ui-cloud-admin-upload": ['[data-admin-course="1"]', '#toast'],
    "ui-cloud-register": ['#auth-form'],
    "ui-cloud-new-customer-cart": ['#who-role', '#cart-card'],
    "ui-cloud-video-s3": ['[data-order="13"]', '#player'],
    "ui-cloud-notifications-new": ['#notifications'],
}
scenario = {"baseUrl": out.resolve().as_uri() + "/", "viewport": {"width": 1600, "height": 900},
            "steps": [{"goto": f"{n}.html", "wait": 2500 if n == "ui-cloud-video-s3" else 800, "shot": n, "boxes": boxes[n]} for n in snaps]}
(HERE / "scenario.json").write_text(json.dumps(scenario), encoding="utf-8")
subprocess.run(["node", str(HERE.parent / "ui_capture.mjs"), str(HERE / "scenario.json"), str(HERE.parent / "shots-cloud-admin")], check=True)

"""Build UI capture scenarios (local: CUSTOMER + ADMIN, cloud: CUSTOMER)."""
import json
import sys
from pathlib import Path

out = Path(sys.argv[1])
LOCAL_ADMIN = ("admin@middlejava.local", "Admin123!Local")  # documented local-only demo account

PRELUDE = r"""
const sleep = (ms) => new Promise(r => setTimeout(r, ms));
const $ = (s) => document.querySelector(s);
const setVal = (el, v) => { el.value = v; el.dispatchEvent(new Event('input', { bubbles: true })); el.dispatchEvent(new Event('change', { bubbles: true })); };
const clickText = (root, text) => { const b = [...document.querySelectorAll(root + ' button')].find(x => x.textContent.trim() === text); if (!b) throw new Error('no button ' + text); b.click(); };
const tab = (name) => $(`button[data-tab="${name}"]`).click();
const addCourse = async (title) => { const card = [...document.querySelectorAll('#catalog article')].find(a => a.querySelector('h3').textContent === title); card.querySelector('button').click(); await sleep(700); };
const login = async (email, password) => { $('#tab-login').click(); const f = $('#auth-form'); f.email.value = email; f.password.value = password; f.requestSubmit(); await sleep(1500); };
window.confirm = () => true;
"""


def step(shot=None, js=None, boxes=None, wait=None, goto=None):
    s = {}
    if goto is not None:
        s["goto"] = goto
    if js:
        s["js"] = PRELUDE + js
    if wait:
        s["wait"] = wait
    if shot:
        s["shot"] = shot
        s["boxes"] = boxes or []
    return s


def customer_steps(prefix):
    return [
        step(goto="/", js="localStorage.clear(); location.reload();", wait=2000),
        step(f"{prefix}-login", boxes=["#auth-form", "#health-text"]),
        step(f"{prefix}-shop", js="""
            window.__customer = 'ui.customer.' + Date.now() + '@example.com';
            localStorage.setItem('mj-demo-customer', window.__customer);
            $('#tab-register').click();
            const f = $('#auth-form'); f.email.value = window.__customer; f.password.value = 'DemoPassword123!'; f.requestSubmit();
            await sleep(2000);""", boxes=["#who", "#catalog"], wait=800),
        step(f"{prefix}-search", js="setVal($('#search-form').q, 'java'); $('#search-form').requestSubmit();",
             boxes=["#search-form", "#catalog"]),
        step(f"{prefix}-cart", js="""
            setVal($('#search-form').q, ''); $('#search-form').requestSubmit(); await sleep(900);
            await addCourse('Middle Java'); await addCourse('Spring Boot Microservices');
            const qty = document.querySelector('#cart-items .qty'); setVal(qty, 2); await sleep(900);""",
             boxes=["#cart-card"], wait=600),
        step(f"{prefix}-cart-removed", js="""
            const items = document.querySelectorAll('#cart-items [data-cart-item]');
            items[items.length - 1].querySelector('button').click(); await sleep(900);""",
             boxes=["#cart-card"], wait=400),
        step(f"{prefix}-checkout", js="setVal($('#payment-method'), 'VNPAY'); $('#checkout').click();",
             boxes=["#toast", "#payment-method"], wait=900),
        step(f"{prefix}-orders-paid", js="tab('orders');", boxes=["#orders tbody tr:first-child"], wait=1400),
        step(f"{prefix}-video", js="clickText('#orders tbody tr:first-child', 'Xem video');", boxes=["#player"], wait=3500),
        step(f"{prefix}-notifications", js="tab('notifications');", boxes=["#notifications"], wait=1400),
        step(f"{prefix}-orders-cod", js="""
            tab('shop'); await sleep(1200); await addCourse('Java Fundamentals');
            setVal($('#payment-method'), 'COD'); $('#checkout').click(); await sleep(1200); tab('orders');""",
             boxes=["#orders tbody tr:first-child"], wait=1500),
    ]


def admin_steps(prefix, email, password):
    return [
        step(f"{prefix}-admin-orders", js=f"""
            $('#logout').click(); await sleep(600); await login({json.dumps(email)}, {json.dumps(password)});""",
             boxes=["#who", "#admin-orders tbody tr:first-child button"], wait=1200),
        step(f"{prefix}-admin-confirmed", js="clickText('#admin-orders tbody tr:first-child', 'Xác nhận đã thanh toán');",
             boxes=["#admin-orders tbody tr:first-child", "#toast"], wait=800),
        step(f"{prefix}-admin-product-form", js="""
            tab('admin-products'); await sleep(1200);
            const f = $('#product-form'); f.slug.value = 'design-patterns-' + Date.now().toString().slice(-6);
            f.title.value = 'Java Design Patterns'; f.description.value = 'Strategy, Factory Method, Observer in real projects.';
            f.price.value = '24.99'; f.youtubeUrl.value = 'https://www.youtube.com/watch?v=v9ejT8FO-7I';""",
             boxes=["#product-form"], wait=500),
        step(f"{prefix}-admin-products", js="$('#product-form').requestSubmit();",
             boxes=["#admin-products tbody tr:last-child", "#toast"], wait=1500),
        step(f"{prefix}-admin-deactivated", js="clickText('#admin-products tbody tr:last-child', 'Ngừng bán');",
             boxes=["#admin-products tbody tr:last-child"], wait=1500),
        step(f"{prefix}-customer-unlocked", js="""
            $('#logout').click(); await sleep(600);
            await login(localStorage.getItem('mj-demo-customer') || window.__customer, 'DemoPassword123!'); tab('orders');""",
             boxes=["#orders tbody tr:first-child"], wait=1600),
    ]


local = {"baseUrl": "http://localhost:8080", "viewport": {"width": 1600, "height": 900},
         "steps": customer_steps("ui-local") + admin_steps("ui-local", *LOCAL_ADMIN)}
cloud = {"baseUrl": sys.argv[2] if len(sys.argv) > 2 else "https://d3jaw9bd80q6ki.cloudfront.net",
         "viewport": {"width": 1600, "height": 900}, "steps": customer_steps("ui-cloud")}
(out / "scenario-local.json").write_text(json.dumps(local, ensure_ascii=False, indent=1), encoding="utf-8")
(out / "scenario-cloud.json").write_text(json.dumps(cloud, ensure_ascii=False, indent=1), encoding="utf-8")
print("ok")

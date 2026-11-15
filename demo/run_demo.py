"""
Run the full OJT demo scenario against a running environment and save every
request/response to demo/output/<env>/steps.json.

    python demo/run_demo.py --env local                        # http://localhost:8080
    python demo/run_demo.py --env cloud --base-url https://...  # AWS CloudFront URL

Admin credentials are read from environment variables, never from this file:
    ADMIN_EMAIL, ADMIN_PASSWORD   (local defaults to the documented local demo admin)
"""
import argparse
import json
import os
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path

LOCAL_ADMIN = ("admin@middlejava.local", "Admin123!Local")  # local-only demo account (OJT_DEMO_VI.md)


class Demo:
    def __init__(self, base_url):
        self.base_url = base_url.rstrip("/")
        self.steps = []
        self.vars = {}

    def call(self, step_id, title, method, path, body=None, token=None, expect=None):
        url = self.base_url + path
        headers = {"Accept": "application/json"}
        data = None
        if body is not None:
            data = json.dumps(body).encode()
            headers["Content-Type"] = "application/json"
        if token:
            headers["Authorization"] = "Bearer " + token
        request = urllib.request.Request(url, data=data, method=method, headers=headers)
        started = time.time()
        for attempt in range(15):
            try:
                with urllib.request.urlopen(request, timeout=30) as response:
                    status, raw = response.status, response.read().decode()
                break
            except urllib.error.HTTPError as error:
                status, raw = error.code, error.read().decode()
                break
            except (urllib.error.URLError, ConnectionError) as error:
                # connection reset during the TLS handshake: the request never reached the server
                if attempt == 14:
                    raise
                print(f"     network error ({error}), retrying...")
                time.sleep(1.5)
                started = time.time()
        elapsed_ms = int((time.time() - started) * 1000)
        try:
            payload = json.loads(raw) if raw else None
        except json.JSONDecodeError:
            payload = raw
        ok = expect is None or status == expect
        self.steps.append({
            "id": step_id, "title": title, "method": method, "path": path, "url": url,
            "auth": "admin" if token and token == self.vars.get("adminToken") else ("customer" if token else None),
            "requestBody": body, "status": status, "elapsedMs": elapsed_ms, "response": payload, "ok": ok,
        })
        marker = "OK " if ok else "!! "
        print(f"{marker}{step_id:>4} {method:6} {path:45} -> {status} ({elapsed_ms} ms)")
        if not ok:
            print("     expected", expect, "got", status, json.dumps(payload)[:300])
        return payload

    def direct(self, step_id, title, method, url, data=None, headers=None, expect=None):
        """Request straight to object storage (presigned URL) - it does not go through the API."""
        request = urllib.request.Request(url, data=data, method=method, headers=headers or {})
        started = time.time()
        for attempt in range(15):
            try:
                with urllib.request.urlopen(request, timeout=60) as response:
                    status, body, content_type = response.status, response.read(), response.headers.get("Content-Type")
                break
            except urllib.error.HTTPError as error:
                status, body, content_type = error.code, error.read(), error.headers.get("Content-Type")
                break
            except (urllib.error.URLError, ConnectionError) as error:
                if attempt == 14:
                    raise
                print(f"     network error ({error}), retrying...")
                time.sleep(1.5)
                started = time.time()
        elapsed_ms = int((time.time() - started) * 1000)
        ok = expect is None or status == expect
        response_info = {"contentType": content_type, "bytes": len(body)}
        if status >= 400:
            response_info["body"] = body.decode(errors="replace")[:300]
        self.steps.append({
            "id": step_id, "title": title, "method": method, "path": url.split("?")[0], "url": url, "auth": "presigned",
            "requestBody": {"file": f"{len(data)} bytes"} if data else None, "status": status,
            "elapsedMs": elapsed_ms, "response": response_info, "ok": ok,
        })
        marker = "OK " if ok else "!! "
        print(f"{marker}{step_id:>4} {method:6} {'(storage) ' + url.split('?')[0][-35:]:45} -> {status} ({elapsed_ms} ms)")
        if not ok:
            print("     expected", expect, "got", status, response_info)
        return status


def upload_course_video(d, admin_token, course_id):
    video = (Path(__file__).parent / "assets" / "middle-java-bai-1.mp4").read_bytes()
    ticket = d.call("3.3", "ADMIN xin URL upload video (presigned)", "POST",
                    f"/api/admin/courses/{course_id}/video/upload-url",
                    {"fileName": "middle-java-bai-1.mp4", "contentType": "video/mp4", "sizeBytes": len(video)},
                    token=admin_token, expect=200)
    d.direct("3.4", "Upload file thẳng lên object storage", ticket["method"], ticket["uploadUrl"],
             data=video, headers=ticket["headers"], expect=200)
    d.call("3.5", "ADMIN xác nhận video cho khóa học", "PUT", f"/api/admin/courses/{course_id}/video",
           {"key": ticket["key"]}, token=admin_token, expect=200)


def admin_course(d, admin_token, stamp):
    new_course = d.call("3.1", "ADMIN tạo sản phẩm mới", "POST", "/api/admin/courses", {
        "slug": f"design-patterns-{stamp}", "title": "Java Design Patterns",
        "description": "Strategy, Factory Method, Observer in real projects.", "price": 24.99,
        "youtubeUrl": "https://www.youtube.com/watch?v=v9ejT8FO-7I"}, token=admin_token, expect=201)
    new_id = new_course["id"]
    d.call("3.2", "ADMIN cập nhật giá", "PUT", f"/api/admin/courses/{new_id}", {
        "slug": f"design-patterns-{stamp}", "title": "Java Design Patterns",
        "description": "Strategy, Factory Method, Observer in real projects.", "price": 19.99,
        "youtubeUrl": "https://www.youtube.com/watch?v=v9ejT8FO-7I"}, token=admin_token, expect=200)

    return new_id


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--env", choices=["local", "cloud"], default="local")
    parser.add_argument("--base-url", default="http://localhost:8080")
    args = parser.parse_args()

    admin_email = os.environ.get("ADMIN_EMAIL") or (LOCAL_ADMIN[0] if args.env == "local" else None)
    admin_password = os.environ.get("ADMIN_PASSWORD") or (LOCAL_ADMIN[1] if args.env == "local" else None)
    with_admin = bool(admin_email and admin_password)
    if not with_admin:
        print("ADMIN_EMAIL/ADMIN_PASSWORD not set: running the CUSTOMER part only")

    d = Demo(args.base_url)
    stamp = time.strftime("%m%d%H%M%S")
    customer = {"email": f"customer.{stamp}@example.com", "password": "DemoPassword123!"}

    # 1. Health + public catalog + search (index)
    d.call("1.1", "Health check qua API Gateway", "GET", "/actuator/health", expect=200)
    catalog = d.call("1.2", "Danh sách sản phẩm (public)", "GET", "/api/courses", expect=200)
    d.call("1.3", "Tìm kiếm theo tên - dùng index", "GET", "/api/courses?search=java", expect=200)
    by_slug = {course["slug"]: course["id"] for course in catalog}
    middle_java, fundamentals = by_slug["middle-java"], by_slug["java-fundamentals"]

    # 2. Authentication + roles
    d.call("2.1", "Đăng ký CUSTOMER", "POST", "/api/auth/register", customer, expect=201)
    token = d.call("2.2", "Đăng nhập CUSTOMER - nhận JWT", "POST", "/api/auth/login", customer, expect=200)["accessToken"]
    d.vars["customerToken"] = token
    d.call("2.3", "CUSTOMER gọi API ADMIN -> 403", "POST", "/api/admin/courses", {}, token=token, expect=403)
    admin_token = None
    if with_admin:
        admin = d.call("2.4", "Đăng nhập ADMIN", "POST", "/api/auth/login",
                       {"email": admin_email, "password": admin_password}, expect=200)
        d.vars["adminToken"] = admin_token = admin["accessToken"]
        # never keep the admin password in the saved output
        d.steps[-1]["requestBody"] = {"email": admin_email, "password": "••••••••"}

    # 3. Product management (ADMIN)
    new_id = by_slug["spring-boot-microservices"]
    if with_admin:
        new_id = admin_course(d, admin_token, stamp)
        upload_course_video(d, admin_token, middle_java)

    # 4. Cart
    d.call("4.1", "Thêm Middle Java vào giỏ", "POST", "/api/cart/items",
           {"courseId": middle_java, "quantity": 1}, token=token, expect=200)
    d.call("4.2", "Thêm khóa thứ hai vào giỏ", "POST", "/api/cart/items",
           {"courseId": new_id, "quantity": 1}, token=token, expect=200)
    d.call("4.3", "Cập nhật số lượng = 2", "PUT", f"/api/cart/items/{middle_java}",
           {"quantity": 2}, token=token, expect=200)
    d.call("4.4", "Xóa sản phẩm khỏi giỏ", "DELETE", f"/api/cart/items/{new_id}", token=token, expect=204)
    d.call("4.5", "Xem giỏ hàng", "GET", "/api/cart", token=token, expect=200)

    # 5. Order + payment (VNPAY)
    d.call("5.1", "Phương thức thanh toán (Factory)", "GET", "/api/orders/payment-methods", token=token, expect=200)
    d.call("5.2", "Chưa mua -> video 403", "GET", f"/api/courses/{middle_java}/video", token=token, expect=403)
    paid = d.call("5.3", "Đặt hàng VNPAY (giả lập)", "POST", "/api/orders", {"paymentMethod": "VNPAY"}, token=token, expect=201)
    d.call("5.4", "Giỏ hàng đã được làm trống", "GET", "/api/cart", token=token, expect=200)
    video = d.call("5.5", "Đã mua -> video 200", "GET", f"/api/courses/{middle_java}/video", token=token, expect=200)
    if video and video.get("source") == "STORAGE":
        signed = video["videoUrl"]
        d.direct("5.5a", "Mở link có chữ ký -> phát được video", "GET", signed, expect=200)
        d.direct("5.5b", "Bỏ chữ ký khỏi link -> 403", "GET", signed.split("?")[0], expect=403)
    time.sleep(1.5)
    d.call("5.6", "Thông báo đơn hàng (Observer)", "GET", "/api/notifications", token=token, expect=200)

    # 6. COD + admin confirmation
    d.call("6.1", "Thêm Java Fundamentals", "POST", "/api/cart/items",
           {"courseId": fundamentals, "quantity": 1}, token=token, expect=200)
    cod = d.call("6.2", "Đặt hàng COD", "POST", "/api/orders", {"paymentMethod": "COD"}, token=token, expect=201)
    d.call("6.3", "COD chưa trả tiền -> video 403", "GET", f"/api/courses/{fundamentals}/video", token=token, expect=403)
    if with_admin:
        d.call("6.4", "ADMIN xem tất cả đơn", "GET", "/api/admin/orders", token=admin_token, expect=200)
        d.call("6.5", "ADMIN xác nhận thanh toán COD", "PUT", f"/api/admin/orders/{cod['id']}/confirm-payment",
               token=admin_token, expect=200)
        d.call("6.6", "Xác nhận lần 2 -> 409", "PUT", f"/api/admin/orders/{cod['id']}/confirm-payment",
               token=admin_token, expect=409)
        d.call("6.7", "Đã xác nhận -> video 200", "GET", f"/api/courses/{fundamentals}/video", token=token, expect=200)
    time.sleep(1.5)
    d.call("6.8", "Thông báo đổi trạng thái", "GET", "/api/notifications", token=token, expect=200)
    d.call("6.9", "Lịch sử đơn hàng", "GET", "/api/orders", token=token, expect=200)

    # 7. Edge cases + soft delete
    d.call("7.1", "Giỏ trống -> 400", "POST", "/api/orders", {"paymentMethod": "VNPAY"}, token=token, expect=400)
    if with_admin:
        d.call("7.2", "ADMIN ngừng bán sản phẩm", "DELETE", f"/api/admin/courses/{new_id}", token=admin_token, expect=204)
        d.call("7.3", "Catalog không còn sản phẩm đã ngừng bán", "GET", "/api/courses", expect=200)

    out = Path(__file__).parent / "output" / args.env
    out.mkdir(parents=True, exist_ok=True)
    (out / "steps.json").write_text(json.dumps({
        "env": args.env, "baseUrl": d.base_url, "runAt": time.strftime("%Y-%m-%d %H:%M:%S"),
        "ids": {"paidOrder": paid["id"], "codOrder": cod["id"], "newCourse": new_id},
        "steps": d.steps}, ensure_ascii=False, indent=2), encoding="utf-8")
    failed = [step["id"] for step in d.steps if not step["ok"]]
    print(f"\n{len(d.steps)} steps, {len(failed)} failed {failed if failed else ''} -> {out / 'steps.json'}")
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()

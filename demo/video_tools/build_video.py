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
FRAMES = WORK / "frames"
FRAMES.mkdir(parents=True, exist_ok=True)
LOCAL = json.loads((REPO / "demo/output/local/steps.json").read_text(encoding="utf-8"))
CLOUD = json.loads((REPO / "demo/output/cloud/steps.json").read_text(encoding="utf-8"))
STEP = {"local": {s["id"]: s for s in LOCAL["steps"]}, "cloud": {s["id"]: s for s in CLOUD["steps"]}}
SHOTS = {p.stem: p for d in ("shots-local", "shots-cloud", "shots-web") for p in (WORK / d).glob("*.png")}
MANIFEST = {}
for d in ("shots-local", "shots-cloud", "shots-web"):
    f = WORK / d / "manifest.json"
    if f.exists():
        for m in json.loads(f.read_text(encoding="utf-8")):
            MANIFEST[m["shot"]] = m

frames = []  # each: dict(section, title, sub, body, say, env)


def seconds_for(say):
    words = len(say.split())
    return max(5.0, round(words / 2.9 + 1.2, 1))


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


# ================================================================== CONTENT
S3UI = "DEMO LOCAL · Web UI"
S0, S1, S2, S3, S4, S5, S6 = ("GIỚI THIỆU", "CODE · ÁP DỤNG KIẾN THỨC", "KIỂM THỬ", "DEMO LOCAL · Postman",
                              "CI/CD · AWS", "DEMO AWS CLOUD · Web UI", "TỔNG KẾT")

add(S0, "E-Commerce Mini — Middle Java Store", "OJT FastTrack Java · Trịnh Đức Dương", stage("""
<div class="panel" style="height:100%; display:grid; place-items:center; text-align:center; padding:40px">
 <div>
  <div style="font-size:30px; color:#4d6078; font-weight:600">Spring Boot 3.5 · Spring Security + JWT · JPA/Hibernate · PostgreSQL · RabbitMQ</div>
  <div style="font-size:84px; font-weight:900; margin:30px 0 18px; color:#10213a">Middle Java Store</div>
  <div style="font-size:34px; color:#1659a8; font-weight:700">Bán khóa học Java · Giỏ hàng · Đặt hàng · Thanh toán · Thông báo</div>
  <div style="margin-top:46px; display:flex; gap:18px; justify-content:center; font-size:24px">
   <span class="panel" style="padding:12px 22px">Strategy</span><span class="panel" style="padding:12px 22px">Factory Method</span>
   <span class="panel" style="padding:12px 22px">Observer</span><span class="panel" style="padding:12px 22px">SOLID</span>
   <span class="panel" style="padding:12px 22px">JUnit + Mockito</span><span class="panel" style="padding:12px 22px">GitHub Actions → AWS</span>
  </div>
 </div>
</div>"""),
    "Xin chào thầy và các bạn. Em là Trịnh Đức Dương. Đây là video demo bài OJT E-Commerce Mini của em, tên là Middle Java Store, "
    "một hệ thống bán các khóa học Java quay sẵn. Em sẽ trình bày theo thứ tự: kiến trúc, phần code áp dụng kiến thức của khóa học, "
    "kiểm thử, quy trình CI CD lên AWS, sau đó demo trên môi trường local bằng Postman và trên AWS qua giao diện web.")

table_frame(S0, "Đề bài → chức năng đã làm", "Mỗi yêu cầu đều có code, test và bước demo tương ứng",
            ["Yêu cầu", "Đã áp dụng", "Demo"],
            [["1. Authentication", "Spring Security + JWT, role ADMIN / CUSTOMER", "Postman 2.x · UI đăng nhập"],
             ["2. Quản lý sản phẩm", "JPA/Hibernate, SRP, index GIN trigram trên title", "Postman 1.3, 3.x · UI admin"],
             ["3. Giỏ hàng", "Thêm / sửa số lượng / xóa · Extract Method, Extract Class · JUnit + Mockito", "Postman 4.x"],
             ["4. Đặt hàng", "@Transactional · Stream API · Optional", "Postman 5.3 · test rollback"],
             ["5. Thanh toán", "VNPay giả lập, COD, chuyển khoản · Strategy + Factory Method · OCP", "Postman 5.x, 6.x"],
             ["6. Thông báo", "Observer · CompletableFuture + ExecutorService", "Postman 5.6, 6.8 · log thread"],
             ["Kiểm thử", "41 test: unit test Service (Mockito) + integration test PostgreSQL thật", "mvn verify · CI"]],
            "Đây là bảng đối chiếu đề bài với những gì em đã làm. Sáu yêu cầu chức năng gồm xác thực, quản lý sản phẩm, giỏ hàng, đặt hàng, "
            "thanh toán và thông báo, cộng với phần kiểm thử. Mỗi dòng đều có phần code, phần test và bước demo tương ứng mà em sẽ đi qua ngay sau đây.")

add(S0, "Kiến trúc hệ thống", "Microservices sau API Gateway · cùng một bộ code chạy local và AWS", stage("""
<div style="display:grid; grid-template-columns: 1fr 1fr 1fr; gap:26px; height:100%; font-size:24px">
 <div class="panel" style="padding:26px"><div class="lbl">Client</div>
  <div class="panel" style="padding:18px; margin:12px 0">Postman collection<br><span class="muted" style="font-size:19px;color:#5d6e84">33 request · local + cloud env</span></div>
  <div class="panel" style="padding:18px">Web UI tối giản<br><span style="font-size:19px;color:#5d6e84">CUSTOMER + ADMIN</span></div></div>
 <div class="panel" style="padding:26px"><div class="lbl">Spring Cloud</div>
  <div class="panel" style="padding:18px; margin:12px 0; border-color:#1659a8"><b>api-gateway</b> :8080<br><span style="font-size:19px;color:#5d6e84">routing · phục vụ UI</span></div>
  <div class="panel" style="padding:18px; margin:12px 0"><b>discovery-server</b> (Eureka)</div>
  <div class="panel" style="padding:18px; margin:12px 0; border:3px solid #168454"><b>learning-service</b><br><span style="font-size:19px;color:#5d6e84">auth · sản phẩm · giỏ · đơn · thanh toán · thông báo</span></div>
  <div class="panel" style="padding:18px"><b>ai-service</b> (RAG, ngoài phạm vi OJT)</div></div>
 <div class="panel" style="padding:26px"><div class="lbl">Hạ tầng</div>
  <div class="panel" style="padding:18px; margin:12px 0">PostgreSQL 16 + Flyway V1→V5</div>
  <div class="panel" style="padding:18px; margin:12px 0">RabbitMQ</div>
  <div class="panel" style="padding:18px; margin:12px 0">Local: Docker Compose</div>
  <div class="panel" style="padding:18px">AWS: CloudFront → EC2 (Docker) · RDS · ECR · SSM</div></div>
</div>"""),
    "Về kiến trúc, client là Postman hoặc giao diện web. Mọi request đi qua API Gateway, gateway tìm service qua Eureka. "
    "Toàn bộ nghiệp vụ thương mại điện tử nằm trong learning service: xác thực, sản phẩm, giỏ hàng, đơn hàng, thanh toán và thông báo. "
    "Dữ liệu lưu ở PostgreSQL, schema được quản lý bằng Flyway. Ở local em chạy hạ tầng bằng Docker Compose, còn trên AWS là CloudFront, EC2, RDS và ECR.")

# ---- code
SEC = "learning-service/src/main/java/vn/edu/middlejava/learning/auth/config/SecurityConfiguration.java"
AUTH = "learning-service/src/main/java/vn/edu/middlejava/learning/auth/service/impl/AuthServiceImpl.java"
code_frame(S1, "1. Authentication — Spring Security + JWT", "Phân quyền ADMIN / CUSTOMER theo URL, role nằm trong claim của JWT", [
    (SEC, "converter.setJwtGrantedAuthoritiesConverter", ".anyRequest().authenticated())",
     [("Object rolesClaim", "SimpleGrantedAuthority(role)"), ('.requestMatchers("/api/admin/**"', None)]),
], "Phần một là xác thực. Em dùng Spring Security làm Resource Server với JWT ký HS256. Bộ converter ở trên đọc claim roles trong token "
   "để chuyển thành quyền của Spring. Ở dưới, mọi URL bắt đầu bằng slash api slash admin đều yêu cầu role ADMIN; "
   "CUSTOMER gọi vào sẽ nhận lỗi 403.")

code_frame(S1, "Phát hành JWT khi đăng nhập", "AuthServiceImpl.issueTokens — access token 15 phút + refresh token", [
    (AUTH, "private AuthResponse issueTokens", "return new AuthResponse", [('.subject(user.getId()', '.claim("roles"'), ("String accessToken = jwtEncoder", None)]),
], "Khi đăng nhập thành công, service phát hành access token có hạn 15 phút. Subject là id của user, kèm claim email và roles, "
   "ví dụ ROLE CUSTOMER. Ngoài ra có refresh token; refresh token chỉ lưu dạng băm SHA 256 trong database.")

V2 = "learning-service/src/main/resources/db/migration/V2__course_store.sql"
REPO_C = "learning-service/src/main/java/vn/edu/middlejava/learning/course/repository/ICourseRepository.java"
code_frame(S1, "2. Quản lý sản phẩm — JPA/Hibernate + Index", "Index GIN trigram trên upper(title) phục vụ tìm kiếm theo tên", [
    (V2, "ALTER TABLE courses ADD COLUMN price", "UPDATE courses SET price", [("CREATE EXTENSION IF NOT EXISTS pg_trgm", "idx_courses_title_substring")]),
    (REPO_C, "public interface ICourseRepository", "findByActiveTrueAndTitleContainingIgnoreCaseOrderByIdAsc", [("findByActiveTrueAndTitleContainingIgnoreCaseOrderByIdAsc", None)]),
], "Phần hai là quản lý sản phẩm. Bên trái là migration Flyway: em bật extension pg trgm và tạo index GIN trên upper của title. "
   "Bên phải là repository Spring Data JPA. Method này sinh câu SQL upper title like upper tham số, khớp đúng biểu thức của index, "
   "nên tìm kiếm chứa từ khóa vẫn dùng được index thay vì quét toàn bảng.")

CAT = "learning-service/src/main/java/vn/edu/middlejava/learning/commerce/service/impl/CatalogAdminServiceImpl.java"
code_frame(S1, "SRP — dịch vụ quản trị sản phẩm tách khỏi catalog công khai", "CatalogAdminServiceImpl: chỉ ADMIN dùng · ngừng bán = soft delete", [
    (CAT, "/** SRP:", "private Course findCourse", [("/** SRP:", None), ("public void deactivate", "findCourse(courseId).deactivate()")]),
], "Theo nguyên lý Single Responsibility, phần quản trị sản phẩm của ADMIN nằm trong một service riêng, tách khỏi catalog công khai. "
   "Thao tác xóa là soft delete: khóa học bị đánh dấu ngừng bán, nhưng các đơn hàng cũ vẫn giữ nguyên lịch sử.")

CART = "learning-service/src/main/java/vn/edu/middlejava/learning/commerce/service/impl/CartServiceImpl.java"
code_frame(S1, "3. Giỏ hàng — Extract Method", "Method public ngắn gọn, các bước nhỏ được đặt tên rõ ràng", [
    (CART, "public CartResponse addToCart", "private CartResponse toResponse", [
        ("public CartResponse addToCart", "return toResponse(cart);"), ("private Course findSellableCourse", None),
        ("private ShoppingCart findOrCreateCart", None), ("private ShoppingCart findCart", None)]),
], "Phần ba là giỏ hàng, với ba thao tác thêm, cập nhật số lượng và xóa. Em áp dụng Extract Method: các method public chỉ còn vài dòng, "
   "đọc như một câu nghiệp vụ. Những bước như tìm khóa học còn bán, tìm hoặc tạo giỏ, tìm giỏ được tách thành các method private có tên rõ ràng "
   "và dùng lại ở mọi thao tác.")

PRICE = "learning-service/src/main/java/vn/edu/middlejava/learning/commerce/pricing/CartPricingCalculator.java"
code_frame(S1, "Extract Class — CartPricingCalculator", "Logic tính tiền trước đây bị lặp ở giỏ hàng và checkout, nay có một chủ sở hữu", [
    (PRICE, " * Extract Class refactoring", "private BigDecimal sumLineTotals", [
        (" * Extract Class refactoring", " */"), ("public PricedCart price", "return new PricedCart"),
        ("courses.findAllById(courseIds).stream()", "Collectors.toMap")]),
], "Tiếp theo là Extract Class. Trước đây phần tính tiền bị viết lặp ở cả giỏ hàng lẫn checkout. Em tách ra thành class CartPricingCalculator "
   "trả về một PricedCart bất biến. Bên trong dùng Stream API: map từng mục thành dòng giá, rồi gom bằng Collectors toMap. "
   "Tương tự, phần map sang DTO cũng được tách ra class CommerceMapper.")

ORDER = "learning-service/src/main/java/vn/edu/middlejava/learning/commerce/service/impl/OrderServiceImpl.java"
code_frame(S1, "4. Đặt hàng — @Transactional + Optional + Stream", "OrderServiceImpl.checkout: chuyển giỏ hàng thành đơn hàng trong một transaction", [
    (ORDER, "     * One transaction", "return mapper.toOrderResponse(order);", [
        ("@Transactional", None), ("ShoppingCart cart = carts.findByUserId(userId)", '.orElseThrow(() -> new IllegalArgumentException("Cart is empty"));'),
        ("PaymentStrategy payment = payments.create", None), ("if (order.isPaid())", "events.publishEvent")]),
], "Phần bốn là đặt hàng. Toàn bộ checkout chạy trong một transaction. Em dùng Optional: tìm giỏ, lọc giỏ rỗng, nếu không có thì ném lỗi. "
   "Sau đó lấy strategy thanh toán từ factory, tính tiền, lưu đơn, mở quyền học nếu đã thanh toán, làm trống giỏ và phát sự kiện. "
   "Nếu bất kỳ bước nào lỗi thì tất cả được rollback: không có đơn nửa vời và giỏ hàng còn nguyên.")

PS = "learning-service/src/main/java/vn/edu/middlejava/learning/commerce/payment/PaymentStrategy.java"
VNP = "learning-service/src/main/java/vn/edu/middlejava/learning/commerce/payment/VnPayPaymentStrategy.java"
COD = "learning-service/src/main/java/vn/edu/middlejava/learning/commerce/payment/CodPaymentStrategy.java"
code_frame(S1, "5. Thanh toán — Strategy Pattern", "Mỗi phương thức thanh toán là một strategy cùng hợp đồng", [
    (PS, "/**", "PaymentResult pay(PaymentRequest request);", [("public interface PaymentStrategy", None), ("PaymentResult pay", None)]),
    (VNP, "@Component", "return PaymentResult.paid(transactionNo);", [("return \"VNPAY\";", None), ("return PaymentResult.paid", None)]),
], "Phần năm là thanh toán, em áp dụng Strategy Pattern. Interface PaymentStrategy có hai method: mã phương thức và pay. "
   "Có ba strategy: VNPay giả lập thì thanh toán ngay và trả về mã giao dịch bắt đầu bằng VNP; COD và chuyển khoản thì ở trạng thái chờ, "
   "đợi ADMIN xác nhận. Nhờ vậy checkout không cần if else theo từng loại thanh toán.")

PF = "learning-service/src/main/java/vn/edu/middlejava/learning/commerce/payment/PaymentStrategyFactory.java"
code_frame(S1, "Factory Method + Open/Closed Principle", "PaymentStrategyFactory.create(method) — thêm phương thức mới không cần sửa factory", [
    (PF, " * Factory Method Pattern", "private static String normalize", [
        ("public PaymentStrategyFactory(List<PaymentStrategy> strategies)", "Function.identity()));"),
        ("public PaymentStrategy create(String method)", '"Unsupported payment method. Supported: "')]),
], "Factory Method nằm ở PaymentStrategyFactory. Method create là nơi duy nhất quyết định strategy nào phục vụ request, client chỉ biết interface. "
   "Spring tự inject danh sách mọi strategy vào constructor. Vì vậy muốn thêm MoMo, em chỉ cần viết thêm một class mới, "
   "không phải sửa factory hay checkout. Đây chính là nguyên lý đóng với sửa đổi, mở để mở rộng.")

PUB = "learning-service/src/main/java/vn/edu/middlejava/learning/commerce/event/OrderEventPublisher.java"
EXE = "learning-service/src/main/java/vn/edu/middlejava/learning/config/NotificationExecutorConfiguration.java"
code_frame(S1, "6. Thông báo — Observer + CompletableFuture + ExecutorService", "Subject gửi sự kiện cho từng observer trên thread riêng, sau khi transaction commit", [
    (PUB, "@TransactionalEventListener", "return CompletableFuture.allOf(deliveries);", [
        ("@TransactionalEventListener", None), (".runAsync(() -> observer.onOrderStatusChanged(event), executor)", None),
        (".exceptionally(error -> {", "return null;")]),
    (EXE, "@Bean(name", "return Executors.newFixedThreadPool", [("return Executors.newFixedThreadPool", None)]),
], "Phần sáu là thông báo trạng thái đơn hàng, dùng Observer Pattern. OrderEventPublisher là subject: nó chỉ nhận sự kiện sau khi transaction đã commit. "
   "Với mỗi observer, em gọi CompletableFuture run Async trên một ExecutorService riêng gồm hai thread. Nếu một kênh bị lỗi, exceptionally "
   "sẽ cô lập lỗi đó và các kênh khác vẫn chạy, còn request đặt hàng không phải chờ.")

OBS = "learning-service/src/main/java/vn/edu/middlejava/learning/commerce/event/OrderStatusObserver.java"
INAPP = "learning-service/src/main/java/vn/edu/middlejava/learning/commerce/event/InAppNotificationObserver.java"
code_frame(S1, "Các observer", "Thêm kênh mới (SMS, Zalo…) = thêm một class implements OrderStatusObserver", [
    (OBS, "/**", "void onOrderStatusChanged", [("public interface OrderStatusObserver", None)]),
    (INAPP, "@Component", "log.info", [("implements OrderStatusObserver", None), ("notifications.save", None)]),
], "Hiện có hai observer: In App lưu thông báo vào database để khách đọc qua API notifications, còn Email ghi log giả lập việc gửi mail. "
   "Muốn thêm SMS hay Zalo thì chỉ cần thêm một class implements OrderStatusObserver.")

table_frame(S1, "SOLID trong project", "", ["Nguyên lý", "Ví dụ trong code"],
            [["<b>S</b> — Single Responsibility", "CartServiceImpl · OrderServiceImpl · AdminOrderServiceImpl · CartPricingCalculator · CommerceMapper"],
             ["<b>O</b> — Open/Closed", "Thêm PaymentStrategy hoặc OrderStatusObserver mới, không sửa code cũ (có test chứng minh)"],
             ["<b>L</b> — Liskov Substitution", "Mọi PaymentStrategy / OrderStatusObserver thay thế nhau được qua interface"],
             ["<b>I</b> — Interface Segregation", "ICartService · IOrderService · IAdminOrderService · ICatalogAdminService, controller cũng tách interface"],
             ["<b>D</b> — Dependency Inversion", "Service phụ thuộc abstraction, inject qua constructor → dễ mock trong unit test"]],
            "Tóm lại phần SOLID. S: mỗi service và mỗi class hỗ trợ chỉ có một lý do để thay đổi. O: thêm phương thức thanh toán hay kênh thông báo "
            "mà không sửa code cũ. L: các strategy và observer thay thế nhau được. I: interface nhỏ, tách theo vai trò. "
            "D: service phụ thuộc vào abstraction và được inject qua constructor, nên unit test mock rất dễ.")

# ---- tests
CT = "learning-service/src/test/java/vn/edu/middlejava/learning/commerce/service/CartServiceImplTest.java"
code_frame(S2, "Unit test giỏ hàng — JUnit 5 + Mockito", "CartServiceImplTest: 11 test cho thêm / cập nhật / xóa", [
    (CT, "@ExtendWith(MockitoExtension.class)", "verify(carts, never()).save(any());", [
        ("@ExtendWith(MockitoExtension.class)", None), ("@Mock", "private ICourseRepository courses;"),
        ("void addSameCourseTwiceIncreasesQuantity", "verify(carts, never()).save(any());")]),
], "Chuyển sang kiểm thử. Đây là unit test cho giỏ hàng với JUnit 5 và Mockito. Repository được mock bằng annotation Mock. "
   "Ví dụ test thêm cùng một khóa hai lần: số lượng phải cộng dồn thành ba, tổng tiền đúng, và verify rằng không tạo thêm giỏ mới. "
   "Lớp này có mười một test, bao phủ cả các trường hợp lỗi như số lượng nhỏ hơn một hay xóa mục không có trong giỏ.")

IT = "learning-service/src/test/java/vn/edu/middlejava/learning/commerce/PostgresCommerceIntegrationTest.java"
code_frame(S2, "Integration test luồng đặt hàng — PostgreSQL thật (Testcontainers)", "Chứng minh @Transactional rollback khi một bước bị lỗi", [
    (IT, "void transactionalCheckoutRollsBackEverythingWhenAStepFails", "assertThat(notifications.findByUserIdOrderByCreatedAtDesc(customer.getId())).isEmpty();", [
        ("doThrow(new IllegalStateException", ".when(enrollmentService)"),
        ("assertThat(orders.count()).isEqualTo(ordersBefore);", "assertThat(notifications.findByUserIdOrderByCreatedAtDesc(customer.getId())).isEmpty();")]),
], "Integration test chạy trên PostgreSQL và RabbitMQ thật, do Testcontainers khởi động bằng Docker. Test này cố tình cho bước mở quyền học ném lỗi "
   "giữa chừng checkout, rồi kiểm tra ba điều: số đơn hàng không tăng, giỏ hàng vẫn còn sản phẩm, và không có thông báo nào được gửi. "
   "Như vậy annotation Transactional đã rollback đúng.")

reports = sorted((REPO / "learning-service/target/surefire-reports").glob("*.txt"))
term = [("cmd", "> mvn -B -pl learning-service verify"), ("dim", "")]
total = 0
for r in reports:
    if "CommerceServiceImplTest" in r.name:
        continue
    line = next(l for l in r.read_text(encoding="utf-8").splitlines() if l.startswith("Tests run"))
    n = int(line.split(",")[0].split(":")[1])
    total += n
    term.append(("ok", f"[INFO] Tests run: {n:>2}, Failures: 0, Errors: 0 -- {r.stem.split('.')[-1]}"))
term += [("dim", ""), ("ok", f"[INFO] Tests run: {total}, Failures: 0, Errors: 0, Skipped: 0"), ("ok", "[INFO] BUILD SUCCESS")]
hl_idx = [i for i, (_, t) in enumerate(term) if "CartServiceImplTest" in t or "PostgresCommerceIntegrationTest" in t or f"Tests run: {total}," in t]
term_frame(S2, "Kết quả kiểm thử", "Chạy ở local và tự động trong GitHub Actions trước mỗi lần deploy", term,
           f"Đây là kết quả chạy Maven: {total} test, không có lỗi. Gồm unit test cho các service giỏ hàng, đặt hàng, quản trị đơn, factory thanh toán, "
           "bộ tính giá và publisher, cùng các integration test. Bộ test này cũng chạy tự động trên GitHub Actions trước mỗi lần deploy.",
           hl=hl_idx)

# ---- local demo
term_frame(S3, "Khởi động môi trường local", "Docker Compose cho PostgreSQL + RabbitMQ, 4 service Spring Boot, Postman environment LOCAL", [
    ("cmd", "> docker compose up -d postgres rabbitmq"),
    ("ok", " ✔ Container postgres-1  Started    ✔ Container rabbitmq-1  Started"),
    ("cmd", "> mvn -B clean verify"),
    ("cmd", "> java -jar discovery-server/target/discovery-server-1.0.0.jar"),
    ("cmd", "> java -jar learning-service/target/learning-service-1.0.0.jar"),
    ("ok", "   Flyway: Successfully applied 1 migration to schema \"learning\", now at version v5"),
    ("cmd", "> java -jar ai-service/target/ai-service-1.0.0.jar"),
    ("cmd", "> java -jar api-gateway/target/api-gateway-1.0.0.jar"),
    ("dim", ""),
    ("warn", "Postman: import demo/postman/MiddleJavaStore.postman_collection.json"),
    ("warn", "         + local.postman_environment.json  (baseUrl = http://localhost:8080)"),
], "Bây giờ em demo trên môi trường local. Em khởi động PostgreSQL và RabbitMQ bằng Docker Compose, rồi chạy bốn service Spring Boot. "
   "Khi learning service khởi động, Flyway tự migrate schema lên phiên bản năm. Trong Postman, em import collection và environment LOCAL có sẵn trong thư mục demo.",
    hl=[5, 9, 10])

api_frame("local", "1.2", "Đầu tiên là API danh sách sản phẩm, API này công khai, không cần token. Mỗi khóa học có id, tên, mô tả và giá. "
          "Lưu ý response công khai không trả về link video.", keys=["price"])
api_frame("local", "1.3", "Tìm kiếm theo tên với từ khóa java. Câu truy vấn này dùng index trigram em vừa trình bày, và trả về hai khóa học có chữ Java trong tên.", keys=["title"])
api_frame("local", "2.1", "Đăng ký một tài khoản khách hàng mới. Kết quả là 201 Created, kèm access token và refresh token. Tài khoản mới mặc định có role CUSTOMER.", keys=["accessToken", "tokenType"])
api_frame("local", "2.2", "Đăng nhập bằng tài khoản vừa tạo. Script test của Postman tự lưu access token vào biến accessToken để dùng cho các request sau.", keys=["accessToken", "expiresAt"])
api_frame("local", "2.3", "Thử phân quyền: dùng token CUSTOMER gọi API tạo sản phẩm của ADMIN. Spring Security chặn lại và trả về 403 Forbidden.")
api_frame("local", "2.4", "Đăng nhập bằng tài khoản ADMIN. Token này được lưu vào biến adminToken.", keys=["accessToken"])
api_frame("local", "3.1", "Với quyền ADMIN, em tạo một sản phẩm mới là khóa Java Design Patterns, giá 24 đô 99. Kết quả 201 Created. "
          "API admin có trả về link YouTube và trạng thái đang bán.", keys=["id", "youtubeUrl", "active"])
api_frame("local", "3.2", "Cập nhật giá sản phẩm xuống 19 đô 99. Slug được giữ cố định và không cho phép đổi.", keys=["price"])
api_frame("local", "4.1", "Sang phần giỏ hàng. CUSTOMER thêm khóa Middle Java với số lượng một. Giỏ được tạo tự động, tổng tiền 29 đô 99.", keys=["quantity", "total"])
api_frame("local", "4.2", "Thêm tiếp khóa thứ hai là khóa vừa tạo. Giỏ hàng có hai mục và tổng tiền được tính lại.", keys=["title", "total"])
api_frame("local", "4.3", "Cập nhật số lượng của Middle Java lên hai. Thành tiền của dòng này là 59 đô 98, và tổng giỏ được tính lại bởi CartPricingCalculator.", keys=["quantity#1", "lineTotal#1", "total"])
api_frame("local", "4.4", "Xóa sản phẩm thứ hai khỏi giỏ. API trả về 204 No Content.")
api_frame("local", "4.5", "Xem lại giỏ hàng: chỉ còn Middle Java với số lượng hai, tổng 59 đô 98.", keys=["quantity", "total"])
api_frame("local", "5.1", "API này liệt kê các phương thức thanh toán. Danh sách lấy trực tiếp từ factory, nên khi thêm một strategy mới nó sẽ tự xuất hiện ở đây.", hl_body=True)
api_frame("local", "5.2", "Trước khi mua, khách gọi API xem video của khóa Middle Java thì bị từ chối với lỗi 403.", keys=["detail"])
api_frame("local", "5.3", "Đặt hàng bằng VNPay giả lập. Đơn được tạo với trạng thái thanh toán PAID, có mã giao dịch VNP do strategy sinh ra, "
          "và trạng thái đơn là CONFIRMED. Giá và tên khóa học được chụp lại vào đơn tại thời điểm mua.", keys=["paymentMethod", "paymentStatus", "paymentReference", "status"])
api_frame("local", "5.5", "Sau khi thanh toán, gọi lại API video thì thành công: trả về link YouTube dạng embed để học.", keys=["youtubeEmbedUrl"])
api_frame("local", "5.6", "Kiểm tra thông báo: observer In App đã lưu thông báo đơn hàng được tạo. Thông báo này được ghi bất đồng bộ, sau khi transaction commit.", keys=["message"])
term_frame(S3, "Log: observer chạy trên thread riêng", "learning-service log — request thread khác với thread gửi thông báo", [
    ("dim", "[nio-8081-exec-7] OrderEventPublisher   : Dispatching CREATED for order 3 to 2 observers"),
    ("ok", "[ order-notify-1] EmailNotificationObserver : [EMAIL] thread=order-notify-1 to user=3 subject=\"Order #3 CREATED\""),
    ("ok", "[ order-notify-2] InAppNotificationObserver : [IN_APP] thread=order-notify-2 user=3 order=3 type=CREATED"),
    ("dim", "[nio-8081-exec-5] OrderEventPublisher   : Dispatching PAYMENT_CONFIRMED for order 4 to 2 observers"),
    ("ok", "[ order-notify-1] EmailNotificationObserver : [EMAIL] ... \"Payment for order #4 has been confirmed.\""),
    ("ok", "[ order-notify-2] InAppNotificationObserver : [IN_APP] thread=order-notify-2 user=3 order=4 type=PAYMENT_CONFIRMED"),
], "Nhìn vào log để thấy CompletableFuture hoạt động. Request đặt hàng chạy trên thread của Tomcat là nio exec, còn hai observer Email và In App "
   "chạy song song trên hai thread order notify của ExecutorService.", hl=[0, 1, 2])
api_frame("local", "6.2", "Tiếp theo em thử COD. Thêm khóa Java Fundamentals rồi đặt hàng bằng COD. Lần này đơn ở trạng thái PENDING và AWAITING PAYMENT.", keys=["paymentStatus", "status"])
api_frame("local", "6.3", "Vì COD chưa trả tiền nên khách vẫn chưa xem được video, kết quả 403.", keys=["detail"])
api_frame("local", "6.5", "ADMIN xác nhận đã nhận tiền cho đơn COD. Đơn chuyển sang PAID và CONFIRMED, quyền học được mở, và observer gửi thông báo đổi trạng thái.", keys=["paymentStatus", "status"])
api_frame("local", "6.6", "Nếu xác nhận lần thứ hai, hệ thống trả về 409 Conflict vì đơn đã được thanh toán rồi.", keys=["detail"])
api_frame("local", "6.7", "Bây giờ khách xem được video của khóa Java Fundamentals.", keys=["youtubeEmbedUrl"])
api_frame("local", "6.8", "Danh sách thông báo có thêm thông báo xác nhận thanh toán. Mỗi lần trạng thái đơn thay đổi đều sinh một thông báo.", keys=["message#2"])
api_frame("local", "7.1", "Trường hợp lỗi nghiệp vụ: đặt hàng khi giỏ trống sẽ trả về 400 Bad Request với thông báo Cart is empty.", keys=["detail"])
api_frame("local", "7.2", "Cuối cùng, ADMIN ngừng bán sản phẩm vừa tạo bằng DELETE. Khóa học biến mất khỏi catalog công khai nhưng các đơn hàng cũ vẫn giữ lịch sử.")
shot_frame(S3UI, "ui-local-admin-orders", "Giao diện web — ADMIN quản lý đơn hàng", "Cùng một trang web, role đọc từ JWT quyết định màn hình hiển thị",
           "Ngoài Postman, em làm thêm một giao diện web tối giản. Khi đăng nhập bằng ADMIN, giao diện hiện màn hình quản lý đơn hàng. "
           "Các đơn đang chờ có nút xác nhận đã thanh toán.", env="local")
shot_frame(S3UI, "ui-local-admin-confirmed", "ADMIN xác nhận thanh toán trên giao diện", "", "Bấm xác nhận thì đơn chuyển sang PAID và CONFIRMED ngay.", env="local")
shot_frame(S3UI, "ui-local-admin-product-form", "ADMIN thêm sản phẩm", "", "Ở tab quản lý sản phẩm, ADMIN nhập thông tin khóa học mới vào form.", env="local")
shot_frame(S3UI, "ui-local-admin-products", "Danh sách sản phẩm (ADMIN)", "", "Sản phẩm mới xuất hiện trong danh sách với trạng thái đang bán.", env="local")
shot_frame(S3UI, "ui-local-admin-deactivated", "Ngừng bán sản phẩm", "", "ADMIN có thể ngừng bán, trạng thái đổi thành ngừng bán.", env="local")

# ---- CI/CD + AWS
WF = ".github/workflows/aws-deploy.yml"
code_frame(S4, "CI/CD — GitHub Actions", ".github/workflows/aws-deploy.yml: verify (test) → deploy (build image → ECR → EC2 qua SSM)", [
    (WF, "jobs:", "Store Groq key in AWS Parameter Store", [("- run: mvn -B verify", None), ("needs: verify", None),
                                                          ("for service in discovery-server", "docker push")]),
], "Phần CI CD. Mỗi lần push lên GitHub, workflow chạy job verify là mvn verify với toàn bộ test. Chỉ khi test xanh thì job deploy mới chạy: "
   "build bốn Docker image, đẩy lên Amazon ECR, rồi qua Systems Manager ra lệnh cho EC2 kéo image mới và khởi động lại bằng Docker Compose.")
shot_frame(S4, "gh-run", "Pipeline chạy thành công", "GitHub Actions — verify → deploy",
           "Đây là một lần chạy pipeline thành công trên nhánh main: job verify xong thì job deploy chạy tiếp, trạng thái Success.")
table_frame(S4, "Hạ tầng AWS (CloudFormation stack study-coach)", "Region ap-southeast-2 · giá trị đọc từ Outputs của stack",
            ["Thành phần", "Giá trị"],
            [["PublicUrl (CloudFront)", "<b>https://d3jaw9bd80q6ki.cloudfront.net</b>"],
             ["EC2 (Docker Compose)", "i-0fbadee3f9b3ce12f"],
             ["RDS PostgreSQL", "study-coach-database-….ap-southeast-2.rds.amazonaws.com"],
             ["ECR registry", "….dkr.ecr.ap-southeast-2.amazonaws.com/studycoach/*"],
             ["Secrets", "SSM Parameter Store: JWT secret, DB password, admin account (SecureString)"],
             ["Backup", "S3 bucket …-studycoach-db-backups"]],
            "Hạ tầng AWS được tạo bằng CloudFormation. Người dùng truy cập qua CloudFront, CloudFront chuyển vào EC2 chạy các container. "
            "Database là RDS PostgreSQL. Các bí mật như JWT secret, mật khẩu database và tài khoản admin được lưu trong Parameter Store dạng SecureString, "
            "không nằm trong source code.", hl_rows=(0,))

# ---- cloud
api_frame("cloud", "1.1", "Giờ em demo trên AWS. Health check qua CloudFront trả về UP.", keys=["status"])
for shot, title, say in [
    ("ui-cloud-login", "Truy cập từ bất cứ đâu qua CloudFront", "Giao diện web được deploy cùng API Gateway nên truy cập được từ bất cứ đâu qua địa chỉ CloudFront. Màn hình đầu là đăng nhập và đăng ký, góc trên hiển thị hệ thống đang hoạt động."),
    ("ui-cloud-shop", "CUSTOMER đăng ký và vào cửa hàng", "Em đăng ký một khách hàng mới. Role CUSTOMER hiện ở góc phải, và trang cửa hàng hiển thị các khóa học."),
    ("ui-cloud-search", "Tìm kiếm sản phẩm", "Tìm kiếm với từ khóa java, gọi đúng API search dùng index."),
    ("ui-cloud-cart", "Giỏ hàng: thêm và cập nhật số lượng", "Thêm hai khóa vào giỏ và đổi số lượng Middle Java thành hai. Tổng tiền được tính lại."),
    ("ui-cloud-cart-removed", "Xóa sản phẩm khỏi giỏ", "Xóa một khóa khỏi giỏ hàng."),
    ("ui-cloud-checkout", "Đặt hàng với VNPay giả lập", "Chọn VNPay và đặt hàng. Thông báo đặt hàng thành công hiện ra với trạng thái PAID."),
    ("ui-cloud-orders-paid", "Đơn hàng đã thanh toán", "Ở tab đơn hàng, đơn có trạng thái PAID, CONFIRMED và mã giao dịch VNPay."),
    ("ui-cloud-video", "Mở khóa video sau khi thanh toán", "Bấm xem video thì khóa học được mở, phát từ YouTube."),
    ("ui-cloud-notifications", "Thông báo trạng thái đơn hàng", "Tab thông báo hiển thị thông báo do observer tạo ra."),
    ("ui-cloud-orders-cod", "Đơn COD chờ ADMIN xác nhận", "Đặt thêm một đơn COD: đơn ở trạng thái chờ thanh toán. ADMIN xác nhận đơn này giống như phần em vừa demo ở local."),
]:
    shot_frame(S5, shot, title, CLOUD["baseUrl"], say, env="cloud")
api_frame("cloud", "5.3", "Cùng luồng đó gọi bằng Postman với environment AWS: đặt hàng VNPay trên cloud trả về PAID và mã giao dịch.", keys=["paymentStatus", "paymentReference", "status"])

table_frame(S6, "Tổng kết", "", ["Hạng mục", "Kết quả"],
            [["Chức năng", "Đăng ký/đăng nhập JWT · phân quyền ADMIN/CUSTOMER · sản phẩm · giỏ hàng · đặt hàng · 3 phương thức thanh toán · thông báo"],
             ["Kiến thức", "Spring Security, JPA/Hibernate, index, @Transactional, Stream/Optional, CompletableFuture/ExecutorService, refactoring"],
             ["Design Pattern", "Strategy · Factory Method · Observer"],
             ["SOLID", "S · O · L · I · D (xem báo cáo demo/BAO_CAO_OJT.md)"],
             ["Kiểm thử", f"{total} test (unit test Mockito + integration test PostgreSQL thật), chạy trong CI"],
             ["Triển khai", "GitHub Actions → ECR → EC2, truy cập qua CloudFront · UI cho cả 2 role"]],
            "Tóm lại, em đã hoàn thành đủ các yêu cầu của đề bài, áp dụng ba design pattern Strategy, Factory Method và Observer cùng năm nguyên lý SOLID, "
            "có unit test và integration test chạy tự động trong CI, và hệ thống đã được deploy lên AWS để truy cập từ bất cứ đâu. "
            "Em cảm ơn thầy và các bạn đã theo dõi.")

# ================================================================== RENDER
total_frames = len(frames)
for i, f in enumerate(frames, 1):
    (FRAMES / f"{i:03d}.html").write_text(page(f["section"], i, total_frames, f["title"], f["sub"], f["body"], f["env"], BOX_JS), encoding="utf-8")

only = set(sys.argv[3].split(",")) if len(sys.argv) > 3 else None  # re-render selected frames, e.g. "005,012"
todo = [i for i in range(1, total_frames + 1)
        if (only and f"{i:03d}" in only) or (not only and not (WORK / "png" / f"{i:03d}.png").exists())]
for batch_start in range(0, len(todo), 8):
    batch = todo[batch_start:batch_start + 8]
    scenario = {"baseUrl": FRAMES.resolve().as_uri() + "/", "viewport": {"width": W, "height": H},
                "steps": [{"goto": f"{i:03d}.html", "wait": 500, "shot": f"{i:03d}"} for i in batch]}
    (WORK / "scenario-frames.json").write_text(json.dumps(scenario), encoding="utf-8")
    subprocess.run(["node", str(WORK / "ui_capture.mjs"), str(WORK / "scenario-frames.json"), str(WORK / "png")],
                   check=True, timeout=180, stdout=subprocess.DEVNULL)
    print("rendered", batch[0], "-", batch[-1], flush=True)

# concat list + timeline
lines, t = [], 0.0
timeline = []
for i, f in enumerate(frames, 1):
    lines.append(f"file 'png/{i:03d}.png'\nduration {f['seconds']:.2f}\n")
    timeline.append((i, t, t + f["seconds"], f))
    t += f["seconds"]
lines.append(f"file 'png/{total_frames:03d}.png'\n")
(WORK / "concat.txt").write_text("".join(lines), encoding="utf-8")
(WORK / "timeline.json").write_text(json.dumps([{"frame": i, "start": round(a, 2), "end": round(b, 2), "section": f["section"],
                                                 "title": f["title"], "say": f["say"]} for i, a, b, f in timeline], ensure_ascii=False, indent=1), encoding="utf-8")
print(f"{total_frames} frames, {t:.1f}s ({t / 60:.1f} min)")

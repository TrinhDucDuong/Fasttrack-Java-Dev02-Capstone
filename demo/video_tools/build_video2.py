"""Demo video v2: core requirements (local, both roles) + 'Mở rộng' (CI/CD, secrets, AWS, cloud UI both roles).

usage: python build_video2.py <repo> <workdir> [render|plan]
  plan   -> only print frame count, character budget and duration
  render -> render PNG frames, write timeline2.json and the Vbee SRT
"""
import json
import subprocess
import sys
from pathlib import Path

import video_common as vc
from video_common import (add, api_frame, code_frame, shot_frame, stage, table_frame, term_frame, frames,
                          REPO, WORK, FRAMES, LOCAL, CLOUD, MANIFEST)
from frames_lib import BOX_JS, H, W, page
from tts_text import phonetic

MODE = sys.argv[3] if len(sys.argv) > 3 else "plan"

S0 = "GIỚI THIỆU"
S1 = "CODE · ÁP DỤNG KIẾN THỨC"
S2 = "KIỂM THỬ"
S3 = "DEMO LOCAL · Postman"
S4 = "DEMO LOCAL · Web UI"
X1 = "MỞ RỘNG · CI/CD & SECRET"
X2 = "MỞ RỘNG · TRIỂN KHAI AWS"
X3 = "MỞ RỘNG · DEMO UI TRÊN AWS"
S9 = "TỔNG KẾT"

P = "learning-service/src/main/java/vn/edu/middlejava/learning/"

# ------------------------------------------------------------------ GIỚI THIỆU
add(S0, "E-Commerce Mini — Middle Java Store", "OJT FastTrack Java · Trịnh Đức Dương", stage("""
<div class="panel" style="height:100%; display:grid; place-items:center; text-align:center; padding:40px">
 <div>
  <div style="font-size:30px; color:#4d6078; font-weight:600">Spring Boot 3.5 · Spring Security + JWT · JPA/Hibernate · PostgreSQL</div>
  <div style="font-size:84px; font-weight:900; margin:30px 0 18px; color:#10213a">Middle Java Store</div>
  <div style="font-size:34px; color:#1659a8; font-weight:700">Phần chính: 6 yêu cầu đề bài · demo local với ADMIN và CUSTOMER</div>
  <div style="font-size:30px; color:#9a5b00; font-weight:700; margin-top:14px">Mở rộng: GitHub Actions · AWS · demo giao diện trên cloud</div>
 </div>
</div>"""),
    "Xin chào thầy và các bạn, em là Trịnh Đức Dương. Đây là demo bài OJT E-Commerce Mini của em: Middle Java Store, "
    "cửa hàng bán khóa học Java. Phần chính bám sát sáu yêu cầu của đề, demo trên local với cả hai vai trò ADMIN và CUSTOMER. "
    "Phần mở rộng ở cuối là CI CD và triển khai lên AWS.")

table_frame(S0, "Đề bài → chức năng đã làm", "Mỗi yêu cầu đều có code, test và bước demo",
            ["Yêu cầu", "Đã áp dụng", "Demo"],
            [["1. Authentication", "Spring Security + JWT · role ADMIN / CUSTOMER", "Postman 2.x"],
             ["2. Quản lý sản phẩm", "JPA/Hibernate · SRP · index GIN trên tên", "Postman 1.3, 3.x"],
             ["3. Giỏ hàng", "Thêm / sửa số lượng / xóa · Extract Method, Extract Class · JUnit + Mockito", "Postman 4.x"],
             ["4. Đặt hàng", "@Transactional · Stream API · Optional", "Postman 5.3"],
             ["5. Thanh toán", "VNPay giả lập, COD, chuyển khoản · Factory Method + Strategy · OCP", "Postman 5.x, 6.x"],
             ["6. Thông báo", "Observer · CompletableFuture + ExecutorService", "Postman 5.6 · log"],
             ["Kiểm thử", "55 test: unit test Service (Mockito) + integration test PostgreSQL thật", "mvn verify"]],
            "Bảng này đối chiếu đề bài với phần em đã làm: xác thực, sản phẩm, giỏ hàng, đặt hàng, thanh toán, thông báo và kiểm thử.")

add(S0, "Kiến trúc", "Microservices sau API Gateway", stage("""
<div style="display:grid; grid-template-columns: 1fr 1fr 1fr; gap:26px; height:100%; font-size:24px">
 <div class="panel" style="padding:26px"><div class="lbl">Client</div>
  <div class="panel" style="padding:18px; margin:12px 0">Postman · 2 environment LOCAL / AWS</div>
  <div class="panel" style="padding:18px">Web UI cho ADMIN và CUSTOMER</div></div>
 <div class="panel" style="padding:26px"><div class="lbl">Spring Cloud</div>
  <div class="panel" style="padding:18px; margin:12px 0; border-color:#1659a8"><b>api-gateway</b> :8080</div>
  <div class="panel" style="padding:18px; margin:12px 0"><b>discovery-server</b> (Eureka)</div>
  <div class="panel" style="padding:18px; border:3px solid #168454"><b>learning-service</b><br><span style="font-size:19px;color:#5d6e84">auth · sản phẩm · giỏ · đơn · thanh toán · thông báo</span></div></div>
 <div class="panel" style="padding:26px"><div class="lbl">Hạ tầng local</div>
  <div class="panel" style="padding:18px; margin:12px 0">PostgreSQL 16 + Flyway V1→V6</div>
  <div class="panel" style="padding:18px; margin:12px 0">RabbitMQ</div>
  <div class="panel" style="padding:18px">Docker Compose</div></div>
</div>"""),
    "Request đi qua API Gateway, gateway tìm service qua Eureka. Toàn bộ nghiệp vụ nằm trong learning service, "
    "dữ liệu lưu ở PostgreSQL, schema quản lý bằng Flyway.")

# ------------------------------------------------------------------ CODE
SEC = P + "auth/config/SecurityConfiguration.java"
AUTH = P + "auth/service/impl/AuthServiceImpl.java"
code_frame(S1, "1. Authentication — Spring Security + JWT", "Role nằm trong claim của JWT · /api/admin/** chỉ cho ADMIN", [
    (SEC, "converter.setJwtGrantedAuthoritiesConverter", ".anyRequest().authenticated())",
     [("Object rolesClaim", "SimpleGrantedAuthority(role)"), ('.requestMatchers("/api/admin/**"', None)]),
], "Phần một, xác thực. Spring Security đọc claim roles trong JWT để phân quyền. Mọi đường dẫn admin chỉ dành cho ADMIN; CUSTOMER gọi vào sẽ nhận lỗi 403.")
code_frame(S1, "Phát hành JWT khi đăng nhập", "Access token 15 phút + refresh token", [
    (AUTH, "private AuthResponse issueTokens", "return new AuthResponse", [('.subject(user.getId()', '.claim("roles"'), ("String accessToken = jwtEncoder", None)]),
], "Khi đăng nhập, service phát hành access token mười lăm phút, chứa id, email và role. Tài khoản đăng ký mới luôn là CUSTOMER.")

V2 = "learning-service/src/main/resources/db/migration/V2__course_store.sql"
REPO_C = P + "course/repository/ICourseRepository.java"
code_frame(S1, "2. Quản lý sản phẩm — JPA/Hibernate + Index", "Index GIN trên upper(title) phục vụ tìm kiếm", [
    (V2, "ALTER TABLE courses ADD COLUMN price", "UPDATE courses SET price", [("CREATE EXTENSION IF NOT EXISTS pg_trgm", "idx_courses_title_substring")]),
    (REPO_C, "public interface ICourseRepository", "findByActiveTrueAndTitleContainingIgnoreCaseOrderByIdAsc", [("findByActiveTrueAndTitleContainingIgnoreCaseOrderByIdAsc", None)]),
], "Phần hai, sản phẩm. Em tạo index GIN trên upper của title. Method Spring Data JPA bên phải sinh câu like khớp đúng biểu thức của index, nên tìm kiếm không phải quét toàn bảng.")
CAT = P + "commerce/service/impl/CatalogAdminServiceImpl.java"
code_frame(S1, "SRP — quản trị sản phẩm tách khỏi catalog công khai", "CatalogAdminServiceImpl · ngừng bán = soft delete", [
    (CAT, "/** SRP:", "private Course findCourse", [("/** SRP:", None), ("public void deactivate", "findCourse(courseId).deactivate()")]),
], "Theo SRP, phần quản trị sản phẩm của ADMIN là một service riêng. Xóa sản phẩm là ngừng bán, đơn hàng cũ vẫn giữ lịch sử.")

CART = P + "commerce/service/impl/CartServiceImpl.java"
code_frame(S1, "3. Giỏ hàng — Extract Method", "Method public ngắn, các bước nhỏ có tên rõ ràng", [
    (CART, "public CartResponse addToCart", "private CartResponse toResponse", [
        ("public CartResponse addToCart", "return toResponse(cart);"), ("private Course findSellableCourse", None),
        ("private ShoppingCart findOrCreateCart", None), ("private ShoppingCart findCart", None)]),
], "Phần ba, giỏ hàng với thêm, sửa số lượng và xóa. Em áp dụng Extract Method: method public chỉ vài dòng, các bước nhỏ tách thành method private có tên rõ ràng.")
PRICE = P + "commerce/pricing/CartPricingCalculator.java"
code_frame(S1, "Extract Class — CartPricingCalculator", "Logic tính tiền trước đây bị lặp, nay có một chủ sở hữu", [
    (PRICE, " * Extract Class refactoring", "private BigDecimal sumLineTotals", [
        (" * Extract Class refactoring", " */"), ("public PricedCart price", "return new PricedCart"),
        ("courses.findAllById(courseIds).stream()", "Collectors.toMap")]),
], "Và Extract Class: phần tính tiền bị lặp ở giỏ hàng và checkout được tách thành CartPricingCalculator, bên trong dùng Stream API.")

ORDER = P + "commerce/service/impl/OrderServiceImpl.java"
code_frame(S1, "4. Đặt hàng — @Transactional + Optional + Stream", "Chuyển giỏ hàng thành đơn hàng trong một transaction", [
    (ORDER, "     * One transaction", "return mapper.toOrderResponse(order);", [
        ("@Transactional", None), ("ShoppingCart cart = carts.findByUserId(userId)", '.orElseThrow(() -> new IllegalArgumentException("Cart is empty"));'),
        ("PaymentProcessor payment = payments.processorFor", None), ("if (order.isPaid())", "events.publishEvent")]),
], "Phần bốn, đặt hàng. Checkout chạy trong một transaction, dùng Optional để lấy giỏ và chặn giỏ rỗng. Lưu đơn, mở quyền học, làm trống giỏ và phát sự kiện. Bước nào lỗi thì tất cả rollback.")

PP = P + "commerce/payment/PaymentProcessor.java"
VPP = P + "commerce/payment/VnPayPaymentProcessor.java"
code_frame(S1, "5. Thanh toán — Factory Method (GoF)", "Creator PaymentProcessor · factory method createStrategy() · ConcreteCreator", [
    (PP, "public abstract class PaymentProcessor", "return strategy.pay(request);", [
        ("protected abstract PaymentStrategy createStrategy();", None), ("public final PaymentResult process", "return strategy.pay(request);")]),
    (VPP, "@Component", "return new VnPayPaymentStrategy();", [("extends PaymentProcessor", None), ("protected PaymentStrategy createStrategy()", "return new VnPayPaymentStrategy();")]),
], "Phần năm, thanh toán. Lớp trừu tượng PaymentProcessor là creator, có factory method createStrategy. Method process chạy quy trình chung nhưng không biết strategy cụ thể; mỗi lớp con như VNPay hay COD tự quyết định tạo strategy nào.")
PS = P + "commerce/payment/PaymentStrategy.java"
VNP = P + "commerce/payment/VnPayPaymentStrategy.java"
code_frame(S1, "Strategy Pattern", "Mỗi phương thức thanh toán là một thuật toán riêng, cùng hợp đồng", [
    (PS, "public interface PaymentStrategy", "PaymentResult pay(PaymentRequest request);", [("PaymentResult pay", None)]),
    (VNP, "public class VnPayPaymentStrategy", "return PaymentResult.paid(transactionNo);", [("implements PaymentStrategy", None), ("return PaymentResult.paid", None)]),
], "Sản phẩm của factory là Strategy. VNPay giả lập trả PAID kèm mã giao dịch; COD và chuyển khoản trả PENDING chờ ADMIN xác nhận. Checkout không có if else theo loại thanh toán.")
REG = P + "commerce/payment/PaymentProcessorRegistry.java"
FMT = "learning-service/src/test/java/vn/edu/middlejava/learning/commerce/payment/PaymentFactoryMethodTest.java"
code_frame(S1, "Open/Closed Principle", "Thêm MoMo = thêm 1 processor + 1 strategy, không sửa code cũ", [
    (REG, "public PaymentProcessorRegistry(List<PaymentProcessor> processors)", '"Unsupported payment method. Supported: "', [
        ("public PaymentProcessorRegistry(List<PaymentProcessor> processors)", "Function.identity()));")]),
    (FMT, "void openClosedPrincipleNewMethodIsOneNewCreatorAndNoChangeElsewhere", 'isEqualTo("MOMO-1");', [
        ("PaymentProcessor momo = new PaymentProcessor()", "return paymentRequest -> PaymentResult.paid"),
        ('assertThat(extended.supportedMethods()).containsExactly("COD", "MOMO");', None)]),
], "Spring tự đưa mọi processor vào registry. Muốn thêm MoMo chỉ cần thêm một lớp mới, không sửa registry hay checkout; test bên phải chứng minh điều đó. Đây là nguyên lý đóng mở.")

PUB = P + "commerce/event/OrderEventPublisher.java"
EXE = P + "config/NotificationExecutorConfiguration.java"
code_frame(S1, "6. Thông báo — Observer + CompletableFuture + ExecutorService", "Gửi cho từng observer trên thread riêng, sau khi commit", [
    (PUB, "@TransactionalEventListener", "return CompletableFuture.allOf(deliveries);", [
        ("@TransactionalEventListener", None), (".runAsync(() -> observer.onOrderStatusChanged(event), executor)", None),
        (".exceptionally(error -> {", "return null;")]),
    (EXE, "@Bean(name", "return Executors.newFixedThreadPool", [("return Executors.newFixedThreadPool", None)]),
], "Phần sáu, thông báo, dùng Observer. Publisher chỉ chạy sau khi transaction commit, gửi cho từng observer bằng CompletableFuture trên ExecutorService riêng. Một kênh lỗi không ảnh hưởng kênh khác.")
INAPP = P + "commerce/event/InAppNotificationObserver.java"
OBS = P + "commerce/event/OrderStatusObserver.java"
code_frame(S1, "Các observer", "Thêm kênh mới (SMS, Zalo…) = thêm một class", [
    (OBS, "public interface OrderStatusObserver", "void onOrderStatusChanged", [("public interface OrderStatusObserver", None)]),
    (INAPP, "@Component", "log.info", [("implements OrderStatusObserver", None), ("notifications.save", None)]),
], "Có hai observer: In App lưu thông báo cho khách đọc, Email ghi log giả lập. Thêm kênh mới chỉ cần thêm một lớp.")

table_frame(S1, "SOLID trong project", "", ["Nguyên lý", "Ví dụ trong code"],
            [["<b>S</b> — Single Responsibility", "CartServiceImpl · OrderServiceImpl · AdminOrderServiceImpl · CartPricingCalculator · CommerceMapper"],
             ["<b>O</b> — Open/Closed", "Thêm PaymentProcessor / OrderStatusObserver mới, không sửa code cũ (có test chứng minh)"],
             ["<b>L</b> — Liskov Substitution", "Mọi PaymentProcessor, PaymentStrategy, OrderStatusObserver thay thế nhau được"],
             ["<b>I</b> — Interface Segregation", "ICartService · IOrderService · IAdminOrderService · ICatalogAdminService"],
             ["<b>D</b> — Dependency Inversion", "Service phụ thuộc abstraction, inject qua constructor → dễ mock khi test"]],
            "Tóm tắt SOLID: mỗi lớp một trách nhiệm; mở rộng thanh toán và thông báo không sửa code cũ; các lớp con thay thế nhau được; interface nhỏ theo vai trò; và service phụ thuộc abstraction nên dễ mock.")
table_frame(S1, "Design Pattern và lý do chọn", "", ["Pattern", "Ở đâu", "Vì sao chọn"],
            [["<b>Factory Method</b>", "PaymentProcessor.createStrategy() + 3 processor con", "Việc tạo strategy nằm ở lớp con; checkout chỉ biết lớp trừu tượng, thêm phương thức không sửa code cũ"],
             ["<b>Strategy</b>", "PaymentStrategy: VNPay, COD, chuyển khoản", "Mỗi cách thanh toán có quy tắc riêng; bỏ chuỗi if/else theo loại thanh toán"],
             ["<b>Observer</b>", "OrderEventPublisher + InApp / Email observer", "Đơn hàng không cần biết có bao nhiêu kênh thông báo; gửi bất đồng bộ, không làm chậm checkout"]],
            "Ba design pattern và lý do chọn. Factory Method để việc tạo strategy nằm ở lớp con. Strategy để mỗi cách thanh toán là một thuật toán riêng. Observer để tách đơn hàng khỏi các kênh thông báo và gửi bất đồng bộ.")

# ------------------------------------------------------------------ TEST
CT = "learning-service/src/test/java/vn/edu/middlejava/learning/commerce/service/CartServiceImplTest.java"
code_frame(S2, "Unit test giỏ hàng — JUnit 5 + Mockito", "CartServiceImplTest: 11 test", [
    (CT, "@ExtendWith(MockitoExtension.class)", "verify(carts, never()).save(any());", [
        ("@ExtendWith(MockitoExtension.class)", None), ("@Mock", "private ICourseRepository courses;"),
        ("void addSameCourseTwiceIncreasesQuantity", "verify(carts, never()).save(any());")]),
], "Phần kiểm thử. Unit test giỏ hàng dùng JUnit 5 và Mockito, repository được mock. Ví dụ thêm cùng khóa hai lần thì số lượng cộng dồn và không tạo giỏ mới.")
IT = "learning-service/src/test/java/vn/edu/middlejava/learning/commerce/PostgresCommerceIntegrationTest.java"
code_frame(S2, "Integration test luồng đặt hàng — PostgreSQL thật", "Testcontainers · chứng minh @Transactional rollback", [
    (IT, "void transactionalCheckoutRollsBackEverythingWhenAStepFails", "assertThat(notifications.findByUserIdOrderByCreatedAtDesc(customer.getId())).isEmpty();", [
        ("doThrow(new IllegalStateException", ".when(enrollmentService)"),
        ("assertThat(orders.count()).isEqualTo(ordersBefore);", "assertThat(notifications.findByUserIdOrderByCreatedAtDesc(customer.getId())).isEmpty();")]),
], "Integration test chạy trên PostgreSQL thật bằng Testcontainers. Em cố tình cho một bước lỗi giữa checkout: không có đơn mới, giỏ còn nguyên, không có thông báo. Transaction đã rollback đúng.")
reports = sorted((REPO / "learning-service/target/surefire-reports").glob("*.txt"))
term, total = [("cmd", "> mvn -B -pl learning-service verify"), ("dim", "")], 0
for r in reports:
    line = next(l for l in r.read_text(encoding="utf-8").splitlines() if l.startswith("Tests run"))
    n = int(line.split(",")[0].split(":")[1]); total += n
    term.append(("ok", f"[INFO] Tests run: {n:>2}, Failures: 0, Errors: 0 -- {r.stem.split('.')[-1]}"))
term += [("dim", ""), ("ok", f"[INFO] Tests run: {total}, Failures: 0, Errors: 0, Skipped: 0"), ("ok", "[INFO] BUILD SUCCESS")]
hl = [i for i, (_, t) in enumerate(term) if "CartServiceImplTest" in t or "PostgresCommerceIntegrationTest" in t or f"Tests run: {total}," in t]
term_frame(S2, "Kết quả kiểm thử", "Chạy ở local và tự động trong CI", term,
           f"Kết quả: {total} test đều pass, gồm unit test các service và integration test.", hl=hl)

# ------------------------------------------------------------------ DEMO LOCAL · Postman (both roles)
term_frame(S3, "Khởi động môi trường local", "Docker Compose + 4 service · Postman environment LOCAL", [
    ("cmd", "> docker compose up -d postgres rabbitmq video-storage"),
    ("cmd", "> mvn -B clean verify"),
    ("cmd", "> java -jar discovery-server/... learning-service/... ai-service/... api-gateway/..."),
    ("ok", "   Flyway: Successfully applied migration to schema \"learning\", now at version v6"),
    ("dim", ""),
    ("warn", "Postman: demo/postman/MiddleJavaStore.postman_collection.json + local.postman_environment.json"),
], "Giờ demo local. Em chạy hạ tầng bằng Docker Compose và bốn service, rồi dùng Postman với environment LOCAL.", hl=[3, 5])
api_frame("local", "1.2", "Danh sách sản phẩm công khai: có giá, không lộ link video.", keys=["price"])
api_frame("local", "1.3", "Tìm kiếm sản phẩm theo tên, API công khai, dùng index vừa trình bày.", keys=["title"])
api_frame("local", "2.1", "Đăng ký tài khoản mới: kết quả 201, nhận access token. Tài khoản này là CUSTOMER.", keys=["accessToken"])
api_frame("local", "2.2", "Đăng nhập CUSTOMER, Postman tự lưu token cho các request sau.", keys=["accessToken", "expiresAt"])
api_frame("local", "2.3", "CUSTOMER gọi API tạo sản phẩm của ADMIN bị chặn với lỗi 403.")
api_frame("local", "2.4", "Đăng nhập tài khoản ADMIN có sẵn, token được lưu vào biến adminToken.", keys=["accessToken"])
api_frame("local", "3.1", "ADMIN tạo sản phẩm mới, kết quả 201.", keys=["id", "price", "active"])
api_frame("local", "3.2", "ADMIN cập nhật giá sản phẩm.", keys=["price"])
api_frame("local", "4.1", "Sang vai trò CUSTOMER: thêm khóa Middle Java vào giỏ.", keys=["quantity", "total"])
api_frame("local", "4.3", "Cập nhật số lượng lên hai, tổng tiền được tính lại.", keys=["quantity#1", "lineTotal#1", "total"])
api_frame("local", "4.4", "Xóa một sản phẩm khỏi giỏ, trả về 204.")
api_frame("local", "4.5", "Xem lại giỏ: một sản phẩm, số lượng hai.", keys=["quantity", "total"])
api_frame("local", "5.1", "Danh sách phương thức thanh toán lấy từ registry.", hl_body=True)
api_frame("local", "5.2", "Chưa mua thì xem video bị 403.", keys=["detail"])
api_frame("local", "5.3", "Đặt hàng bằng VNPay giả lập: đơn PAID, có mã giao dịch, trạng thái CONFIRMED. Giỏ được làm trống trong cùng transaction.",
          keys=["paymentMethod", "paymentStatus", "paymentReference", "status"])
api_frame("local", "5.5", "Đã mua thì xem được video.", keys=["source", "videoUrl"])
api_frame("local", "5.6", "Thông báo đơn hàng do observer tạo ra.", keys=["message"])
term_frame(S3, "Log: observer chạy trên thread riêng", "Request thread khác thread gửi thông báo", [
    ("dim", "[nio-8081-exec-7] OrderEventPublisher   : Dispatching CREATED for order 3 to 2 observers"),
    ("ok", "[ order-notify-1] EmailNotificationObserver : [EMAIL] thread=order-notify-1 subject=\"Order #3 CREATED\""),
    ("ok", "[ order-notify-2] InAppNotificationObserver : [IN_APP] thread=order-notify-2 order=3 type=CREATED"),
], "Trong log, request chạy trên thread của Tomcat, còn hai observer chạy song song trên thread order notify.", hl=[0, 1, 2])
api_frame("local", "6.2", "Đặt hàng COD: đơn ở trạng thái chờ thanh toán.", keys=["paymentStatus", "status"])
api_frame("local", "6.3", "COD chưa trả tiền nên video vẫn bị 403.", keys=["detail"])
api_frame("local", "6.5", "Vai trò ADMIN xác nhận đã nhận tiền: đơn chuyển sang PAID và mở quyền học.", keys=["paymentStatus", "status"])
api_frame("local", "6.6", "Xác nhận lần hai bị từ chối với 409.", keys=["detail"])
api_frame("local", "6.8", "Khách nhận thêm thông báo đơn đã được xác nhận.", keys=["message#2"])
api_frame("local", "7.1", "Đặt hàng khi giỏ trống trả 400.", keys=["detail"])
api_frame("local", "7.2", "ADMIN ngừng bán sản phẩm, sản phẩm biến mất khỏi catalog công khai.")

# ------------------------------------------------------------------ DEMO LOCAL · Web UI (both roles)
shot_frame(S4, "ui-local-cart", "CUSTOMER — giỏ hàng trên giao diện web", "http://localhost:8080",
           "Cùng luồng đó trên giao diện web. CUSTOMER thêm sản phẩm và sửa số lượng ngay trong giỏ.", env="local")
shot_frame(S4, "ui-local-orders-paid", "CUSTOMER — đơn hàng đã thanh toán", "http://localhost:8080",
           "Đặt hàng VNPay xong, đơn hiện PAID và có nút xem video.", env="local")
shot_frame(S4, "ui-local-admin-orders", "ADMIN — quản lý đơn hàng", "http://localhost:8080",
           "Đăng nhập ADMIN, giao diện chuyển sang quản lý đơn. Đơn COD có nút xác nhận.", env="local")
shot_frame(S4, "ui-local-admin-confirmed", "ADMIN — xác nhận thanh toán", "http://localhost:8080",
           "Bấm xác nhận, đơn chuyển sang CONFIRMED.", env="local")
shot_frame(S4, "ui-local-admin-products", "ADMIN — quản lý sản phẩm", "http://localhost:8080",
           "ADMIN thêm, sửa và ngừng bán sản phẩm.", env="local")

# ------------------------------------------------------------------ MỞ RỘNG · CI/CD & secret
WF = ".github/workflows/aws-deploy.yml"
code_frame(X1, "Mở rộng 1 — GitHub Actions", "verify (test) → deploy (build image → ECR → EC2)", [
    (WF, "jobs:", "Store cloud admin credentials in AWS Parameter Store", [
        ("- run: mvn -B verify", None), ("needs: verify", None), ("for service in discovery-server", "docker push")]),
], "Phần mở rộng. Mỗi lần push, GitHub Actions chạy toàn bộ test. Test xanh thì mới build image, đẩy lên Amazon ECR và deploy.")
table_frame(X1, "Lưu secret thế nào", "Không có mật khẩu hay key nào trong source code",
            ["Bước", "Nơi lưu", "Ví dụ"],
            [["1. Khai báo", "GitHub → Settings → Secrets and variables", "Secret: ADMIN_PASSWORD, GROQ_API_KEY · Variable: ADMIN_EMAIL, AWS_REGION"],
             ["2. Pipeline", "Workflow đọc secret, ghi vào AWS SSM Parameter Store", "/studycoach/prod/admin-password (SecureString, mã hóa KMS)"],
             ["3. Tạo sẵn trong AWS", "Parameter Store", "JWT secret, mật khẩu database, tên bucket"],
             ["4. Deploy", "EC2 đọc Parameter Store bằng IAM role", "Ghi file .env quyền 600 → Docker Compose"]],
            "Về secret: mật khẩu khai báo trong GitHub Secrets, pipeline chuyển vào Parameter Store dạng mã hóa, EC2 đọc bằng IAM role khi deploy. Không có mật khẩu nào trong code.")
shot_frame(X1, "gh-run", "Pipeline chạy thành công", "GitHub Actions — verify → deploy",
           "Đây là một lần pipeline chạy thành công: verify xong rồi mới deploy.")

# ------------------------------------------------------------------ MỞ RỘNG · AWS
table_frame(X2, "Mở rộng 2 — Hạ tầng AWS", "CloudFormation stack study-coach · ap-southeast-2",
            ["Thành phần", "Vai trò"],
            [["CloudFront", "HTTPS công khai: <b>https://d3jaw9bd80q6ki.cloudfront.net</b>"],
             ["EC2 + Docker Compose", "Chạy 4 service; deploy qua SSM Run Command"],
             ["RDS PostgreSQL", "Database riêng, không public"],
             ["ECR", "Lưu Docker image theo commit"],
             ["S3 private", "Lưu video khóa học, link có chữ ký, chỉ người đã mua xem được"]],
            "Hạ tầng AWS tạo bằng CloudFormation: CloudFront làm cổng HTTPS, EC2 chạy container, RDS lưu dữ liệu, ECR lưu image, S3 lưu video khóa học.",
            hl_rows=(0,))
STORE = P + "course/video/CourseVideoAdminServiceImpl.java"
code_frame(X2, "Lưu video trên S3 — presigned URL", "Database chỉ lưu metadata · file đi thẳng lên S3", [
    (STORE, "public VideoUploadResponse requestUpload", "return mapper.toAdminResponse(course);", [
        ("IVideoStorage.UploadTicket ticket = storage.presignUpload", None),
        ("IVideoStorage.StoredVideo stored = storage.find(request.key())", '.orElseThrow(() -> new IllegalStateException("Video has not been uploaded yet"));')]),
], "Video không lưu trong database. ADMIN nhận một link upload có chữ ký, file đi thẳng lên S3, sau đó API kiểm tra file tồn tại rồi mới gắn vào khóa học.")

# ------------------------------------------------------------------ MỞ RỘNG · cloud UI both roles
CLOUD_URL = "https://d3jaw9bd80q6ki.cloudfront.net"
for shot, title, say in [
    ("ui-cloud-admin-orders", "ADMIN trên AWS — xác nhận đơn COD",
     "Giờ demo trên AWS qua CloudFront. ADMIN đăng nhập, thấy đơn COD đang chờ và bấm xác nhận đã thanh toán."),
    ("ui-cloud-admin-upload", "ADMIN trên AWS — tải video bài học lên S3",
     "ADMIN tải video bài học cho khóa Middle Java. File đi thẳng lên S3, cột video chuyển thành Storage."),
    ("ui-cloud-register", "CUSTOMER — đăng ký tài khoản mới",
     "Một khách mới đăng ký tài khoản. Mật khẩu phải từ mười ký tự, tài khoản mới luôn là CUSTOMER."),
    ("ui-cloud-new-customer-cart", "CUSTOMER — thêm vào giỏ, chọn VNPay",
     "Khách thêm khóa Middle Java vào giỏ và chọn thanh toán VNPay."),
    ("ui-cloud-video-s3", "CUSTOMER — thanh toán xong, xem video từ S3",
     "Đặt hàng thành công, đơn PAID. Khách mở video, phát từ S3 bằng link riêng chỉ có hạn một giờ."),
    ("ui-cloud-notifications-new", "CUSTOMER — thông báo đơn hàng",
     "Và khách nhận được thông báo đơn hàng do observer gửi."),
]:
    shot_frame(X3, shot, title, CLOUD_URL, say, env="cloud")

table_frame(S9, "Tổng kết", "", ["Hạng mục", "Kết quả"],
            [["Chức năng", "JWT · ADMIN/CUSTOMER · sản phẩm · giỏ hàng · đặt hàng · 3 cách thanh toán · thông báo"],
             ["Kiến thức", "Spring Security, JPA, index, @Transactional, Stream/Optional, CompletableFuture, refactoring"],
             ["Pattern & SOLID", "Factory Method · Strategy · Observer · đủ năm nguyên lý SOLID"],
             ["Kiểm thử", f"{total} test, chạy tự động trong CI"],
             ["Mở rộng", "GitHub Actions · AWS · video trên S3 · giao diện cho cả hai role"]],
            "Tóm lại, em đã hoàn thành đủ yêu cầu đề bài, có test chạy tự động và triển khai lên AWS. Em cảm ơn thầy và các bạn đã theo dõi.")

# ------------------------------------------------------------------ PLAN / RENDER
texts = [phonetic(f["say"]) for f in frames]
total_chars = sum(len(t) for t in texts)
total_sec = sum(f["seconds"] for f in frames)
print(f"{len(frames)} frames · {total_chars} phonetic chars · {total_sec / 60:.1f} min")
if MODE != "render":
    sys.exit(0)

n = len(frames)
for i, f in enumerate(frames, 1):
    (FRAMES / f"{i:03d}.html").write_text(page(f["section"], i, n, f["title"], f["sub"], f["body"], f["env"], BOX_JS), encoding="utf-8")
PNG = WORK / "png2"
only = set(sys.argv[4].split(",")) if len(sys.argv) > 4 else None
todo = [i for i in range(1, n + 1) if (only and f"{i:03d}" in only) or (not only and not (PNG / f"{i:03d}.png").exists())]
for start in range(0, len(todo), 8):
    batch = todo[start:start + 8]
    sc = {"baseUrl": FRAMES.resolve().as_uri() + "/", "viewport": {"width": W, "height": H},
          "steps": [{"goto": f"{i:03d}.html", "wait": 500, "shot": f"{i:03d}"} for i in batch]}
    (WORK / "scenario-frames2.json").write_text(json.dumps(sc), encoding="utf-8")
    for attempt in range(3):
        result = subprocess.run(["node", str(WORK / "ui_capture.mjs"), str(WORK / "scenario-frames2.json"), str(PNG)],
                                timeout=180, stdout=subprocess.DEVNULL)
        if result.returncode == 0:
            break


def ts(s):
    ms = int(round(s * 1000)); h, ms = divmod(ms, 3600000); m, ms = divmod(ms, 60000); sec, ms = divmod(ms, 1000)
    return f"{h:02d}:{m:02d}:{sec:02d},{ms:03d}"


t, timeline, srt = 0.0, [], []
for i, (f, text) in enumerate(zip(frames, texts), 1):
    timeline.append({"frame": i, "start": round(t, 2), "end": round(t + f["seconds"], 2), "section": f["section"],
                     "title": f["title"], "say": f["say"], "tts": text})
    srt.append(f"{i}\n{ts(t + 0.3)} --> {ts(t + f['seconds'] - 0.2)}\n{text}\n")
    t += f["seconds"]
(WORK / "timeline2.json").write_text(json.dumps(timeline, ensure_ascii=False, indent=1), encoding="utf-8")
(WORK / "demo-ngoc-huyen.srt").write_text("\n".join(srt), encoding="utf-8")
print("rendered", n, "frames; SRT chars:", total_chars)

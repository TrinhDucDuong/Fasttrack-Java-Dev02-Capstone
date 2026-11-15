# Kịch bản demo OJT — Middle Java Store (E-Commerce Mini)

Hệ thống bán các khóa Java quay sẵn: sản phẩm là khóa học, video nằm trên YouTube. Khách đăng ký, tìm khóa học, thêm vào giỏ, đặt hàng và thanh toán (VNPay giả lập, COD, chuyển khoản). Khi đã thanh toán, khách được mở quyền xem video. ADMIN quản lý sản phẩm và xác nhận thanh toán cho đơn COD hoặc chuyển khoản. Mỗi lần trạng thái đơn đổi, hệ thống gửi thông báo.

| Tài liệu | Nội dung |
|---|---|
| `demo/BAO_CAO_OJT.md` | Báo cáo: kiến thức nào ở chức năng nào, SOLID, Design Pattern và lý do chọn |
| `demo/postman/` | Postman collection (33 request, có test script tự lưu token và id) + environment LOCAL và AWS |
| `demo/run_demo.py` | Chạy toàn bộ kịch bản bằng một lệnh, dùng để tập dượt hoặc kiểm tra trước khi quay |
| `demo/SCRIPT_NOI.md` | Script nói có mốc thời gian, khớp với video `demo/video/MiddleJavaStore_Demo.mp4` |
| `api-gateway/src/main/resources/static/index.html` | Giao diện web tối giản cho CUSTOMER và ADMIN |

## 1. Chuẩn bị môi trường local

Yêu cầu: Java 17, Maven, Docker Desktop đang chạy.

```powershell
docker compose up -d postgres rabbitmq
mvn -B clean verify
```

`mvn verify` chạy toàn bộ test (unit test Mockito + integration test PostgreSQL/RabbitMQ bằng Testcontainers) và build JAR. Sau đó mở 4 cửa sổ PowerShell và chạy theo thứ tự:

```powershell
java -jar discovery-server/target/discovery-server-1.0.0.jar
```

```powershell
$env:ADMIN_EMAIL = 'admin@middlejava.local'
$env:ADMIN_PASSWORD = 'Admin123!Local'
java -jar learning-service/target/learning-service-1.0.0.jar
```

```powershell
java -jar ai-service/target/ai-service-1.0.0.jar
```

```powershell
java -jar api-gateway/target/api-gateway-1.0.0.jar
```

- Gateway: `http://localhost:8080`. Health check: `/actuator/health`. Mở `http://localhost:8080/` để xem giao diện web.
- Tài khoản ADMIN local `admin@middlejava.local` / `Admin123!Local` chỉ dùng cho máy local.
- Tập dượt nhanh trước khi quay: `python demo/run_demo.py --env local`. Lệnh in ra 32 bước, bước nào cũng phải `OK`.

## 2. Demo local bằng Postman

Import `demo/postman/MiddleJavaStore.postman_collection.json` và `demo/postman/local.postman_environment.json`, chọn environment **Middle Java Store - LOCAL**. Mỗi lần quay nên đổi biến `customerEmail` sang một email mới. Bấm **Send** lần lượt từ trên xuống; test script tự lưu `accessToken`, `adminToken`, `courseId`, `newCourseId`, `codOrderId`.

| # | Request | Kết quả cần chỉ ra | Kiến thức / pattern |
|---|---|---|---|
| 1.1 | `GET /actuator/health` | `status: UP` | API Gateway + Eureka |
| 1.2 | `GET /api/courses` | Có giá, **không** có link video | DTO tách khỏi entity |
| 1.3 | `GET /api/courses?search=java` | Lọc theo tên | JPA query + index GIN trigram (V2) |
| 2.1–2.2 | Đăng ký, đăng nhập CUSTOMER | 201 / 200, có `accessToken` | Spring Security + JWT |
| 2.3 | CUSTOMER gọi `POST /api/admin/courses` | **403** | Phân quyền ADMIN/CUSTOMER |
| 2.4 | Đăng nhập ADMIN | `adminToken` | |
| 3.1–3.3 | ADMIN tạo, sửa giá, xem danh sách sản phẩm | 201 / 200, admin thấy `youtubeUrl`, `active` | SRP: `CatalogAdminServiceImpl` |
| 4.1–4.5 | Thêm 2 khóa, sửa số lượng = 2, xóa 1 khóa, xem giỏ | `total` được tính lại, xóa trả 204 | Extract Method / Extract Class |
| 5.1 | `GET /api/orders/payment-methods` | `BANK_TRANSFER, COD, VNPAY` | Factory tự nhận strategy (OCP) |
| 5.2 | Xem video trước khi mua | **403** | |
| 5.3 | `POST /api/orders` `{"paymentMethod":"VNPAY"}` | `PAID`, `paymentReference: VNP…`, `CONFIRMED` | `@Transactional`, Strategy + Factory Method |
| 5.4–5.5 | Giỏ đã trống, video trả `youtubeEmbedUrl` | | |
| 5.6 | `GET /api/notifications` | Thông báo "Order #… has been created" | Observer + CompletableFuture |
| 6.1–6.3 | Thêm Java Fundamentals, đặt hàng COD, xem video | `PENDING / AWAITING_PAYMENT`, video **403** | Strategy khác, cùng hợp đồng |
| 6.4–6.5 | ADMIN xem tất cả đơn, xác nhận thanh toán | `PAID / CONFIRMED` | `AdminOrderServiceImpl` |
| 6.6 | Xác nhận lần 2 | **409 Conflict** | |
| 6.7–6.9 | Video 200, thông báo "Payment … confirmed", lịch sử đơn | | Observer gửi khi đổi trạng thái |
| 7.1 | Đặt hàng khi giỏ trống | **400** `Cart is empty` | `Optional.filter().orElseThrow()` |
| 7.2–7.3 | ADMIN ngừng bán sản phẩm, catalog không còn sản phẩm đó | 204 | Soft delete (V5) |

Muốn chứng minh observer chạy bất đồng bộ, mở log của `learning-service` sau bước 5.3. Request đặt hàng chạy trên thread `nio-8081-exec-*`, còn `[EMAIL]` và `[IN_APP]` chạy trên `order-notify-1/2`.

## 3. Demo trên AWS

- URL công khai: **https://d3jaw9bd80q6ki.cloudfront.net** (CloudFront → EC2 chạy Docker Compose, RDS PostgreSQL, image trên ECR).
- CI/CD: mỗi lần push lên `main`, GitHub Actions chạy `mvn verify`. Test xanh thì build 4 image, đẩy lên ECR và deploy lên EC2 qua SSM.
- Tài khoản ADMIN trên cloud: email là repository variable `ADMIN_EMAIL`, mật khẩu là secret `ADMIN_PASSWORD`. Pipeline chép cả hai vào AWS Parameter Store. Không đưa mật khẩu lên màn hình hoặc vào file export của Postman.

**Bằng giao diện web** (mở URL trên ở bất kỳ trình duyệt nào):

1. Đăng ký một tài khoản CUSTOMER, tìm "java", thêm 2 khóa vào giỏ, đổi số lượng, xóa 1 khóa.
2. Chọn **VNPAY** rồi **Đặt hàng**. Sang tab **Đơn hàng**: đơn `PAID`, có mã `VNP…`. Bấm **Xem video**.
3. Tab **Thông báo** có thông báo của đơn vừa đặt.
4. Đặt thêm một đơn **COD**, đơn ở trạng thái chờ thanh toán.
5. Đăng xuất, đăng nhập ADMIN. Tab **Quản lý đơn hàng**: bấm **Xác nhận đã thanh toán** cho đơn COD. Tab **Quản lý sản phẩm**: thêm, sửa, ngừng bán.
6. Đăng nhập lại CUSTOMER: đơn COD đã `PAID`, xem được video, có thêm thông báo xác nhận.

**Bằng Postman:** chọn environment **Middle Java Store - AWS** (`demo/postman/cloud.postman_environment.json`), điền `adminPassword` ngay trong Postman (đừng lưu vào file), rồi chạy cùng collection như ở local.

Chạy kiểm tra cloud bằng script (phần CUSTOMER chạy được khi chưa đặt mật khẩu admin):

```powershell
python demo/run_demo.py --env cloud --base-url https://d3jaw9bd80q6ki.cloudfront.net
$env:ADMIN_EMAIL = '<email admin>'; $env:ADMIN_PASSWORD = '<mật khẩu admin>'
python demo/run_demo.py --env cloud --base-url https://d3jaw9bd80q6ki.cloudfront.net
```

## 4. Đường dẫn code để thuyết trình

| Kiến thức | File |
|---|---|
| Spring Security + JWT, ADMIN/CUSTOMER | `auth/config/SecurityConfiguration.java`, `auth/service/impl/AuthServiceImpl.java` |
| JPA/Hibernate, index tìm kiếm | `course/repository/ICourseRepository.java`, `db/migration/V2__course_store.sql` |
| SRP | `commerce/service/impl/CartServiceImpl.java`, `OrderServiceImpl.java`, `AdminOrderServiceImpl.java`, `CatalogAdminServiceImpl.java` |
| Extract Method / Extract Class | `CartServiceImpl` (private helpers), `commerce/pricing/CartPricingCalculator.java`, `commerce/mapper/CommerceMapper.java` |
| `@Transactional`, Stream, Optional | `OrderServiceImpl.checkout`, `CartPricingCalculator`, `PaymentStrategyFactory.create` |
| Strategy + Factory Method + OCP | `commerce/payment/*` |
| Observer + CompletableFuture + ExecutorService | `commerce/event/*`, `config/NotificationExecutorConfiguration.java` |
| Unit test (JUnit 5 + Mockito) | `src/test/.../commerce/service/CartServiceImplTest.java`, `OrderServiceImplTest.java`, `AdminOrderServiceImplTest.java`, `payment/PaymentStrategyFactoryTest.java`, `event/OrderEventPublisherTest.java` |
| Integration test luồng đặt hàng | `src/test/.../commerce/PostgresCommerceIntegrationTest.java` (Testcontainers), `LearningServiceContextTest.java` (MockMvc) |
| Migration mới | `db/migration/V5__payments_catalog_status.sql` |

(các đường dẫn tính từ `learning-service/src/main/java/vn/edu/middlejava/learning/` hoặc `learning-service/src/test/java/vn/edu/middlejava/learning/`)

## Phụ lục — Chatbot trợ lý RAG (ngoài phạm vi đề bài)

`POST /api/materials` tạo tài liệu cho một unit (`courseId`, `unitId`, `title`, `content`). RabbitMQ chuyển yêu cầu indexing cho worker; worker tách chunk và lưu embedding vào pgvector. Gọi `GET /api/materials/{id}` đến khi trạng thái là `READY`, rồi `POST /api/ai/ask` với `unitId` và `question`. Response có câu trả lời và danh sách `sources`. API key Groq chỉ nằm trong biến môi trường hoặc Parameter Store, không đưa lên màn hình demo.

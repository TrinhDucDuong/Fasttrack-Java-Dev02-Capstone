# Kịch bản demo OJT — Middle Java Store (E-Commerce Mini)

Hệ thống bán các khóa Java quay sẵn: sản phẩm là khóa học, video nằm trên YouTube. Khách đăng ký, tìm khóa học, thêm vào giỏ, đặt hàng và thanh toán (VNPay giả lập, COD, chuyển khoản). Khi đã thanh toán, khách được mở quyền xem video. ADMIN quản lý sản phẩm và xác nhận thanh toán cho đơn COD hoặc chuyển khoản. Mỗi lần trạng thái đơn đổi, hệ thống gửi thông báo.

| Tài liệu | Nội dung |
|---|---|
| `demo/BAO_CAO_OJT.md` | Báo cáo: kiến thức nào ở chức năng nào, SOLID, Design Pattern và lý do chọn |
| `demo/postman/` | Postman collection (38 request, có test script tự lưu token và id) + environment LOCAL và AWS |
| `demo/run_demo.py` | Chạy toàn bộ kịch bản bằng một lệnh, dùng để tập dượt hoặc kiểm tra trước khi quay |
| `demo/SCRIPT_NOI.md` | Kịch bản thuyết trình theo từng phần của video demo |
| `api-gateway/src/main/resources/static/index.html` | Giao diện web tối giản cho CUSTOMER và ADMIN |

## 1. Chuẩn bị môi trường local

Yêu cầu: Java 17, Maven, Docker Desktop đang chạy.

```powershell
docker compose up -d postgres rabbitmq video-storage
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
| 3.4 | ADMIN xin URL upload video cho khóa Middle Java | 200, `uploadUrl` có `X-Amz-Signature`, hết hạn sau 15 phút | Presigned URL, DIP: `IVideoStorage` |
| 3.5 | `PUT {{uploadUrl}}`, Body → binary → chọn `demo/assets/middle-java-bai-1.mp4` | 200; file đi **thẳng** vào S3/SeaweedFS, không qua API | Object storage |
| 3.6 | ADMIN xác nhận video | 200, `videoKey` = `courses/1/…mp4` (database chỉ lưu metadata) | SRP: `CourseVideoAdminServiceImpl` |
| 4.1–4.5 | Thêm 2 khóa, sửa số lượng = 2, xóa 1 khóa, xem giỏ | `total` được tính lại, xóa trả 204 | Extract Method / Extract Class |
| 5.1 | `GET /api/orders/payment-methods` | `BANK_TRANSFER, COD, VNPAY` | Registry tự nhận mọi `PaymentProcessor` (OCP) |
| 5.2 | Xem video trước khi mua | **403** | |
| 5.3 | `POST /api/orders` `{"paymentMethod":"VNPAY"}` | `PAID`, `paymentReference: VNP…`, `CONFIRMED` | `@Transactional`, Factory Method (`PaymentProcessor.createStrategy`) + Strategy |
| 5.4–5.5 | Giỏ đã trống; video trả `source: STORAGE` + `videoUrl` có chữ ký và `expiresAt` | | Chỉ người đã mua nhận được link |
| 5.5a–5.5b | Mở `{{signedVideoUrl}}` → 200; mở link đã bỏ chữ ký → **403** | | Kho lưu trữ private |
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
| `@Transactional`, Stream, Optional | `OrderServiceImpl.checkout`, `CartPricingCalculator`, `PaymentProcessorRegistry.processorFor` |
| Factory Method + Strategy + OCP | `commerce/payment/PaymentProcessor.java` (Creator), `*PaymentProcessor.java` (ConcreteCreator), `*PaymentStrategy.java` (Product/Strategy), `PaymentProcessorRegistry.java` |
| Observer + CompletableFuture + ExecutorService | `commerce/event/*`, `config/NotificationExecutorConfiguration.java` |
| Unit test (JUnit 5 + Mockito) | `src/test/.../commerce/service/CartServiceImplTest.java`, `OrderServiceImplTest.java`, `AdminOrderServiceImplTest.java`, `payment/PaymentFactoryMethodTest.java`, `event/OrderEventPublisherTest.java` |
| Integration test luồng đặt hàng | `src/test/.../commerce/PostgresCommerceIntegrationTest.java` (Testcontainers), `LearningServiceContextTest.java` (MockMvc) |
| Lưu video trên object storage | `course/video/IVideoStorage.java`, `S3VideoStorage.java`, `CourseVideoAdminServiceImpl.java`, `CourseEnrollmentServiceImpl.getVideo` |
| Migration mới | `db/migration/V5__payments_catalog_status.sql`, `V6__course_video_storage.sql` |

(các đường dẫn tính từ `learning-service/src/main/java/vn/edu/middlejava/learning/` hoặc `learning-service/src/test/java/vn/edu/middlejava/learning/`)

## 5. Lưu trữ video (object storage)

File video **không** nằm trong database. Database chỉ lưu metadata: `video_key`, loại file, dung lượng, thời điểm upload (migration V6). Bản thân file nằm trong object storage:

| | Local | AWS |
|---|---|---|
| Kho lưu trữ | SeaweedFS (tương thích S3) trong `docker-compose.yml`, cổng `8333`, có bật xác thực (`deploy/local/seaweedfs-s3.json`) | Bucket S3 private `…-studycoach-course-videos`: mã hóa, chặn public, chỉ cho CORS từ domain CloudFront |
| Credential | Access key local trong `application.yml` (chỉ dùng cho máy dev) | IAM role của EC2, không có key nào trong code hay config |
| Code | Cùng một class `S3VideoStorage` (AWS SDK v2), chỉ khác endpoint cấu hình bằng biến môi trường | |

Luồng:
1. ADMIN gọi `POST /api/admin/courses/{id}/video/upload-url`. API kiểm tra loại file (mp4/webm/mov) và dung lượng (≤ 500 MB), rồi trả **presigned PUT URL** hết hạn sau 15 phút.
2. Client `PUT` file thẳng lên kho lưu trữ. File không đi qua server ứng dụng.
3. ADMIN gọi `PUT /api/admin/courses/{id}/video` kèm `key`. API kiểm tra object thật sự tồn tại (HEAD) rồi mới gắn vào khóa học.
4. Khách đã mua gọi `GET /api/courses/{id}/video` và nhận **link có chữ ký hết hạn sau 1 giờ**. Khách chưa mua bị 403 và không bao giờ nhận được link. Khóa học chưa có video upload thì vẫn dùng YouTube.

Trên giao diện web: ADMIN vào tab **Quản lý sản phẩm** → **Tải video** → chọn file (có hiện % tiến độ). Khách vào **Đơn hàng** → **Xem video**, video phát bằng thẻ `<video>`.

Kiểm thử: `CourseVideoAdminServiceImplTest`, `CourseEnrollmentServiceImplTest` (Mockito) và `S3VideoStorageIntegrationTest`. Test tích hợp chạy SeaweedFS thật bằng Testcontainers, chứng minh: upload và xem được; link không chữ ký, link bị sửa chữ ký và link đã hết hạn đều bị **403**.

## Phụ lục — Chatbot trợ lý RAG (ngoài phạm vi đề bài)

`POST /api/materials` tạo tài liệu cho một unit (`courseId`, `unitId`, `title`, `content`). RabbitMQ chuyển yêu cầu indexing cho worker; worker tách chunk và lưu embedding vào pgvector. Gọi `GET /api/materials/{id}` đến khi trạng thái là `READY`, rồi `POST /api/ai/ask` với `unitId` và `question`. Response có câu trả lời và danh sách `sources`. API key Groq chỉ nằm trong biến môi trường hoặc Parameter Store, không đưa lên màn hình demo.

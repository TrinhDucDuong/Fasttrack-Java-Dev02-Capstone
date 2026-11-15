# Kịch bản OJT — Middle Java Course Store

## Mục tiêu và câu chuyện

Hệ thống bán các khóa Java quay sẵn. Sản phẩm gồm thông tin khóa học, giá và đường dẫn YouTube; video được lưu tại YouTube. Sau khi thanh toán thành công, API cấp quyền học và trả về URL dạng iframe. Trợ lý AI trả lời câu hỏi dựa trên tài liệu của từng unit bằng RAG.

Demo thực hiện bằng Postman gọi API qua API Gateway, đúng phạm vi đề bài không yêu cầu UI.

## Chuẩn bị chạy local

Yêu cầu Java 17, Maven và Docker Desktop đang chạy.

```powershell
docker compose up -d postgres rabbitmq
mvn -B clean verify
```

Maven chạy toàn bộ test và tạo các JAR. PostgreSQL chạy ở `localhost:5433`, RabbitMQ ở `localhost:5672`, giao diện quản trị RabbitMQ ở `http://localhost:15672`.

Mở bốn cửa sổ PowerShell tại thư mục project và chạy theo thứ tự:

```powershell
java -jar discovery-server/target/discovery-server-1.0.0.jar
```

```powershell
$env:ADMIN_EMAIL = 'admin@middlejava.local'
$env:ADMIN_PASSWORD = 'Admin123!Local'
java -jar learning-service/target/learning-service-1.0.0.jar
```

```powershell
# Đặt GROQ_API_KEY trong môi trường trước khi chạy service; không ghi key vào file hoặc video demo.
java -jar ai-service/target/ai-service-1.0.0.jar
```

```powershell
java -jar api-gateway/target/api-gateway-1.0.0.jar
```

Gateway nhận request tại `http://localhost:8080`. Health check: `GET http://localhost:8080/actuator/health`. Tài khoản ADMIN trên cấu hình local demo là `admin@middlejava.local` / `Admin123!Local`; chỉ dùng ở máy local demo.

## Postman — thứ tự thao tác

Tạo Postman environment với các biến `baseUrl=http://localhost:8080`, `accessToken`, `adminToken`, `courseId`, `paidCourseId`, `unitId`, `orderId` và `materialId`. Với request cần đăng nhập, thêm header `Authorization: Bearer {{accessToken}}`; request quản trị dùng `{{adminToken}}`.

### 1. Health, catalog và tìm kiếm

| Method | URL | Kết quả cần thấy |
|---|---|---|
| GET | `{{baseUrl}}/actuator/health` | `status: UP` |
| GET | `{{baseUrl}}/api/courses` | Danh sách khóa học và giá; response công khai không chứa URL video |
| GET | `{{baseUrl}}/api/courses?search=Java` | Tìm khóa học theo title |

Lấy id của khóa đầu tiên gán `courseId`, khóa thứ hai gán `paidCourseId`.

### 2. Đăng ký, đăng nhập và phân quyền

Đăng ký CUSTOMER:

```http
POST {{baseUrl}}/api/auth/register
Content-Type: application/json
```

```json
{"email":"customer.demo@example.com","password":"DemoPassword123!"}
```

Đăng nhập bằng `POST {{baseUrl}}/api/auth/login` với cùng body. Lưu `accessToken` vào biến `accessToken`. Khi demo local, đăng nhập bằng tài khoản ADMIN local đã nêu ở phần chuẩn bị. Khi demo AWS, đăng nhập bằng tài khoản ADMIN đã được cấu hình trong GitHub Actions/AWS Parameter Store. Lưu token ADMIN vào `adminToken`; không lưu mật khẩu vào Postman collection export.

Gọi `POST {{baseUrl}}/api/admin/courses` bằng `accessToken` của CUSTOMER; kết quả mong đợi là `403 Forbidden`. Gọi `GET {{baseUrl}}/api/courses/{{courseId}}/units` bằng token hợp lệ, lấy id unit đầu tiên lưu vào `unitId`.

### 3. Quản trị catalog

Gọi `PUT {{baseUrl}}/api/admin/courses/{{courseId}}` bằng `adminToken` để cập nhật sản phẩm. Body mẫu dùng thông tin course seed:

```json
{
  "slug": "middle-java",
  "title": "Middle Java",
  "description": "A practical learning path for Java backend engineering.",
  "price": 29.99,
  "youtubeUrl": "https://www.youtube.com/watch?v=GoXwIVyNvX0"
}
```

Kết quả trả về từ admin API có URL YouTube để quản trị. Catalog công khai vẫn không trả về link video.

### 4. Giỏ hàng và thanh toán

Thêm sản phẩm bằng `POST {{baseUrl}}/api/cart/items`:

```json
{"courseId": {{courseId}}, "quantity": 1}
```

Thay đổi số lượng bằng `PUT {{baseUrl}}/api/cart/items/{{courseId}}`:

```json
{"quantity": 2}
```

`GET {{baseUrl}}/api/cart` để kiểm tra số lượng và tổng tiền. Gọi `GET {{baseUrl}}/api/courses/{{courseId}}/video` trước khi mua; kết quả là `403 Forbidden`.

Tạo đơn chuyển khoản:

```http
POST {{baseUrl}}/api/orders
```

```json
{"paymentMethod":"BANK_TRANSFER"}
```

Order có `paymentStatus=PENDING`; gọi lại video vẫn nhận `403`, vì đơn chuyển khoản chưa được xác nhận đã trả tiền.

Thêm `paidCourseId` vào giỏ rồi checkout bằng phương thức mô phỏng:

```json
{"paymentMethod":"SIMULATED"}
```

Order có `paymentStatus=PAID`. Lưu id đơn vào `orderId`, sau đó gọi `GET {{baseUrl}}/api/courses/{{paidCourseId}}/video`. Response trả URL YouTube embed vì user đã có enrollment. Không có video file nào được upload vào database.

Kiểm tra `GET {{baseUrl}}/api/orders` và `GET {{baseUrl}}/api/notifications`. Notification được lưu bất đồng bộ sau khi order được tạo.

### 5. Chatbot trợ lý RAG

Tạo tài liệu cho unit bằng `POST {{baseUrl}}/api/materials`:

```json
{
  "courseId": {{courseId}},
  "unitId": {{unitId}},
  "title": "SOLID và Single Responsibility Principle",
  "content": "Single Responsibility Principle nói rằng một lớp nên có một trách nhiệm chính và một lý do để thay đổi. Ví dụ, lớp xử lý đơn hàng không nên đồng thời gửi email và ghi báo cáo. Tách các trách nhiệm thành service riêng giúp giảm coupling, dễ kiểm thử bằng Mockito và mở rộng theo Open/Closed Principle."
}
```

Response có `id` và trạng thái ban đầu. Lưu id vào `materialId`; gọi `GET {{baseUrl}}/api/materials/{{materialId}}` đến khi trạng thái là `READY`. RabbitMQ chuyển yêu cầu indexing tới worker, worker tách chunk và lưu embedding vào PostgreSQL pgvector.

Gửi câu hỏi bằng `POST {{baseUrl}}/api/ai/ask`:

```json
{
  "unitId": {{unitId}},
  "question": "Vì sao Single Responsibility Principle giúp giảm coupling?"
}
```

Response có câu trả lời từ Groq, model và danh sách `sources` được truy xuất. API key chỉ nằm trong biến môi trường local hoặc secret/parameter bảo mật của môi trường deploy; không đưa key lên màn hình demo.

## Đường dẫn code để thuyết trình

| Kiến thức | Nơi trình bày |
|---|---|
| Layered Architecture, Controller → Service → Repository, DTO/Mapper, Constructor DI | `learning-service/src/main/java/vn/edu/middlejava/learning/commerce/` |
| SRP và checkout transaction | `commerce/service/impl/CommerceServiceImpl.java` |
| Strategy và Factory Method cho thanh toán | `commerce/payment/BankTransferPaymentStrategy.java`, `SimulatedPaymentStrategy.java`, `PaymentStrategyFactory.java` |
| Observer và xử lý notification sau commit | `commerce/event/OrderCreatedEvent.java`, `OrderNotificationObserver.java` |
| JWT, Spring Security, CUSTOMER/ADMIN | `learning-service/src/main/java/vn/edu/middlejava/learning/auth/` |
| JPA, giá khóa học, enrollment và index tìm kiếm | `course/entity/`, `course/repository/`, `learning-service/src/main/resources/db/migration/V2__course_store.sql`, `V4__course_enrollments.sql` |
| JUnit/Mockito unit test | `learning-service/src/test/java/vn/edu/middlejava/learning/commerce/service/CommerceServiceImplTest.java` |
| Integration test với PostgreSQL và RabbitMQ | `learning-service/src/test/java/vn/edu/middlejava/learning/commerce/PostgresCommerceIntegrationTest.java` |
| API integration test | `learning-service/src/test/java/vn/edu/middlejava/learning/LearningServiceContextTest.java` |
| Ingestion, chunking, vector search và chatbot | `ai-service/src/main/java/vn/edu/middlejava/ai/ingestion/`, `chat/`, `provider/` |

Chạy lại toàn bộ test bằng `mvn -B clean verify`. Testcontainers cần Docker đang chạy. Lần kiểm tra hoàn tất gần nhất có 21 test, 0 failures, 0 skipped.

## Script thuyết trình video

### Mở đầu — 30 giây

“Em xin chào các anh. Hôm nay em trình bày Middle Java Course Store, một hệ thống bán các khóa học Java qua video quay sẵn. Em giữ lại chatbot trợ lý từ hệ thống AI Study Coach cũ và chuyển nghiệp vụ trung tâm sang đăng ký, catalog, giỏ hàng, đặt hàng và thanh toán. Vì đề bài không yêu cầu giao diện, em sẽ thao tác trực tiếp các API bằng Postman.”

### Kiến trúc và catalog — 1 phút

“Luồng request đi từ Postman vào API Gateway. Gateway định tuyến sang learning-service hoặc ai-service; Eureka phục vụ discovery. PostgreSQL lưu user, khóa học, giỏ, order và enrollment. RabbitMQ xử lý indexing tài liệu cho RAG. Em gọi health rồi lấy catalog và tìm kiếm theo title. Response công khai chỉ có thông tin và giá; đường dẫn video được trả khi tài khoản đã có quyền học.”

### Authentication và quản trị — 1 phút

“Em đăng ký một CUSTOMER và nhận JWT. Token mang thông tin user/role; Spring Security kiểm tra token và phân quyền. Khi CUSTOMER gọi endpoint quản trị, API trả 403. Em đăng nhập ADMIN rồi cập nhật catalog. Controller chỉ nhận request/response; nghiệp vụ ở service và truy cập dữ liệu ở repository, mỗi lớp có một trách nhiệm.”

### Cart và checkout — 2 phút

“Em thêm khóa học vào cart, cập nhật số lượng rồi xem tổng tiền. Trước thanh toán, endpoint video trả 403. Em checkout BANK_TRANSFER; đơn có trạng thái PENDING nên quyền học chưa được cấp. Tiếp theo em checkout SIMULATED; đơn có trạng thái PAID, cart được xóa và enrollment được tạo trong cùng transaction. Sau đó endpoint video trả URL YouTube embed. Database chỉ lưu metadata và đường dẫn video.”

“Hai cách thanh toán dùng Strategy để tách hành vi. PaymentStrategyFactory chọn strategy theo paymentMethod, nên có thể thêm phương thức mới mà không viết lại flow checkout — đây là cách em áp dụng OCP. Checkout dùng transaction để order, cart và enrollment nhất quán; dữ liệu order lưu snapshot tên/giá tại thời điểm mua.”

### Notification và RAG — 1 phút 30 giây

“Sau khi order được tạo, event được phát và Observer lưu notification bất đồng bộ. Em kiểm tra thông báo qua API. Tiếp theo em đưa tài liệu unit vào hệ thống. RabbitMQ chuyển tài liệu tới worker, worker chia chunk, tạo embedding và lưu vào pgvector. Khi em hỏi chatbot, hệ thống truy xuất đoạn liên quan rồi gửi context cho Groq; response trả lời kèm nguồn để kiểm tra căn cứ.”

### Kiểm thử và nguyên tắc — 1 phút

“Em dùng JUnit và Mockito để kiểm tra service cart/checkout độc lập dependency. Integration test chạy PostgreSQL và RabbitMQ thật bằng Testcontainers; API integration test kiểm tra JWT, role, cart, checkout, enrollment và quyền xem video. SOLID được thể hiện qua service tách trách nhiệm, phụ thuộc interface và constructor injection; Optional/Stream được dùng trong xử lý nghiệp vụ.”

### Kết thúc — 20 giây

“Tóm lại, project chuyển hệ thống học tập AI thành một course store có authentication, catalog, cart, order, hai phương thức thanh toán, notification và chatbot RAG. Em đã kiểm tra toàn bộ test suite và chạy luồng nghiệp vụ qua API. Em xin kết thúc phần trình bày, cảm ơn các anh.”

## Lưu ý khi quay

- Không quay lộ `GROQ_API_KEY`, JWT hoặc màn hình GitHub/AWS có thông tin bí mật.
- Dùng email CUSTOMER mới cho mỗi lần chạy demo; Postman lưu token trong environment hiện tại.
- Các khoản thanh toán là mô phỏng: `BANK_TRANSFER` tạo đơn chờ xác nhận, `SIMULATED` đánh dấu trả tiền thành công.
- Nếu video YouTube chặn nhúng, thay link course bằng video công khai cho phép embed qua endpoint quản trị.
- Hệ thống không kèm frontend; video được chứng minh bằng response `youtubeEmbedUrl` sau khi mua.

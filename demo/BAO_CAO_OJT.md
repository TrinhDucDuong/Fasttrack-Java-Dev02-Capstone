# Báo cáo OJT — E-Commerce Mini "Middle Java Store"

Hệ thống bán khóa học Java quay sẵn (sản phẩm = khóa học, video lưu trên YouTube). Khách hàng đăng ký, tìm khóa học, thêm vào giỏ, đặt hàng, thanh toán; khi thanh toán xong thì được mở quyền xem video. ADMIN quản lý sản phẩm và xác nhận thanh toán của đơn COD hoặc chuyển khoản.

- Kiến trúc: Spring Boot 3.5 / Java 17, microservices (Eureka `discovery-server`, `api-gateway`, `learning-service`, `ai-service`), PostgreSQL + Flyway, RabbitMQ.
- Giao diện web tối giản cho cả 2 role nằm ở `api-gateway/src/main/resources/static/index.html`; API vẫn demo được bằng Postman.
- Môi trường: local (Docker Compose + 4 JAR) và AWS (EC2 + RDS PostgreSQL + CloudFront + ECR). CI/CD bằng GitHub Actions: `mvn verify` → build image → push ECR → deploy qua SSM.

## 1. Kiến thức áp dụng theo từng chức năng

| Yêu cầu đề bài | Áp dụng ở đâu | Giải thích |
|---|---|---|
| Spring Security + JWT | `auth/config/SecurityConfiguration.java`, `auth/service/impl/AuthServiceImpl.java` | Đăng nhập trả về access token JWT (HS256, 15 phút) và refresh token. Resource Server tự kiểm tra chữ ký và đọc claim `roles`. |
| Phân quyền ADMIN / CUSTOMER | `SecurityConfiguration`: `/api/admin/**` yêu cầu `hasRole("ADMIN")` | Tài khoản mới đăng ký có role `CUSTOMER` (`StudyUser`). ADMIN được tạo bởi `AdminAccountSeeder` từ biến môi trường hoặc AWS Parameter Store. CUSTOMER gọi API admin sẽ nhận 403. |
| JPA/Hibernate | `course/entity/Course.java`, `commerce/entity/*`, các repository Spring Data | Quan hệ `@OneToMany` giữa giỏ hàng và mục hàng, cascade + `orphanRemoval`; query sinh từ tên method; cache cấp 2 (JCache) cho `Course`. |
| Index cho trường tìm kiếm | `db/migration/V2__course_store.sql`: `idx_courses_title_substring` (GIN trigram trên `upper(title)`) | `findByActiveTrueAndTitleContainingIgnoreCaseOrderByIdAsc` sinh `upper(title) like upper(?)`, nên dùng được index trigram. Integration test kiểm tra index tồn tại. |
| Quản lý sản phẩm | `AdminCatalogControllerImpl` + `CatalogAdminServiceImpl` | ADMIN xem danh sách, thêm, sửa, ngừng bán (soft delete, cột `active` – migration V5). Catalog công khai chỉ hiện khóa đang bán. |
| Giỏ hàng: thêm / sửa số lượng / xóa | `CartControllerImpl` → `CartServiceImpl` → `ShoppingCart` | Thêm cùng khóa thì cộng dồn số lượng; số lượng < 1 bị từ chối; xóa mục không có trong giỏ trả về 404. |
| Refactoring – Extract Method | `CartServiceImpl.findSellableCourse / findOrCreateCart / findCart / toResponse`, `CartPricingCalculator.loadCatalog / toLine / sumLineTotals`, `OrderServiceImpl.buildOrder` | Mỗi bước có tên rõ ràng, các method public chỉ còn 2–4 dòng. |
| Refactoring – Extract Class | `commerce/pricing/CartPricingCalculator` + `PricedCart`, `commerce/mapper/CommerceMapper`, tách `CommerceServiceImpl` thành `CartServiceImpl` / `OrderServiceImpl` / `AdminOrderServiceImpl` | Trước đây logic tính tiền bị lặp ở giỏ hàng và checkout; nay chỉ còn một class sở hữu. Việc map DTO cũng tách khỏi service. |
| Unit test giỏ hàng (JUnit 5 + Mockito) | `CartServiceImplTest` (11 test) | `@ExtendWith(MockitoExtension)`, `@Mock ShoppingCartRepository / ICourseRepository`, `verify(..., never())`. |
| Đặt hàng: giỏ → đơn | `OrderServiceImpl.checkout` | Snapshot giá và tên tại thời điểm mua vào `customer_order_items`, sau đó làm trống giỏ. |
| `@Transactional` | `OrderServiceImpl.checkout`, `AdminOrderServiceImpl.confirmPayment`, `@Transactional(readOnly = true)` cho truy vấn | Lưu đơn + mở quyền học + xóa giỏ trong 1 transaction. Test `transactionalCheckoutRollsBackEverythingWhenAStepFails` cho một bước ném lỗi rồi kiểm tra: không có đơn mới, giỏ còn nguyên, không phát thông báo. |
| Stream API / Optional | `CartPricingCalculator` (`stream().map().reduce`, `Collectors.toMap`), `OrderServiceImpl` (`findByUserId().filter().orElseThrow()`), `PaymentStrategyFactory.create` (`Optional.ofNullable().map().orElseThrow()`), `CourseServiceImpl.searchCourses` | Không dùng vòng for hay `if (x != null)` thủ công. |
| ≥ 2 phương thức thanh toán | `VnPayPaymentStrategy` (giả lập, PAID + mã giao dịch `VNP…`), `CodPaymentStrategy` (PENDING), `BankTransferPaymentStrategy` (PENDING + mã chuyển khoản `BT-…`) | Đơn PENDING được ADMIN xác nhận qua `PUT /api/admin/orders/{id}/confirm-payment`. |
| Thông báo trạng thái đơn hàng | `OrderEventPublisher`, `InAppNotificationObserver`, `EmailNotificationObserver` | Gửi khi tạo đơn và khi ADMIN xác nhận thanh toán. Khách đọc qua `GET /api/notifications`; email được ghi log giả lập. |
| CompletableFuture / ExecutorService | `OrderEventPublisher.notifyObservers` + `config/NotificationExecutorConfiguration` | Mỗi observer chạy bằng `CompletableFuture.runAsync(..., executor)` trên pool riêng `order-notify-*`. `exceptionally` cô lập lỗi; `allOf` gom kết quả. Listener chạy `AFTER_COMMIT` nên chỉ thông báo khi dữ liệu đã chắc chắn được lưu. |
| Unit test Service + Mockito | `CartServiceImplTest`, `OrderServiceImplTest`, `AdminOrderServiceImplTest`, `CourseServiceImplTest`, `PaymentStrategyFactoryTest`, `CartPricingCalculatorTest`, `OrderEventPublisherTest` | Mock repository, `ApplicationEventPublisher`, `ICourseEnrollmentService`; `ArgumentCaptor` kiểm tra event được publish. |
| Integration test luồng đặt hàng | `PostgresCommerceIntegrationTest` (Testcontainers: PostgreSQL + RabbitMQ thật), `LearningServiceContextTest` (MockMvc, gọi HTTP đầy đủ với JWT) | Kiểm tra: Flyway V1–V5, index, checkout VNPay, COD + ADMIN xác nhận, rollback, thông báo bất đồng bộ, phân quyền 403/404/409. |

Tổng cộng 41 test ở `learning-service`, chạy tự động trong GitHub Actions trước mỗi lần deploy.

## 2. SOLID

| Nguyên lý | Ví dụ trong code |
|---|---|
| **S** – Single Responsibility | `CartServiceImpl` chỉ quản lý giỏ hàng; `OrderServiceImpl` chỉ checkout và đọc đơn; `AdminOrderServiceImpl` chỉ xử lý nghiệp vụ back-office; `CartPricingCalculator` chỉ tính tiền; `CommerceMapper` chỉ map DTO; mỗi observer lo một kênh thông báo. |
| **O** – Open/Closed | Thêm phương thức thanh toán (ví dụ MoMo) chỉ cần một class `@Component implements PaymentStrategy`, không sửa `PaymentStrategyFactory` hay `OrderServiceImpl`. Test `openClosedPrincipleNewMethodNeedsNoFactoryChange` chứng minh điều này. Thêm kênh thông báo (SMS) cũng chỉ cần một `OrderStatusObserver` mới. |
| **L** – Liskov Substitution | Mọi `PaymentStrategy` và `OrderStatusObserver` thay thế nhau được: checkout và publisher chỉ dựa vào hợp đồng của interface. |
| **I** – Interface Segregation | Interface nhỏ và tách theo vai trò: `ICartService`, `IOrderService`, `IAdminOrderService`, `ICatalogAdminService`; controller cũng tách interface (`ICartController`, `IOrderController`, …). |
| **D** – Dependency Inversion | Service phụ thuộc vào abstraction (`PaymentStrategy`, `OrderStatusObserver`, repository interface, `ApplicationEventPublisher`), được Spring inject qua constructor. Nhờ vậy unit test thay bằng mock dễ dàng. |

## 3. Design Pattern và lý do chọn

| Pattern | Class | Vì sao chọn |
|---|---|---|
| **Strategy** | `PaymentStrategy` + `VnPayPaymentStrategy`, `CodPaymentStrategy`, `BankTransferPaymentStrategy` | Mỗi phương thức thanh toán có quy tắc riêng (trả ngay hay chờ, mã tham chiếu). Strategy gom từng thuật toán vào một class, nên checkout không còn chuỗi `if/switch` theo loại thanh toán. |
| **Factory Method** | `PaymentStrategyFactory.create(method)` | Một điểm duy nhất quyết định tạo hoặc chọn strategy theo dữ liệu từ client, trả về kiểu interface. Client không `new` class cụ thể. Factory tự nhận các strategy qua Spring nên đóng với sửa đổi, mở để mở rộng (OCP). |
| **Observer** | Subject `OrderEventPublisher`; Observer `OrderStatusObserver` → `InAppNotificationObserver`, `EmailNotificationObserver`; Event `OrderStatusChangedEvent` | Đơn hàng không cần biết có bao nhiêu kênh thông báo. Thêm hoặc bớt kênh không ảnh hưởng nghiệp vụ đặt hàng. Kết hợp `CompletableFuture` + `ExecutorService` để thông báo không làm chậm request. |
| Repository / DTO / Mapper | Spring Data repository, record DTO, `CommerceMapper`, `CourseMapper` | Tách tầng dữ liệu và tầng API; entity không lộ ra ngoài. |

## 4. API chính

| Method | Endpoint | Role |
|---|---|---|
| POST | `/api/auth/register`, `/api/auth/login`, `/api/auth/refresh` | public |
| GET | `/api/courses`, `/api/courses?search=` | public |
| GET/POST/PUT/DELETE | `/api/admin/courses[/{id}]` | ADMIN |
| GET/POST/PUT/DELETE | `/api/cart`, `/api/cart/items[/{courseId}]` | đã đăng nhập |
| GET | `/api/orders/payment-methods` | đã đăng nhập |
| POST/GET | `/api/orders`, `/api/orders/{id}` | đã đăng nhập |
| GET | `/api/admin/orders` | ADMIN |
| PUT | `/api/admin/orders/{id}/confirm-payment` | ADMIN |
| GET | `/api/notifications` | đã đăng nhập |
| GET | `/api/courses/{id}/video` | đã mua |

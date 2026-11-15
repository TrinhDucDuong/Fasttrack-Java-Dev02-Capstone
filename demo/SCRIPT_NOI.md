# Kịch bản thuyết trình — Middle Java Store

Kịch bản thuyết trình cho video demo, chia theo từng phần và từng màn hình. Mỗi mục ghi mốc thời gian, tên màn hình và nội dung trình bày.

## Mục lục

| Phần | Bắt đầu |
|---|---|
| GIỚI THIỆU | 00:00.0 |
| CODE · ÁP DỤNG KIẾN THỨC | 00:42.2 |
| KIỂM THỬ | 03:36.4 |
| DEMO LOCAL · Postman | 04:07.5 |
| DEMO LOCAL · Web UI | 06:28.6 |
| MỞ RỘNG · CI/CD & SECRET | 06:56.0 |
| MỞ RỘNG · TRIỂN KHAI AWS | 07:24.3 |
| MỞ RỘNG · DEMO UI TRÊN AWS | 07:48.7 |
| TỔNG KẾT | 08:30.9 |

---

## GIỚI THIỆU

### [00:00.0 → 00:21.0] #01 · E-Commerce Mini — Middle Java Store

Xin chào thầy và các bạn, em là Trịnh Đức Dương. Đây là demo bài OJT E-Commerce Mini của em: Middle Java Store, cửa hàng bán khóa học Java. Phần chính bám sát sáu yêu cầu của đề, demo trên local với cả hai vai trò ADMIN và CUSTOMER. Phần mở rộng ở cuối là CI CD và triển khai lên AWS.


### [00:21.0 → 00:29.9] #02 · Đề bài → chức năng đã làm

Bảng này đối chiếu đề bài với phần em đã làm: xác thực, sản phẩm, giỏ hàng, đặt hàng, thanh toán, thông báo và kiểm thử.


### [00:29.9 → 00:42.2] #03 · Kiến trúc

Request đi qua API Gateway, gateway tìm service qua Eureka. Toàn bộ nghiệp vụ nằm trong learning service, dữ liệu lưu ở PostgreSQL, schema quản lý bằng Flyway.


## CODE · ÁP DỤNG KIẾN THỨC

### [00:42.2 → 00:54.3] #04 · 1. Authentication — Spring Security + JWT

Phần một, xác thực. Spring Security đọc claim roles trong JWT để phân quyền. Mọi đường dẫn admin chỉ dành cho ADMIN; CUSTOMER gọi vào sẽ nhận lỗi 403.


### [00:54.3 → 01:03.6] #05 · Phát hành JWT khi đăng nhập

Khi đăng nhập, service phát hành access token mười lăm phút, chứa id, email và role. Tài khoản đăng ký mới luôn là CUSTOMER.


### [01:03.6 → 01:16.8] #06 · 2. Quản lý sản phẩm — JPA/Hibernate + Index

Phần hai, sản phẩm. Em tạo index GIN trên upper của title. Method Spring Data JPA bên phải sinh câu like khớp đúng biểu thức của index, nên tìm kiếm không phải quét toàn bảng.


### [01:16.8 → 01:26.0] #07 · SRP — quản trị sản phẩm tách khỏi catalog công khai

Theo SRP, phần quản trị sản phẩm của ADMIN là một service riêng. Xóa sản phẩm là ngừng bán, đơn hàng cũ vẫn giữ lịch sử.


### [01:26.0 → 01:37.9] #08 · 3. Giỏ hàng — Extract Method

Phần ba, giỏ hàng với thêm, sửa số lượng và xóa. Em áp dụng Extract Method: method public chỉ vài dòng, các bước nhỏ tách thành method private có tên rõ ràng.


### [01:37.9 → 01:48.4] #09 · Extract Class — CartPricingCalculator

Và Extract Class: phần tính tiền bị lặp ở giỏ hàng và checkout được tách thành CartPricingCalculator, bên trong dùng Stream API.


### [01:48.4 → 02:01.6] #10 · 4. Đặt hàng — @Transactional + Optional + Stream

Phần bốn, đặt hàng. Checkout chạy trong một transaction, dùng Optional để lấy giỏ và chặn giỏ rỗng. Lưu đơn, mở quyền học, làm trống giỏ và phát sự kiện. Bước nào lỗi thì tất cả rollback.


### [02:01.6 → 02:19.0] #11 · 5. Thanh toán — Factory Method (GoF)

Phần năm, thanh toán. Lớp trừu tượng PaymentProcessor là creator, có factory method createStrategy. Method process chạy quy trình chung nhưng không biết strategy cụ thể; mỗi lớp con như VNPay hay COD tự quyết định tạo strategy nào.


### [02:19.0 → 02:31.8] #12 · Strategy Pattern

Sản phẩm của factory là Strategy. VNPay giả lập trả PAID kèm mã giao dịch; COD và chuyển khoản trả PENDING chờ ADMIN xác nhận. Checkout không có if else theo loại thanh toán.


### [02:31.8 → 02:44.7] #13 · Open/Closed Principle

Spring tự đưa mọi processor vào registry. Muốn thêm MoMo chỉ cần thêm một lớp mới, không sửa registry hay checkout; test bên phải chứng minh điều đó. Đây là nguyên lý đóng mở.


### [02:44.7 → 02:59.3] #14 · 6. Thông báo — Observer + CompletableFuture + ExecutorService

Phần sáu, thông báo, dùng Observer. Publisher chỉ chạy sau khi transaction commit, gửi cho từng observer bằng CompletableFuture trên ExecutorService riêng. Một kênh lỗi không ảnh hưởng kênh khác.


### [02:59.3 → 03:07.6] #15 · Các observer

Có hai observer: In App lưu thông báo cho khách đọc, Email ghi log giả lập. Thêm kênh mới chỉ cần thêm một lớp.


### [03:07.6 → 03:21.3] #16 · SOLID trong project

Tóm tắt SOLID: mỗi lớp một trách nhiệm; mở rộng thanh toán và thông báo không sửa code cũ; các lớp con thay thế nhau được; interface nhỏ theo vai trò; và service phụ thuộc abstraction nên dễ mock.


### [03:21.3 → 03:36.4] #17 · Design Pattern và lý do chọn

Ba design pattern và lý do chọn. Factory Method để việc tạo strategy nằm ở lớp con. Strategy để mỗi cách thanh toán là một thuật toán riêng. Observer để tách đơn hàng khỏi các kênh thông báo và gửi bất đồng bộ.


## KIỂM THỬ

### [03:36.4 → 03:48.1] #18 · Unit test giỏ hàng — JUnit 5 + Mockito

Phần kiểm thử. Unit test giỏ hàng dùng JUnit 5 và Mockito, repository được mock. Ví dụ thêm cùng khóa hai lần thì số lượng cộng dồn và không tạo giỏ mới.


### [03:48.1 → 04:01.5] #19 · Integration test luồng đặt hàng — PostgreSQL thật

Integration test chạy trên PostgreSQL thật bằng Testcontainers. Em cố tình cho một bước lỗi giữa checkout: không có đơn mới, giỏ còn nguyên, không có thông báo. Transaction đã rollback đúng.


### [04:01.5 → 04:07.5] #20 · Kết quả kiểm thử

Kết quả: 56 test đều pass, gồm unit test các service và integration test.


## DEMO LOCAL · Postman

### [04:07.5 → 04:15.8] #21 · Khởi động môi trường local

Giờ demo local. Em chạy hạ tầng bằng Docker Compose và bốn service, rồi dùng Postman với environment LOCAL.


### [04:15.8 → 04:21.0] #22 · 1.2  Danh sách sản phẩm (public)

Danh sách sản phẩm công khai: có giá, không lộ link video.


### [04:21.0 → 04:27.1] #23 · 1.3  Tìm kiếm theo tên - dùng index

Tìm kiếm sản phẩm theo tên, API công khai, dùng index vừa trình bày.


### [04:27.1 → 04:33.9] #24 · 2.1  Đăng ký CUSTOMER

Đăng ký tài khoản mới: kết quả 201, nhận access token. Tài khoản này là CUSTOMER.


### [04:33.9 → 04:39.4] #25 · 2.2  Đăng nhập CUSTOMER - nhận JWT

Đăng nhập CUSTOMER, Postman tự lưu token cho các request sau.


### [04:39.4 → 04:45.1] #26 · 2.3  CUSTOMER gọi API ADMIN -> 403

CUSTOMER gọi API tạo sản phẩm của ADMIN bị chặn với lỗi 403.


### [04:45.1 → 04:51.3] #27 · 2.4  Đăng nhập ADMIN

Đăng nhập tài khoản ADMIN có sẵn, token được lưu vào biến adminToken.


### [04:51.3 → 04:55.8] #28 · 3.1  ADMIN tạo sản phẩm mới

ADMIN tạo sản phẩm mới, kết quả 201.


### [04:55.8 → 05:00.3] #29 · 3.2  ADMIN cập nhật giá

ADMIN cập nhật giá sản phẩm.


### [05:00.3 → 05:05.2] #30 · 4.1  Thêm Middle Java vào giỏ

Sang vai trò CUSTOMER: thêm khóa Middle Java vào giỏ.


### [05:05.2 → 05:09.8] #31 · 4.3  Cập nhật số lượng = 2

Cập nhật số lượng lên hai, tổng tiền được tính lại.


### [05:09.8 → 05:14.3] #32 · 4.4  Xóa sản phẩm khỏi giỏ

Xóa một sản phẩm khỏi giỏ, trả về 204.


### [05:14.3 → 05:18.8] #33 · 4.5  Xem giỏ hàng

Xem lại giỏ: một sản phẩm, số lượng hai.


### [05:18.8 → 05:23.4] #34 · 5.1  Phương thức thanh toán (Factory)

Danh sách phương thức thanh toán lấy từ registry.


### [05:23.4 → 05:27.9] #35 · 5.2  Chưa mua -> video 403

Chưa mua thì xem video bị 403.


### [05:27.9 → 05:37.0] #36 · 5.3  Đặt hàng VNPAY (giả lập)

Đặt hàng bằng VNPay giả lập: đơn PAID, có mã giao dịch, trạng thái CONFIRMED. Giỏ được làm trống trong cùng transaction.


### [05:37.0 → 05:41.5] #37 · 5.5  Đã mua -> video 200

Đã mua thì xem được video.


### [05:41.5 → 05:46.0] #38 · 5.6  Thông báo đơn hàng (Observer)

Thông báo đơn hàng do observer tạo ra.


### [05:46.0 → 05:54.3] #39 · Log: observer chạy trên thread riêng

Trong log, request chạy trên thread của Tomcat, còn hai observer chạy song song trên thread order notify.


### [05:54.3 → 05:58.8] #40 · 6.2  Đặt hàng COD

Đặt hàng COD: đơn ở trạng thái chờ thanh toán.


### [05:58.8 → 06:03.3] #41 · 6.3  COD chưa trả tiền -> video 403

COD chưa trả tiền nên video vẫn bị 403.


### [06:03.3 → 06:09.3] #42 · 6.5  ADMIN xác nhận thanh toán COD

Vai trò ADMIN xác nhận đã nhận tiền: đơn chuyển sang PAID và mở quyền học.


### [06:09.3 → 06:13.8] #43 · 6.6  Xác nhận lần 2 -> 409

Xác nhận lần hai bị từ chối với 409.


### [06:13.8 → 06:18.3] #44 · 6.8  Thông báo đổi trạng thái

Khách nhận thêm thông báo đơn đã được xác nhận.


### [06:18.3 → 06:22.8] #45 · 7.1  Giỏ trống -> 400

Đặt hàng khi giỏ trống trả 400.


### [06:22.8 → 06:28.6] #46 · 7.2  ADMIN ngừng bán sản phẩm

ADMIN ngừng bán sản phẩm, sản phẩm biến mất khỏi catalog công khai.


## DEMO LOCAL · Web UI

### [06:28.6 → 06:35.6] #47 · CUSTOMER — giỏ hàng trên giao diện web

Cùng luồng đó trên giao diện web. CUSTOMER thêm sản phẩm và sửa số lượng ngay trong giỏ.


### [06:35.6 → 06:40.8] #48 · CUSTOMER — đơn hàng đã thanh toán

Đặt hàng VNPay xong, đơn hiện PAID và có nút xem video.


### [06:40.8 → 06:47.0] #49 · ADMIN — quản lý đơn hàng

Đăng nhập ADMIN, giao diện chuyển sang quản lý đơn. Đơn COD có nút xác nhận.


### [06:47.0 → 06:51.5] #50 · ADMIN — xác nhận thanh toán

Bấm xác nhận, đơn chuyển sang CONFIRMED.


### [06:51.5 → 06:56.0] #51 · ADMIN — quản lý sản phẩm

ADMIN thêm, sửa và ngừng bán sản phẩm.


## MỞ RỘNG · CI/CD & SECRET

### [06:56.0 → 07:05.3] #52 · Mở rộng 1 — GitHub Actions

Phần mở rộng. Mỗi lần push, GitHub Actions chạy toàn bộ test. Test xanh thì mới build image, đẩy lên Amazon ECR và deploy.


### [07:05.3 → 07:18.3] #53 · Lưu secret thế nào

Về secret: mật khẩu khai báo trong GitHub Secrets, pipeline chuyển vào Parameter Store dạng mã hóa, EC2 đọc bằng IAM role khi deploy. Không có mật khẩu nào trong code.


### [07:18.3 → 07:24.3] #54 · Pipeline chạy thành công

Đây là một lần pipeline chạy thành công: verify xong rồi mới deploy.


## MỞ RỘNG · TRIỂN KHAI AWS

### [07:24.3 → 07:37.1] #55 · Mở rộng 2 — Hạ tầng AWS

Hạ tầng AWS tạo bằng CloudFormation: CloudFront làm cổng HTTPS, EC2 chạy container, RDS lưu dữ liệu, ECR lưu image, S3 lưu video khóa học.


### [07:37.1 → 07:48.7] #56 · Lưu video trên S3 — presigned URL

Video không lưu trong database. ADMIN nhận một link upload có chữ ký, file đi thẳng lên S3, sau đó API kiểm tra file tồn tại rồi mới gắn vào khóa học.


## MỞ RỘNG · DEMO UI TRÊN AWS

### [07:48.7 → 07:57.5] #57 · ADMIN trên AWS — xác nhận đơn COD

Giờ demo trên AWS qua CloudFront. ADMIN đăng nhập, thấy đơn COD đang chờ và bấm xác nhận đã thanh toán.


### [07:57.5 → 08:05.8] #58 · ADMIN trên AWS — tải video bài học lên S3

ADMIN tải video bài học cho khóa Middle Java. File đi thẳng lên S3, cột video chuyển thành Storage.


### [08:05.8 → 08:13.1] #59 · CUSTOMER — đăng ký tài khoản mới

Một khách mới đăng ký tài khoản. Mật khẩu phải từ mười ký tự, tài khoản mới luôn là CUSTOMER.


### [08:13.1 → 08:18.6] #60 · CUSTOMER — thêm vào giỏ, chọn VNPay

Khách thêm khóa Middle Java vào giỏ và chọn thanh toán VNPay.


### [08:18.6 → 08:26.1] #61 · CUSTOMER — thanh toán xong, xem video từ S3

Đặt hàng thành công, đơn PAID. Khách mở video, phát từ S3 bằng link riêng chỉ có hạn một giờ.


### [08:26.1 → 08:30.9] #62 · CUSTOMER — thông báo đơn hàng

Và khách nhận được thông báo đơn hàng do observer gửi.


## TỔNG KẾT

### [08:30.9 → 08:43.8] #63 · Tổng kết

Tóm lại, em đã hoàn thành đủ yêu cầu đề bài, có test chạy tự động và triển khai lên AWS. Em cảm ơn thầy và các bạn đã theo dõi.


# Script nói — video demo OJT "Middle Java Store"

Script này khớp với `demo/video/MiddleJavaStore_Demo.mp4`: video dài **14:27.8** (1920×1080, 30 fps, 67 khung hình) và có sẵn một track âm thanh im lặng để bạn thay bằng giọng thu.

## Cách thu âm và ghép

1. Mỗi khung hình bên dưới có mốc **bắt đầu → kết thúc**. Đọc đúng đoạn lời nói trong khoảng thời gian đó. Thời lượng đã tính theo tốc độ nói khoảng 2,9 từ/giây cộng thêm khoảng nghỉ, nên bạn có thể nói chậm, rõ ràng.
2. Cách dễ nhất là thu một file liền mạch, vừa nói vừa nhìn video phát ở chế độ im lặng. Cũng có thể thu từng đoạn rồi đặt vào đúng mốc thời gian trong CapCut, Premiere hoặc DaVinci.
3. Ghép bằng ffmpeg (giữ nguyên hình, thay track tiếng):

```bash
ffmpeg -i demo/video/MiddleJavaStore_Demo.mp4 -i giong_noi.m4a -map 0:v -map 1:a -c:v copy -c:a aac -shortest demo/video/MiddleJavaStore_Demo_final.mp4
```

Khung đỏ trên màn hình đánh dấu đúng phần đang nói tới (dòng code, field JSON, nút trên giao diện). Khi đọc, hãy nhắc tới đúng phần đó.

## Mục lục

| Phần | Bắt đầu |
|---|---|
| GIỚI THIỆU | 00:00.0 |
| CODE · ÁP DỤNG KIẾN THỨC | 01:17.1 |
| KIỂM THỬ | 05:33.4 |
| DEMO LOCAL · Postman | 06:40.4 |
| DEMO LOCAL · Web UI | 11:14.3 |
| CI/CD · AWS | 11:50.0 |
| DEMO AWS CLOUD · API | 12:36.7 |
| DEMO AWS CLOUD · Web UI | 12:42.0 |
| TỔNG KẾT | 14:04.5 |

---

## GIỚI THIỆU

### [00:00.0 → 00:28.1] #01 · E-Commerce Mini — Middle Java Store

Xin chào thầy và các bạn. Em là Trịnh Đức Dương. Đây là video demo bài OJT E-Commerce Mini của em, tên là Middle Java Store, một hệ thống bán các khóa học Java quay sẵn. Em sẽ trình bày theo thứ tự: kiến trúc, phần code áp dụng kiến thức của khóa học, kiểm thử, quy trình CI CD lên AWS, sau đó demo trên môi trường local bằng Postman và trên AWS qua giao diện web.

### [00:28.1 → 00:50.0] #02 · Đề bài → chức năng đã làm

Đây là bảng đối chiếu đề bài với những gì em đã làm. Sáu yêu cầu chức năng gồm xác thực, quản lý sản phẩm, giỏ hàng, đặt hàng, thanh toán và thông báo, cộng với phần kiểm thử. Mỗi dòng đều có phần code, phần test và bước demo tương ứng mà em sẽ đi qua ngay sau đây.

### [00:50.0 → 01:17.1] #03 · Kiến trúc hệ thống

Về kiến trúc, client là Postman hoặc giao diện web. Mọi request đi qua API Gateway, gateway tìm service qua Eureka. Toàn bộ nghiệp vụ thương mại điện tử nằm trong learning service: xác thực, sản phẩm, giỏ hàng, đơn hàng, thanh toán và thông báo. Dữ liệu lưu ở PostgreSQL, schema được quản lý bằng Flyway. Ở local em chạy hạ tầng bằng Docker Compose, còn trên AWS là CloudFront, EC2, RDS và ECR.

## CODE · ÁP DỤNG KIẾN THỨC

### [01:17.1 → 01:36.9] #04 · 1. Authentication — Spring Security + JWT

Phần một là xác thực. Em dùng Spring Security làm Resource Server với JWT ký HS256. Bộ converter ở trên đọc claim roles trong token để chuyển thành quyền của Spring. Ở dưới, mọi URL bắt đầu bằng slash api slash admin đều yêu cầu role ADMIN; CUSTOMER gọi vào sẽ nhận lỗi 403.

### [01:36.9 → 01:52.9] #05 · Phát hành JWT khi đăng nhập

Khi đăng nhập thành công, service phát hành access token có hạn 15 phút. Subject là id của user, kèm claim email và roles, ví dụ ROLE CUSTOMER. Ngoài ra có refresh token; refresh token chỉ lưu dạng băm SHA 256 trong database.

### [01:52.9 → 02:16.2] #06 · 2. Quản lý sản phẩm — JPA/Hibernate + Index

Phần hai là quản lý sản phẩm. Bên trái là migration Flyway: em bật extension pg trgm và tạo index GIN trên upper của title. Bên phải là repository Spring Data JPA. Method này sinh câu SQL upper title like upper tham số, khớp đúng biểu thức của index, nên tìm kiếm chứa từ khóa vẫn dùng được index thay vì quét toàn bảng.

### [02:16.2 → 02:32.9] #07 · SRP — dịch vụ quản trị sản phẩm tách khỏi catalog công khai

Theo nguyên lý Single Responsibility, phần quản trị sản phẩm của ADMIN nằm trong một service riêng, tách khỏi catalog công khai. Thao tác xóa là soft delete: khóa học bị đánh dấu ngừng bán, nhưng các đơn hàng cũ vẫn giữ nguyên lịch sử.

### [02:32.9 → 02:56.5] #08 · 3. Giỏ hàng — Extract Method

Phần ba là giỏ hàng, với ba thao tác thêm, cập nhật số lượng và xóa. Em áp dụng Extract Method: các method public chỉ còn vài dòng, đọc như một câu nghiệp vụ. Những bước như tìm khóa học còn bán, tìm hoặc tạo giỏ, tìm giỏ được tách thành các method private có tên rõ ràng và dùng lại ở mọi thao tác.

### [02:56.5 → 03:18.0] #09 · Extract Class — CartPricingCalculator

Tiếp theo là Extract Class. Trước đây phần tính tiền bị viết lặp ở cả giỏ hàng lẫn checkout. Em tách ra thành class CartPricingCalculator trả về một PricedCart bất biến. Bên trong dùng Stream API: map từng mục thành dòng giá, rồi gom bằng Collectors toMap. Tương tự, phần map sang DTO cũng được tách ra class CommerceMapper.

### [03:18.0 → 03:44.4] #10 · 4. Đặt hàng — @Transactional + Optional + Stream

Phần bốn là đặt hàng. Toàn bộ checkout chạy trong một transaction. Em dùng Optional: tìm giỏ, lọc giỏ rỗng, nếu không có thì ném lỗi. Sau đó lấy strategy thanh toán từ factory, tính tiền, lưu đơn, mở quyền học nếu đã thanh toán, làm trống giỏ và phát sự kiện. Nếu bất kỳ bước nào lỗi thì tất cả được rollback: không có đơn nửa vời và giỏ hàng còn nguyên.

### [03:44.4 → 04:08.0] #11 · 5. Thanh toán — Strategy Pattern

Phần năm là thanh toán, em áp dụng Strategy Pattern. Interface PaymentStrategy có hai method: mã phương thức và pay. Có ba strategy: VNPay giả lập thì thanh toán ngay và trả về mã giao dịch bắt đầu bằng VNP; COD và chuyển khoản thì ở trạng thái chờ, đợi ADMIN xác nhận. Nhờ vậy checkout không cần if else theo từng loại thanh toán.

### [04:08.0 → 04:30.9] #12 · Factory Method + Open/Closed Principle

Factory Method nằm ở PaymentStrategyFactory. Method create là nơi duy nhất quyết định strategy nào phục vụ request, client chỉ biết interface. Spring tự inject danh sách mọi strategy vào constructor. Vì vậy muốn thêm MoMo, em chỉ cần viết thêm một class mới, không phải sửa factory hay checkout. Đây chính là nguyên lý đóng với sửa đổi, mở để mở rộng.

### [04:30.9 → 04:54.2] #13 · 6. Thông báo — Observer + CompletableFuture + ExecutorService

Phần sáu là thông báo trạng thái đơn hàng, dùng Observer Pattern. OrderEventPublisher là subject: nó chỉ nhận sự kiện sau khi transaction đã commit. Với mỗi observer, em gọi CompletableFuture run Async trên một ExecutorService riêng gồm hai thread. Nếu một kênh bị lỗi, exceptionally sẽ cô lập lỗi đó và các kênh khác vẫn chạy, còn request đặt hàng không phải chờ.

### [04:54.2 → 05:08.8] #14 · Các observer

Hiện có hai observer: In App lưu thông báo vào database để khách đọc qua API notifications, còn Email ghi log giả lập việc gửi mail. Muốn thêm SMS hay Zalo thì chỉ cần thêm một class implements OrderStatusObserver.

### [05:08.8 → 05:33.4] #15 · SOLID trong project

Tóm lại phần SOLID. S: mỗi service và mỗi class hỗ trợ chỉ có một lý do để thay đổi. O: thêm phương thức thanh toán hay kênh thông báo mà không sửa code cũ. L: các strategy và observer thay thế nhau được. I: interface nhỏ, tách theo vai trò. D: service phụ thuộc vào abstraction và được inject qua constructor, nên unit test mock rất dễ.

## KIỂM THỬ

### [05:33.4 → 06:00.5] #16 · Unit test giỏ hàng — JUnit 5 + Mockito

Chuyển sang kiểm thử. Đây là unit test cho giỏ hàng với JUnit 5 và Mockito. Repository được mock bằng annotation Mock. Ví dụ test thêm cùng một khóa hai lần: số lượng phải cộng dồn thành ba, tổng tiền đúng, và verify rằng không tạo thêm giỏ mới. Lớp này có mười một test, bao phủ cả các trường hợp lỗi như số lượng nhỏ hơn một hay xóa mục không có trong giỏ.

### [06:00.5 → 06:22.0] #17 · Integration test luồng đặt hàng — PostgreSQL thật (Testcontainers)

Integration test chạy trên PostgreSQL và RabbitMQ thật, do Testcontainers khởi động bằng Docker. Test này cố tình cho bước mở quyền học ném lỗi giữa chừng checkout, rồi kiểm tra ba điều: số đơn hàng không tăng, giỏ hàng vẫn còn sản phẩm, và không có thông báo nào được gửi. Như vậy annotation Transactional đã rollback đúng.

### [06:22.0 → 06:40.4] #18 · Kết quả kiểm thử

Đây là kết quả chạy Maven: 41 test, không có lỗi. Gồm unit test cho các service giỏ hàng, đặt hàng, quản trị đơn, factory thanh toán, bộ tính giá và publisher, cùng các integration test. Bộ test này cũng chạy tự động trên GitHub Actions trước mỗi lần deploy.

## DEMO LOCAL · Postman

### [06:40.4 → 06:58.8] #19 · Khởi động môi trường local

Bây giờ em demo trên môi trường local. Em khởi động PostgreSQL và RabbitMQ bằng Docker Compose, rồi chạy bốn service Spring Boot. Khi learning service khởi động, Flyway tự migrate schema lên phiên bản năm. Trong Postman, em import collection và environment LOCAL có sẵn trong thư mục demo.

### [06:58.8 → 07:12.1] #20 · 1.2  Danh sách sản phẩm (public)

Đầu tiên là API danh sách sản phẩm, API này công khai, không cần token. Mỗi khóa học có id, tên, mô tả và giá. Lưu ý response công khai không trả về link video.

### [07:12.1 → 07:23.6] #21 · 1.3  Tìm kiếm theo tên - dùng index

Tìm kiếm theo tên với từ khóa java. Câu truy vấn này dùng index trigram em vừa trình bày, và trả về hai khóa học có chữ Java trong tên.

### [07:23.6 → 07:34.1] #22 · 2.1  Đăng ký CUSTOMER

Đăng ký một tài khoản khách hàng mới. Kết quả là 201 Created, kèm access token và refresh token. Tài khoản mới mặc định có role CUSTOMER.

### [07:34.1 → 07:43.6] #23 · 2.2  Đăng nhập CUSTOMER - nhận JWT

Đăng nhập bằng tài khoản vừa tạo. Script test của Postman tự lưu access token vào biến accessToken để dùng cho các request sau.

### [07:43.6 → 07:52.4] #24 · 2.3  CUSTOMER gọi API ADMIN -> 403

Thử phân quyền: dùng token CUSTOMER gọi API tạo sản phẩm của ADMIN. Spring Security chặn lại và trả về 403 Forbidden.

### [07:52.4 → 07:58.1] #25 · 2.4  Đăng nhập ADMIN

Đăng nhập bằng tài khoản ADMIN. Token này được lưu vào biến adminToken.

### [07:58.1 → 08:11.0] #26 · 3.1  ADMIN tạo sản phẩm mới

Với quyền ADMIN, em tạo một sản phẩm mới là khóa Java Design Patterns, giá 24 đô 99. Kết quả 201 Created. API admin có trả về link YouTube và trạng thái đang bán.

### [08:11.0 → 08:18.8] #27 · 3.2  ADMIN cập nhật giá

Cập nhật giá sản phẩm xuống 19 đô 99. Slug được giữ cố định và không cho phép đổi.

### [08:18.8 → 08:27.9] #28 · 4.1  Thêm Middle Java vào giỏ

Sang phần giỏ hàng. CUSTOMER thêm khóa Middle Java với số lượng một. Giỏ được tạo tự động, tổng tiền 29 đô 99.

### [08:27.9 → 08:36.0] #29 · 4.2  Thêm khóa thứ hai vào giỏ

Thêm tiếp khóa thứ hai là khóa vừa tạo. Giỏ hàng có hai mục và tổng tiền được tính lại.

### [08:36.0 → 08:46.2] #30 · 4.3  Cập nhật số lượng = 2

Cập nhật số lượng của Middle Java lên hai. Thành tiền của dòng này là 59 đô 98, và tổng giỏ được tính lại bởi CartPricingCalculator.

### [08:46.2 → 08:51.9] #31 · 4.4  Xóa sản phẩm khỏi giỏ

Xóa sản phẩm thứ hai khỏi giỏ. API trả về 204 No Content.

### [08:51.9 → 08:58.6] #32 · 4.5  Xem giỏ hàng

Xem lại giỏ hàng: chỉ còn Middle Java với số lượng hai, tổng 59 đô 98.

### [08:58.6 → 09:09.8] #33 · 5.1  Phương thức thanh toán (Factory)

API này liệt kê các phương thức thanh toán. Danh sách lấy trực tiếp từ factory, nên khi thêm một strategy mới nó sẽ tự xuất hiện ở đây.

### [09:09.8 → 09:17.6] #34 · 5.2  Chưa mua -> video 403

Trước khi mua, khách gọi API xem video của khóa Middle Java thì bị từ chối với lỗi 403.

### [09:17.6 → 09:34.0] #35 · 5.3  Đặt hàng VNPAY (giả lập)

Đặt hàng bằng VNPay giả lập. Đơn được tạo với trạng thái thanh toán PAID, có mã giao dịch VNP do strategy sinh ra, và trạng thái đơn là CONFIRMED. Giá và tên khóa học được chụp lại vào đơn tại thời điểm mua.

### [09:34.0 → 09:41.8] #36 · 5.5  Đã mua -> video 200

Sau khi thanh toán, gọi lại API video thì thành công: trả về link YouTube dạng embed để học.

### [09:41.8 → 09:52.3] #37 · 5.6  Thông báo đơn hàng (Observer)

Kiểm tra thông báo: observer In App đã lưu thông báo đơn hàng được tạo. Thông báo này được ghi bất đồng bộ, sau khi transaction commit.

### [09:52.3 → 10:05.9] #38 · Log: observer chạy trên thread riêng

Nhìn vào log để thấy CompletableFuture hoạt động. Request đặt hàng chạy trên thread của Tomcat là nio exec, còn hai observer Email và In App chạy song song trên hai thread order notify của ExecutorService.

### [10:05.9 → 10:15.4] #39 · 6.2  Đặt hàng COD

Tiếp theo em thử COD. Thêm khóa Java Fundamentals rồi đặt hàng bằng COD. Lần này đơn ở trạng thái PENDING và AWAITING PAYMENT.

### [10:15.4 → 10:21.8] #40 · 6.3  COD chưa trả tiền -> video 403

Vì COD chưa trả tiền nên khách vẫn chưa xem được video, kết quả 403.

### [10:21.8 → 10:32.3] #41 · 6.5  ADMIN xác nhận thanh toán COD

ADMIN xác nhận đã nhận tiền cho đơn COD. Đơn chuyển sang PAID và CONFIRMED, quyền học được mở, và observer gửi thông báo đổi trạng thái.

### [10:32.3 → 10:40.1] #42 · 6.6  Xác nhận lần 2 -> 409

Nếu xác nhận lần thứ hai, hệ thống trả về 409 Conflict vì đơn đã được thanh toán rồi.

### [10:40.1 → 10:45.1] #43 · 6.7  Đã xác nhận -> video 200

Bây giờ khách xem được video của khóa Java Fundamentals.

### [10:45.1 → 10:54.6] #44 · 6.8  Thông báo đổi trạng thái

Danh sách thông báo có thêm thông báo xác nhận thanh toán. Mỗi lần trạng thái đơn thay đổi đều sinh một thông báo.

### [10:54.6 → 11:03.4] #45 · 7.1  Giỏ trống -> 400

Trường hợp lỗi nghiệp vụ: đặt hàng khi giỏ trống sẽ trả về 400 Bad Request với thông báo Cart is empty.

### [11:03.4 → 11:14.3] #46 · 7.2  ADMIN ngừng bán sản phẩm

Cuối cùng, ADMIN ngừng bán sản phẩm vừa tạo bằng DELETE. Khóa học biến mất khỏi catalog công khai nhưng các đơn hàng cũ vẫn giữ lịch sử.

## DEMO LOCAL · Web UI

### [11:14.3 → 11:27.9] #47 · Giao diện web — ADMIN quản lý đơn hàng

Ngoài Postman, em làm thêm một giao diện web tối giản. Khi đăng nhập bằng ADMIN, giao diện hiện màn hình quản lý đơn hàng. Các đơn đang chờ có nút xác nhận đã thanh toán.

### [11:27.9 → 11:32.9] #48 · ADMIN xác nhận thanh toán trên giao diện

Bấm xác nhận thì đơn chuyển sang PAID và CONFIRMED ngay.

### [11:32.9 → 11:39.3] #49 · ADMIN thêm sản phẩm

Ở tab quản lý sản phẩm, ADMIN nhập thông tin khóa học mới vào form.

### [11:39.3 → 11:45.0] #50 · Danh sách sản phẩm (ADMIN)

Sản phẩm mới xuất hiện trong danh sách với trạng thái đang bán.

### [11:45.0 → 11:50.0] #51 · Ngừng bán sản phẩm

ADMIN có thể ngừng bán, trạng thái đổi thành ngừng bán.

## CI/CD · AWS

### [11:50.0 → 12:09.8] #52 · CI/CD — GitHub Actions

Phần CI CD. Mỗi lần push lên GitHub, workflow chạy job verify là mvn verify với toàn bộ test. Chỉ khi test xanh thì job deploy mới chạy: build bốn Docker image, đẩy lên Amazon ECR, rồi qua Systems Manager ra lệnh cho EC2 kéo image mới và khởi động lại bằng Docker Compose.

### [12:09.8 → 12:18.6] #53 · Pipeline chạy thành công

Đây là một lần chạy pipeline thành công trên nhánh main: job verify xong thì job deploy chạy tiếp, trạng thái Success.

### [12:18.6 → 12:36.7] #54 · Hạ tầng AWS (CloudFormation stack study-coach)

Hạ tầng AWS được tạo bằng CloudFormation. Người dùng truy cập qua CloudFront, CloudFront chuyển vào EC2 chạy các container. Database là RDS PostgreSQL. Các bí mật như JWT secret, mật khẩu database và tài khoản admin được lưu trong Parameter Store dạng SecureString, không nằm trong source code.

## DEMO AWS CLOUD · API

### [12:36.7 → 12:42.0] #55 · 1.1  Health check qua API Gateway

Giờ em demo trên AWS. Health check qua CloudFront trả về UP.

## DEMO AWS CLOUD · Web UI

### [12:42.0 → 12:56.3] #56 · Truy cập từ bất cứ đâu qua CloudFront

Giao diện web được deploy cùng API Gateway nên truy cập được từ bất cứ đâu qua địa chỉ CloudFront. Màn hình đầu là đăng nhập và đăng ký, góc trên hiển thị hệ thống đang hoạt động.

### [12:56.3 → 13:05.1] #57 · CUSTOMER đăng ký và vào cửa hàng

Em đăng ký một khách hàng mới. Role CUSTOMER hiện ở góc phải, và trang cửa hàng hiển thị các khóa học.

### [13:05.1 → 13:10.4] #58 · Tìm kiếm sản phẩm

Tìm kiếm với từ khóa java, gọi đúng API search dùng index.

### [13:10.4 → 13:17.8] #59 · Giỏ hàng: thêm và cập nhật số lượng

Thêm hai khóa vào giỏ và đổi số lượng Middle Java thành hai. Tổng tiền được tính lại.

### [13:17.8 → 13:22.8] #60 · Xóa sản phẩm khỏi giỏ

Xóa một khóa khỏi giỏ hàng.

### [13:22.8 → 13:29.9] #61 · Đặt hàng với VNPay giả lập

Chọn VNPay và đặt hàng. Thông báo đặt hàng thành công hiện ra với trạng thái PAID.

### [13:29.9 → 13:36.3] #62 · Đơn hàng đã thanh toán

Ở tab đơn hàng, đơn có trạng thái PAID, CONFIRMED và mã giao dịch VNPay.

### [13:36.3 → 13:41.3] #63 · Mở khóa video sau khi thanh toán

Bấm xem video thì khóa học được mở, phát từ YouTube.

### [13:41.3 → 13:46.3] #64 · Thông báo trạng thái đơn hàng

Tab thông báo hiển thị thông báo do observer tạo ra.

### [13:46.3 → 13:56.1] #65 · Đơn COD chờ ADMIN xác nhận

Đặt thêm một đơn COD: đơn ở trạng thái chờ thanh toán. ADMIN xác nhận đơn này giống như phần em vừa demo ở local.

## DEMO AWS CLOUD · API

### [13:56.1 → 14:04.5] #66 · 5.3  Đặt hàng VNPAY (giả lập)

Cùng luồng đó gọi bằng Postman với environment AWS: đặt hàng VNPay trên cloud trả về PAID và mã giao dịch.

## TỔNG KẾT

### [14:04.5 → 14:27.8] #67 · Tổng kết

Tóm lại, em đã hoàn thành đủ các yêu cầu của đề bài, áp dụng ba design pattern Strategy, Factory Method và Observer cùng năm nguyên lý SOLID, có unit test và integration test chạy tự động trong CI, và hệ thống đã được deploy lên AWS để truy cập từ bất cứ đâu. Em cảm ơn thầy và các bạn đã theo dõi.

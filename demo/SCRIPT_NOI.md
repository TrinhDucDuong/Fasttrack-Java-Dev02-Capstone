# Script nói — video demo "Middle Java Store"

Script này khớp với `demo/video/MiddleJavaStore_Demo_NgocHuyen.mp4`: video dài **08:43.8** (1920×1080, 30 fps, 63 khung), đã ghép sẵn giọng đọc Vbee **HN - Ngọc Huyền**.

- Mỗi khung có mốc **bắt đầu → kết thúc** và hai dòng: **Lời nói** (nội dung hiển thị cho người đọc) và **Đọc cho TTS** (thuật ngữ đã phiên âm theo `demo/video_tools/phien_am.csv`, chính là văn bản đã gửi Vbee).
- Muốn tự thu giọng thay giọng Vbee: đọc phần **Lời nói** đúng khoảng thời gian, rồi thay track tiếng:

```bash
ffmpeg -i demo/video/MiddleJavaStore_Demo_NgocHuyen.mp4 -i giong_noi.m4a -map 0:v -map 1:a -c:v copy -c:a aac -shortest demo/video/MiddleJavaStore_Demo_final.mp4
```

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

**Lời nói:** Xin chào thầy và các bạn, em là Trịnh Đức Dương. Đây là demo bài OJT E-Commerce Mini của em: Middle Java Store, cửa hàng bán khóa học Java. Phần chính bám sát sáu yêu cầu của đề, demo trên local với cả hai vai trò ADMIN và CUSTOMER. Phần mở rộng ở cuối là CI CD và triển khai lên AWS.

**Đọc cho TTS:** Xin chào thầy và các bạn, em là Trịnh Đức Dương. Đây là đề mô bài âu di ti e com mợc mi ni của em: mít-đồ gia-va xờ-to, cửa hàng bán khóa học gia-va. Phần chính bám sát sáu yêu cầu của đề, đề mô trên lô cồ với cả hai vai trò át-min và cớt từm mờ. Phần mở rộng ở cuối là xi ai xi đi và triển khai lên a đáp liu ét.

### [00:21.0 → 00:29.9] #02 · Đề bài → chức năng đã làm

**Lời nói:** Bảng này đối chiếu đề bài với phần em đã làm: xác thực, sản phẩm, giỏ hàng, đặt hàng, thanh toán, thông báo và kiểm thử.

**Đọc cho TTS:** Bảng này đối chiếu đề bài với phần em đã làm: xác thực, sản phẩm, giỏ hàng, đặt hàng, thanh toán, thông báo và kiểm thử.

### [00:29.9 → 00:42.2] #03 · Kiến trúc

**Lời nói:** Request đi qua API Gateway, gateway tìm service qua Eureka. Toàn bộ nghiệp vụ nằm trong learning service, dữ liệu lưu ở PostgreSQL, schema quản lý bằng Flyway.

**Đọc cho TTS:** ri-quét đi qua ây-pi-ai ghết-guây, ghết-guây tìm sơ vít qua êu rê ca. Toàn bộ nghiệp vụ nằm trong lơn-ninh xơ-vít, dữ liệu lưu ở pót gờ ry, xờ-ki-ma quản lý bằng phờ-lai-guây.

## CODE · ÁP DỤNG KIẾN THỨC

### [00:42.2 → 00:54.3] #04 · 1. Authentication — Spring Security + JWT

**Lời nói:** Phần một, xác thực. Spring Security đọc claim roles trong JWT để phân quyền. Mọi đường dẫn admin chỉ dành cho ADMIN; CUSTOMER gọi vào sẽ nhận lỗi 403.

**Đọc cho TTS:** Phần một, xác thực. xờ-pring xi-kiu-ri-ti đọc cờ-lêm rôn trong di, đáp-liu, ti để phân quyền. Mọi đường dẫn át-min chỉ dành cho át-min; cớt từm mờ gọi vào sẽ nhận lỗi 403.

### [00:54.3 → 01:03.6] #05 · Phát hành JWT khi đăng nhập

**Lời nói:** Khi đăng nhập, service phát hành access token mười lăm phút, chứa id, email và role. Tài khoản đăng ký mới luôn là CUSTOMER.

**Đọc cho TTS:** Khi đăng nhập, sơ vít phát hành ách sét tốc cừn mười lăm phút, chứa id, i-meo và rôn. Tài khoản đăng ký mới luôn là cớt từm mờ.

### [01:03.6 → 01:16.8] #06 · 2. Quản lý sản phẩm — JPA/Hibernate + Index

**Lời nói:** Phần hai, sản phẩm. Em tạo index GIN trên upper của title. Method Spring Data JPA bên phải sinh câu like khớp đúng biểu thức của index, nên tìm kiếm không phải quét toàn bảng.

**Đọc cho TTS:** Phần hai, sản phẩm. Em tạo in đếch gin trên ắp-pơ của tai-tồ. me thót xờ-pring đây-ta di pi ây bên phải sinh câu lai khớp đúng biểu thức của in đếch, nên tìm kiếm không phải quét toàn bảng.

### [01:16.8 → 01:26.0] #07 · SRP — quản trị sản phẩm tách khỏi catalog công khai

**Lời nói:** Theo SRP, phần quản trị sản phẩm của ADMIN là một service riêng. Xóa sản phẩm là ngừng bán, đơn hàng cũ vẫn giữ lịch sử.

**Đọc cho TTS:** Theo ét a pi, phần quản trị sản phẩm của át-min là một sơ vít riêng. Xóa sản phẩm là ngừng bán, đơn hàng cũ vẫn giữ lịch sử.

### [01:26.0 → 01:37.9] #08 · 3. Giỏ hàng — Extract Method

**Lời nói:** Phần ba, giỏ hàng với thêm, sửa số lượng và xóa. Em áp dụng Extract Method: method public chỉ vài dòng, các bước nhỏ tách thành method private có tên rõ ràng.

**Đọc cho TTS:** Phần ba, giỏ hàng với thêm, sửa số lượng và xóa. Em áp dụng ích trách me thốt: me thót pắp-lích chỉ vài dòng, các bước nhỏ tách thành me thót pờ rai-vết có tên rõ ràng.

### [01:37.9 → 01:48.4] #09 · Extract Class — CartPricingCalculator

**Lời nói:** Và Extract Class: phần tính tiền bị lặp ở giỏ hàng và checkout được tách thành CartPricingCalculator, bên trong dùng Stream API.

**Đọc cho TTS:** Và ích trách cờ lát: phần tính tiền bị lặp ở giỏ hàng và chếch ao được tách thành cạt pờ rai xinh côn kiu lây tơ, bên trong dùng xờ-trim ây-pi-ai.

### [01:48.4 → 02:01.6] #10 · 4. Đặt hàng — @Transactional + Optional + Stream

**Lời nói:** Phần bốn, đặt hàng. Checkout chạy trong một transaction, dùng Optional để lấy giỏ và chặn giỏ rỗng. Lưu đơn, mở quyền học, làm trống giỏ và phát sự kiện. Bước nào lỗi thì tất cả rollback.

**Đọc cho TTS:** Phần bốn, đặt hàng. chếch ao chạy trong một chan séc xừn, dùng óp-sừn-nồ để lấy giỏ và chặn giỏ rỗng. Lưu đơn, mở quyền học, làm trống giỏ và phát sự kiện. Bước nào lỗi thì tất cả rôn-bách.

### [02:01.6 → 02:19.0] #11 · 5. Thanh toán — Factory Method (GoF)

**Lời nói:** Phần năm, thanh toán. Lớp trừu tượng PaymentProcessor là creator, có factory method createStrategy. Method process chạy quy trình chung nhưng không biết strategy cụ thể; mỗi lớp con như VNPay hay COD tự quyết định tạo strategy nào.

**Đọc cho TTS:** Phần năm, thanh toán. Lớp trừu tượng pây-mừn pờ rô-xét-xơ là cờ rít tờ, có phách-to-ri me thót cờ rít xờ chết gi. me thót pờ rô-xét chạy quy trình chung nhưng không biết sờ chết gi cụ thể; mỗi lớp con như vi-en-pay hay cốt tự quyết định tạo sờ chết gi nào.

### [02:19.0 → 02:31.8] #12 · Strategy Pattern

**Lời nói:** Sản phẩm của factory là Strategy. VNPay giả lập trả PAID kèm mã giao dịch; COD và chuyển khoản trả PENDING chờ ADMIN xác nhận. Checkout không có if else theo loại thanh toán.

**Đọc cho TTS:** Sản phẩm của phách to ri là sờ chết gi. vi-en-pay giả lập trả pết kèm mã giao dịch; cốt và chuyển khoản trả pen-đinh chờ át-min xác nhận. chếch ao không có íp eo theo loại thanh toán.

### [02:31.8 → 02:44.7] #13 · Open/Closed Principle

**Lời nói:** Spring tự đưa mọi processor vào registry. Muốn thêm MoMo chỉ cần thêm một lớp mới, không sửa registry hay checkout; test bên phải chứng minh điều đó. Đây là nguyên lý đóng mở.

**Đọc cho TTS:** xờ-pring tự đưa mọi pờ rô-xét-xơ vào re git tri. Muốn thêm mô-mô chỉ cần thêm một lớp mới, không sửa re git tri hay chếch ao; tét bên phải chứng minh điều đó. Đây là nguyên lý đóng mở.

### [02:44.7 → 02:59.3] #14 · 6. Thông báo — Observer + CompletableFuture + ExecutorService

**Lời nói:** Phần sáu, thông báo, dùng Observer. Publisher chỉ chạy sau khi transaction commit, gửi cho từng observer bằng CompletableFuture trên ExecutorService riêng. Một kênh lỗi không ảnh hưởng kênh khác.

**Đọc cho TTS:** Phần sáu, thông báo, dùng ốp-sơ-vờ. pắp-lít-sơ chỉ chạy sau khi chan séc xừn com-mít, gửi cho từng ốp-sơ-vờ bằng com-pờ lít-tơ-bồ phiu-chờ trên ích-xơ-kiu-tờ xơ-vít riêng. Một kênh lỗi không ảnh hưởng kênh khác.

### [02:59.3 → 03:07.6] #15 · Các observer

**Lời nói:** Có hai observer: In App lưu thông báo cho khách đọc, Email ghi log giả lập. Thêm kênh mới chỉ cần thêm một lớp.

**Đọc cho TTS:** Có hai ốp-sơ-vờ: in áp lưu thông báo cho khách đọc, i-meo ghi lốc giả lập. Thêm kênh mới chỉ cần thêm một lớp.

### [03:07.6 → 03:21.3] #16 · SOLID trong project

**Lời nói:** Tóm tắt SOLID: mỗi lớp một trách nhiệm; mở rộng thanh toán và thông báo không sửa code cũ; các lớp con thay thế nhau được; interface nhỏ theo vai trò; và service phụ thuộc abstraction nên dễ mock.

**Đọc cho TTS:** Tóm tắt xô-lít: mỗi lớp một trách nhiệm; mở rộng thanh toán và thông báo không sửa cốt cũ; các lớp con thay thế nhau được; in-tơ-phây nhỏ theo vai trò; và sơ vít phụ thuộc áp chách sừn nên dễ móc.

### [03:21.3 → 03:36.4] #17 · Design Pattern và lý do chọn

**Lời nói:** Ba design pattern và lý do chọn. Factory Method để việc tạo strategy nằm ở lớp con. Strategy để mỗi cách thanh toán là một thuật toán riêng. Observer để tách đơn hàng khỏi các kênh thông báo và gửi bất đồng bộ.

**Đọc cho TTS:** Ba đi-dai pát-từn và lý do chọn. phách-to-ri me thót để việc tạo sờ chết gi nằm ở lớp con. sờ chết gi để mỗi cách thanh toán là một thuật toán riêng. ốp-sơ-vờ để tách đơn hàng khỏi các kênh thông báo và gửi bất đồng bộ.

## KIỂM THỬ

### [03:36.4 → 03:48.1] #18 · Unit test giỏ hàng — JUnit 5 + Mockito

**Lời nói:** Phần kiểm thử. Unit test giỏ hàng dùng JUnit 5 và Mockito, repository được mock. Ví dụ thêm cùng khóa hai lần thì số lượng cộng dồn và không tạo giỏ mới.

**Đọc cho TTS:** Phần kiểm thử. iu-nít tét giỏ hàng dùng di-iu-nít năm và móc-ki-tô, rì-pô-si-tơ-ri được móc. Ví dụ thêm cùng khóa hai lần thì số lượng cộng dồn và không tạo giỏ mới.

### [03:48.1 → 04:01.5] #19 · Integration test luồng đặt hàng — PostgreSQL thật

**Lời nói:** Integration test chạy trên PostgreSQL thật bằng Testcontainers. Em cố tình cho một bước lỗi giữa checkout: không có đơn mới, giỏ còn nguyên, không có thông báo. Transaction đã rollback đúng.

**Đọc cho TTS:** in-tơ-grây-sừn tét chạy trên pót gờ ry thật bằng tét con-tên-nơ. Em cố tình cho một bước lỗi giữa chếch ao: không có đơn mới, giỏ còn nguyên, không có thông báo. chan séc xừn đã rôn-bách đúng.

### [04:01.5 → 04:07.5] #20 · Kết quả kiểm thử

**Lời nói:** Kết quả: 56 test đều pass, gồm unit test các service và integration test.

**Đọc cho TTS:** Kết quả: 56 tét đều pát, gồm iu-nít tét các sơ vít và in-tơ-grây-sừn tét.

## DEMO LOCAL · Postman

### [04:07.5 → 04:15.8] #21 · Khởi động môi trường local

**Lời nói:** Giờ demo local. Em chạy hạ tầng bằng Docker Compose và bốn service, rồi dùng Postman với environment LOCAL.

**Đọc cho TTS:** Giờ đề mô lô cồ. Em chạy hạ tầng bằng đốc-cơ còm-pâu và bốn sơ vít, rồi dùng pốt-men với en vai ron mừn lô cồ.

### [04:15.8 → 04:21.0] #22 · 1.2  Danh sách sản phẩm (public)

**Lời nói:** Danh sách sản phẩm công khai: có giá, không lộ link video.

**Đọc cho TTS:** Danh sách sản phẩm công khai: có giá, không lộ linh vi-đi-ô.

### [04:21.0 → 04:27.1] #23 · 1.3  Tìm kiếm theo tên - dùng index

**Lời nói:** Tìm kiếm sản phẩm theo tên, API công khai, dùng index vừa trình bày.

**Đọc cho TTS:** Tìm kiếm sản phẩm theo tên, ây-pi-ai công khai, dùng in đếch vừa trình bày.

### [04:27.1 → 04:33.9] #24 · 2.1  Đăng ký CUSTOMER

**Lời nói:** Đăng ký tài khoản mới: kết quả 201, nhận access token. Tài khoản này là CUSTOMER.

**Đọc cho TTS:** Đăng ký tài khoản mới: kết quả 201, nhận ách sét tốc cừn. Tài khoản này là cớt từm mờ.

### [04:33.9 → 04:39.4] #25 · 2.2  Đăng nhập CUSTOMER - nhận JWT

**Lời nói:** Đăng nhập CUSTOMER, Postman tự lưu token cho các request sau.

**Đọc cho TTS:** Đăng nhập cớt từm mờ, pốt-men tự lưu tốc cừn cho các ri-quét sau.

### [04:39.4 → 04:45.1] #26 · 2.3  CUSTOMER gọi API ADMIN -> 403

**Lời nói:** CUSTOMER gọi API tạo sản phẩm của ADMIN bị chặn với lỗi 403.

**Đọc cho TTS:** cớt từm mờ gọi ây-pi-ai tạo sản phẩm của át-min bị chặn với lỗi 403.

### [04:45.1 → 04:51.3] #27 · 2.4  Đăng nhập ADMIN

**Lời nói:** Đăng nhập tài khoản ADMIN có sẵn, token được lưu vào biến adminToken.

**Đọc cho TTS:** Đăng nhập tài khoản át-min có sẵn, tốc cừn được lưu vào biến át-min tốc cừn.

### [04:51.3 → 04:55.8] #28 · 3.1  ADMIN tạo sản phẩm mới

**Lời nói:** ADMIN tạo sản phẩm mới, kết quả 201.

**Đọc cho TTS:** át-min tạo sản phẩm mới, kết quả 201.

### [04:55.8 → 05:00.3] #29 · 3.2  ADMIN cập nhật giá

**Lời nói:** ADMIN cập nhật giá sản phẩm.

**Đọc cho TTS:** át-min cập nhật giá sản phẩm.

### [05:00.3 → 05:05.2] #30 · 4.1  Thêm Middle Java vào giỏ

**Lời nói:** Sang vai trò CUSTOMER: thêm khóa Middle Java vào giỏ.

**Đọc cho TTS:** Sang vai trò cớt từm mờ: thêm khóa mít-đồ da-va vào giỏ.

### [05:05.2 → 05:09.8] #31 · 4.3  Cập nhật số lượng = 2

**Lời nói:** Cập nhật số lượng lên hai, tổng tiền được tính lại.

**Đọc cho TTS:** Cập nhật số lượng lên hai, tổng tiền được tính lại.

### [05:09.8 → 05:14.3] #32 · 4.4  Xóa sản phẩm khỏi giỏ

**Lời nói:** Xóa một sản phẩm khỏi giỏ, trả về 204.

**Đọc cho TTS:** Xóa một sản phẩm khỏi giỏ, trả về 204.

### [05:14.3 → 05:18.8] #33 · 4.5  Xem giỏ hàng

**Lời nói:** Xem lại giỏ: một sản phẩm, số lượng hai.

**Đọc cho TTS:** Xem lại giỏ: một sản phẩm, số lượng hai.

### [05:18.8 → 05:23.4] #34 · 5.1  Phương thức thanh toán (Factory)

**Lời nói:** Danh sách phương thức thanh toán lấy từ registry.

**Đọc cho TTS:** Danh sách phương thức thanh toán lấy từ re git tri.

### [05:23.4 → 05:27.9] #35 · 5.2  Chưa mua -> video 403

**Lời nói:** Chưa mua thì xem video bị 403.

**Đọc cho TTS:** Chưa mua thì xem vi-đi-ô bị 403.

### [05:27.9 → 05:37.0] #36 · 5.3  Đặt hàng VNPAY (giả lập)

**Lời nói:** Đặt hàng bằng VNPay giả lập: đơn PAID, có mã giao dịch, trạng thái CONFIRMED. Giỏ được làm trống trong cùng transaction.

**Đọc cho TTS:** Đặt hàng bằng vi-en-pay giả lập: đơn pết, có mã giao dịch, trạng thái còn-phơm. Giỏ được làm trống trong cùng chan séc xừn.

### [05:37.0 → 05:41.5] #37 · 5.5  Đã mua -> video 200

**Lời nói:** Đã mua thì xem được video.

**Đọc cho TTS:** Đã mua thì xem được vi-đi-ô.

### [05:41.5 → 05:46.0] #38 · 5.6  Thông báo đơn hàng (Observer)

**Lời nói:** Thông báo đơn hàng do observer tạo ra.

**Đọc cho TTS:** Thông báo đơn hàng do ốp-sơ-vờ tạo ra.

### [05:46.0 → 05:54.3] #39 · Log: observer chạy trên thread riêng

**Lời nói:** Trong log, request chạy trên thread của Tomcat, còn hai observer chạy song song trên thread order notify.

**Đọc cho TTS:** Trong lốc, ri-quét chạy trên thờ-rét của tom-cát, còn hai ốp-sơ-vờ chạy song song trên thờ-rét o-đờ no-ti-phai.

### [05:54.3 → 05:58.8] #40 · 6.2  Đặt hàng COD

**Lời nói:** Đặt hàng COD: đơn ở trạng thái chờ thanh toán.

**Đọc cho TTS:** Đặt hàng cốt: đơn ở trạng thái chờ thanh toán.

### [05:58.8 → 06:03.3] #41 · 6.3  COD chưa trả tiền -> video 403

**Lời nói:** COD chưa trả tiền nên video vẫn bị 403.

**Đọc cho TTS:** cốt chưa trả tiền nên vi-đi-ô vẫn bị 403.

### [06:03.3 → 06:09.3] #42 · 6.5  ADMIN xác nhận thanh toán COD

**Lời nói:** Vai trò ADMIN xác nhận đã nhận tiền: đơn chuyển sang PAID và mở quyền học.

**Đọc cho TTS:** Vai trò át-min xác nhận đã nhận tiền: đơn chuyển sang pết và mở quyền học.

### [06:09.3 → 06:13.8] #43 · 6.6  Xác nhận lần 2 -> 409

**Lời nói:** Xác nhận lần hai bị từ chối với 409.

**Đọc cho TTS:** Xác nhận lần hai bị từ chối với 409.

### [06:13.8 → 06:18.3] #44 · 6.8  Thông báo đổi trạng thái

**Lời nói:** Khách nhận thêm thông báo đơn đã được xác nhận.

**Đọc cho TTS:** Khách nhận thêm thông báo đơn đã được xác nhận.

### [06:18.3 → 06:22.8] #45 · 7.1  Giỏ trống -> 400

**Lời nói:** Đặt hàng khi giỏ trống trả 400.

**Đọc cho TTS:** Đặt hàng khi giỏ trống trả 400.

### [06:22.8 → 06:28.6] #46 · 7.2  ADMIN ngừng bán sản phẩm

**Lời nói:** ADMIN ngừng bán sản phẩm, sản phẩm biến mất khỏi catalog công khai.

**Đọc cho TTS:** át-min ngừng bán sản phẩm, sản phẩm biến mất khỏi ca-ta-lốc công khai.

## DEMO LOCAL · Web UI

### [06:28.6 → 06:35.6] #47 · CUSTOMER — giỏ hàng trên giao diện web

**Lời nói:** Cùng luồng đó trên giao diện web. CUSTOMER thêm sản phẩm và sửa số lượng ngay trong giỏ.

**Đọc cho TTS:** Cùng luồng đó trên giao diện web. cớt từm mờ thêm sản phẩm và sửa số lượng ngay trong giỏ.

### [06:35.6 → 06:40.8] #48 · CUSTOMER — đơn hàng đã thanh toán

**Lời nói:** Đặt hàng VNPay xong, đơn hiện PAID và có nút xem video.

**Đọc cho TTS:** Đặt hàng vi-en-pay xong, đơn hiện pết và có nút xem vi-đi-ô.

### [06:40.8 → 06:47.0] #49 · ADMIN — quản lý đơn hàng

**Lời nói:** Đăng nhập ADMIN, giao diện chuyển sang quản lý đơn. Đơn COD có nút xác nhận.

**Đọc cho TTS:** Đăng nhập át-min, giao diện chuyển sang quản lý đơn. Đơn cốt có nút xác nhận.

### [06:47.0 → 06:51.5] #50 · ADMIN — xác nhận thanh toán

**Lời nói:** Bấm xác nhận, đơn chuyển sang CONFIRMED.

**Đọc cho TTS:** Bấm xác nhận, đơn chuyển sang còn-phơm.

### [06:51.5 → 06:56.0] #51 · ADMIN — quản lý sản phẩm

**Lời nói:** ADMIN thêm, sửa và ngừng bán sản phẩm.

**Đọc cho TTS:** át-min thêm, sửa và ngừng bán sản phẩm.

## MỞ RỘNG · CI/CD & SECRET

### [06:56.0 → 07:05.3] #52 · Mở rộng 1 — GitHub Actions

**Lời nói:** Phần mở rộng. Mỗi lần push, GitHub Actions chạy toàn bộ test. Test xanh thì mới build image, đẩy lên Amazon ECR và deploy.

**Đọc cho TTS:** Phần mở rộng. Mỗi lần pút, gít-hắp ác-sừn chạy toàn bộ tét. tét xanh thì mới biu i-mịch, đẩy lên a-ma-dôn i xi a và đi pờ loi.

### [07:05.3 → 07:18.3] #53 · Lưu secret thế nào

**Lời nói:** Về secret: mật khẩu khai báo trong GitHub Secrets, pipeline chuyển vào Parameter Store dạng mã hóa, EC2 đọc bằng IAM role khi deploy. Không có mật khẩu nào trong code.

**Đọc cho TTS:** Về xíc-cờ rít: mật khẩu khai báo trong gít-hắp xi-cờ rít, pai-lai chuyển vào pa-ra-mét-tơ xờ-to dạng mã hóa, i xi tu đọc bằng ai ây em rôn khi đi pờ loi. Không có mật khẩu nào trong cốt.

### [07:18.3 → 07:24.3] #54 · Pipeline chạy thành công

**Lời nói:** Đây là một lần pipeline chạy thành công: verify xong rồi mới deploy.

**Đọc cho TTS:** Đây là một lần pai-lai chạy thành công: ve-ri-phai xong rồi mới đi pờ loi.

## MỞ RỘNG · TRIỂN KHAI AWS

### [07:24.3 → 07:37.1] #55 · Mở rộng 2 — Hạ tầng AWS

**Lời nói:** Hạ tầng AWS tạo bằng CloudFormation: CloudFront làm cổng HTTPS, EC2 chạy container, RDS lưu dữ liệu, ECR lưu image, S3 lưu video khóa học.

**Đọc cho TTS:** Hạ tầng a đáp liu ét tạo bằng cờ-lao phom-mây-sừn: cờ-lao phờ-ron làm cổng ết ti ti pi ét, i xi tu chạy còn-tên-nơ, a đi ét lưu dữ liệu, i xi a lưu i-mịch, ét ba lưu vi-đi-ô khóa học.

### [07:37.1 → 07:48.7] #56 · Lưu video trên S3 — presigned URL

**Lời nói:** Video không lưu trong database. ADMIN nhận một link upload có chữ ký, file đi thẳng lên S3, sau đó API kiểm tra file tồn tại rồi mới gắn vào khóa học.

**Đọc cho TTS:** vi-đi-ô không lưu trong đa ta bây. át-min nhận một linh ắp loát có chữ ký, phai đi thẳng lên ét ba, sau đó ây-pi-ai kiểm tra phai tồn tại rồi mới gắn vào khóa học.

## MỞ RỘNG · DEMO UI TRÊN AWS

### [07:48.7 → 07:57.5] #57 · ADMIN trên AWS — xác nhận đơn COD

**Lời nói:** Giờ demo trên AWS qua CloudFront. ADMIN đăng nhập, thấy đơn COD đang chờ và bấm xác nhận đã thanh toán.

**Đọc cho TTS:** Giờ đề mô trên a đáp liu ét qua cờ-lao phờ-ron. át-min đăng nhập, thấy đơn cốt đang chờ và bấm xác nhận đã thanh toán.

### [07:57.5 → 08:05.8] #58 · ADMIN trên AWS — tải video bài học lên S3

**Lời nói:** ADMIN tải video bài học cho khóa Middle Java. File đi thẳng lên S3, cột video chuyển thành Storage.

**Đọc cho TTS:** át-min tải vi-đi-ô bài học cho khóa mít-đồ da-va. phai đi thẳng lên ét ba, cột vi-đi-ô chuyển thành xờ-to-rếc.

### [08:05.8 → 08:13.1] #59 · CUSTOMER — đăng ký tài khoản mới

**Lời nói:** Một khách mới đăng ký tài khoản. Mật khẩu phải từ mười ký tự, tài khoản mới luôn là CUSTOMER.

**Đọc cho TTS:** Một khách mới đăng ký tài khoản. Mật khẩu phải từ mười ký tự, tài khoản mới luôn là cớt từm mờ.

### [08:13.1 → 08:18.6] #60 · CUSTOMER — thêm vào giỏ, chọn VNPay

**Lời nói:** Khách thêm khóa Middle Java vào giỏ và chọn thanh toán VNPay.

**Đọc cho TTS:** Khách thêm khóa mít-đồ da-va vào giỏ và chọn thanh toán vi-en-pay.

### [08:18.6 → 08:26.1] #61 · CUSTOMER — thanh toán xong, xem video từ S3

**Lời nói:** Đặt hàng thành công, đơn PAID. Khách mở video, phát từ S3 bằng link riêng chỉ có hạn một giờ.

**Đọc cho TTS:** Đặt hàng thành công, đơn pết. Khách mở vi-đi-ô, phát từ ét ba bằng linh riêng chỉ có hạn một giờ.

### [08:26.1 → 08:30.9] #62 · CUSTOMER — thông báo đơn hàng

**Lời nói:** Và khách nhận được thông báo đơn hàng do observer gửi.

**Đọc cho TTS:** Và khách nhận được thông báo đơn hàng do ốp-sơ-vờ gửi.

## TỔNG KẾT

### [08:30.9 → 08:43.8] #63 · Tổng kết

**Lời nói:** Tóm lại, em đã hoàn thành đủ yêu cầu đề bài, có test chạy tự động và triển khai lên AWS. Em cảm ơn thầy và các bạn đã theo dõi.

**Đọc cho TTS:** Tóm lại, em đã hoàn thành đủ yêu cầu đề bài, có tét chạy tự động và triển khai lên a đáp liu ét. Em cảm ơn thầy và các bạn đã theo dõi.

# Script nói — video demo "Middle Java Store"

Script này khớp với `demo/video/MiddleJavaStore_Demo_NgocHuyen.mp4`: video dài **08:46.4** (1920×1080, 30 fps, 63 khung), đã ghép sẵn giọng đọc Vbee **HN - Ngọc Huyền**.

- Mỗi khung có mốc **bắt đầu → kết thúc** và hai dòng: **Lời nói** (nội dung hiển thị cho người đọc) và **Đọc cho TTS** (thuật ngữ đã phiên âm tiếng Việt, chính là văn bản đã gửi Vbee).
- Muốn tự thu giọng thay giọng Vbee: đọc phần **Lời nói** đúng khoảng thời gian, rồi thay track tiếng:

```bash
ffmpeg -i demo/video/MiddleJavaStore_Demo_NgocHuyen.mp4 -i giong_noi.m4a -map 0:v -map 1:a -c:v copy -c:a aac -shortest demo/video/MiddleJavaStore_Demo_final.mp4
```

## Mục lục

| Phần | Bắt đầu |
|---|---|
| GIỚI THIỆU | 00:00.0 |
| CODE · ÁP DỤNG KIẾN THỨC | 00:42.3 |
| KIỂM THỬ | 03:37.3 |
| DEMO LOCAL · Postman | 04:08.3 |
| DEMO LOCAL · Web UI | 06:29.5 |
| MỞ RỘNG · CI/CD & SECRET | 06:57.2 |
| MỞ RỘNG · TRIỂN KHAI AWS | 07:24.8 |
| MỞ RỘNG · DEMO UI TRÊN AWS | 07:49.2 |
| TỔNG KẾT | 08:32.0 |

---

## GIỚI THIỆU

### [00:00.0 → 00:21.2] #01 · E-Commerce Mini — Middle Java Store

**Lời nói:** Xin chào thầy và các bạn, em là Trịnh Đức Dương. Đây là demo bài OJT E-Commerce Mini của em: Middle Java Store, cửa hàng bán khóa học Java. Phần chính bám sát sáu yêu cầu của đề, demo trên local với cả hai vai trò ADMIN và CUSTOMER. Phần mở rộng ở cuối là CI CD và triển khai lên AWS.

**Đọc cho TTS:** Xin chào thầy và các bạn, em là Trịnh Đức Dương. Đây là đê-mô bài âu di ti i com-mơ-xơ mi-ni của em: mít-đồ gia-va xờ-to, cửa hàng bán khóa học gia-va. Phần chính bám sát sáu yêu cầu của đề, đê-mô trên lâu-cồ với cả hai vai trò át-min và cát-xtơ-mơ. Phần mở rộng ở cuối là xi ai xi đi và triển khai lên ây đắp-liu ét.

### [00:21.2 → 00:30.1] #02 · Đề bài → chức năng đã làm

**Lời nói:** Bảng này đối chiếu đề bài với phần em đã làm: xác thực, sản phẩm, giỏ hàng, đặt hàng, thanh toán, thông báo và kiểm thử.

**Đọc cho TTS:** Bảng này đối chiếu đề bài với phần em đã làm: xác thực, sản phẩm, giỏ hàng, đặt hàng, thanh toán, thông báo và kiểm thử.

### [00:30.1 → 00:42.3] #03 · Kiến trúc

**Lời nói:** Request đi qua API Gateway, gateway tìm service qua Eureka. Toàn bộ nghiệp vụ nằm trong learning service, dữ liệu lưu ở PostgreSQL, schema quản lý bằng Flyway.

**Đọc cho TTS:** ri-qoét đi qua ây-pi-ai ghết-uây, ghết-uây tìm xơ-vít qua iu-ri-ca. Toàn bộ nghiệp vụ nằm trong lơ-ninh xơ-vít, dữ liệu lưu ở pốt-gờ-rét, xơ-ki-ma quản lý bằng phờ-lai-guây.

## CODE · ÁP DỤNG KIẾN THỨC

### [00:42.3 → 00:54.4] #04 · 1. Authentication — Spring Security + JWT

**Lời nói:** Phần một, xác thực. Spring Security đọc claim roles trong JWT để phân quyền. Mọi đường dẫn admin chỉ dành cho ADMIN; CUSTOMER gọi vào sẽ nhận lỗi 403.

**Đọc cho TTS:** Phần một, xác thực. xờ-pring xi-kiu-ri-ti đọc cờ-lêm rôn trong di, đắp-liu, ti để phân quyền. Mọi đường dẫn át-min chỉ dành cho át-min; cát-xtơ-mơ gọi vào sẽ nhận lỗi 403.

### [00:54.4 → 01:03.6] #05 · Phát hành JWT khi đăng nhập

**Lời nói:** Khi đăng nhập, service phát hành access token mười lăm phút, chứa id, email và role. Tài khoản đăng ký mới luôn là CUSTOMER.

**Đọc cho TTS:** Khi đăng nhập, xơ-vít phát hành ác-xét tô-kần mười lăm phút, chứa id, i-meo và rôn. Tài khoản đăng ký mới luôn là cát-xtơ-mơ.

### [01:03.6 → 01:16.7] #06 · 2. Quản lý sản phẩm — JPA/Hibernate + Index

**Lời nói:** Phần hai, sản phẩm. Em tạo index GIN trên upper của title. Method Spring Data JPA bên phải sinh câu like khớp đúng biểu thức của index, nên tìm kiếm không phải quét toàn bảng.

**Đọc cho TTS:** Phần hai, sản phẩm. Em tạo in-đéc gin trên ắp-pơ của tai-tồ. mé-thợt xờ-pring đây-ta di pi ây bên phải sinh câu lai khớp đúng biểu thức của in-đéc, nên tìm kiếm không phải quét toàn bảng.

### [01:16.7 → 01:25.9] #07 · SRP — quản trị sản phẩm tách khỏi catalog công khai

**Lời nói:** Theo SRP, phần quản trị sản phẩm của ADMIN là một service riêng. Xóa sản phẩm là ngừng bán, đơn hàng cũ vẫn giữ lịch sử.

**Đọc cho TTS:** Theo ét a pi, phần quản trị sản phẩm của át-min là một xơ-vít riêng. Xóa sản phẩm là ngừng bán, đơn hàng cũ vẫn giữ lịch sử.

### [01:25.9 → 01:37.8] #08 · 3. Giỏ hàng — Extract Method

**Lời nói:** Phần ba, giỏ hàng với thêm, sửa số lượng và xóa. Em áp dụng Extract Method: method public chỉ vài dòng, các bước nhỏ tách thành method private có tên rõ ràng.

**Đọc cho TTS:** Phần ba, giỏ hàng với thêm, sửa số lượng và xóa. Em áp dụng ích-xờ-trắc mé-thợt: mé-thợt páp-lích chỉ vài dòng, các bước nhỏ tách thành mé-thợt prai-vết có tên rõ ràng.

### [01:37.8 → 01:48.3] #09 · Extract Class — CartPricingCalculator

**Lời nói:** Và Extract Class: phần tính tiền bị lặp ở giỏ hàng và checkout được tách thành CartPricingCalculator, bên trong dùng Stream API.

**Đọc cho TTS:** Và ích-xờ-trắc cờ-lát: phần tính tiền bị lặp ở giỏ hàng và chéc-ao được tách thành cát prai-xinh can-kiu-lây-tơ, bên trong dùng xờ-trim ây-pi-ai.

### [01:48.3 → 02:01.4] #10 · 4. Đặt hàng — @Transactional + Optional + Stream

**Lời nói:** Phần bốn, đặt hàng. Checkout chạy trong một transaction, dùng Optional để lấy giỏ và chặn giỏ rỗng. Lưu đơn, mở quyền học, làm trống giỏ và phát sự kiện. Bước nào lỗi thì tất cả rollback.

**Đọc cho TTS:** Phần bốn, đặt hàng. chéc-ao chạy trong một tran-xắc-sần, dùng óp-sần-nồ để lấy giỏ và chặn giỏ rỗng. Lưu đơn, mở quyền học, làm trống giỏ và phát sự kiện. Bước nào lỗi thì tất cả rôn-béc.

### [02:01.4 → 02:19.2] #11 · 5. Thanh toán — Factory Method (GoF)

**Lời nói:** Phần năm, thanh toán. Lớp trừu tượng PaymentProcessor là creator, có factory method createStrategy. Method process chạy quy trình chung nhưng không biết strategy cụ thể; mỗi lớp con như VNPay hay COD tự quyết định tạo strategy nào.

**Đọc cho TTS:** Phần năm, thanh toán. Lớp trừu tượng pây-mần prô-xét-xơ là cri-ây-tơ, có phác-tơ-ri mé-thợt cri-ết xờ-tra-tơ-gi. mé-thợt prô-xét chạy quy trình chung nhưng không biết xờ-tra-tơ-gi cụ thể; mỗi lớp con như vi-en-pây hay xi âu đi tự quyết định tạo xờ-tra-tơ-gi nào.

### [02:19.2 → 02:32.5] #12 · Strategy Pattern

**Lời nói:** Sản phẩm của factory là Strategy. VNPay giả lập trả PAID kèm mã giao dịch; COD và chuyển khoản trả PENDING chờ ADMIN xác nhận. Checkout không có if else theo loại thanh toán.

**Đọc cho TTS:** Sản phẩm của phác-tơ-ri là xờ-tra-tơ-gi. vi-en-pây giả lập trả pết kèm mã giao dịch; xi âu đi và chuyển khoản trả pen-đinh chờ át-min xác nhận. chéc-ao không có ịp en-xơ theo loại thanh toán.

### [02:32.5 → 02:45.6] #13 · Open/Closed Principle

**Lời nói:** Spring tự đưa mọi processor vào registry. Muốn thêm MoMo chỉ cần thêm một lớp mới, không sửa registry hay checkout; test bên phải chứng minh điều đó. Đây là nguyên lý đóng mở.

**Đọc cho TTS:** xờ-pring tự đưa mọi prô-xét-xơ vào re-gít-xờ-tri. Muốn thêm mô-mô chỉ cần thêm một lớp mới, không sửa re-gít-xờ-tri hay chéc-ao; tét bên phải chứng minh điều đó. Đây là nguyên lý đóng mở.

### [02:45.6 → 02:59.9] #14 · 6. Thông báo — Observer + CompletableFuture + ExecutorService

**Lời nói:** Phần sáu, thông báo, dùng Observer. Publisher chỉ chạy sau khi transaction commit, gửi cho từng observer bằng CompletableFuture trên ExecutorService riêng. Một kênh lỗi không ảnh hưởng kênh khác.

**Đọc cho TTS:** Phần sáu, thông báo, dùng ốp-dơ-vơ. pắp-li-sơ chỉ chạy sau khi tran-xắc-sần cơ-mít, gửi cho từng ốp-dơ-vơ bằng com-pli-tơ-bồ phiu-chơ trên ích-xê-kiu-tơ xơ-vít riêng. Một kênh lỗi không ảnh hưởng kênh khác.

### [02:59.9 → 03:08.2] #15 · Các observer

**Lời nói:** Có hai observer: In App lưu thông báo cho khách đọc, Email ghi log giả lập. Thêm kênh mới chỉ cần thêm một lớp.

**Đọc cho TTS:** Có hai ốp-dơ-vơ: in áp lưu thông báo cho khách đọc, i-meo ghi lóc giả lập. Thêm kênh mới chỉ cần thêm một lớp.

### [03:08.2 → 03:22.0] #16 · SOLID trong project

**Lời nói:** Tóm tắt SOLID: mỗi lớp một trách nhiệm; mở rộng thanh toán và thông báo không sửa code cũ; các lớp con thay thế nhau được; interface nhỏ theo vai trò; và service phụ thuộc abstraction nên dễ mock.

**Đọc cho TTS:** Tóm tắt xô-lít: mỗi lớp một trách nhiệm; mở rộng thanh toán và thông báo không sửa cốt cũ; các lớp con thay thế nhau được; in-tơ-phây nhỏ theo vai trò; và xơ-vít phụ thuộc áp-xờ-trắc-sần nên dễ mốc.

### [03:22.0 → 03:37.3] #17 · Design Pattern và lý do chọn

**Lời nói:** Ba design pattern và lý do chọn. Factory Method để việc tạo strategy nằm ở lớp con. Strategy để mỗi cách thanh toán là một thuật toán riêng. Observer để tách đơn hàng khỏi các kênh thông báo và gửi bất đồng bộ.

**Đọc cho TTS:** Ba đi-dai pát-tần và lý do chọn. phác-tơ-ri mé-thợt để việc tạo xờ-tra-tơ-gi nằm ở lớp con. xờ-tra-tơ-gi để mỗi cách thanh toán là một thuật toán riêng. ốp-dơ-vơ để tách đơn hàng khỏi các kênh thông báo và gửi bất đồng bộ.

## KIỂM THỬ

### [03:37.3 → 03:49.0] #18 · Unit test giỏ hàng — JUnit 5 + Mockito

**Lời nói:** Phần kiểm thử. Unit test giỏ hàng dùng JUnit 5 và Mockito, repository được mock. Ví dụ thêm cùng khóa hai lần thì số lượng cộng dồn và không tạo giỏ mới.

**Đọc cho TTS:** Phần kiểm thử. iu-nít tét giỏ hàng dùng di-iu-nít năm và mốc-ki-tô, ri-pô-di-to-ri được mốc. Ví dụ thêm cùng khóa hai lần thì số lượng cộng dồn và không tạo giỏ mới.

### [03:49.0 → 04:02.3] #19 · Integration test luồng đặt hàng — PostgreSQL thật

**Lời nói:** Integration test chạy trên PostgreSQL thật bằng Testcontainers. Em cố tình cho một bước lỗi giữa checkout: không có đơn mới, giỏ còn nguyên, không có thông báo. Transaction đã rollback đúng.

**Đọc cho TTS:** in-tơ-grây-sần tét chạy trên pốt-gờ-rét thật bằng tét con-tê-nơ. Em cố tình cho một bước lỗi giữa chéc-ao: không có đơn mới, giỏ còn nguyên, không có thông báo. tran-xắc-sần đã rôn-béc đúng.

### [04:02.3 → 04:08.3] #20 · Kết quả kiểm thử

**Lời nói:** Kết quả: 56 test đều pass, gồm unit test các service và integration test.

**Đọc cho TTS:** Kết quả: 56 tét đều pát, gồm iu-nít tét các xơ-vít và in-tơ-grây-sần tét.

## DEMO LOCAL · Postman

### [04:08.3 → 04:16.7] #21 · Khởi động môi trường local

**Lời nói:** Giờ demo local. Em chạy hạ tầng bằng Docker Compose và bốn service, rồi dùng Postman với environment LOCAL.

**Đọc cho TTS:** Giờ đê-mô lâu-cồ. Em chạy hạ tầng bằng đốc-cơ com-pâu và bốn xơ-vít, rồi dùng pốt-men với en-vai-rần-mần lâu-cồ.

### [04:16.7 → 04:21.9] #22 · 1.2  Danh sách sản phẩm (public)

**Lời nói:** Danh sách sản phẩm công khai: có giá, không lộ link video.

**Đọc cho TTS:** Danh sách sản phẩm công khai: có giá, không lộ linh vi-đi-ô.

### [04:21.9 → 04:27.9] #23 · 1.3  Tìm kiếm theo tên - dùng index

**Lời nói:** Tìm kiếm sản phẩm theo tên, API công khai, dùng index vừa trình bày.

**Đọc cho TTS:** Tìm kiếm sản phẩm theo tên, ây-pi-ai công khai, dùng in-đéc vừa trình bày.

### [04:27.9 → 04:34.6] #24 · 2.1  Đăng ký CUSTOMER

**Lời nói:** Đăng ký tài khoản mới: kết quả 201, nhận access token. Tài khoản này là CUSTOMER.

**Đọc cho TTS:** Đăng ký tài khoản mới: kết quả 201, nhận ác-xét tô-kần. Tài khoản này là cát-xtơ-mơ.

### [04:34.6 → 04:40.0] #25 · 2.2  Đăng nhập CUSTOMER - nhận JWT

**Lời nói:** Đăng nhập CUSTOMER, Postman tự lưu token cho các request sau.

**Đọc cho TTS:** Đăng nhập cát-xtơ-mơ, pốt-men tự lưu tô-kần cho các ri-qoét sau.

### [04:40.0 → 04:45.7] #26 · 2.3  CUSTOMER gọi API ADMIN -> 403

**Lời nói:** CUSTOMER gọi API tạo sản phẩm của ADMIN bị chặn với lỗi 403.

**Đọc cho TTS:** cát-xtơ-mơ gọi ây-pi-ai tạo sản phẩm của át-min bị chặn với lỗi 403.

### [04:45.7 → 04:51.7] #27 · 2.4  Đăng nhập ADMIN

**Lời nói:** Đăng nhập tài khoản ADMIN có sẵn, token được lưu vào biến adminToken.

**Đọc cho TTS:** Đăng nhập tài khoản át-min có sẵn, tô-kần được lưu vào biến át-min tô-kần.

### [04:51.7 → 04:56.2] #28 · 3.1  ADMIN tạo sản phẩm mới

**Lời nói:** ADMIN tạo sản phẩm mới, kết quả 201.

**Đọc cho TTS:** át-min tạo sản phẩm mới, kết quả 201.

### [04:56.2 → 05:00.7] #29 · 3.2  ADMIN cập nhật giá

**Lời nói:** ADMIN cập nhật giá sản phẩm.

**Đọc cho TTS:** át-min cập nhật giá sản phẩm.

### [05:00.7 → 05:05.7] #30 · 4.1  Thêm Middle Java vào giỏ

**Lời nói:** Sang vai trò CUSTOMER: thêm khóa Middle Java vào giỏ.

**Đọc cho TTS:** Sang vai trò cát-xtơ-mơ: thêm khóa mít-đồ gia-va vào giỏ.

### [05:05.7 → 05:10.3] #31 · 4.3  Cập nhật số lượng = 2

**Lời nói:** Cập nhật số lượng lên hai, tổng tiền được tính lại.

**Đọc cho TTS:** Cập nhật số lượng lên hai, tổng tiền được tính lại.

### [05:10.3 → 05:14.8] #32 · 4.4  Xóa sản phẩm khỏi giỏ

**Lời nói:** Xóa một sản phẩm khỏi giỏ, trả về 204.

**Đọc cho TTS:** Xóa một sản phẩm khỏi giỏ, trả về 204.

### [05:14.8 → 05:19.3] #33 · 4.5  Xem giỏ hàng

**Lời nói:** Xem lại giỏ: một sản phẩm, số lượng hai.

**Đọc cho TTS:** Xem lại giỏ: một sản phẩm, số lượng hai.

### [05:19.3 → 05:24.1] #34 · 5.1  Phương thức thanh toán (Factory)

**Lời nói:** Danh sách phương thức thanh toán lấy từ registry.

**Đọc cho TTS:** Danh sách phương thức thanh toán lấy từ re-gít-xờ-tri.

### [05:24.1 → 05:28.6] #35 · 5.2  Chưa mua -> video 403

**Lời nói:** Chưa mua thì xem video bị 403.

**Đọc cho TTS:** Chưa mua thì xem vi-đi-ô bị 403.

### [05:28.6 → 05:37.7] #36 · 5.3  Đặt hàng VNPAY (giả lập)

**Lời nói:** Đặt hàng bằng VNPay giả lập: đơn PAID, có mã giao dịch, trạng thái CONFIRMED. Giỏ được làm trống trong cùng transaction.

**Đọc cho TTS:** Đặt hàng bằng vi-en-pây giả lập: đơn pết, có mã giao dịch, trạng thái con-phơm. Giỏ được làm trống trong cùng tran-xắc-sần.

### [05:37.7 → 05:42.2] #37 · 5.5  Đã mua -> video 200

**Lời nói:** Đã mua thì xem được video.

**Đọc cho TTS:** Đã mua thì xem được vi-đi-ô.

### [05:42.2 → 05:46.7] #38 · 5.6  Thông báo đơn hàng (Observer)

**Lời nói:** Thông báo đơn hàng do observer tạo ra.

**Đọc cho TTS:** Thông báo đơn hàng do ốp-dơ-vơ tạo ra.

### [05:46.7 → 05:55.1] #39 · Log: observer chạy trên thread riêng

**Lời nói:** Trong log, request chạy trên thread của Tomcat, còn hai observer chạy song song trên thread order notify.

**Đọc cho TTS:** Trong lóc, ri-qoét chạy trên thờ-rét của tom-ket, còn hai ốp-dơ-vơ chạy song song trên thờ-rét o-đơ nâu-ti-phai.

### [05:55.1 → 05:59.7] #40 · 6.2  Đặt hàng COD

**Lời nói:** Đặt hàng COD: đơn ở trạng thái chờ thanh toán.

**Đọc cho TTS:** Đặt hàng xi âu đi: đơn ở trạng thái chờ thanh toán.

### [05:59.7 → 06:04.2] #41 · 6.3  COD chưa trả tiền -> video 403

**Lời nói:** COD chưa trả tiền nên video vẫn bị 403.

**Đọc cho TTS:** xi âu đi chưa trả tiền nên vi-đi-ô vẫn bị 403.

### [06:04.2 → 06:10.2] #42 · 6.5  ADMIN xác nhận thanh toán COD

**Lời nói:** Vai trò ADMIN xác nhận đã nhận tiền: đơn chuyển sang PAID và mở quyền học.

**Đọc cho TTS:** Vai trò át-min xác nhận đã nhận tiền: đơn chuyển sang pết và mở quyền học.

### [06:10.2 → 06:14.7] #43 · 6.6  Xác nhận lần 2 -> 409

**Lời nói:** Xác nhận lần hai bị từ chối với 409.

**Đọc cho TTS:** Xác nhận lần hai bị từ chối với 409.

### [06:14.7 → 06:19.2] #44 · 6.8  Thông báo đổi trạng thái

**Lời nói:** Khách nhận thêm thông báo đơn đã được xác nhận.

**Đọc cho TTS:** Khách nhận thêm thông báo đơn đã được xác nhận.

### [06:19.2 → 06:23.7] #45 · 7.1  Giỏ trống -> 400

**Lời nói:** Đặt hàng khi giỏ trống trả 400.

**Đọc cho TTS:** Đặt hàng khi giỏ trống trả 400.

### [06:23.7 → 06:29.5] #46 · 7.2  ADMIN ngừng bán sản phẩm

**Lời nói:** ADMIN ngừng bán sản phẩm, sản phẩm biến mất khỏi catalog công khai.

**Đọc cho TTS:** át-min ngừng bán sản phẩm, sản phẩm biến mất khỏi ca-ta-lóc công khai.

## DEMO LOCAL · Web UI

### [06:29.5 → 06:36.5] #47 · CUSTOMER — giỏ hàng trên giao diện web

**Lời nói:** Cùng luồng đó trên giao diện web. CUSTOMER thêm sản phẩm và sửa số lượng ngay trong giỏ.

**Đọc cho TTS:** Cùng luồng đó trên giao diện web. cát-xtơ-mơ thêm sản phẩm và sửa số lượng ngay trong giỏ.

### [06:36.5 → 06:41.7] #48 · CUSTOMER — đơn hàng đã thanh toán

**Lời nói:** Đặt hàng VNPay xong, đơn hiện PAID và có nút xem video.

**Đọc cho TTS:** Đặt hàng vi-en-pây xong, đơn hiện pết và có nút xem vi-đi-ô.

### [06:41.7 → 06:48.2] #49 · ADMIN — quản lý đơn hàng

**Lời nói:** Đăng nhập ADMIN, giao diện chuyển sang quản lý đơn. Đơn COD có nút xác nhận.

**Đọc cho TTS:** Đăng nhập át-min, giao diện chuyển sang quản lý đơn. Đơn xi âu đi có nút xác nhận.

### [06:48.2 → 06:52.7] #50 · ADMIN — xác nhận thanh toán

**Lời nói:** Bấm xác nhận, đơn chuyển sang CONFIRMED.

**Đọc cho TTS:** Bấm xác nhận, đơn chuyển sang con-phơm.

### [06:52.7 → 06:57.2] #51 · ADMIN — quản lý sản phẩm

**Lời nói:** ADMIN thêm, sửa và ngừng bán sản phẩm.

**Đọc cho TTS:** át-min thêm, sửa và ngừng bán sản phẩm.

## MỞ RỘNG · CI/CD & SECRET

### [06:57.2 → 07:06.4] #52 · Mở rộng 1 — GitHub Actions

**Lời nói:** Phần mở rộng. Mỗi lần push, GitHub Actions chạy toàn bộ test. Test xanh thì mới build image, đẩy lên Amazon ECR và deploy.

**Đọc cho TTS:** Phần mở rộng. Mỗi lần pút, gít-hắp ác-sần chạy toàn bộ tét. tét xanh thì mới biu im-mịch, đẩy lên a-ma-dôn i xi a và đi-ploi.

### [07:06.4 → 07:18.9] #53 · Lưu secret thế nào

**Lời nói:** Về secret: mật khẩu khai báo trong GitHub Secrets, pipeline chuyển vào Parameter Store dạng mã hóa, EC2 đọc bằng IAM role khi deploy. Không có mật khẩu nào trong code.

**Đọc cho TTS:** Về xi-crít: mật khẩu khai báo trong gít-hắp xi-crít, pai-lai chuyển vào pa-ra-mi-tơ xờ-to dạng mã hóa, i xi tu đọc bằng ai ây em rôn khi đi-ploi. Không có mật khẩu nào trong cốt.

### [07:18.9 → 07:24.8] #54 · Pipeline chạy thành công

**Lời nói:** Đây là một lần pipeline chạy thành công: verify xong rồi mới deploy.

**Đọc cho TTS:** Đây là một lần pai-lai chạy thành công: ve-ri-phai xong rồi mới đi-ploi.

## MỞ RỘNG · TRIỂN KHAI AWS

### [07:24.8 → 07:37.6] #55 · Mở rộng 2 — Hạ tầng AWS

**Lời nói:** Hạ tầng AWS tạo bằng CloudFormation: CloudFront làm cổng HTTPS, EC2 chạy container, RDS lưu dữ liệu, ECR lưu image, S3 lưu video khóa học.

**Đọc cho TTS:** Hạ tầng ây đắp-liu ét tạo bằng cờ-lao pho-mây-sần: cờ-lao phờ-rân làm cổng ết ti ti pi ét, i xi tu chạy con-tê-nơ, a đi ét lưu dữ liệu, i xi a lưu im-mịch, ét ba lưu vi-đi-ô khóa học.

### [07:37.6 → 07:49.2] #56 · Lưu video trên S3 — presigned URL

**Lời nói:** Video không lưu trong database. ADMIN nhận một link upload có chữ ký, file đi thẳng lên S3, sau đó API kiểm tra file tồn tại rồi mới gắn vào khóa học.

**Đọc cho TTS:** vi-đi-ô không lưu trong đây-ta-bết. át-min nhận một linh áp-lốt có chữ ký, phai đi thẳng lên ét ba, sau đó ây-pi-ai kiểm tra phai tồn tại rồi mới gắn vào khóa học.

## MỞ RỘNG · DEMO UI TRÊN AWS

### [07:49.2 → 07:58.4] #57 · ADMIN trên AWS — xác nhận đơn COD

**Lời nói:** Giờ demo trên AWS qua CloudFront. ADMIN đăng nhập, thấy đơn COD đang chờ và bấm xác nhận đã thanh toán.

**Đọc cho TTS:** Giờ đê-mô trên ây đắp-liu ét qua cờ-lao phờ-rân. át-min đăng nhập, thấy đơn xi âu đi đang chờ và bấm xác nhận đã thanh toán.

### [07:58.4 → 08:06.8] #58 · ADMIN trên AWS — tải video bài học lên S3

**Lời nói:** ADMIN tải video bài học cho khóa Middle Java. File đi thẳng lên S3, cột video chuyển thành Storage.

**Đọc cho TTS:** át-min tải vi-đi-ô bài học cho khóa mít-đồ gia-va. phai đi thẳng lên ét ba, cột vi-đi-ô chuyển thành xờ-to-rịch.

### [08:06.8 → 08:14.1] #59 · CUSTOMER — đăng ký tài khoản mới

**Lời nói:** Một khách mới đăng ký tài khoản. Mật khẩu phải từ mười ký tự, tài khoản mới luôn là CUSTOMER.

**Đọc cho TTS:** Một khách mới đăng ký tài khoản. Mật khẩu phải từ mười ký tự, tài khoản mới luôn là cát-xtơ-mơ.

### [08:14.1 → 08:19.7] #60 · CUSTOMER — thêm vào giỏ, chọn VNPay

**Lời nói:** Khách thêm khóa Middle Java vào giỏ và chọn thanh toán VNPay.

**Đọc cho TTS:** Khách thêm khóa mít-đồ gia-va vào giỏ và chọn thanh toán vi-en-pây.

### [08:19.7 → 08:27.2] #61 · CUSTOMER — thanh toán xong, xem video từ S3

**Lời nói:** Đặt hàng thành công, đơn PAID. Khách mở video, phát từ S3 bằng link riêng chỉ có hạn một giờ.

**Đọc cho TTS:** Đặt hàng thành công, đơn pết. Khách mở vi-đi-ô, phát từ ét ba bằng linh riêng chỉ có hạn một giờ.

### [08:27.2 → 08:32.0] #62 · CUSTOMER — thông báo đơn hàng

**Lời nói:** Và khách nhận được thông báo đơn hàng do observer gửi.

**Đọc cho TTS:** Và khách nhận được thông báo đơn hàng do ốp-dơ-vơ gửi.

## TỔNG KẾT

### [08:32.0 → 08:46.4] #63 · Tổng kết

**Lời nói:** Tóm lại, em đã hoàn thành đủ yêu cầu đề bài, có test chạy tự động và triển khai lên AWS. Em cảm ơn thầy và các bạn đã theo dõi.

**Đọc cho TTS:** Tóm lại, em đã hoàn thành đủ yêu cầu đề bài, có tét chạy tự động và triển khai lên ây đắp-liu ét. Em cảm ơn thầy và các bạn đã theo dõi.

# Mina Chat Application

Code app nằm trong folder appCode

Code Mina Server nằm trong folder minaServer. Trước khi chạy cần thêm các file jar trong folder externalLib trong minaServer vào trong thư viện (Trong trường hợp Eclipse sử dụng Add External JARs)

Trước khi chạy cần đảm bảo:

+ Server và máy chạy app cùng kết nối với 1 wifi

+ Sửa địa chỉ ipv6 tại biến HOSTNAME trong file Client.java tại code Mina Server và biến serverIp trong file MySocket.java tại code app

Kịch bản chạy của em:

+ Chạy file MinaTimeServer.java của code Mina Server

+ Chạy app. Nhấn button "LOGIN" và nhập thông tin đăng nhập với username=client0; password=pass0. Chờ khi chuyển sang màn hình tiếp theo trước khi chạy bước tiếp theo

+ Chạy file Client.java của code Mina Server (bao gồm 100 thread user)

+ Tại màn hình app, nhấn nút "REFRESH" sẽ xuất hiện danh sách các user đang connect với app user. Nhấn vào 1 user để chuyển sang màn hình chat.

+ Tại màn hình chat, gửi text bằng cách edit text và nhấn nút "Send"; gửi file bằng cách nhấn "Browse", chọn file rồi cuối cùng nhấn "Upload" (khi user kia nhận đủ file data sẽ có thông báo "Uploading completed" trên màn hình). Để kích hoạt gửi trên các thread user, gửi tin nhắn text "hello". Thoát khỏi màn hình chat bằng cách nhấn "Exit".

1 số vấn đề:

+ Khi thoát màn chat thì toàn bộ phần chat đó sẽ không được lưu.

+ Phần in tin nhắn còn sơ sài với định dạng: Người gửi---Nội dung (Em thử tạo phần in riêng thì đang bị lỗi)

Thầy test xem có vấn đề gì không ạ.

Em cảm ơn thầy

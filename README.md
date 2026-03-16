# 🔨 Auction-System-OOD | Hệ Thống Đấu Giá Trực Tuyến
> Dự án bài tập lớn môn Lập trình Nâng cao - Xây dựng dựa trên kiến trúc hướng đối tượng (OOD).

[![Java CI with Maven](https://github.com/25023189-bit/Auction-System-OOD/actions/workflows/maven.yml/badge.svg)](https://github.com/25023189-bit/Auction-System-OOD/actions)
![Java Version](https://img.shields.io/badge/Java-17-orange)
![Framework](https://img.shields.io/badge/UI-JavaFX-blue)

## 💡 Ý tưởng dự án
Hệ thống mô phỏng một sàn đấu giá chuyên nghiệp, nơi các "đại gia" (Clients) có thể vào tranh giành các món hàng độc lạ được quản lý bởi Server. Mọi giao dịch, bước giá và thời gian đều được đồng bộ hóa real-time qua Socket.

## 🤺 Đội ngũ thực hiện & "Địa bàn" phụ trách
Dự án được chia theo mô hình **Vertical Slicing** (Cắt dọc). Mỗi thành viên là một "Full-stack Java Developer" tự quản lý từ giao diện đến database cho tính năng của mình:

* **⚡ Thành viên 1:** * *Sứ mệnh:* Kiến trúc hệ thống, CI/CD, Quản lý "danh phận" (Đăng ký/Đăng nhập) & Phân quyền User/Admin.
* **📦 Thành viên 2:** * *Sứ mệnh:* "Kho hàng" - Phụ trách toàn bộ hệ thống quản lý sản phẩm, thêm mới và lưu trữ kho đồ đấu giá.
* **⚖️ Thành viên 3:** * *Sứ mệnh:* "Sàn đấu" - Xử lý logic đặt giá, thuật toán kiểm tra tính hợp lệ và đảm bảo công bằng cho mỗi lượt bid.
* **🏆 Thành viên 4:** * *Sứ mệnh:* "Chốt đơn" - Xử lý thời gian đếm ngược, xác định người thắng cuộc và hệ thống thông báo nâng cao.

## 🏗 Cấu trúc mã nguồn (MVC Pattern)
Dự án được tổ chức "sạch sẽ" để ai nhìn vào cũng hiểu:
- `com.auction.server`: Trái tim của hệ thống (Xử lý Data, Socket Server).
- `com.auction.client`: Bộ não giao diện (JavaFX Controllers, FXML).
- `com.auction.common`: Các tài nguyên dùng chung (Model Classes, JSON Utils).
- `src/test`: Nơi chứa các "vũ khí" JUnit để đảm bảo code không bao giờ lỗi.

## 🛠 Cách "Build" dự án trên máy cá nhân
1.  **Clone code:** `git clone https://github.com/25023189-bit/Auction-System-OOD.git`
2.  **Mở IDE:** Dùng IntelliJ hoặc Eclipse mở folder dự án.
3.  **Maven Magic:** Chạy `mvn clean install` để hệ thống tự động tải thư viện.
4.  **Run:** Chạy `Server.main()` trước, sau đó mở các `Client.main()`.

## 📜 "Luật chơi" trên GitHub (Git Flow)
Để tránh tình trạng "code chồng code", nhóm thống nhất:
1.  **Main là vùng cấm:** Không push thẳng lên `main`.
2.  **Làm việc riêng:** Code trên nhánh `feature/ten-tinh-nang`.
3.  **Hỏi ý kiến đồng đội:** Phải tạo **Pull Request**, đợi tích xanh CI và ít nhất 2 người Approve mới được Merge.

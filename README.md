# 🔨 Hệ Thống Đấu Giá Trực Tuyến (Online Auction System)
**Môn học:** Lập trình Nâng cao (OOD)  
**Nhóm:** [Tên Nhóm của bạn]

## 📝 Giới thiệu dự án
Dự án xây dựng hệ thống đấu giá trực tuyến sử dụng mô hình Client-Server. Hệ thống cho phép người dùng đăng ký, đăng nhập, quản lý sản phẩm và tham gia đấu giá thời gian thực.

## 👥 Thành viên và Phân công (Vertical Slicing)
Dự án được triển khai theo mô hình "Cắt dọc" tính năng để đảm bảo mọi thành viên đều làm việc với đủ các tầng: GUI, Logic và Data.

| Thành viên | Trụ cột (Vertical Slice) | Chức năng chính |
| :--- | :--- | :--- |
| **Thành viên 1** | **Trụ cột 1 (Quản trị)** | Thiết lập CI/CD, Quản lý người dùng (Login/Register), Phân quyền. |
| **Thành viên 2** | **Trụ cột 2 (Sản phẩm)** | Quản lý danh mục sản phẩm, Thêm/Sửa/Xóa sản phẩm đấu giá. |
| **Thành viên 3** | **Trụ cột 3 (Giao dịch)** | Logic đấu giá cốt lõi, xử lý bước giá, kiểm tra tính hợp lệ của giá đặt. |
| **Thành viên 4** | **Trụ cột 4 (Kết quả)** | Xử lý kết thúc phiên, xác định người thắng, thông báo và nâng cao. |

## 🏗 Kiến trúc dự án (MVC)
Dự án tuân thủ nghiêm ngặt mô hình MVC để đảm bảo dễ bảo trì và kiểm thử:
* **Model:** Chứa các lớp thực thể (User, Item, Bid).
* **View:** Giao diện người dùng sử dụng JavaFX (.fxml).
* **Controller:** Xử lý điều hướng và logic trung gian.
* **DAO (Data Access Object):** Quản lý truy xuất dữ liệu.

## 🚀 Công nghệ sử dụng
* **Ngôn ngữ:** Java 17.
* **Quản lý dự án:** Maven.
* **Giao diện:** JavaFX.
* **Kiểm thử:** JUnit 5 (Tự động chạy qua GitHub Actions).
* **CI/CD:** GitHub Actions.

## 🛠 Hướng dẫn cài đặt
1. Clone dự án: `git clone https://github.com/25023189-bit/Auction-System-OOD.git`.
2. Mở dự án bằng IntelliJ IDEA hoặc Eclipse (Chọn "Import as Maven Project").
3. Chạy lệnh `mvn clean install` để tải các thư viện cần thiết.

## 🛡 Quy trình phát triển (Git Workflow)
Để đảm bảo chất lượng mã nguồn, nhóm áp dụng quy trình:
1. Không push trực tiếp lên nhánh `main`.
2. Mọi tính năng phải được phát triển trên nhánh `feature/[tên-tính-năng]`.
3. Phải tạo **Pull Request** và vượt qua kiểm tra từ **GitHub Actions** (CI) mới được merge vào `main`.

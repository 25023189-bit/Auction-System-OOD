# Auction System - OOD (Object-Oriented Design)

## 📋 Mô Tả Dự Án

### Bài Toán
Đây là project hệ thống đấu giá realtime sử dụng **JavaFX** cho client, **Socket** cho server và **MySQL** cho database.

### Phạm Vi Hệ Thống

Project mô phỏng một hệ thống đấu giá với các chức năng chính:

- ✅ Đăng ký, đăng nhập
- ✅ Tạo phiên đấu giá cho seller
- ✅ Tham gia đấu giá cho bidder
- ✅ Đặt giá realtime
- ✅ Tự động gia hạn phiên ở thời điểm cuối (Anti-sniping)
- ✅ Tự động chốt phiên và xử lý tiền
- ✅ Tự động đóng phòng và đưa người dùng về sảnh khi phiên kết thúc
- ✅ AI ChatBot hỗ trợ trả lời câu hỏi
- ✅ Auto-approve phiên bằng ML model

---

## 🛠 Công Nghệ & Môi Trường

### Công Nghệ Sử Dụng

| Công Nghệ | Tỷ Lệ | Mục Đích |
|-----------|-------|---------|
| Java | 82.7% | Backend server, client logic |
| Python | 11.0% | AI ChatBot, ML Auto-approve |
| CSS | 6.3% | UI styling cho JavaFX |

**Tech Stack chính:**
- **JavaFX 25**: Giao diện người dùng hiện đại
- **Java Socket**: Giao tiếp realtime client-server (port 5000)
- **MySQL 8.0+**: Cơ sở dữ liệu (Database: `aivien`)
- **Maven 3.6+**: Multi-module build tool
- **BCrypt**: Mã hóa mật khẩu
- **HikariCP**: Connection pooling
- **Python ML**: AI auto-approve phiên (Logistic Regression)
- **Logging**: SLF4J + Logback

### Môi Trường & Yêu Cầu

- **JDK**: 25 (để biên dịch và chạy JavaFX 25)
- **MySQL**: 8.0 trở lên
- **IDE**: IntelliJ IDEA hoặc tương đương
- **Python**: 3.8+ (cho AI ChatBot)
- **Maven**: 3.6+

### Yêu Cầu Cài Đặt

```bash
# 1. Clone repository
git clone https://github.com/25023189-bit/Auction-System-OOD.git
cd Auction-System-OOD

# 2. Tạo database (sử dụng tên 'aivien')
mysql -u root -p -e "CREATE DATABASE aivien;"

# 3. Import database schema
mysql -u root -p aivien < auction-server/src/main/resources/database/init_db.sql

# 4. Cấu hình database (tạo file application.properties)
# Xem chi tiết ở phần "Cấu Hình Database"

# 5. Cài Python dependencies (cho ChatBot)
pip install -r Auction_AI/ChatBot/requirements.txt

# 6. Build all modules
mvn clean package
```

---

## 📁 Cấu Trúc Thư Mục & Module Chi Tiết

```
Auction-System-OOD/                                   [Root Project]
│
├── 📄 pom.xml                                         [Parent POM - Multi-module]
├── 📄 README.md
├── 📄 DATABASE_SCHEMA_AND_DAO_GUIDE.md
├── 📄 auction_system_functions.json
│
├── 📁 .github/                                        [GitHub Actions]
├── 📁 .mvn/                                           [Maven Wrapper]
├── 📁 .idea/                                          [IntelliJ Config]
│
├── 📦 AUCTION-COMMON MODULE
│   ├── 📄 pom.xml
│   ├── src/main/java/com/auction/common/
│   │   ├── dto/
│   │   │   ├── Message.java
│   │   │   ├── AuctionEndNotificationPayload.java
│   │   │   ├── AutoBidRequest.java
│   │   │   └── ...
│   │   ├── model/
│   │   │   ├── User.java
│   │   │   ├── AuctionRoom.java
│   │   │   ├── Item.java
│   │   │   └── ...
│   │   └── role/
│   │       └── UserRole.java
│
├── 📦 AUCTION-SERVER MODULE
│   ├── 📄 pom.xml
│   ├── src/main/java/com/auction/server/
│   │   ├── main/
│   │   │   └── AuctionServer.java                    [Entry point (port 5000)]
│   │   ├── handler/
│   │   ├── network/
│   │   ├── service/
│   │   ├── dao/
│   │   ├── config/
│   │   ├── AI/
│   │   └── utils/
│   ├── src/main/resources/
│   │   ├── 📄 application.properties
│   │   ├── 📄 logback.xml
│   │   └── database/
│   │       └── 📄 init_db.sql
│
├── 📦 AUCTION-CLIENT MODULE
│   ├── 📄 pom.xml
│   ├── src/main/java/com/auction/client/
│   │   ├── app/
│   │   ├── core/
│   │   ├── feature/
│   │   ├── network/
│   │   ├── service/
│   │   └── session/
│   ├── src/main/resources/
│   │   ├── logback.xml
│   │   ├── fxml/
│   │   ├── css/
│   │   └── images/
│
├── 📁 Auction_AI/
│   ├── ChatBot/
│   │   ├── 🐍 chatbot.py
│   │   ├── 📄 requirements.txt
│   │   └── 📄 README.md
│   └── AutoApprove/
│       ├── 🐍 train_logistic_regression.py
│       ├── 📦 auction_model.pkl
│       ├── 📊 Training_Data.csv
│       └── 📄 README.md
│
├── 📊 DOCUMENTATION FILES
│   ├── 📄 AuctionSystem_AllClasses_StarUML_Final.mdj
│   ├── 📄 system-overview.puml
│   └── ...
│
└── 🛠 BUILD & CONFIG FILES
    ├── 📄 mvnw, mvnw.cmd
    ├── 📄 .gitignore
    └── 📄 pom.xml
```

---

## 📦 Vị Trí File JAR

Các file JAR nằm trong thư mục:

```
release/
├── auction-server.jar                    [Server executable]
├── auction-client.jar                    [Client executable]
└── client.properties                     [Client config file]
```

Hoặc sau khi build bằng Maven:
```
target/
├── auction-common-1.0-SNAPSHOT.jar
├── auction-server-1.0-SNAPSHOT.jar
├── auction-client-1.0-SNAPSHOT.jar
└── lib/                                  [Dependencies]
```

---

## Hướng dẫn chạy ứng dụng

Phần này hướng dẫn chạy dự án ngay sau khi pull code về máy mới. Giả định máy chưa cài sẵn Java, Maven, Python hoặc các công cụ liên quan.

---

## 1. Yêu cầu môi trường

Trước khi chạy dự án, cần cài các công cụ sau:

### 1.1. Cài Git

Dùng để clone/pull source code từ GitHub.

Tải Git tại:

```text
https://git-scm.com/downloads
```

Kiểm tra sau khi cài:

```bash
git --version
```

Nếu hiện phiên bản Git, ví dụ `git version 2.x.x`, nghĩa là cài thành công.

---

### 1.2. Cài JDK

Dự án sử dụng Java để chạy client JavaFX, server và common module.

Khuyến nghị dùng JDK 17 trở lên. Nếu dự án đang được phát triển bằng JDK 25 thì nên dùng đúng JDK 25 để tránh lỗi không tương thích.

Kiểm tra sau khi cài:

```bash
java --version
javac --version
```

Nếu cả hai lệnh đều hiện phiên bản Java, nghĩa là JDK đã sẵn sàng.

---

### 1.3. Cài Maven

Dự án dùng Maven để quản lý thư viện, build source code và tạo file `.jar`.

Tải Maven tại:

```text
https://maven.apache.org/download.cgi
```

Sau khi cài, kiểm tra:

```bash
mvn --version
```

Nếu hiện phiên bản Maven và Java đang dùng, nghĩa là Maven đã hoạt động.

---

### 1.4. Cài Python

Phần AI của hệ thống sử dụng Python để xử lý các tác vụ như phân loại intent chatbot hoặc auto-approve.

Khuyến nghị dùng Python 3.10 trở lên.

Kiểm tra sau khi cài:

```bash
python --version
```

Hoặc nếu máy dùng lệnh `py`:

```bash
py --version
```

---

## 2. Clone source code về máy

Mở Terminal, PowerShell hoặc Git Bash tại thư mục muốn lưu project, sau đó chạy:

```bash
git clone <URL_REPOSITORY>
```

Sau đó vào thư mục dự án:

```bash
cd AuctionSystem
```

Nếu đã clone rồi và chỉ muốn cập nhật code mới nhất:

```bash
git pull origin main
```

---

## 3. Kiểm tra cấu trúc thư mục

Sau khi pull code, cấu trúc project thường gồm các module chính:

```text
AuctionSystem/
├── auction-client/
├── auction-server/
├── auction-common/
├── Auction_AI/
├── pom.xml
└── README.md
```

Ý nghĩa các phần chính:

| Thư mục          | Vai trò                                                        |
| ---------------- | -------------------------------------------------------------- |
| `auction-client` | Giao diện JavaFX phía người dùng                               |
| `auction-server` | Server xử lý nghiệp vụ đấu giá                                 |
| `auction-common` | Chứa model, message, protocol dùng chung giữa client và server |
| `Auction_AI`     | Chứa phần AI hỗ trợ chatbot và auto-approve                    |
| `pom.xml`        | File Maven root để build toàn bộ project                       |

---

## 4. Cài thư viện Python cho phần AI

Vào thư mục AI:

```bash
cd Auction_AI
```

Tạo môi trường ảo Python:

```bash
python -m venv .venv
```

Kích hoạt môi trường ảo trên Windows PowerShell:

```bash
.venv\Scripts\Activate.ps1
```

Nếu dùng CMD:

```bash
.venv\Scripts\activate.bat
```

Nếu dùng macOS/Linux:

```bash
source .venv/bin/activate
```

Cài thư viện Python:

```bash
pip install -r requirements.txt
```

Nếu project không có file `requirements.txt`, có thể cài các thư viện thường dùng cho phần AI:

```bash
pip install scikit-learn pandas numpy joblib
```

Sau khi cài xong, quay lại thư mục gốc:

```bash
cd ..
```

---

## 5. Build toàn bộ project bằng Maven

Tại thư mục gốc `AuctionSystem`, chạy:

```bash
mvn clean package
```

Nếu muốn build nhanh và bỏ qua test:

```bash
mvn clean package -DskipTests
```

Nếu build thành công, Maven sẽ tạo các file `.jar` trong thư mục `target` của từng module, ví dụ:

```text
auction-server/target/
auction-client/target/
auction-common/target/
```

---

## 6. Cấu hình kết nối client-server

Client cần biết địa chỉ IP và port của server.

Nếu project có file cấu hình dạng:

```text
auction-client/src/main/resources/client.properties
```

hoặc:

```text
auction-client/src/main/resources/config/client.properties
```

hãy kiểm tra các trường tương tự:

```properties
server.host=localhost
server.port=12345
```

Ý nghĩa:

| Cấu hình                           | Ý nghĩa                                      |
| ---------------------------------- | -------------------------------------------- |
| `server.host=localhost`            | Client kết nối tới server chạy trên cùng máy |
| `server.host=<IP_MAY_CHAY_SERVER>` | Client kết nối tới server chạy ở máy khác    |
| `server.port=12345`                | Port mà server đang mở để nhận kết nối       |

Nếu chạy client và server trên cùng một máy, để:

```properties
server.host=localhost
```

Nếu chạy nhiều máy trong cùng mạng LAN, máy client cần trỏ tới IP của máy đang chạy server, ví dụ:

```properties
server.host=192.168.1.10
server.port=12345
```

Trên máy chạy server, có thể kiểm tra IP bằng lệnh:

```bash
ipconfig
```

Tìm dòng `IPv4 Address`.

---

## 7. Chạy server

Server phải được chạy trước client.

Tại thư mục gốc project, chạy một trong các cách sau.

### Cách 1: Chạy bằng Maven

```bash
mvn -pl auction-server exec:java
```

Nếu project có khai báo main class trong Maven, server sẽ khởi động trực tiếp.

### Cách 2: Chạy bằng file JAR

Sau khi build xong, chạy:

```bash
java -jar auction-server/target/<TEN_FILE_SERVER>.jar
```

Ví dụ:

```bash
java -jar auction-server/target/auction-server.jar
```

Khi server chạy thành công, terminal thường hiển thị log cho biết server đã mở port và sẵn sàng nhận client.

Ví dụ:

```text
Auction server started on port 12345
Waiting for clients...
```

Không tắt cửa sổ terminal đang chạy server trong lúc test.

---

## 8. Chạy client

Mở một terminal khác, vẫn tại thư mục gốc project.

### Cách 1: Chạy client bằng Maven

```bash
mvn -pl auction-client javafx:run
```

### Cách 2: Chạy client bằng file JAR

Sau khi build xong:

```bash
java -jar auction-client/target/<TEN_FILE_CLIENT>.jar
```

Ví dụ:

```bash
java -jar auction-client/target/auction-client.jar
```

Nếu chạy thành công, giao diện JavaFX sẽ hiện ra.

---

## 9. Thứ tự chạy đúng

Thứ tự chạy chuẩn là:

```text
Bước 1: Cài môi trường Java, Maven, Python
Bước 2: Pull code mới nhất
Bước 3: Cài thư viện Python cho Auction_AI
Bước 4: Build project bằng Maven
Bước 5: Chạy server
Bước 6: Chạy client
Bước 7: Đăng nhập / đăng ký / test chức năng
```

Không chạy client trước server, vì client cần kết nối tới server để đăng nhập, vào phòng đấu giá và gửi request.

---

## 10. Chạy nhiều client để test đấu giá

Để test đấu giá nhiều người dùng:

1. Chạy server trước.
2. Mở client thứ nhất.
3. Mở thêm terminal khác và chạy client thứ hai.
4. Đăng nhập bằng hai tài khoản khác nhau.
5. Một tài khoản vào vai seller/admin nếu cần tạo hoặc duyệt phiên.
6. Một hoặc nhiều tài khoản vào vai bidder để tham gia đặt giá.

Có thể chạy nhiều client trên cùng một máy để test nhanh.

---

## 11. Chạy client từ máy khác trong cùng mạng LAN

Trên máy chạy server:

1. Mở server.
2. Kiểm tra IP bằng lệnh:

```bash
ipconfig
```

Ví dụ IP server là:

```text
192.168.1.10
```

Trên máy client:

1. Pull hoặc copy project về.
2. Sửa file `client.properties`:

```properties
server.host=192.168.1.10
server.port=12345
```

3. Build và chạy client.

Lưu ý:

* Các máy phải cùng mạng LAN.
* Firewall trên máy server có thể chặn kết nối.
* Nếu không kết nối được, thử tắt tạm firewall hoặc cho phép Java qua firewall.
* Server phải đang chạy trước khi client kết nối.

---

## 12. Dừng server hoặc client

Để dừng client, đóng cửa sổ JavaFX.

Để dừng server, vào terminal đang chạy server và nhấn:

```bash
Ctrl + C
```

Nếu server bị treo hoặc port bị chiếm, có thể tắt toàn bộ tiến trình Java trên Windows bằng PowerShell:

```powershell
Get-Process java | Stop-Process -Force
```

Sau đó chạy lại server.

---

## 13. Một số lỗi thường gặp

### 13.1. Lỗi `Address already in use`

Thông báo ví dụ:

```text
java.net.BindException: Address already in use
```

Nguyên nhân: port server đang bị một tiến trình Java khác chiếm.

Cách xử lý nhanh trên Windows PowerShell:

```powershell
Get-Process java | Stop-Process -Force
```

Sau đó chạy lại server.

---

### 13.2. Client không kết nối được server

Kiểm tra lần lượt:

1. Server đã chạy chưa?
2. `server.host` trong `client.properties` có đúng không?
3. `server.port` có trùng với port server không?
4. Hai máy có cùng mạng LAN không?
5. Firewall có chặn Java không?

Nếu chạy cùng máy, nên để:

```properties
server.host=localhost
```

Nếu chạy khác máy, phải dùng IP của máy chạy server.

---

### 13.3. Lỗi JavaFX khi chạy client

Nếu gặp lỗi liên quan JavaFX, hãy kiểm tra:

1. Đã dùng đúng JDK chưa?
2. Đã build bằng Maven chưa?
3. Module `auction-client` có đầy đủ dependency JavaFX trong `pom.xml` chưa?
4. Có chạy đúng lệnh Maven không?

Lệnh khuyến nghị:

```bash
mvn -pl auction-client javafx:run
```

---

### 13.4. Lỗi Python hoặc AI không chạy

Kiểm tra:

1. Đã cài Python chưa?

```bash
python --version
```

2. Đã kích hoạt môi trường ảo chưa?

Windows PowerShell:

```bash
.venv\Scripts\Activate.ps1
```

3. Đã cài thư viện chưa?

```bash
pip install -r requirements.txt
```

4. Đường dẫn file input/output JSON của AI có đúng không?

Phần AI thường giao tiếp với Java thông qua file JSON hoặc script Python. Nếu đường dẫn sai, Java có thể không nhận được kết quả từ AI.

---

## 14. Lệnh chạy nhanh cho Windows

Dưới đây là chuỗi lệnh mẫu cho lần chạy đầu tiên trên Windows PowerShell:

```powershell
git clone <URL_REPOSITORY>
cd AuctionSystem

cd Auction_AI
python -m venv .venv
.venv\Scripts\Activate.ps1
pip install -r requirements.txt
cd ..

mvn clean package -DskipTests
java -jar auction-server\target\<TEN_FILE_SERVER>.jar
```

Sau đó mở terminal thứ hai:

```powershell
cd AuctionSystem
java -jar auction-client\target\<TEN_FILE_CLIENT>.jar
```

Nếu không dùng file JAR, có thể chạy client bằng Maven:

```powershell
mvn -pl auction-client javafx:run
```

---

## 15. Ghi chú cho người mới pull code

* Luôn chạy server trước client.
* Luôn pull code mới nhất trước khi build.
* Nếu vừa đổi branch hoặc vừa pull nhiều thay đổi lớn, nên chạy lại:

```bash
mvn clean package -DskipTests
```

* Nếu lỗi khó hiểu sau nhiều lần chạy, thử tắt toàn bộ tiến trình Java rồi chạy lại:

```powershell
Get-Process java | Stop-Process -Force
```

* Nếu test nhiều máy, cần sửa `server.host` ở client thành IP của máy chạy server.
* Không tự ý sửa logic AI, server handler hoặc protocol message nếu chỉ đang muốn chạy ứng dụng.


## ✅ Danh Sách Chức Năng Hoàn Thành

### Chức Năng Người Dùng (General)
- [x] Đăng ký tài khoản mới
- [x] Đăng nhập / Đăng xuất
- [x] Quên mật khẩu + reset bằng email
- [x] Quản lý thông tin tài khoản cá nhân
- [x] Xem lịch sử giao dịch

### Chức Năng Người Bán (Seller)
- [x] Xem danh sách sản phẩm
- [x] Tạo phiên đấu giá (gửi yêu cầu Admin duyệt)
- [x] Xem danh sách phiên đang hoạt động
- [x] Xem danh sách phiên đã kết thúc
- [x] Chủ động đóng phiên
- [x] Xem lịch sử các phiên đã tạo

### Chức Năng Người Mua (Bidder)
- [x] Xem danh sách các phiên đấu giá đang chạy
- [x] Lọc và tìm kiếm phiên theo tiêu chí
- [x] Xem chi tiết sản phẩm và phiên
- [x] Tham gia phòng đấu giá (join room)
- [x] Đặt giá realtime (bidding)
- [x] Xem giá cao nhất và người thắng hiện tại
- [x] Xem lịch sử tất cả bid của mình
- [x] Xem danh sách phiên đã thắng

### Chức Năng Quản Trị (Admin)
- [x] Xem danh sách yêu cầu tạo phiên chờ duyệt
- [x] Duyệt hoặc từ chối yêu cầu phiên
- [x] Quản lý người dùng (view, suspend, activate)
- [x] Quản lý sản phẩm
- [x] Hủy phiên nếu cần
- [x] Xem báo cáo thống kê
- [x] Xem nhật ký hoạt động (audit log)

### Chức Năng Kỹ Thuật
- [x] Kiểm soát đồng thời truy cập (Thread-safe)
- [x] Anti-sniping (gia hạn tự động trong 30s cuối nếu có bid)
- [x] Xác thực & phân quyền người dùng
- [x] Mã hóa mật khẩu BCrypt (cost 12)
- [x] Lưu trữ dữ liệu an toàn trên MySQL (Database: aivien)
- [x] Xử lý lỗi và exception handling
- [x] Logging và monitoring (SLF4J + Logback)
- [x] Rate limiting để tránh spam
- [x] Email notification (quên mật khẩu)
- [x] AI ChatBot hỗ trợ trả lời câu hỏi
- [x] AI auto-approve phiên (Logistic Regression ML model)
- [x] HikariCP connection pooling

---

## 📄 Tài Liệu & Demo

### Link Báo Cáo & Tài Liệu
- 📖 **PDF báo cáo**: [Xem báo cáo chi tiết](https://drive.google.com/drive/folders/1h1dDZL0z_v0DlvvriZxQpq55o1cZbCIU?fbclid=IwY2xjawSJPsVleHRuA2FlbQIxMQBzcnRjBmFwcF9pZAEwAAEeyR33LWsn0rig1rUlONhsDz16dw7yUxwh57nIHQ78aF0jD2zzev9PL2ssVCs_aem_Ctgy-v8wmfexHFGxw2F_jg).
- 📖 **Database Schema & DAO Guide**: [DATABASE_SCHEMA_AND_DAO_GUIDE.md](./DATABASE_SCHEMA_AND_DAO_GUIDE.md)
- 📊 **Class Diagram Images**: [Xem ảnh thiết kế sơ đồ lớp](https://drive.google.com/drive/folders/1N_xzXZ_c1i6-L7C3Bl-t3bsYXWSXIsf2?fbclid=IwY2xjawSJPsVleHRuA2FlbQIxMQBzcnRjBmFwcF9pZAEwAAEeyR33LWsn0rig1rUlONhsDz16dw7yUxwh57nIHQ78aF0jD2zzev9PL2ssVCs_aem_Ctgy-v8wmfexHFGxw2F_jg).

### Link Video Demo
- 🎥 **Video DEMO sản phẩm**: [Xem video chi tiết](https://drive.google.com/file/d/1jm25PCK1x3LAd_zeir_PhlkSpqa3lszx/view)

---

## 🔄 Quy Tắc Nghiệp Vụ Chính

## Quy Tắc Nghiệp Vụ Chính

Phần này mô tả các quy tắc nghiệp vụ cốt lõi của hệ thống đấu giá. Tất cả thành viên khi phát triển, sửa lỗi hoặc kiểm thử cần nắm rõ để tránh làm sai logic chính của hệ thống.

---

## 1. Nguyên tắc tổng quát

Hệ thống được xây dựng theo mô hình client-server.

* Client JavaFX chịu trách nhiệm hiển thị giao diện, nhận thao tác người dùng và gửi yêu cầu đến server.
* Server chịu trách nhiệm xử lý nghiệp vụ, kiểm tra điều kiện hợp lệ, cập nhật trạng thái và gửi kết quả về client.
* Common module định nghĩa các model, message và cấu trúc dữ liệu dùng chung giữa client và server.
* AI module chỉ đóng vai trò hỗ trợ, không thay thế nghiệp vụ lõi của server.

Nguyên tắc quan trọng nhất:

```text
Client không tự quyết định kết quả nghiệp vụ.
Server là nguồn sự thật chính của hệ thống.
```

Ví dụ:

* Client có thể kiểm tra sơ bộ dữ liệu nhập.
* Nhưng việc đăng nhập thành công hay không, phiên có được tạo hay không, giá đặt có hợp lệ hay không, người thắng là ai... phải do server quyết định.

---

## 2. Vai trò người dùng

Hệ thống có ba nhóm vai trò chính:

| Vai trò | Quyền chính                                                                              |
| ------- | ---------------------------------------------------------------------------------------- |
| Bidder  | Xem phiên đấu giá, tham gia phòng, đặt giá, dùng auto-bid, theo dõi lịch sử và thời gian |
| Seller  | Tạo yêu cầu phiên đấu giá, quản lý phiên của mình, theo dõi trạng thái phiên             |
| Admin   | Quản lý hệ thống, duyệt/kiểm soát phiên, xử lý các trường hợp cần can thiệp              |

Mỗi người dùng chỉ được thao tác trong phạm vi quyền của vai trò hiện tại.

Ví dụ:

* Bidder không được tạo phiên đấu giá như seller.
* Seller không được đặt giá như bidder trong phiên của chính mình nếu hệ thống không cho phép.
* Admin có quyền quản lý nhưng không nên bị xử lý như bidder thông thường trong luồng đặt giá.

---

## 3. Quy tắc đăng nhập và phân quyền

Người dùng cần đăng nhập trước khi sử dụng các chức năng chính của hệ thống.

Sau khi đăng nhập thành công:

1. Server xác định thông tin người dùng.
2. Server xác định vai trò của người dùng.
3. Client điều hướng đến giao diện tương ứng với vai trò đó.

Quy tắc:

* Tài khoản không hợp lệ thì không được vào hệ thống.
* Mỗi vai trò chỉ được hiển thị và sử dụng các chức năng phù hợp.
* Không được xử lý quyền chỉ bằng cách ẩn nút trên giao diện. Server vẫn phải kiểm tra quyền khi nhận request.

---

## 4. Vòng đời phiên đấu giá

Một phiên đấu giá thường đi qua các trạng thái chính sau:

```text
Tạo yêu cầu → Chờ duyệt / Kiểm tra → Đang diễn ra → Kết thúc
```

Tùy cách triển khai, hệ thống có thể có thêm trạng thái phụ, nhưng về nghiệp vụ cần đảm bảo:

| Trạng thái   | Ý nghĩa                                          |
| ------------ | ------------------------------------------------ |
| Chờ duyệt    | Phiên mới được tạo, chưa chắc được mở cho bidder |
| Đang diễn ra | Bidder có thể tham gia và đặt giá                |
| Kết thúc     | Phiên đã đóng, không cho phép đặt giá thêm       |

Quy tắc:

* Chỉ phiên hợp lệ mới được hiển thị để bidder tham gia.
* Phiên đã kết thúc không được nhận bid mới.
* Trạng thái phiên phải được cập nhật đồng bộ giữa server và các client liên quan.
* Nếu client hiển thị sai trạng thái nhưng server đã kết thúc phiên, server vẫn có quyền từ chối thao tác.

---

## 5. Quy tắc tạo phiên đấu giá

Seller có thể tạo yêu cầu phiên đấu giá bằng cách nhập các thông tin cần thiết như:

* Tên sản phẩm.
* Mô tả sản phẩm.
* Danh mục.
* Giá khởi điểm.
* Thời gian bắt đầu.
* Thời gian kết thúc.
* Ảnh sản phẩm nếu có.
* Các thông tin cấu hình khác như bước giá hoặc gia hạn chống sniping nếu hệ thống hỗ trợ.

Quy tắc:

* Thông tin phiên phải hợp lệ trước khi được tạo.
* Giá khởi điểm phải là số hợp lệ và lớn hơn hoặc bằng mức tối thiểu nếu có quy định.
* Thời gian kết thúc phải sau thời gian bắt đầu.
* Phiên không được mở nếu thiếu dữ liệu bắt buộc.
* Seller chỉ được quản lý các phiên thuộc quyền của mình, trừ khi vai trò hiện tại là admin.

Nếu có AI auto-approve:

* AI chỉ hỗ trợ đánh giá phiên.
* Kết quả AI không được hiểu là thay thế hoàn toàn kiểm tra nghiệp vụ.
* Server vẫn cần kiểm soát cách dùng kết quả AI.

---

## 6. Quy tắc duyệt và kiểm soát phiên

Admin có quyền kiểm soát các phiên đấu giá trong hệ thống.

Admin có thể:

* Xem danh sách phiên.
* Duyệt hoặc từ chối phiên nếu hệ thống có cơ chế duyệt.
* Đóng phiên trong trường hợp cần thiết.
* Theo dõi trạng thái chung của hệ thống.

Quy tắc:

* Admin không được làm thay đổi trạng thái phiên một cách mâu thuẫn với dữ liệu hiện tại.
* Khi admin đóng hoặc cập nhật phiên, server phải thông báo lại cho các client liên quan.
* Các thao tác quản trị phải đi qua server, không xử lý cục bộ ở client.

---

## 7. Quy tắc tham gia phòng đấu giá

Bidder có thể tham gia một phòng đấu giá nếu phiên đang ở trạng thái cho phép tham gia.

Luồng cơ bản:

```text
Bidder chọn phiên → Client gửi yêu cầu vào phòng → Server kiểm tra → Server trả dữ liệu phòng → Client hiển thị phòng đấu giá
```

Quy tắc:

* Không được tham gia phiên không tồn tại.
* Không được tham gia phiên đã kết thúc.
* Không được tham gia phiên chưa mở nếu hệ thống chưa cho phép.
* Khi vào phòng, client cần hiển thị đúng các thông tin hiện tại: giá cao nhất, người giữ giá cao nhất, thời gian còn lại, lịch sử bid và chat nếu có.
* Trạng thái phòng phải lấy từ server, không tự dựng từ dữ liệu cũ ở client.

---

## 8. Quy tắc đặt giá

Đặt giá là nghiệp vụ trung tâm của hệ thống.

Luồng cơ bản:

```text
Bidder nhập giá → Client gửi request → Server kiểm tra → Server cập nhật giá nếu hợp lệ → Server broadcast kết quả → Client cập nhật giao diện
```

Một lượt đặt giá chỉ hợp lệ khi thỏa mãn các điều kiện chính:

* Phiên đấu giá đang diễn ra.
* Người đặt giá có quyền đặt giá trong phiên đó.
* Giá đặt là số hợp lệ.
* Giá đặt phải lớn hơn giá hiện tại.
* Giá đặt phải đáp ứng bước giá tối thiểu nếu hệ thống có quy định.
* Người dùng phải đủ điều kiện tài khoản/số dư nếu hệ thống kiểm tra số dư.
* Phiên chưa bị đóng bởi seller hoặc admin.
* Phiên chưa hết thời gian.

Nếu giá đặt hợp lệ:

* Server cập nhật giá hiện tại.
* Server cập nhật người giữ giá cao nhất.
* Server ghi nhận lịch sử bid nếu có.
* Server gửi thông báo cập nhật cho các client trong phòng.

Nếu giá đặt không hợp lệ:

* Server từ chối request.
* Client hiển thị thông báo lỗi phù hợp.
* Trạng thái giá hiện tại không được thay đổi.

---

## 9. Quy tắc chống sniping

Anti-sniping là cơ chế gia hạn thời gian khi có bid hợp lệ gần thời điểm kết thúc phiên.

Mục tiêu:

* Tránh việc người dùng đặt giá ở vài giây cuối khiến người khác không có cơ hội phản hồi.
* Giúp phiên đấu giá công bằng hơn.

Quy tắc:

* Chỉ kích hoạt khi bid hợp lệ.
* Chỉ kích hoạt trong khoảng thời gian gần cuối phiên nếu hệ thống có cấu hình.
* Thời gian gia hạn không được vượt quá giới hạn hệ thống quy định.
* Sau khi gia hạn, server phải cập nhật thời gian mới cho các client trong phòng.
* Client không tự ý gia hạn thời gian nếu server chưa xác nhận.

---

## 10. Quy tắc auto-bidding

Auto-bidding là chức năng hỗ trợ bidder tự động đặt giá theo cấu hình đã thiết lập.

Người dùng có thể cấu hình:

* Giá tối đa muốn theo.
* Bước tăng giá tự động.
* Phiên muốn bật auto-bid.

Quy tắc:

* Auto-bid không được vượt quá giới hạn tối đa người dùng đã đặt.
* Auto-bid vẫn phải tuân thủ toàn bộ luật đặt giá thông thường.
* Auto-bid không phải cơ chế can thiệp kết quả.
* Khi có giá mới, server mới là nơi quyết định auto-bid nào được kích hoạt.
* Nếu người dùng hủy auto-bid, server phải xóa cấu hình auto-bid tương ứng.
* Client chỉ cập nhật trạng thái auto-bid sau khi nhận phản hồi từ server.

Ví dụ:

```text
Bidder A đặt auto-bid tối đa 1.000.000
Bidder B đặt giá 800.000
Hệ thống có thể tự đặt giá tiếp cho A nếu còn trong giới hạn
Nhưng nếu giá cần đặt vượt quá 1.000.000 thì auto-bid của A không được tiếp tục
```

---

## 11. Quy tắc kết thúc phiên

Một phiên đấu giá có thể kết thúc khi:

* Hết thời gian.
* Seller đóng phiên nếu hệ thống cho phép.
* Admin đóng phiên vì lý do quản trị.
* Hệ thống tự động đóng phiên theo trạng thái.

Khi phiên kết thúc:

* Không nhận thêm bid mới.
* Không kích hoạt auto-bid mới.
* Server xác định kết quả cuối cùng.
* Nếu có người đặt giá hợp lệ cao nhất, người đó là người thắng.
* Nếu không có bid hợp lệ, phiên có thể kết thúc mà không có người thắng.
* Client phải khóa hoặc ẩn các thao tác không còn hợp lệ.

Quy tắc:

```text
Sau khi phiên đã kết thúc, mọi request đặt giá gửi lên server đều phải bị từ chối.
```

---

## 12. Quy tắc cập nhật giao diện realtime

Khi trạng thái phiên thay đổi, các client liên quan cần được cập nhật.

Các sự kiện thường cần broadcast:

* Có bid mới hợp lệ.
* Giá hiện tại thay đổi.
* Người giữ giá cao nhất thay đổi.
* Thời gian được gia hạn.
* Phiên kết thúc.
* Chat trong phòng nếu có.
* Người dùng vào/rời phòng nếu hệ thống hiển thị.

Quy tắc:

* Server gửi cập nhật đến các client trong phòng.
* Client nhận message và cập nhật UI.
* Client không tự đoán kết quả nếu chưa có phản hồi từ server.
* Nếu mất kết nối, client cần báo lỗi hoặc không cho thao tác tiếp.

---

## 13. Quy tắc chatbot

Chatbot trong hệ thống đóng vai trò hướng dẫn người dùng sử dụng ứng dụng.

Chatbot có thể hỗ trợ:

* Hướng dẫn đăng nhập.
* Hướng dẫn xem danh sách phiên.
* Hướng dẫn tham gia phòng đấu giá.
* Hướng dẫn đặt giá.
* Hướng dẫn dùng auto-bid.
* Giải thích trạng thái phiên, thời gian còn lại, số dư, vai trò người dùng.
* Trả lời các câu hỏi liên quan trực tiếp đến cách sử dụng hệ thống.

Chatbot không được:

* Tiết lộ chi tiết nội bộ không cần thiết như tên message server, handler, protocol nội bộ.
* Hướng dẫn fake bid, bid ảo hoặc can thiệp kết quả.
* Hướng dẫn sửa dữ liệu trực tiếp trong database.
* Trả lời vượt quá phạm vi hệ thống nếu không có cơ chế fallback phù hợp.
* Thay server quyết định nghiệp vụ.

Nếu chatbot dùng AI/LLM:

* AI chỉ tạo câu trả lời hỗ trợ người dùng.
* AI không được tự ý gọi nghiệp vụ hệ thống nếu chưa có thiết kế rõ ràng.
* Câu trả lời cần ưu tiên an toàn, đúng vai trò và đúng phạm vi người dùng được phép biết.

---

## 14. Quy tắc AI auto-approve

AI auto-approve là module hỗ trợ đánh giá yêu cầu tạo phiên đấu giá.

Quy tắc:

* AI nhận dữ liệu đầu vào có cấu trúc.
* AI trả kết quả có cấu trúc.
* Server hoặc tầng tích hợp quyết định dùng kết quả đó như thế nào.
* AI không được tự ý sửa dữ liệu phiên.
* AI không được tự ý tạo, duyệt hoặc xóa phiên nếu không thông qua logic hệ thống.
* Nếu AI lỗi, hệ thống cần có fallback phù hợp để tránh làm hỏng luồng chính.

Nguyên tắc:

```text
AI là công cụ hỗ trợ quyết định, không phải nguồn sự thật nghiệp vụ.
```

---

## 15. Quy tắc dữ liệu ảnh sản phẩm

Nếu hệ thống hỗ trợ ảnh sản phẩm trong phiên đấu giá:

* Ảnh được dùng để hiển thị sản phẩm ở lobby, phòng đấu giá hoặc popup chi tiết.
* Nếu ảnh chỉ phục vụ trong vòng đời phiên hiện tại, không nhất thiết lưu vĩnh viễn vào database.
* Nếu thiếu ảnh, client cần có ảnh mặc định hoặc placeholder.
* Không được để lỗi ảnh làm hỏng toàn bộ luồng tạo/xem phiên.

Quy tắc:

* Ảnh là dữ liệu hiển thị bổ sung.
* Logic đấu giá không được phụ thuộc hoàn toàn vào ảnh.
* Phiên vẫn phải hoạt động đúng nếu ảnh không tải được.

---

## 16. Quy tắc quản lý số dư

Nếu hệ thống có kiểm tra số dư tài khoản:

* Bidder chỉ được đặt giá trong phạm vi số dư hoặc điều kiện tài chính hợp lệ.
* Server phải kiểm tra số dư khi xử lý bid.
* Client có thể hiển thị số dư nhưng không được tự quyết định bid hợp lệ chỉ dựa trên dữ liệu local.
* Khi phiên kết thúc, hệ thống cần xử lý kết quả tiền/số dư theo luật đã thiết kế.

Quy tắc:

```text
Mọi thay đổi liên quan đến tiền hoặc số dư phải được xử lý ở server.
```

---

## 17. Quy tắc xử lý lỗi

Khi request không hợp lệ, server cần trả phản hồi rõ ràng để client hiển thị thông báo phù hợp.

Một số lỗi thường gặp:

| Tình huống           | Cách xử lý                         |
| -------------------- | ---------------------------------- |
| Đăng nhập sai        | Từ chối đăng nhập và báo lỗi       |
| Phiên không tồn tại  | Không cho vào phòng                |
| Phiên đã kết thúc    | Không cho bid                      |
| Giá đặt không hợp lệ | Từ chối bid                        |
| Không đủ quyền       | Từ chối thao tác                   |
| Mất kết nối server   | Client báo lỗi kết nối             |
| AI lỗi               | Dùng fallback hoặc báo lỗi phù hợp |

Nguyên tắc:

* Không được để client treo vô hạn.
* Không được cập nhật UI như thể thao tác thành công nếu server chưa xác nhận.
* Không được nuốt lỗi âm thầm khiến người dùng không hiểu chuyện gì xảy ra.

---

## 18. Quy tắc đồng bộ trạng thái

Trong hệ thống đấu giá, nhiều client có thể cùng xem và thao tác trên một phiên.

Vì vậy cần đảm bảo:

* Server quản lý trạng thái chính.
* Client chỉ giữ bản sao hiển thị.
* Khi có thay đổi, server gửi cập nhật cho các client liên quan.
* Nếu client bị lệch trạng thái, dữ liệu từ server phải được ưu tiên.
* Không xử lý kết quả đấu giá dựa trên trạng thái cũ ở client.

Nguyên tắc:

```text
State ở server là state đáng tin cậy nhất.
State ở client chỉ phục vụ hiển thị.
```

---

## 19. Quy tắc không nên phá khi sửa code

Khi sửa hoặc refactor hệ thống, cần giữ các quy tắc sau:

* Không đổi tên message/protocol nếu chưa cập nhật cả client và server.
* Không xóa handler khi chưa kiểm tra router còn gọi hay không.
* Không đổi `fx:id` hoặc `onAction` trong FXML nếu controller còn dùng.
* Không sửa logic backend chỉ để phục vụ thay đổi giao diện.
* Không để client tự quyết định nghiệp vụ thay server.
* Không để AI thay thế các kiểm tra bắt buộc ở server.
* Không làm mất khả năng chạy nhiều client cùng lúc.
* Không làm mất luồng bidder/seller/admin đã có.

---

## 20. Tóm tắt nguyên tắc cốt lõi

Có thể nhớ ngắn gọn như sau:

```text
1. Client hiển thị và gửi yêu cầu.
2. Server kiểm tra, xử lý và quyết định kết quả.
3. Common module giúp client và server dùng chung model/message.
4. AI chỉ hỗ trợ, không thay thế nghiệp vụ lõi.
5. Phiên đấu giá phải có trạng thái rõ ràng.
6. Bid chỉ hợp lệ khi server xác nhận.
7. Auto-bid vẫn phải tuân theo luật bid thường.
8. Phiên kết thúc thì không được nhận bid mới.
9. Role nào chỉ được làm đúng quyền của role đó.
10. Khi sửa code, phải giữ đúng flow UI → Controller → Message → Router → Handler → State → Response → UI.
```


## 🔧 Troubleshooting


## Troubleshooting

Phần này tổng hợp các lỗi thường gặp khi cài đặt, build và chạy hệ thống AuctionSystem. Khi gặp lỗi, nên kiểm tra theo thứ tự từ môi trường → build → server → client → AI → kết nối mạng.

---

## 1. Lỗi không nhận lệnh `java`, `javac`, `mvn`, `python` hoặc `git`

### Biểu hiện

Terminal báo các lỗi tương tự:

```text
'java' is not recognized as an internal or external command
'mvn' is not recognized as an internal or external command
'python' is not recognized as an internal or external command
'git' is not recognized as an internal or external command
```

### Nguyên nhân

Máy chưa cài công cụ tương ứng, hoặc đã cài nhưng chưa cấu hình biến môi trường `PATH`.

### Cách xử lý

Kiểm tra từng công cụ:

```bash
java --version
javac --version
mvn --version
python --version
git --version
```

Nếu lệnh nào không chạy, cần cài lại hoặc thêm đường dẫn công cụ đó vào `PATH`.

Với Java, cần đảm bảo đã cài **JDK**, không chỉ JRE.

---

## 2. Lỗi build Maven thất bại

### Biểu hiện

Khi chạy:

```bash
mvn clean package
```

Maven báo `BUILD FAILURE`.

### Cách xử lý nhanh

Thử build lại và bỏ qua test:

```bash
mvn clean package -DskipTests
```

Nếu vẫn lỗi, kiểm tra:

1. Đang đứng đúng thư mục gốc chứa `pom.xml` chưa.
2. Maven đã cài đúng chưa:

```bash
mvn --version
```

3. Java version có phù hợp không:

```bash
java --version
```

4. Có module nào thiếu dependency không.
5. Có file FXML/CSS/resource nào bị lỗi đường dẫn không.

Nếu vừa pull code mới, nên chạy lại:

```bash
mvn clean
mvn package -DskipTests
```

---

## 3. Lỗi port server bị chiếm

### Biểu hiện

Server báo lỗi:

```text
java.net.BindException: Address already in use: bind
```

### Nguyên nhân

Một server cũ hoặc tiến trình Java khác vẫn đang chạy và chiếm port.

### Cách xử lý trên Windows PowerShell

Tắt toàn bộ tiến trình Java:

```powershell
Get-Process java | Stop-Process -Force
```

Sau đó chạy lại server.

### Cách xử lý thủ công

Có thể mở Task Manager, tìm các tiến trình Java đang chạy và tắt chúng.

---

## 4. Client không kết nối được server

### Biểu hiện

Client mở được giao diện nhưng không đăng nhập được, không vào phòng được hoặc báo lỗi kết nối server.

### Nguyên nhân thường gặp

* Chưa chạy server.
* Server chạy sai port.
* Client trỏ sai `server.host`.
* Firewall chặn kết nối.
* Client và server không cùng mạng LAN.
* Server đang chạy bản cũ.

### Cách kiểm tra

1. Đảm bảo server đã chạy trước client.
2. Kiểm tra file cấu hình client, ví dụ:

```properties
server.host=localhost
server.port=12345
```

Nếu client và server chạy cùng máy:

```properties
server.host=localhost
```

Nếu client chạy ở máy khác:

```properties
server.host=<IP_MAY_CHAY_SERVER>
```

3. Trên máy server, kiểm tra IP:

```bash
ipconfig
```

4. Nếu vẫn không kết nối được, thử tắt tạm firewall hoặc cho phép Java qua firewall.

---

## 5. Chạy client trước server

### Biểu hiện

Client báo mất kết nối, đăng nhập không được hoặc không gửi request được.

### Nguyên nhân

Client cần server để xử lý đăng nhập, phân quyền, phòng đấu giá và bid.

### Cách xử lý

Luôn chạy theo thứ tự:

```text
1. Chạy server
2. Chạy client
3. Đăng nhập và test chức năng
```

Nếu đã mở client trước server, hãy tắt client, chạy server rồi mở lại client.

---

## 6. Server chạy nhưng client vẫn kết nối sai bản

### Biểu hiện

Đã sửa code nhưng khi chạy vẫn thấy lỗi cũ.

### Nguyên nhân

Có thể đang chạy `.jar` cũ, hoặc một server cũ vẫn còn chạy nền.

### Cách xử lý

Tắt toàn bộ Java:

```powershell
Get-Process java | Stop-Process -Force
```

Build lại project:

```bash
mvn clean package -DskipTests
```

Chạy lại server từ file `.jar` mới tạo trong thư mục `target`.

---

## 7. Lỗi JavaFX khi chạy client

### Biểu hiện

Client không mở được giao diện, terminal báo lỗi liên quan JavaFX, FXML hoặc runtime.

Ví dụ:

```text
javafx.fxml.LoadException
Location is not set
Missing expression
```

### Nguyên nhân thường gặp

* Sai đường dẫn file FXML.
* Sai `fx:id`.
* Sai `onAction`.
* Controller thiếu method được khai báo trong FXML.
* CSS/FXML có cú pháp không hợp lệ.
* Resource ảnh/font không tồn tại.
* Chạy sai module hoặc thiếu dependency JavaFX.

### Cách xử lý

1. Kiểm tra file FXML được báo trong stack trace.
2. Kiểm tra dòng lỗi cụ thể nếu stack trace có line number.
3. Đảm bảo `fx:controller` đúng class controller.
4. Đảm bảo các method trong `onAction` tồn tại trong controller.
5. Đảm bảo các `fx:id` khớp với field trong controller nếu có inject.
6. Nếu lỗi do CSS, thử comment đoạn CSS mới sửa để khoanh vùng.
7. Chạy client bằng Maven:

```bash
mvn -pl auction-client javafx:run
```

---

## 8. Lỗi FXML `Missing expression`

### Biểu hiện

Thông báo dạng:

```text
javafx.fxml.LoadException: Missing expression
```

### Nguyên nhân

FXML có thuộc tính binding hoặc expression bị viết sai cú pháp. Ví dụ thiếu dấu `{}`, thiếu giá trị, sai cú pháp `%`, `$`, hoặc escape sai.

### Cách xử lý

1. Mở đúng file FXML và dòng được báo lỗi.
2. Kiểm tra các thuộc tính quanh dòng đó.
3. Tạm thời comment node mới sửa gần dòng lỗi.
4. Mở lại bằng Scene Builder để xác định lỗi cú pháp.
5. Không sửa hàng loạt nếu chưa xác định đúng node gây lỗi.

---

## 9. Lỗi `Location is not set`

### Biểu hiện

```text
java.lang.IllegalStateException: Location is not set
```

### Nguyên nhân

`FXMLLoader` không tìm thấy file FXML vì sai đường dẫn resource.

### Cách xử lý

Kiểm tra đoạn load FXML:

```java
FXMLLoader loader = new FXMLLoader(getClass().getResource("/path/to/view.fxml"));
```

Đảm bảo:

* Đường dẫn bắt đầu bằng `/` nếu dùng absolute resource path.
* File FXML thật sự nằm trong `src/main/resources`.
* Tên file đúng cả chữ hoa/chữ thường.
* File được include vào build output.

---

## 10. Lỗi ảnh, icon hoặc CSS không hiển thị

### Biểu hiện

Giao diện mở được nhưng thiếu ảnh, icon, background hoặc style.

### Nguyên nhân

* Sai đường dẫn resource.
* File ảnh không nằm trong `resources`.
* CSS chưa được add vào scene.
* Tên file sai chữ hoa/chữ thường.
* Đường dẫn trong CSS sai.

### Cách xử lý

Kiểm tra đường dẫn resource.

Ví dụ trong CSS:

```css
-fx-background-image: url("../images/background.png");
```

Nếu dùng trong Java:

```java
getClass().getResource("/com/example/images/background.png")
```

Nên đặt resource trong `src/main/resources` để Maven đóng gói vào `.jar`.

---

## 11. Lỗi đăng nhập không thành công

### Biểu hiện

Nhập tài khoản nhưng không vào được hệ thống.

### Nguyên nhân thường gặp

* Server chưa chạy.
* Client không kết nối được server.
* Sai username/password.
* Tài khoản chưa tồn tại.
* Server đang dùng dữ liệu fallback khác với dữ liệu mong đợi.
* Database hoặc DAO lỗi nếu đang bật DB.

### Cách xử lý

1. Kiểm tra server terminal có nhận request đăng nhập không.
2. Kiểm tra client có báo lỗi kết nối không.
3. Thử tài khoản test đã được định nghĩa sẵn trong hệ thống.
4. Nếu dùng database, kiểm tra DB đã chạy và connection config đúng.
5. Nếu dùng fallback HashMap, kiểm tra tài khoản seed trong code.

---

## 12. Lỗi role hiển thị sai giao diện

### Biểu hiện

Đăng nhập bidder nhưng thấy chức năng seller/admin, hoặc ngược lại.

### Nguyên nhân

* Server trả sai role.
* Client route sai màn hình theo role.
* UI không reset đúng khi đổi tài khoản.
* Session cũ chưa được clear.

### Cách xử lý

1. Kiểm tra response đăng nhập từ server có role đúng không.
2. Kiểm tra đoạn điều hướng màn hình theo role.
3. Đăng xuất rồi đăng nhập lại.
4. Tắt client và mở lại để loại trừ state cũ.
5. Không chỉ ẩn nút ở client; server vẫn phải kiểm tra quyền khi xử lý request.

---

## 13. Không thấy phiên đấu giá trong lobby

### Nguyên nhân thường gặp

* Chưa có phiên nào được tạo.
* Phiên chưa được duyệt.
* Phiên chưa đến thời gian bắt đầu.
* Phiên đã kết thúc.
* Client chưa refresh danh sách.
* Server chưa broadcast hoặc chưa trả danh sách mới.

### Cách xử lý

1. Tạo phiên mới bằng tài khoản seller.
2. Nếu hệ thống có admin approve, đăng nhập admin để duyệt.
3. Kiểm tra thời gian bắt đầu/kết thúc của phiên.
4. Refresh lobby hoặc đăng nhập lại.
5. Kiểm tra log server xem phiên có được tạo thành công không.

---

## 14. Không vào được phòng đấu giá

### Nguyên nhân thường gặp

* Phiên không tồn tại.
* Phiên đã kết thúc.
* Phiên chưa được mở.
* Client gửi sai `roomId` hoặc `auctionId`.
* Server không có handler cho request join room.
* Client không xử lý đúng response join room.

### Cách xử lý

1. Kiểm tra phiên đang ở trạng thái active.
2. Kiểm tra server log khi bấm vào phòng.
3. Kiểm tra message join room có đúng mã phiên/phòng không.
4. Kiểm tra client có handler nhận response vào phòng không.
5. Nếu lỗi chỉ xảy ra sau khi sửa UI, kiểm tra `onAction` của nút vào phòng.

---

## 15. Đặt giá không được

### Nguyên nhân thường gặp

* Phiên đã kết thúc.
* Giá nhập không phải số.
* Giá đặt nhỏ hơn hoặc bằng giá hiện tại.
* Không đạt bước giá tối thiểu.
* Người dùng không đủ quyền hoặc không đủ số dư.
* Client chưa vào phòng đúng cách.
* Server từ chối bid nhưng client không hiển thị lỗi rõ.

### Cách xử lý

1. Kiểm tra thời gian còn lại của phiên.
2. Nhập giá lớn hơn giá hiện tại.
3. Kiểm tra role hiện tại có phải bidder không.
4. Kiểm tra server log có nhận request bid không.
5. Kiểm tra response server trả về là success hay failed.
6. Nếu nhiều client test, đảm bảo tất cả đang ở cùng một phòng.

---

## 16. Giá không cập nhật realtime giữa nhiều client

### Nguyên nhân thường gặp

* Server chỉ gửi response cho người đặt giá, chưa broadcast cho cả phòng.
* Client không đăng ký listener nhận message.
* Client ở sai room.
* UI thread JavaFX không được cập nhật đúng cách.
* Message type update giá chưa có handler ở client.

### Cách xử lý

1. Mở ít nhất 2 client cùng vào một phòng.
2. Đặt giá ở client A.
3. Kiểm tra client B có nhận message update không.
4. Kiểm tra server có broadcast cho room không.
5. Kiểm tra client handler có cập nhật UI bằng JavaFX Application Thread không, ví dụ `Platform.runLater`.

---

## 17. Auto-bidding không hoạt động

### Nguyên nhân thường gặp

* Chưa bật auto-bid.
* Giá tối đa đặt quá thấp.
* Bước tăng không hợp lệ.
* Server chưa kích hoạt auto-bid sau bid mới.
* Auto-bid bị hủy hoặc không được lưu vào state.
* Người dùng không đủ điều kiện đặt giá.

### Cách xử lý

1. Bật auto-bid với mức giá tối đa cao hơn giá hiện tại.
2. Dùng client khác đặt giá để kích hoạt cạnh tranh.
3. Kiểm tra server log xem auto-bid agent có được đăng ký không.
4. Kiểm tra auto-bid có bị giới hạn bởi max bid không.
5. Kiểm tra response `AUTO_BID_SET_SUCCESS` hoặc lỗi tương ứng.

---

## 18. Hủy auto-bidding không có tác dụng

### Nguyên nhân thường gặp

* Client không gửi request hủy lên server.
* Server hủy bằng sai định danh người dùng.
* Server không xóa auto-bid agent khỏi queue.
* Client cập nhật UI như đã hủy nhưng server chưa hủy thật.
* Đang chạy server `.jar` cũ.

### Cách xử lý

1. Bấm hủy auto-bid và kiểm tra server có nhận request không.
2. Đảm bảo server hủy theo `userId`, không dùng `username` hoặc display name.
3. Server cần trả response rõ ràng:

   * `AUTO_BID_CANCEL_SUCCESS`
   * `AUTO_BID_CANCEL_FAILED`
4. Client chỉ nên tắt UI auto-bid sau khi nhận success từ server.
5. Nếu đã sửa code, tắt toàn bộ Java và build lại:

```powershell
Get-Process java | Stop-Process -Force
mvn clean package -DskipTests
```

---

## 19. Phiên không tự kết thúc khi hết giờ

### Nguyên nhân thường gặp

* Timer server không chạy.
* Client chỉ đếm ngược UI nhưng server không cập nhật state.
* Server không broadcast sự kiện kết thúc phiên.
* Thread scheduler bị lỗi.
* Thời gian hệ thống sai.

### Cách xử lý

1. Kiểm tra server có cơ chế kiểm tra phiên hết hạn không.
2. Kiểm tra log khi countdown về 0.
3. Kiểm tra client có nhận message phiên kết thúc không.
4. Đảm bảo logic kết thúc nằm ở server, không chỉ ở client.
5. Kiểm tra giờ hệ thống của máy chạy server.

---

## 20. Chat trong phòng không hoạt động

### Nguyên nhân thường gặp

* Client không gửi chat message lên server.
* Server không route message chat.
* Server không broadcast chat cho phòng.
* Client không có handler hiển thị chat message.
* Người dùng chưa vào phòng nhưng đã gửi chat.

### Cách xử lý

1. Kiểm tra server log khi gửi chat.
2. Kiểm tra message type chat có được router xử lý không.
3. Kiểm tra broadcast có gửi cho các client trong cùng room không.
4. Kiểm tra UI chat có append message đúng không.
5. Test bằng hai client trong cùng phòng.

---

## 21. Chatbot không trả lời

### Nguyên nhân thường gặp

* Python chưa được cài.
* Thiếu thư viện AI.
* Script chatbot không chạy được.
* Sai đường dẫn input/output JSON.
* Model/vectorizer chưa được train hoặc chưa tồn tại.
* LLM/Ollama chưa chạy nếu chatbot dùng fallback LLM.

### Cách xử lý

1. Kiểm tra Python:

```bash
python --version
```

2. Kích hoạt môi trường ảo:

```powershell
cd Auction_AI
.venv\Scripts\Activate.ps1
```

3. Cài thư viện:

```bash
pip install -r requirements.txt
```

4. Chạy thử script chatbot trực tiếp nếu có.
5. Kiểm tra file input/output JSON có được tạo không.
6. Nếu dùng Ollama, kiểm tra Ollama đã chạy chưa.

---

## 22. AI auto-approve không chạy

### Nguyên nhân thường gặp

* Thiếu thư viện Python.
* Sai đường dẫn script Python.
* Sai đường dẫn file input/output.
* Model `.pkl` hoặc `.joblib` chưa tồn tại.
* Java gọi Python bằng command không phù hợp với máy hiện tại.
* Python command là `python` trên máy này nhưng là `py` trên máy khác.

### Cách xử lý

1. Chạy thử script AI thủ công trong terminal.
2. Kiểm tra file model đã tồn tại chưa.
3. Kiểm tra Java đang gọi đúng lệnh Python không.
4. Kiểm tra thư mục working directory khi Java gọi Python.
5. Nếu cần, đổi command từ `python` sang `py` hoặc ngược lại tùy máy.

---

## 23. Lỗi thiếu file model AI

### Biểu hiện

Python báo lỗi dạng:

```text
FileNotFoundError
No such file or directory: model.pkl
```

### Nguyên nhân

Model chưa được train, chưa được commit vào repo, hoặc đường dẫn model sai.

### Cách xử lý

1. Kiểm tra file model có tồn tại trong thư mục AI không.
2. Nếu repo có script train, chạy lại script train.
3. Kiểm tra đường dẫn model trong script inference.
4. Không đổi tên file model nếu code đang hard-code tên đó.

---

## 24. Lỗi database hoặc DAO

### Biểu hiện

Server báo lỗi kết nối database, đăng nhập lỗi hoặc không lấy được dữ liệu.

### Nguyên nhân thường gặp

* Database chưa chạy.
* Sai username/password DB.
* Sai database name.
* Thiếu table.
* Project đang được cấu hình dùng DB nhưng máy mới chưa setup DB.

### Cách xử lý

1. Kiểm tra hệ thống hiện tại có bắt buộc dùng DB không.
2. Nếu có fallback HashMap, kiểm tra fallback có được bật không.
3. Nếu dùng DB, tạo database và import schema/data mẫu.
4. Kiểm tra file cấu hình kết nối DB.
5. Đảm bảo MySQL service đang chạy nếu dùng MySQL.

---

## 25. Lỗi do vừa sửa FXML/CSS

### Biểu hiện

Trước đó chạy được, sau khi sửa giao diện thì client lỗi khi mở màn hình.

### Nguyên nhân thường gặp

* Xóa nhầm `fx:id`.
* Đổi tên method `onAction`.
* Xóa node mà controller vẫn dùng.
* CSS sai cú pháp.
* Đường dẫn ảnh sai.
* FXML khai báo controller sai.

### Cách xử lý

1. Xem stack trace báo file và dòng nào.
2. Kiểm tra lại thay đổi gần nhất trong FXML/CSS.
3. Không xóa `fx:id` nếu chưa kiểm tra controller.
4. Không đổi `onAction` nếu chưa đổi method tương ứng.
5. Dùng Git để xem diff:

```bash
git diff
```

6. Nếu cần, revert riêng file FXML/CSS vừa sửa.

---

## 26. Lỗi do protocol/message không khớp giữa client và server

### Biểu hiện

Client gửi request nhưng server không xử lý, hoặc server gửi response nhưng client không cập nhật UI.

### Nguyên nhân

Tên message type/action ở client và server không giống nhau.

Ví dụ:

```text
Client gửi: JOIN_ROOM
Server chờ: ROOM_JOIN
```

### Cách xử lý

1. Kiểm tra message type được gửi từ client.
2. Kiểm tra router/handler phía server có case tương ứng không.
3. Kiểm tra response từ server.
4. Kiểm tra message handler phía client có xử lý response đó không.
5. Khi đổi tên protocol, phải cập nhật cả client, server và common nếu có.

---

## 27. Lỗi UI bị treo hoặc không cập nhật

### Nguyên nhân thường gặp

* Cập nhật JavaFX UI từ thread nền.
* Request gửi đi nhưng không có response.
* Client đang chờ pending state nhưng server không trả kết quả.
* Exception bị nuốt trong listener.

### Cách xử lý

1. Kiểm tra terminal client có exception không.
2. Đảm bảo cập nhật UI trong `Platform.runLater`.
3. Nếu button bị pending mãi, kiểm tra server có trả success/fail không.
4. Kiểm tra client có handler cho cả success và failed response không.
5. Không cập nhật UI thành công trước khi server xác nhận đối với nghiệp vụ quan trọng.

---

## 28. Lỗi chạy nhiều client trên cùng máy

### Biểu hiện

Client thứ nhất chạy được, client thứ hai lỗi hoặc không đăng nhập được.

### Nguyên nhân

* Client cũng cố mở port cố định.
* Hai client dùng chung file state tạm.
* Session không được tách riêng.
* Server giới hạn kết nối.

### Cách xử lý

1. Đảm bảo chỉ server mở port lắng nghe.
2. Client chỉ kết nối outbound đến server.
3. Mở client bằng terminal riêng.
4. Đăng nhập bằng tài khoản khác nhau.
5. Kiểm tra server log có nhận nhiều connection không.

---

## 29. Lỗi Git sau khi pull code

### Biểu hiện

Pull bị conflict hoặc project không build được sau khi merge.

### Cách xử lý

Kiểm tra file đang conflict:

```bash
git status
```

Mở từng file conflict, xử lý các đoạn:

```text
<<<<<<< HEAD
...
=======
...
>>>>>>> branch-name
```

Sau khi xử lý:

```bash
git add .
git commit -m "resolve merge conflicts"
```

Nếu muốn bỏ toàn bộ thay đổi local chưa commit:

```bash
git reset --hard
git pull origin main
```

Cẩn thận: lệnh `git reset --hard` sẽ xóa thay đổi local chưa commit.

---

## 30. Checklist debug nhanh

Khi có lỗi, kiểm tra theo thứ tự:

```text
1. Đã pull code mới nhất chưa?
2. Đã build lại bằng mvn clean package chưa?
3. Server đã chạy chưa?
4. Client có trỏ đúng host/port không?
5. Có server cũ đang chiếm port không?
6. Terminal server có nhận request không?
7. Terminal client có exception không?
8. Message type giữa client-server có khớp không?
9. Handler server có xử lý request đó không?
10. Client có handler xử lý response không?
11. Nếu liên quan AI, Python và model đã sẵn sàng chưa?
12. Nếu liên quan UI, FXML/CSS có lỗi sau lần sửa gần nhất không?
```

---

## 31. Lệnh dọn và chạy lại từ đầu trên Windows

Khi gặp lỗi khó xác định, có thể dùng quy trình sau:

```powershell
Get-Process java | Stop-Process -Force
mvn clean package -DskipTests
java -jar auction-server\target\<TEN_FILE_SERVER>.jar
```

Sau đó mở terminal thứ hai:

```powershell
cd AuctionSystem
java -jar auction-client\target\<TEN_FILE_CLIENT>.jar
```

Nếu chạy bằng Maven:

```powershell
mvn -pl auction-client javafx:run
```

---

## 32. Nguyên tắc khi sửa lỗi

Khi fix bug, cần tuân thủ:

* Không sửa UI để che lỗi backend.
* Không cho client tự quyết định nghiệp vụ thay server.
* Không đổi protocol nếu chưa cập nhật cả hai phía.
* Không xóa class hoặc handler khi chưa kiểm tra còn được route tới hay không.
* Không sửa AI nếu lỗi nằm ở đường dẫn hoặc môi trường.
* Luôn test lại ít nhất một luồng đầy đủ sau khi sửa.

Luồng test tối thiểu:

```text
Đăng nhập → Vào lobby → Tạo/duyệt phiên → Vào phòng → Đặt giá → Realtime update → Kết thúc phiên
```

## 👨‍💻 Thông Tin Tác Giả & Phân Công

### Thông Tin Dự Án
- **Repository**: [25023189-bit/Auction-System-OOD](https://github.com/25023189-bit/Auction-System-OOD)
- **Bài tập**: Lập Trình Nâng Cao - Object-Oriented Design
- **Architecture**: Multi-module Maven (auction-common, auction-server, auction-client)
- **Database**: MySQL `aivien`
- **Server Port**: 5000
- **Language Composition**: Java 82.7%, Python 11.0%, CSS 6.3%

### Phân Công Công Việc

| Tác Giả | Trách Nhiệm |
|--------|-----------|
| **Nguyễn Hữu Hùng** | Đăng nhập, Đăng ký, AI ChatBot, Tạo phiên, Biến động số dư, Đặt giá, Kết thúc phiên, Phân quyền Admin |
| **Tô Bảo Hân** | Lập trình mạng, Xử lý Server, Đa luồng (Multithreading), Quản trị trạng thái hệ thống, Quản lý mã nguồn |
| **Bùi Thế Dũng** | Database design (aivien), Kết nối database, Lấy lại mật khẩu bằng email, Testing & debugging |
| **Đặng Đức Anh** | UI/UX JavaFX (Login, Lobby, Room, Dashboard), Thiết kế giao diện |

---

## 📜 License

This project is part of a university assignment. All rights reserved.

---

## 🔗 Tham Khảo Ý Tưởng & Thiết Kế

**Auction Platform**:
- https://www.bpiauctions.com/
- https://www.namecheap.com/
- https://dgts.moj.gov.vn/
- https://daugiaviet.vn/home
- https://www.ebay.com/
- https://www.catawiki.com/en
- https://www.dealdash.com/

**Social & Commerce**:
- https://www.facebook.com/
- https://www.tiktok.com/
- https://www.instagram.com/
- https://www.amazon.com/

---

**Cập nhật lần cuối**: 25/05/2026
**Database**: aivien
**Language Composition**: Java 82.7%, Python 11.0%, CSS 6.3%

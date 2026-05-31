# Auction System - OOD (Object-Oriented Design)

## 📋 Mô Tả Dự Án

### Bài Toán
Đây là project hệ thống đấu giá realtime sử dụng **JavaFX** cho client, **Socket** cho server và **MySQL** cho database.

### Link Giới thiệu Hệ Thống
📖 [Xem báo cáo chi tiết](https://drive.google.com/file/d/1jm25PCK1x3LAd_zeir_PhlkSpqa3lszx/view)

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
- 📖 **Database Schema & DAO Guide**: [DATABASE_SCHEMA_AND_DAO_GUIDE.md](./DATABASE_SCHEMA_AND_DAO_GUIDE.md)
- 📊 **Function Definitions**: [auction_system_functions.json](./auction_system_functions.json)
- 📚 **PlantUML Diagrams**:
  - [system-overview.puml](./system-overview.puml)
  - [server-core.puml](./server-core.puml)
  - [client-mvc.puml](./client-mvc.puml)
- 📊 **StarUML Class Diagram**: [AuctionSystem_AllClasses_StarUML_Final.mdj](./AuctionSystem_AllClasses_StarUML_Final.mdj)

### Link Video Demo
- 🎥 **Video hướng dẫn sử dụng**: [Xem Demo Video](#)
- 🎥 **Video demo Server/Client startup**: [Xem Demo Video](#)
- 🎥 **Video demo Anti-sniping & realtime bid**: [Xem Demo Video](#)

*Thay thế link video bằng link thực tế trên YouTube/Google Drive*

---

## 🔄 Quy Tắc Nghiệp Vụ Chính

### 1. Trạng Thái Phiên Đấu Giá

| Trạng thái | Ý nghĩa |
|-----------|---------|
| OPEN | Phiên đã tạo nhưng chưa bắt đầu |
| RUNNING | Phiên đang diễn ra |
| SOLD | Phiên kết thúc và bán thành công |
| UNSOLD | Phiên kết thúc nhưng không có người mua |
| CLOSED_BY_SELLER | Seller chủ động đóng phiên |
| CANCELED_BY_ADMIN | Admin hủy phiên |

### 2. Quy Tắc Tiền

| Trường hợp | Kết Quả |
|----------|--------|
| Seller đóng phiên | Không chuyển tiền |
| Admin hủy phiên | Không chuyển tiền |
| Hết giờ + có người thắng | Trừ tiền bidder thắng, cộng tiền cho seller |
| Hết giờ + không có ai bid | Không chuyển tiền |

### 3. Anti-Sniping

- Trong **30 giây cuối** của phiên, hệ thống **chặn người mới** tham gia
- Nếu có bid hợp lệ ở giai đoạn cuối, phiên sẽ được **gia hạn** thêm (mặc định 30-120s)
- **Số lần gia hạn không bị giới hạn** nếu vẫn có bid hợp lệ
- Các **participant đã join** vẫn được phép bid cho đến khi phiên thực sự kết thúc

### 4. Quy Tắc Tạo Phiên

- Seller nhập **giá khởi điểm** (starting_price)
- Seller nhập **bước giá** (bid_step)
- Seller nhập **số tiền tối thiểu** để tham gia (minimum_join_amount)
- **Constraint**:
  - `minimum_join_amount` > 10% của `starting_price`
  - `minimum_join_amount` < 75% của `starting_price`
- Validation được thực hiện bởi lớp `AuctionCreationValidator`

### 5. Khi Hết Thời Gian

- Hệ thống **tự động chốt phiên**
- Dữ liệu phiên vẫn được **lưu trong database**
- **Toàn bộ seller & bidder** trong phiên được **đưa về sảnh**
- Phiên **biến mất khỏi danh sách** đang hoạt động trên UI

---

## 🔧 Troubleshooting

### Vấn đề kết nối Server
```
Error: Connection refused
Solution: 
- Kiểm tra server đã khởi động chưa
- Kiểm tra port 5000 không bị chiếm bởi ứng dụng khác
- Kiểm tra firewall có chặn port không
```

### Vấn đề Database
```
Error: No suitable driver found / SQLNonTransientConnectionException
Solution:
- Kiểm tra MySQL service đang chạy
- Kiểm tra file application.properties có cấu hình đúng
- Kiểm tra có tạo database "aivien" chưa
- Kiểm tra user/password MySQL đúng
```

### Vấn đề JavaFX GUI
```
Error: JavaFX initialization failed
Solution:
- Kiểm tra JDK 25 có cài module JavaFX
- Kiểm tra javafx dependency trong pom.xml
```

### Port 5000 bị chiếm
```
Error: Address already in use
Solution:
- Thay đổi server.port trong application.properties
- Hoặc kill process chiếm port 5000
```

### Không thấy Room trong Lobby
```
Reason: 
- Server chưa tạo phiên (hoặc phiên chưa được Admin duyệt)
- Phiên đã kết thúc/bị đóng
- Bidder không có đủ tiền để join phiên
Solution:
- Seller tạo phiên mới → Admin duyệt
- Bidder check số dư tài khoản
```

---

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

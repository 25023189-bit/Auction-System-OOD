## 📦 Phân tích chuyên sâu: `Message.java` (Giao thức & Trái tim hệ thống)

Để team dễ dàng kế thừa và phát triển dự án, mọi người cần nắm vững bản chất của lớp `Message`. Đây không chỉ là một class thông thường, mà nó là **Hợp đồng giao tiếp (Protocol)** duy nhất giữa Client và Server.



### 1. Vị trí của Message trong kiến trúc MVC & Client-Server
Dự án áp dụng mô hình **MVC (Model - View - Controller)** phân tán trên nền tảng **Client-Server**:
* **View (JavaFX):** Giao diện người dùng (`.fxml`).
* **Controller (Client/Server):** `AuctionController` (nhận sự kiện UI) và `ClientHandler` (nhận luồng mạng).
* **Model:** Các thực thể như `AuctionRoom`, `User`...
* **🌟 Vai trò của Message:** Trong môi trường mạng, Controller ở Client và Controller ở Server nằm trên 2 máy ảo Java (JVM) khác nhau, chúng không thể gọi hàm trực tiếp của nhau. Do đó, `Message` đóng vai trò là "Người vận chuyển" (DTO - Data Transfer Object) mang dữ liệu (Model) từ Client bọc lại, truyền qua mạng (Socket) để gửi tới Server xử lý, và ngược lại.

### 2. Cấu trúc đóng gói (Payload Structure)
Mỗi gói `Message` được thiết kế cực kỳ tối giản và linh hoạt với 3 thành phần:
* `action` (String): Mệnh lệnh điều hướng. Đóng vai trò như các Endpoint/URL trong RESTful API (VD: `"LOGIN"`, `"BID"`, `"CREATE_AUCTION"`). Giúp `ClientHandler` rẽ nhánh `switch-case` chính xác.
* `id` (String): Định danh ngữ cảnh (VD: Tên username người gửi, hoặc "SERVER" nếu từ hệ thống gửi về).
* `data` (Object): Khối dữ liệu payload. Vì dùng kiểu `Object`, chúng ta có thể nhét bất cứ thứ gì vào đây (String, Double, ArrayList, hoặc một object `AuctionRoom`). Khi nhận, **bắt buộc phải ép kiểu (Casting)** lại cho đúng.

---

### 3. Bản chất Truyền thông tin & Luồng I/O (I/O Streams)



Để truyền một object `Message` qua dây cáp mạng (Socket TCP/IP), chúng ta áp dụng cơ chế **Luồng vào - ra (I/O Streams)** của Java:
* **Tại đầu gửi (`ObjectOutputStream`):** Đối tượng `Message` đang nằm trong RAM (không gian 3D) sẽ được **Tuần tự hóa (Serialization)** thành một chuỗi byte (mảng 1 chiều) để bơm vào đường truyền mạng.
* **Tại đầu nhận (`ObjectInputStream`):** Chuỗi byte được đọc lên và **Giải tuần tự hóa (Deserialization)** để phục dựng lại thành đối tượng `Message` nguyên vẹn.

> ⚠️ **ĐIỀU KIỆN TIÊN QUYẾT:** Để làm được điều trên, lớp `Message` và MỌI lớp được nhét vào biến `data` (như `User`, `AuctionRoom`) đều **BẮT BUỘC** phải `implements java.io.Serializable`. Lệnh `private static final long serialVersionUID = 1L;` giúp đồng bộ phiên bản code giữa Client và Server.

---

### 4. Bắt buộc xử lý Ngoại lệ (Checked Exceptions)
Theo kiến thức bài giảng, **Ngoại lệ (Exception)** là sự kiện phá vỡ *luồng hoạt động mong đợi* của chương trình. Khi làm việc với mạng (Socket) và luồng I/O, hệ thống cực kỳ dễ bị phá vỡ luồng do các tác nhân bên ngoài (đứt cáp, Client tắt máy đột ngột, v.v.).

Vì vậy, Java định nghĩa các thao tác đọc/ghi `Message` sẽ sinh ra **Ngoại lệ được kiểm tra (Checked Exception)**. Team code bắt buộc phải dùng khối `try-catch` để xử lý các lỗi sau, tránh việc ứng dụng bị crash (văng):

* **`IOException`:** Thường gặp nhất khi đang gọi `in.readObject()` hoặc `out.writeObject()`. Báo hiệu đường truyền mạng đã bị ngắt. Cách xử lý lý tưởng trong catch là đóng các luồng I/O, ngắt Socket và gỡ Client đó khỏi danh sách online của Server.
* **`ClassNotFoundException`:** Xảy ra khi Server nhận được chuỗi byte của `Message` nhưng không tìm thấy file `Message.java` tương ứng trong cấu trúc thư mục của nó để phục dựng.

> 💡 **Best Practice cho Team:** Khi gọi hàm gửi đi (`out.writeObject(msg)`), luôn luôn ghi kèm `out.flush()` để đẩy dữ liệu ra khỏi bộ đệm ngay lập tức, và dùng `out.reset()` nếu cần gửi lặp lại cùng một object để tránh bộ đệm (cache) của luồng I/O gửi dữ liệu cũ.
# Bộ UML dùng để nộp và thuyết trình

File StarUML nên dùng để nộp/thuyết trình là:

- `AuctionSystem_Presentation_StarUML.mdj`

File `AuctionSystem_AllClasses_StarUML_Final.mdj` vẫn được giữ để tham khảo, nhưng các sơ đồ cũ đã được đổi tiền tố `Reference - ...` để tránh mở nhầm khi thuyết trình.

Dùng các file PlantUML sau:

1. `system-overview.puml` - sơ đồ tổng quan kiến trúc Client-Server.
2. `client-mvc.puml` - sơ đồ MVC phía client JavaFX.
3. `server-core.puml` - sơ đồ server socket, router và handler.
4. `service-layer.puml` - sơ đồ service và DAO phía server.
5. `model-dto-layer.puml` - sơ đồ model, DTO và role policy dùng chung.

Nguyên tắc trình bày:

- Mỗi sơ đồ chỉ giải thích một ý chính.
- Không đưa `Label`, `Button`, `TextField`, `FXMLLoader`, `Logger`, `Socket stream` vào sơ đồ thuyết trình.
- Không hiển thị toàn bộ getter/setter.
- Không gom client, server, database, model và thư viện ngoài vào cùng một class diagram.
- Khi cần nói về mô hình MVC, dùng `client-mvc.puml`, không dùng All Classes.

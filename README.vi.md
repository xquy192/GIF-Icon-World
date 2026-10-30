# GIF Icon World

![Logo GIF Icon World](src/main/resources/assets/gif_icon_world/icon.png)

Mod Fabric phía client cho Minecraft Java 26.3, cho phép mỗi thế giới trong danh sách Chơi đơn dùng biểu tượng GIF động.

## Tính năng

- Tự nhận diện `icon.gif` trong thư mục save; không cần resource pack hoặc đổi sang PNG.
- Giữ `icon.png` làm ảnh dự phòng, không xóa ảnh gốc.
- Có nút **GIF Icon World** trong màn hình **Chỉnh sửa thế giới**.
- Nhập GIF bằng hộp thoại chọn file hoặc kéo-thả vào Minecraft.
- Xem trước animation trước khi lưu.
- Chỉnh tốc độ, frame đầu/cuối, vị trí X/Y, zoom, cách căn ảnh và chế độ phát.
- Lưu thiết lập riêng trong `gif_icon_world.json` của từng world.

## Yêu cầu

- Minecraft Java 26.3
- Fabric Loader 0.20.1 trở lên
- Java 25 trở lên
- Chỉ cần cài ở client

## Cài đặt

1. Cài profile Fabric 26.3 và mở game một lần.
2. Tải JAR ở mục Releases khi đã có bản phát hành, hoặc tự build từ source.
3. Chép JAR vào thư mục `mods` của profile.
4. Mở Minecraft và vào **Chơi đơn**.

Hướng dẫn chi tiết: [docs/INSTALLATION.md](docs/INSTALLATION.md).

## Sử dụng nhanh

Đặt file `icon.gif` cạnh `level.dat` trong thư mục thế giới:

```text
.minecraft/saves/TenTheGioi/
├── level.dat
├── icon.png
├── icon.gif
└── gif_icon_world.json
```

Hoặc vào **Chơi đơn → chọn world → Chỉnh sửa → GIF Icon World**, chọn/kéo GIF, chỉnh trong màn hình xem trước rồi nhấn **Lưu vào thế giới**. File JSON được tạo khi lưu thiết lập. Không cần xóa `icon.png`.

## Các tuỳ chỉnh

| Mục | Ý nghĩa |
| --- | --- |
| Tốc độ | 0.1× đến 4.0× |
| Frame bắt đầu/kết thúc | Khoảng frame được phát, đánh số từ 0 |
| Vị trí X/Y | Dịch ảnh trong icon |
| Zoom | Phóng to hoặc thu nhỏ |
| Căn ảnh | Vừa khung, phủ khung hoặc kéo giãn |
| Chế độ phát | Lặp vô hạn, tiến-lùi hoặc chạy một lần |

Icon được kết xuất về 64×64 pixel; GIF nguồn hỗ trợ độ phân giải khác nhau. Xem [docs/SETTINGS.md](docs/SETTINGS.md).

## Build

Windows: chạy `build.bat` ở thư mục dự án. Script kiểm tra JDK 25 và Gradle 9.8.2, sau đó chạy `clean build`. JAR được tạo trong `build/libs/`.

Build thủ công cần JDK 25 và Gradle 9.8.2:

```sh
gradle --no-daemon clean build
```

Xem [docs/BUILDING.md](docs/BUILDING.md) và [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md).

## Liên hệ

- GitHub: [xquy192](https://github.com/xquy192)
- Discord: `xuanquy192_21009`
- Email: [dinhxuanquy192@gmail.com](mailto:dinhxuanquy192@gmail.com)

Email được công khai trong repository. Báo lỗi nên tạo GitHub Issue và đính kèm `latest.log` sau khi xoá thông tin riêng tư.

## Giấy phép

[Mạng MIT](LICENSE).

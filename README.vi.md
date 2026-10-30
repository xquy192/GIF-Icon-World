# GIF Icon World

![Logo GIF Icon World](src/main/resources/assets/gif_icon_world/icon.png)

GIF Icon World là mod Fabric chạy phía client cho Minecraft Java 26.3. Mod cho phép dùng ảnh GIF động làm biểu tượng của từng thế giới trong danh sách Chơi đơn.

Bạn có thể đặt file `icon.gif` vào thư mục save hoặc mở **Chỉnh sửa thế giới → GIF Icon World** để nhập ảnh. Mod cập nhật biểu tượng đang được Minecraft hiển thị cho thế giới đó. File `icon.png` gốc vẫn được giữ lại và sẽ được dùng khi GIF không đọc được.

## Tính năng

- Tự tìm `icon.gif` trong thư mục của từng thế giới.
- Nhập GIF bằng hộp thoại chọn file hoặc kéo-thả vào màn hình mod.
- Xem trước ảnh động trước khi lưu.
- Chỉnh tốc độ phát, frame đầu và cuối, vị trí X/Y, zoom, cách căn ảnh và chế độ phát.
- Lưu thiết lập riêng cho từng thế giới trong `gif_icon_world.json`.
- Kiểm tra khi file GIF hoặc file thiết lập thay đổi trong lúc danh sách thế giới đang mở.
- Dùng lại biểu tượng tĩnh khi GIF bị thiếu hoặc không đọc được.

## Yêu cầu

- Minecraft Java 26.3
- Fabric Loader 0.20.1 trở lên
- Java 25 trở lên
- Chỉ cần cài ở client

## Cài đặt

1. Mở profile Fabric 26.3 một lần.
2. Tải JAR trong mục Releases khi có bản phát hành, hoặc tự build từ source.
3. Chép file JAR vào thư mục `mods` của profile.
4. Mở Minecraft và vào **Chơi đơn**.

Không chép file source ZIP vào `mods`. Minecraft cần file JAR đã build. Mod không cần cài trên dedicated server.

Hướng dẫn chi tiết: [docs/INSTALLATION.md](docs/INSTALLATION.md).

## Dùng GIF làm biểu tượng

Mở thư mục save của thế giới rồi đặt file GIF cạnh `level.dat`, tên file là `icon.gif`:

```text
.minecraft/saves/TenTheGioi/
├── level.dat
├── icon.png
├── icon.gif
└── gif_icon_world.json
```

Bạn cũng có thể vào **Chơi đơn → chọn thế giới → Chỉnh sửa → GIF Icon World**. Chọn file GIF hoặc kéo file vào khung, chỉnh trong màn hình xem trước rồi nhấn **Lưu**. File thiết lập được tạo khi bạn lưu. Không cần xóa `icon.png`.

## Thiết lập GIF

| Mục | Ý nghĩa |
| --- | --- |
| Tốc độ | Từ 0.1× đến 4.0× |
| Frame bắt đầu/kết thúc | Chọn đoạn animation cần phát, đánh số từ 0 |
| Vị trí X/Y | Dịch ảnh trong khung icon |
| Zoom | Phóng to hoặc thu nhỏ ảnh |
| Căn ảnh | Vừa khung, phủ khung hoặc kéo giãn |
| Chế độ phát | Lặp, chạy tiến-lùi hoặc phát một lần |

Ảnh hiển thị được kết xuất về khung 64×64 pixel. GIF đầu vào có thể dùng độ phân giải khác nhau, trong giới hạn đọc file của mod. Các giới hạn và tùy chọn được ghi trong [docs/SETTINGS.md](docs/SETTINGS.md).

## Build

Trên Windows, chạy `build.bat` trong thư mục dự án. Script kiểm tra JDK 25 và Gradle 9.8.2, sau đó chạy `clean build`. File JAR nằm trong `build/libs/`.

Build thủ công cần JDK 25 và Gradle 9.8.2:

```sh
gradle --no-daemon clean build
```

Xem [docs/BUILDING.md](docs/BUILDING.md) và [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md).

## Liên hệ

- GitHub: [xquy192](https://github.com/xquy192)
- Discord: `xuanquy192_21009`
- Email: [dinhxuanquy192@gmail.com](mailto:dinhxuanquy192@gmail.com)

## Giấy phép

[MIT](LICENSE).

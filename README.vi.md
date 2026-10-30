# GIF Icon World

![Logo GIF Icon World](src/main/resources/assets/gif_icon_world/icon.png)

GIF Icon World cho phép dùng ảnh GIF động làm biểu tượng cho từng thế giới trong danh sách Chơi đơn của Minecraft Java 26.3. Mod chạy ở phía client và dùng Fabric.

Bạn có thể đặt file `icon.gif` cạnh `level.dat` trong thư mục save. Hoặc mở **Chỉnh sửa thế giới → GIF Icon World**, chọn file GIF hoặc kéo thả ảnh vào khung. Khi lưu, mod chép ảnh vào thế giới với tên `icon.gif` và ghi thiết lập vào `gif_icon_world.json`.

File `icon.png` của Minecraft vẫn được giữ nguyên. Nếu GIF bị thiếu hoặc không đọc được, biểu tượng tĩnh sẽ được dùng.

## Chỉnh sửa ảnh động

Trong màn hình chỉnh sửa, bạn có thể đổi tốc độ phát, chọn frame đầu và cuối, di chuyển ảnh theo trục X/Y và chỉnh zoom. Có thêm tùy chọn căn ảnh và cách phát animation.

- Căn ảnh: `CONTAIN`, `COVER`, `STRETCH`
- Chế độ phát: `LOOP`, `PING_PONG`, `ONCE`
- Tốc độ: 0.1× đến 4.0×
- Zoom: 0.5× đến 2.0×

Khung xem trước hiển thị ảnh trước khi bạn lưu vào thế giới.

## Yêu cầu

- Minecraft Java 26.3
- Fabric Loader 0.20.1 trở lên
- Java 25 trở lên
- Chỉ cài ở client

## Cài đặt

1. Mở profile Fabric 26.3 một lần.
2. Tải file JAR trong mục Releases khi có bản phát hành, hoặc tự build từ source.
3. Chép JAR vào thư mục `mods` của profile.
4. Mở Minecraft và vào **Chơi đơn**.

Minecraft cần file JAR đã build. Không chép source ZIP vào thư mục `mods`. Mod không cần cài trên dedicated server.

Hướng dẫn: [docs/INSTALLATION.md](docs/INSTALLATION.md).

## Đặt GIF thủ công

Mở thư mục save của thế giới rồi đặt file GIF cạnh `level.dat`. Đổi tên file thành `icon.gif`.

```text
.minecraft/saves/TenTheGioi/
├── level.dat
├── icon.png
├── icon.gif
└── gif_icon_world.json
```

Bạn không cần tự tạo `gif_icon_world.json`. File này được tạo khi lưu thiết lập trong editor.

## Thiết lập GIF

| Mục | Ý nghĩa |
| --- | --- |
| Tốc độ | 0.1× đến 4.0× |
| Frame bắt đầu/kết thúc | Chọn đoạn animation cần phát, đánh số từ 0 |
| Vị trí X/Y | Dịch ảnh trong khung icon |
| Zoom | Phóng to hoặc thu nhỏ ảnh |
| Căn ảnh | Vừa khung, phủ khung hoặc kéo giãn |
| Chế độ phát | Lặp, chạy tiến-lùi hoặc phát một lần |

Ảnh trên world list được kết xuất về khung 64×64 pixel. GIF đầu vào có thể có độ phân giải khác nhau, trong giới hạn đọc file của mod. Xem [docs/SETTINGS.md](docs/SETTINGS.md) để biết giới hạn và định dạng file thiết lập.

## Build

Trên Windows, chạy `build.bat` trong thư mục dự án. Script kiểm tra JDK 25 và Gradle 9.8.2, sau đó chạy `clean build`. File JAR nằm trong `build/libs/`.

Build thủ công cần JDK 25 và Gradle 9.8.2:

```sh
gradle --no-daemon clean build
```

Hướng dẫn nằm trong [docs/BUILDING.md](docs/BUILDING.md).

## Liên kết

- Mã nguồn: https://github.com/xquy192/GIF-Icon-World
- Báo lỗi: https://github.com/xquy192/GIF-Icon-World/issues
- Discord: `xuanquy192_21009`
- Email: [dinhxuanquy192@gmail.com](mailto:dinhxuanquy192@gmail.com)

## Giấy phép

[MIT](LICENSE)

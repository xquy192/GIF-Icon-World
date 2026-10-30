# GIF Icon World

![GIF Icon World](src/main/resources/assets/gif_icon_world/icon.png)

GIF Icon World adds animated GIF icons to the Singleplayer world list in Minecraft Java 26.3. It is a client-side Fabric mod.

Place a file named `icon.gif` beside `level.dat` in a world's save folder. You can also open **Edit World → GIF Icon World** and import a GIF there. The editor saves the image as `icon.gif` and writes its settings to `gif_icon_world.json`.

Minecraft's regular `icon.png` stays in the save folder. The mod uses it when no readable GIF is available.

## Editor controls

You can change the playback speed, choose the first and last frames, move the image, and adjust its zoom. The editor also has fit and playback modes.

- Fit: `CONTAIN`, `COVER`, `STRETCH`
- Playback: `LOOP`, `PING_PONG`, `ONCE`
- Speed: 0.1× to 4.0×
- Position: X and Y offsets
- Zoom: 0.5× to 2.0×

The preview shows the GIF before you save it.

## Requirements

- Minecraft Java 26.3
- Fabric Loader 0.20.1 or newer
- Java 25 or newer
- Client installation

## Build

On Windows, run `build.bat`. It checks for Java and Gradle, then runs a clean build. The JAR is placed in `build/libs/`.

For a manual build, use JDK 25 and Gradle 9.8.2:

```sh
gradle --no-daemon clean build
```

See [docs/BUILDING.md](docs/BUILDING.md) for details.

## Documentation

- [Vietnamese README](README.vi.md)
- [Installation](docs/INSTALLATION.md)
- [GIF settings](docs/SETTINGS.md)
- [Development notes](docs/DEVELOPMENT.md)
- [Changelog](CHANGELOG.md)
- [Contributing](CONTRIBUTING.md)

## Links

- Source: https://github.com/xquy192/GIF-Icon-World
- Issues: https://github.com/xquy192/GIF-Icon-World/issues
- Discord: `xuanquy192_21009`
- Email: [dinhxuanquy192@gmail.com](mailto:dinhxuanquy192@gmail.com)

## License

[MIT](LICENSE)

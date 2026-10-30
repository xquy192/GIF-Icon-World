# GIF Icon World

![GIF Icon World](src/main/resources/assets/gif_icon_world/icon.png)

A client-side Fabric mod for Minecraft Java 26.3. It lets each singleplayer save use an animated GIF as its world-list icon.

Drop a file named `icon.gif` into the save folder, or import one from the in-game **Edit World → GIF Icon World** screen. The mod updates Minecraft's existing world icon texture; it does not draw a second icon over the world row. The normal `icon.png` is kept as a fallback.

## Features

- Automatic discovery of `icon.gif` in each save folder.
- Drag-and-drop and native file picker in the world editor.
- Live preview before saving.
- Speed, frame range, X/Y offset, zoom, fit mode and playback controls.
- Per-world settings in `gif_icon_world.json`.
- Periodic file checks and cached GIF decoding.
- Fallback to the regular world icon when a GIF is missing or invalid.

## Requirements

- Minecraft Java 26.3
- Fabric Loader 0.20.1+
- Java 25+
- Client side only

## Build

On Windows, run `build.bat` from the project directory. It prepares Java and Gradle when needed and runs a clean build. The output JAR is placed in `build/libs/`.

For a manual build, use JDK 25 and Gradle 9.8.2:

```sh
gradle --no-daemon clean build
```

See [docs/BUILDING.md](docs/BUILDING.md) for details.

## Documentation

- [Vietnamese README](README.vi.md)
- [Installation](docs/INSTALLATION.md)
- [Settings and GIF limits](docs/SETTINGS.md)
- [Development notes](docs/DEVELOPMENT.md)
- [Build guide](docs/BUILDING.md)
- [Changelog](CHANGELOG.md)
- [Contributing](CONTRIBUTING.md)

## Contact

- GitHub: [xquy192](https://github.com/xquy192)
- Discord: `xuanquy192_21009`
- Email: [dinhxuanquy192@gmail.com](mailto:dinhxuanquy192@gmail.com)

The email address is public in this repository. GitHub Issues are preferred for bug reports.

## License

[MIT](LICENSE)

# GIF Icon World

![GIF Icon World](src/main/resources/assets/gif_icon_world/icon.png)

GIF Icon World is a client-side Fabric mod for Minecraft Java 26.3. It lets each singleplayer save use an animated GIF in the world list.

Put a file named `icon.gif` in a world's save folder, or open **Edit World → GIF Icon World** to import one. The mod updates the icon Minecraft already displays for that save. The original `icon.png` stays in place and is used when the GIF cannot be loaded.

## What it does

- Checks each save folder for `icon.gif`.
- Imports GIFs through the file picker or drag-and-drop.
- Shows a preview before saving.
- Lets you change playback speed, frame range, X/Y position, zoom, fit mode and playback mode.
- Stores settings for each world in `gif_icon_world.json`.
- Checks for changed GIF and settings files while the world list is open.
- Falls back to the static world icon if a GIF is missing or cannot be read.

## Requirements

- Minecraft Java 26.3
- Fabric Loader 0.20.1 or newer
- Java 25 or newer
- Client side only

## Build

On Windows, run `build.bat` from the project folder. It checks for Java and Gradle and runs a clean build. The JAR is written to `build/libs/`.

For a manual build, use JDK 25 and Gradle 9.8.2:

```sh
gradle --no-daemon clean build
```

See [docs/BUILDING.md](docs/BUILDING.md) for setup details.

## Documentation

- [Vietnamese README](README.vi.md)
- [Installation](docs/INSTALLATION.md)
- [GIF settings](docs/SETTINGS.md)
- [Development notes](docs/DEVELOPMENT.md)
- [Build instructions](docs/BUILDING.md)
- [Changelog](CHANGELOG.md)
- [Contributing](CONTRIBUTING.md)

## Project and contact

- Project: [GIF Icon World](https://github.com/xquy192/GIF-Icon-World)
- GitHub: [xquy192](https://github.com/xquy192)
- Discord: `xuanquy192_21009`
- Email: [dinhxuanquy192@gmail.com](mailto:dinhxuanquy192@gmail.com)

## License

[MIT](LICENSE)

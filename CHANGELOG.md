# Changelog

## 1.2.5 - 2026-10-30

### Fixed
- Use the project logo as the Fabric mod icon.
- Place the GIF button below the vanilla Optimize World button and update its position when the screen layout changes.

## 1.2.4 - 2026-10-30

### Fixed
- Add the project logo to the JAR root and point Fabric metadata to it.

## 1.2.3 - 2026-10-30

### Fixed
- Position the GIF Icon World button using the Optimize World button's bounds.
- Update the button position when the Edit World screen opens.

## 1.2.2 - 2026-10-30

### Fixed
- Validate the GIF fit and playback settings.
- Get the save directory through `LevelStorageAccess#getLevelPath(LevelResource.ROOT)`.
- Keep Gradle compiler output in the build log without raising a second PowerShell error.

## 1.2.1

- Add the project logo to the mod metadata.
- Add installation, settings, build and development guides.
- Add a GitHub Actions build workflow and issue templates.
- Reformat settings and playback code.

## 1.2.0

- Add the GIF import screen to Edit World.
- Add file picker and drag-and-drop import.
- Add the editor preview and animation controls.
- Save settings for each world in `gif_icon_world.json`.
- Load `icon.gif` automatically and use `icon.png` when no GIF is available.
- Cache decoded GIFs and check files periodically.

# Changelog

## 1.2.3 - 2026-10-30

### Fixed
- Move the GIF Icon World button below Minecraft's Optimize World button. Its position and size come from the vanilla button.
- Recalculate the position each time the Edit World screen opens, so the button follows the current GUI layout.

## 1.2.2 - 2026-10-30

### Fixed
- Fix enum validation for GIF fit and playback settings.
- Get the save directory with `LevelStorageAccess#getLevelPath(LevelResource.ROOT)`, as required by the Minecraft 26.3 client API.
- Keep Gradle compiler output in the build log without triggering a second PowerShell error.

## 1.2.1

- Add the project logo to the mod metadata and repository.
- Add separate guides for installation, GIF settings, building and development.
- Add a GitHub Actions build workflow and issue templates.
- Reformat settings and playback code without changing the editor controls.

## 1.2.0

- Add the GIF import screen to Edit World.
- Add file picker and drag-and-drop import.
- Add an editor preview with speed, frame range, position, zoom, fit and playback controls.
- Save settings for each world in `gif_icon_world.json`.
- Find `icon.gif` automatically and fall back to `icon.png`.
- Cache decoded GIFs and check files periodically.

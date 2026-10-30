# Changelog

## 1.2.2 - 2026-10-30

### Fixed
- Corrected enum validation calls for GIF fit and playback settings.
- Resolve the world save directory with `LevelStorageAccess#getLevelPath(LevelResource.ROOT)` for the Minecraft 26.3 client API.
- Improved Windows build-helper error reporting so Gradle compiler diagnostics are preserved without a secondary PowerShell `Write-Error` failure.

## 1.2.1

- Added a project logo to the mod metadata and repository assets.
- Reworked the project documentation and added separate install, settings, build and development guides.
- Added a GitHub Actions build workflow and issue templates.
- Cleaned up settings and playback code formatting without changing the editor controls.

## 1.2.0

- Added the GIF import screen to Edit World.
- Added drag-and-drop and native file selection.
- Added a live preview with playback, frame range, speed, offset, zoom and fit controls.
- Added per-world settings in `gif_icon_world.json`.
- Added automatic discovery of `icon.gif` and fallback to `icon.png`.
- Added cached GIF decoding and periodic file checks.

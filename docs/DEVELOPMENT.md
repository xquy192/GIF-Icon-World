# Development notes

GIF Icon World is a client-only mod. Source packages follow the responsibility of each part of the feature.

## Source map

| File | Responsibility |
| --- | --- |
| `gif/GifAnimation.java` | GIF decoding, frame compositing, delay reading and 64×64 output rendering. |
| `gif/GifIconSettings.java` | Per-world JSON load, validation and save. |
| `gif/WorldGifIconManager.java` | Decoded animation cache, per-icon playback and texture uploads. |
| `mixin/EditWorldScreenMixin.java` | Adds the editor entry to Minecraft's Edit World screen. |
| `mixin/WorldListEntryMixin.java` | Updates the existing favicon before the world row is extracted. |
| `screen/GifIconImportScreen.java` | File picker, drag-and-drop and opening the save folder. |
| `screen/GifIconEditorScreen.java` | Preview and animation controls. |

## Runtime flow

1. The world-list mixin resolves the selected save directory and its vanilla `FaviconTexture`.
2. The manager checks `icon.gif` and `gif_icon_world.json`.
3. A changed GIF is decoded and composited once.
4. The playback clock and settings determine which frame should be displayed.
5. Only a changed frame is converted to `NativeImage` and uploaded to the existing vanilla texture.
6. If no valid GIF is available, the normal static icon is restored or left in place.

The editor owns a separate preview texture. Saving copies the animation and settings into the save folder and invalidates the manager's cache for that world.

## Design notes

- The mod changes the vanilla favicon texture instead of drawing an extra image over the row.
- Cache keys use normalized save paths, so separate worlds can have separate GIFs and settings.
- File timestamps and sizes are checked periodically; the GIF is not decoded on every render call.
- Input size, canvas dimensions and frame count are bounded.
- No server networking or world-data migration is required.

## Before changing a mixin

Minecraft's world-selection APIs are version-sensitive. Check the exact 26.3 class signatures before changing injection points or shadowed fields. Keep injections narrow and test the world list and editor in-game after a change.

## Local test checklist

- Start a Fabric 26.3 client with the mod.
- Open Singleplayer with a world that only has `icon.png`.
- Add `icon.gif` and confirm it animates without changing the PNG.
- Import another GIF through Edit World → GIF Icon World.
- Test fit and playback modes.
- Save, return to the world list and reopen the editor.
- Remove or corrupt `icon.gif` and confirm the static icon remains available.
- Check `latest.log` for decode or texture-upload errors.

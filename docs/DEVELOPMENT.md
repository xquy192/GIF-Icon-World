# Development notes

GIF Icon World runs on the Minecraft client. The source is split by feature.

## Source files

| File | What it contains |
| --- | --- |
| `gif/GifAnimation.java` | Reads GIF frames, applies disposal rules and renders frames to the world icon. |
| `gif/GifIconSettings.java` | Loads, validates and saves settings for a world. |
| `gif/WorldGifIconManager.java` | Keeps decoded GIFs and playback state, then uploads changed frames. |
| `mixin/EditWorldScreenMixin.java` | Adds the GIF editor button to Edit World. |
| `mixin/WorldListEntryMixin.java` | Updates the existing world icon before Minecraft draws the row. |
| `screen/GifIconImportScreen.java` | Handles file selection, drag-and-drop and opening the save folder. |
| `screen/GifIconEditorScreen.java` | Displays the preview and editing controls. |

## What happens in-game

1. The world-list mixin gets the save folder and Minecraft's existing `FaviconTexture`.
2. The manager checks for `icon.gif` and `gif_icon_world.json`.
3. When the GIF changes, the manager decodes it and composites its frames.
4. Playback settings determine which frame should be shown.
5. The manager uploads a frame only when the displayed frame changes.
6. If no usable GIF is available, the static world icon is restored or left unchanged.

The editor uses its own preview texture. When you save, it copies the selected GIF and settings into the save folder, then clears the manager's cached entries for that world.

## Implementation notes

- The mod updates Minecraft's existing favicon texture. It does not draw a second image over the world row.
- Cache keys use normalized save paths, so each world has its own GIF and settings.
- The manager checks file timestamps and sizes once per second. It does not decode the GIF on every render call.
- GIF file size, image dimensions and frame count are limited.
- The mod does not use server networking or migrate world data.

## Changing a mixin

World-selection classes can change between Minecraft versions. Check the Minecraft 26.3 signatures before changing an injection point or shadowed field. After a mixin change, test both the world list and Edit World screen in-game.

## In-game checks

- Start a Fabric 26.3 client with the mod.
- Open a world that has only `icon.png`.
- Add `icon.gif` and check that it animates while the PNG remains in the folder.
- Import a different GIF through **Edit World → GIF Icon World**.
- Try each fit and playback mode.
- Save, return to the world list and open the editor again.
- Remove or corrupt `icon.gif` and check that the static icon is still available.
- Check `latest.log` for GIF decoding or texture upload errors.

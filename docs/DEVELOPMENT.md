# Development

All game code runs on the client. The source is split into three packages.

## Source layout

| File | Purpose |
| --- | --- |
| `gif/GifAnimation.java` | Reads GIF frames, handles disposal methods and renders a frame. |
| `gif/GifIconSettings.java` | Reads and writes a world's GIF settings. |
| `gif/WorldGifIconManager.java` | Caches decoded GIFs and updates the world icon texture. |
| `mixin/EditWorldScreenMixin.java` | Adds the GIF button to Edit World. |
| `mixin/WorldListEntryMixin.java` | Updates the icon used by each world-list row. |
| `screen/GifIconImportScreen.java` | Imports a GIF or opens the world folder. |
| `screen/GifIconEditorScreen.java` | Shows the GIF preview and editing controls. |

## World-list update

When Minecraft draws a world-list row, the mixin gets that world's save folder and passes its `icon.gif` path and vanilla `FaviconTexture` to the manager.

The manager checks the GIF and `gif_icon_world.json`. It decodes the animation when the file changes, then uploads a frame when playback advances. File timestamps and sizes are checked once per second. The GIF is not decoded on every render call.

If the GIF is missing or cannot be read, the manager restores the regular world icon. When a row closes, its playback entry is released.

## Saving from the editor

The editor has a separate preview texture. On save, it copies the selected file to the world's save folder as `icon.gif`, writes `gif_icon_world.json` and clears the cached entries for that world.

The mod changes the favicon texture used by Minecraft's world row. It does not draw another image on top of the row. It does not use server networking or change world data.

## Mixins

World-selection classes can change between Minecraft releases. Check the 26.3 signatures before changing a target method or shadowed field. After changing a mixin, open both the world list and Edit World in-game.

## In-game checks

- Start a Fabric 26.3 client with the mod.
- Open a world that has only `icon.png`.
- Add `icon.gif` and check the animation in the world list.
- Import another GIF through **Edit World → GIF Icon World**.
- Try the fit and playback modes.
- Save, return to the world list and open the editor again.
- Remove or corrupt `icon.gif` and check the static icon.
- Check `latest.log` if GIF decoding or texture upload fails.

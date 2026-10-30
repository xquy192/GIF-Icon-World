# GIF files and world settings

Each world stores its GIF and settings in its save folder, beside `level.dat` and Minecraft's usual `icon.png`.

```text
<world folder>/
├── level.dat
├── icon.png
├── icon.gif
└── gif_icon_world.json
```

## GIF limits

| Limit | Value |
| --- | ---: |
| Maximum file size | 64 MiB |
| Maximum width or height | 4096 pixels |
| Maximum logical canvas | 16,777,216 pixels |
| Maximum frame count | 300 |
| Maximum stored frame side | 128 pixels |

The displayed icon is rendered onto a 64×64 canvas. `CONTAIN` keeps the whole image visible. `COVER` fills the square and may crop the edges. `STRETCH` fills the square without keeping the original aspect ratio. Frame delays shorter than 20 ms are treated as 20 ms.

## Settings file

The file `gif_icon_world.json` is created when you save from the editor. It can also be edited by hand while Minecraft is closed.

Example:

```json
{
  "speed": 1.0,
  "startFrame": 0,
  "endFrame": 5,
  "offsetX": 0,
  "offsetY": 0,
  "zoom": 1.0,
  "fitMode": "CONTAIN",
  "playbackMode": "LOOP"
}
```

| Key | Allowed values | Default |
| --- | --- | --- |
| `speed` | 0.1–4.0 | `1.0` |
| `startFrame` | 0 to the last frame | `0` |
| `endFrame` | Start frame to the last frame | Last frame |
| `offsetX`, `offsetY` | -32 to 32 pixels | `0` |
| `zoom` | 0.5–2.0 | `1.0` |
| `fitMode` | `CONTAIN`, `COVER`, `STRETCH` | `CONTAIN` |
| `playbackMode` | `LOOP`, `PING_PONG`, `ONCE` | `LOOP` |

Frame numbers start at zero. The start and end frames are both included. Values outside the allowed range are clamped. If the settings file is missing or cannot be read, the mod uses the defaults.

## Playback modes

- `LOOP`: starts again at the first selected frame after reaching the last.
- `PING_PONG`: plays forward, then backward through the selected frames.
- `ONCE`: plays the selected range once and holds on the last frame.

The world list checks the settings file's timestamp and size periodically. If you edit the JSON while Minecraft is running, the new values are loaded when the file is checked again.

# GIF files and settings

Each world's GIF and settings are stored beside `level.dat` and Minecraft's `icon.png`.

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

The world-list icon is drawn on a 64×64 canvas. `CONTAIN` shows the full image. `COVER` fills the square and can crop the edges. `STRETCH` fills the square without keeping the image's aspect ratio. Frame delays below 20 ms are treated as 20 ms.

## Settings file

The editor creates `gif_icon_world.json` when you save. You can edit it by hand while Minecraft is closed.

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
| `speed` | 0.1-4.0 | `1.0` |
| `startFrame` | 0 to the last frame | `0` |
| `endFrame` | Start frame to the last frame | Last frame |
| `offsetX`, `offsetY` | -32 to 32 pixels | `0` |
| `zoom` | 0.5-2.0 | `1.0` |
| `fitMode` | `CONTAIN`, `COVER`, `STRETCH` | `CONTAIN` |
| `playbackMode` | `LOOP`, `PING_PONG`, `ONCE` | `LOOP` |

Frame numbers start at zero. The selected start and end frames are both played. Values outside the allowed range are clamped. If the settings file is missing or unreadable, the mod uses the defaults.

## Playback modes

- `LOOP` plays the selected frames from start to end, then starts again.
- `PING_PONG` plays forward and then backward.
- `ONCE` plays the selected range once and holds on the last frame.

The world list checks the settings file's timestamp and size periodically. Changes made while Minecraft is running are picked up when the file is checked again.

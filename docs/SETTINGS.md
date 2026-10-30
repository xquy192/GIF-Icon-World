# GIF files and per-world settings

Each save keeps its own animation and settings beside `level.dat` and Minecraft's normal `icon.png`.

```text
<world folder>/
├── level.dat
├── icon.png
├── icon.gif
└── gif_icon_world.json
```

## GIF input limits

| Limit | Current value |
| --- | ---: |
| File size | 64 MiB |
| Width or height | 4096 pixels |
| Logical canvas | 16,777,216 pixels |
| Frame count | 300 |
| Stored frame maximum side | 128 pixels |

The output is rendered onto a 64×64 canvas. `CONTAIN` keeps the complete image visible, `COVER` fills the square and can crop the edges, and `STRETCH` fills the square without preserving the aspect ratio. Very short GIF delays are clamped to 20 ms.

## `gif_icon_world.json`

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

| Key | Values | Default |
| --- | --- | --- |
| `speed` | 0.1–4.0 | `1.0` |
| `startFrame` | 0 through last frame | `0` |
| `endFrame` | start frame through last frame | Last frame |
| `offsetX`, `offsetY` | -32–32 pixels | `0` |
| `zoom` | 0.5–2.0 | `1.0` |
| `fitMode` | `CONTAIN`, `COVER`, `STRETCH` | `CONTAIN` |
| `playbackMode` | `LOOP`, `PING_PONG`, `ONCE` | `LOOP` |

Frame numbers start at zero; start and end are inclusive. Invalid values are clamped. A missing or unreadable settings file uses defaults.

## Playback

- `LOOP`: returns to the first selected frame after the last.
- `PING_PONG`: plays forward and then backward.
- `ONCE`: plays once and holds the last selected frame.

You can edit the JSON while Minecraft is closed. The world list periodically checks its timestamp and file size and reloads changed settings.

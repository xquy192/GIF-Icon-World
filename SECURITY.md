# Security notes

GIF Icon World reads GIF files supplied by the player. The decoder limits file size, canvas dimensions and frame count to reduce accidental memory spikes. These limits are not a guarantee that every malformed image is harmless.

If a GIF causes a crash or unusually high memory use, do not upload private world data. Open an issue with a minimal reproducing GIF, the game/mod versions and the relevant `latest.log` lines.

For a suspected security issue that could affect other users, contact the maintainer privately before publishing a problematic sample.

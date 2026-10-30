# Security notes

GIF Icon World reads GIF files from a player's world folder or from a file selected in the editor. The decoder limits the input file to 64 MiB, the image dimensions to 4096 pixels, and the animation to 300 frames. These checks reduce the chance of excessive memory use, but they cannot guarantee that every malformed GIF will be harmless.

If a GIF causes a crash or unusually high memory use, report it with a small reproduction file, the Minecraft and mod versions, and the relevant lines from `latest.log`. Do not upload a private world save. Remove personal paths and other private information from logs before posting.

For a suspected security issue that could affect other users, contact the maintainer privately before sharing the problematic file publicly.

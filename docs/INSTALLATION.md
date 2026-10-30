# Installation

GIF Icon World is a client-side Fabric mod for Minecraft Java 26.3.

## Requirements

- Minecraft Java 26.3
- Fabric Loader 0.20.1 or newer
- Java 25 or newer
- A Minecraft client profile that can access the world's save folder

## Install the mod

1. Start your Fabric 26.3 profile once.
2. Download a published JAR from Releases when one is available, or build the project from source.
3. Put the JAR in the `mods` folder used by that profile.
4. Start Minecraft and open Singleplayer.

Use the built JAR, not the source ZIP. The mod runs on the client and is not needed on a dedicated server.

## Add a GIF manually

Open the world's save folder and put the GIF beside `level.dat`. Name it `icon.gif`. The usual folder is:

```text
.minecraft/saves/<world folder>/
```

While the world list is open, the mod checks for this file. A readable GIF appears in the existing world icon slot. If the file is missing or cannot be read, Minecraft uses the regular `icon.png`.

## Import a GIF in-game

1. Open Singleplayer and select a world.
2. Choose **Edit**.
3. Click **GIF Icon World**.
4. Drop a GIF onto the panel or choose one with the file picker.
5. Adjust the preview and playback settings.
6. Save the changes to the world.

The editor copies the selected image to the save folder as `icon.gif` and writes `gif_icon_world.json`. Cancel returns to the previous screen without saving the new selection.

## Remove the animated icon

Close the world list, then delete `icon.gif` and `gif_icon_world.json` from the save folder. Keep `icon.png` if you want Minecraft's static icon.

Use **Open World Folder** in the import screen to open the selected save directory.

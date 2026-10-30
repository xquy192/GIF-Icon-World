# Installation

GIF Icon World is a client-side Fabric mod for Minecraft Java 26.3.

You need:

- Minecraft Java 26.3
- Fabric Loader 0.20.1 or newer
- Java 25 or newer

## Install

1. Start your Fabric 26.3 profile once.
2. Download a JAR from the project's Releases page when one is available, or build it from source.
3. Put the JAR in the `mods` folder for that profile.
4. Start Minecraft and open Singleplayer.

Use the built JAR. The source ZIP cannot be loaded as a mod. GIF Icon World runs on the client, so it is not needed on a dedicated server.

## Add a GIF manually

Open the world's save folder and place the GIF beside `level.dat`. Name it `icon.gif`.

The folder is usually under:

```text
.minecraft/saves/<world folder>/
```

While the world list is open, the mod checks for this file. If it can read the GIF, it appears in the world's icon slot. If the GIF is missing or unreadable, Minecraft uses `icon.png`.

## Import from Minecraft

1. Open Singleplayer and select a world.
2. Click **Edit**.
3. Open **GIF Icon World**.
4. Drop a GIF onto the panel or choose one with the file picker.
5. Adjust the preview and playback settings.
6. Save.

The editor copies the GIF into the save folder as `icon.gif` and writes `gif_icon_world.json`. Cancel returns to the previous screen without saving the new selection.

## Remove an animated icon

Close the world list, then delete `icon.gif` and `gif_icon_world.json` from the save folder. Keep `icon.png` to use the static icon.

The import screen has an **Open World Folder** button for the selected save.

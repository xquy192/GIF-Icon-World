# Installation

GIF Icon World is a client-side Fabric mod for Minecraft Java 26.3.

## Requirements

- Minecraft Java 26.3.
- Fabric Loader 0.20.1 or newer.
- Java 25 or newer.
- A client profile with access to the world's save folder.

## Install

1. Start the Fabric 26.3 profile once.
2. Download a published JAR from Releases when available, or build the project yourself.
3. Put the mod JAR in the `mods` folder used by that profile.
4. Start Minecraft and open Singleplayer.

Do not put the source ZIP in `mods`; Minecraft needs the built JAR. The mod is client-only and is not needed on a dedicated server.

## Add an icon manually

Open a world's save folder and place the GIF beside `level.dat` as `icon.gif`. The usual location is `.minecraft/saves/<world folder>/`.

The mod checks this file while the world list is displayed. A valid GIF is animated in the existing world icon slot. If the GIF is missing or invalid, Minecraft's original `icon.png` is used.

## Use the in-game editor

1. Open Singleplayer and select a world.
2. Choose Edit.
3. Open **GIF Icon World**.
4. Drop a GIF onto the panel or choose it with the file picker.
5. Adjust the preview and playback options.
6. Save to the world.

The editor copies the selected GIF to the save folder as `icon.gif` and writes `gif_icon_world.json`. Cancel does not save the new selection.

## Remove an animated icon

Close the world list, then remove `icon.gif` and `gif_icon_world.json` from the save folder. Keep `icon.png` for the normal static icon.

Use the editor's **Open World Folder** button to locate the selected save.

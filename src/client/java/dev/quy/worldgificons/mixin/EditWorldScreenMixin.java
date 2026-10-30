package dev.quy.worldgificons.mixin;

import dev.quy.worldgificons.screen.GifIconImportScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

/**
 * Adds the GIF editor button directly below Minecraft's Optimize World button.
 * Its position and dimensions are read from the vanilla widget each time the
 * screen is initialized, so GUI scaling and window resizing are respected.
 */
@Mixin(EditWorldScreen.class)
public abstract class EditWorldScreenMixin extends Screen {
    @Shadow @Final private LevelStorageSource.LevelStorageAccess levelAccess;

    protected EditWorldScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void gificonworld$addGifIconButton(CallbackInfo ci) {
        Button optimizeButton = null;

        // Match the translation key instead of visible text, so every language works.
        for (var child : this.children()) {
            if (child instanceof Button candidate
                    && candidate.getMessage().getContents() instanceof TranslatableContents contents
                    && "selectWorld.optimize".equals(contents.getKey())) {
                optimizeButton = candidate;
                break;
            }
        }

        // Avoid placing the mod button at an unrelated fixed position if vanilla's
        // Optimize World button is not present on this screen.
        if (optimizeButton == null) {
            return;
        }

        Path worldDirectory = this.levelAccess.getLevelPath(LevelResource.ROOT);
        int buttonX = optimizeButton.getX();
        int buttonY = optimizeButton.getY() + optimizeButton.getHeight() + 4;
        int buttonWidth = optimizeButton.getWidth();
        int buttonHeight = optimizeButton.getHeight();

        this.addRenderableWidget(Button.builder(Component.literal("GIF Icon World"), button ->
                        Minecraft.getInstance().gui.setScreen(
                                new GifIconImportScreen((Screen) (Object) this, worldDirectory)))
                .bounds(buttonX, buttonY, buttonWidth, buttonHeight)
                .build());
    }
}

package dev.quy.worldgificons.mixin;

import dev.quy.worldgificons.screen.GifIconImportScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

@Mixin(EditWorldScreen.class)
public abstract class EditWorldScreenMixin extends Screen {
    @Shadow @Final private static Component OPTIMIZE_BUTTON;
    @Shadow @Final private LevelStorageSource.LevelStorageAccess levelAccess;

    @Unique
    private Button gificonworld$gifIconButton;

    protected EditWorldScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void gificonworld$addGifIconButton(CallbackInfo ci) {
        Path worldDirectory = this.levelAccess.getLevelPath(LevelResource.ROOT);
        this.gificonworld$gifIconButton = this.addRenderableWidget(
                Button.builder(Component.literal("GIF Icon World"), button ->
                                Minecraft.getInstance().setScreen(
                                        new GifIconImportScreen((Screen) (Object) this, worldDirectory)))
                        .bounds(0, 0, 1, 1)
                        .build()
        );
        this.gificonworld$positionGifIconButton();
    }

    @Inject(method = "repositionElements", at = @At("TAIL"))
    private void gificonworld$repositionGifIconButton(CallbackInfo ci) {
        this.gificonworld$positionGifIconButton();
    }

    @Unique
    private void gificonworld$positionGifIconButton() {
        if (this.gificonworld$gifIconButton == null) {
            return;
        }

        Button optimizeButton = this.gificonworld$findOptimizeButton();
        if (optimizeButton == null) {
            this.gificonworld$gifIconButton.visible = false;
            return;
        }

        this.gificonworld$gifIconButton.setRectangle(
                optimizeButton.getWidth(),
                optimizeButton.getHeight(),
                optimizeButton.getX(),
                optimizeButton.getBottom() + 4
        );
        this.gificonworld$gifIconButton.visible = true;
    }

    @Unique
    private Button gificonworld$findOptimizeButton() {
        for (var child : this.children()) {
            if (child instanceof Button candidate
                    && candidate.getMessage().equals(OPTIMIZE_BUTTON)) {
                return candidate;
            }
        }
        return null;
    }
}

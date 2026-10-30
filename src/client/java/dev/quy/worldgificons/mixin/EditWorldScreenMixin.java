package dev.quy.worldgificons.mixin;

import dev.quy.worldgificons.screen.GifIconImportScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

/** Adds the GIF editor button to Minecraft's existing Edit World screen. */
@Mixin(EditWorldScreen.class)
public abstract class EditWorldScreenMixin extends Screen {
    @Shadow @Final private LevelStorageSource.LevelStorageAccess levelAccess;

    protected EditWorldScreenMixin(Component title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void gificonworld$addGifIconButton(CallbackInfo ci) {
        Path worldDirectory = this.levelAccess.getWorldDir();
        int buttonY = Math.min(this.height - 40, this.height / 2 + 137);
        this.addRenderableWidget(Button.builder(Component.literal("GIF Icon World"), button ->
                Minecraft.getInstance().gui.setScreen(new GifIconImportScreen((Screen) (Object) this, worldDirectory)))
                .bounds(this.width / 2 - 200, buttonY, 400, 20)
                .build());
    }
}

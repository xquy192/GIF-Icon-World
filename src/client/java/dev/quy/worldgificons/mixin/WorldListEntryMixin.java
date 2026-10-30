package dev.quy.worldgificons.mixin;

import dev.quy.worldgificons.gif.WorldGifIconManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

/** Updates the vanilla favicon texture immediately before the list row extracts its draw state. */
@Mixin(WorldSelectionList.WorldListEntry.class)
public abstract class WorldListEntryMixin {
    @Shadow @Final private FaviconTexture icon;
    @Shadow @Final private LevelSummary summary;

    @Inject(method = "extractContent", at = @At("HEAD"))
    private void gificonworld$updateAnimatedIcon(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                                  boolean hovered, float partialTick, CallbackInfo ci) {
        try {
            Path worldDirectory = Minecraft.getInstance().getLevelSource().getLevelPath(this.summary.getLevelId());
            WorldGifIconManager.update(worldDirectory.resolve("icon.gif"), this.summary.getIcon(), this.icon);
        } catch (RuntimeException exception) {
            WorldGifIconManager.logUnexpectedFailure(this.summary.getLevelId(), exception);
        }
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void gificonworld$releasePlayback(CallbackInfo ci) {
        WorldGifIconManager.release(this.icon);
    }
}

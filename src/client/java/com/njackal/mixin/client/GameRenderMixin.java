package com.njackal.mixin.client;

import com.njackal.BuildingUtils;
import com.njackal.BuildingUtilsClient;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRenderMixin {
    @Inject(method = "close", at = @At("RETURN"))
    private void onGameRendererClose(CallbackInfo ci) {
        BuildingUtilsClient.getInstance().close();
    }
}

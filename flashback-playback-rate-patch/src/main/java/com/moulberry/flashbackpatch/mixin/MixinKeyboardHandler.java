package com.moulberry.flashbackpatch.mixin;

import com.moulberry.flashback.Flashback;
import com.moulberry.flashback.editor.ui.ReplayUI;
import com.moulberry.flashback.playback.ReplayServer;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.resources.language.I18n;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(KeyboardHandler.class)
public class MixinKeyboardHandler {

    @Unique
    private static final float MIN_TICK_RATE = 1.0f;
    @Unique
    private static final float NORMAL_TICK_RATE = 20.0f;

    @Inject(method = "keyPress", at = @At("HEAD"))
    private void flashbackPatch$keyPress(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        if (action != GLFW.GLFW_PRESS || !Flashback.isInReplay()) {
            return;
        }

        if ((modifiers & (GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_ALT | GLFW.GLFW_MOD_SUPER)) != 0) {
            return;
        }

        ReplayServer replayServer = Flashback.getReplayServer();
        if (replayServer == null) {
            return;
        }

        if (key == GLFW.GLFW_KEY_EQUAL || key == GLFW.GLFW_KEY_KP_ADD) {
            setReplaySpeed(replayServer, true);
        } else if (key == GLFW.GLFW_KEY_MINUS || key == GLFW.GLFW_KEY_KP_SUBTRACT) {
            setReplaySpeed(replayServer, false);
        } else if (key == GLFW.GLFW_KEY_0 || key == GLFW.GLFW_KEY_KP_0) {
            replayServer.setDesiredTickRate(NORMAL_TICK_RATE, true);
            showCurrentReplaySpeed(NORMAL_TICK_RATE);
        }
    }

    @Unique
    private static void setReplaySpeed(ReplayServer replayServer, boolean increase) {
        float currentTickRate = Math.max(MIN_TICK_RATE, replayServer.getDesiredTickRate(true));
        float targetTickRate;

        if (increase) {
            targetTickRate = currentTickRate >= Float.MAX_VALUE / 2.0f ? Float.MAX_VALUE : currentTickRate * 2.0f;
        } else {
            targetTickRate = Math.max(MIN_TICK_RATE, currentTickRate / 2.0f);
        }

        if (targetTickRate != currentTickRate) {
            replayServer.setDesiredTickRate(targetTickRate, true);
        }
        showCurrentReplaySpeed(targetTickRate);
    }

    @Unique
    private static void showCurrentReplaySpeed(float tickRate) {
        String speed = String.format(Locale.ROOT, "%.2fx", tickRate / 20.0f);
        ReplayUI.setInfoOverlayShort(I18n.get("flashback.keyframe.speed") + ": " + speed);
    }

}

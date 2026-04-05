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
    private static final float[] REPLAY_TICK_SPEEDS = new float[]{1.0f, 2.0f, 4.0f, 10.0f, 20.0f, 40.0f, 100.0f, 200.0f, 400.0f};

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
            replayServer.setDesiredTickRate(20.0f, true);
            showCurrentReplaySpeed(20.0f);
        }
    }

    @Unique
    private static void setReplaySpeed(ReplayServer replayServer, boolean increase) {
        float currentTickRate = replayServer.getDesiredTickRate(true);
        float targetTickRate = increase ? REPLAY_TICK_SPEEDS[REPLAY_TICK_SPEEDS.length - 1] : REPLAY_TICK_SPEEDS[0];

        if (increase) {
            for (float replayTickSpeed : REPLAY_TICK_SPEEDS) {
                if (replayTickSpeed > currentTickRate) {
                    targetTickRate = replayTickSpeed;
                    break;
                }
            }
        } else {
            for (int i = REPLAY_TICK_SPEEDS.length - 1; i >= 0; i--) {
                float replayTickSpeed = REPLAY_TICK_SPEEDS[i];
                if (replayTickSpeed < currentTickRate) {
                    targetTickRate = replayTickSpeed;
                    break;
                }
            }
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

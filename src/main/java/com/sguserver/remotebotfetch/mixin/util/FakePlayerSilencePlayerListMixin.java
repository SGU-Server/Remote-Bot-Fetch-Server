/*
 * Copyright (C) 2025  Remote-Bot-Fetch-Server contributors
 * https://github.com/SGU-Server/Remote-Bot-Fetch-Server

 * Remote Bot Fetch Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.

 * Remote Bot Fetch Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Remote Bot Fetch Server.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.sguserver.remotebotfetch.mixin.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import com.sguserver.remotebotfetch.bot.FakePlayerSpawner;

import org.slf4j.Logger;

@Mixin(PlayerList.class)
public class FakePlayerSilencePlayerListMixin {
    private static final ThreadLocal<Integer> rbf$SILENT_PLAYER_LIST_DEPTH = ThreadLocal.withInitial(() -> 0);

    @Inject(method = "broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V", at = @At("HEAD"),
        cancellable = true)
    private void rbf$silenceBroadcast(Component message, boolean overlay, CallbackInfo ci) {
        if (rbf$shouldSilence()) {
            ci.cancel();
        }
    }

    @WrapWithCondition(method = "placeNewPlayer", at = @At(value = "INVOKE", remap = false,
        target = "Lorg/slf4j/Logger;info(Ljava/lang/String;[Ljava/lang/Object;)V"))
    private boolean rbf$silenceLoginLog(Logger instance, String format, Object[] args) {
        return !rbf$shouldSilence();
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void rbf$enterSilentRemove(ServerPlayer player, CallbackInfo ci) {
        if (FakePlayerSpawner.shouldSilencePlayer(player)) {
            rbf$SILENT_PLAYER_LIST_DEPTH.set(rbf$SILENT_PLAYER_LIST_DEPTH.get() + 1);
        }
    }

    @Inject(method = "remove", at = @At("RETURN"))
    private void rbf$exitSilentRemove(ServerPlayer player, CallbackInfo ci) {
        int depth = rbf$SILENT_PLAYER_LIST_DEPTH.get();
        if (depth <= 1) {
            rbf$SILENT_PLAYER_LIST_DEPTH.remove();
        } else {
            rbf$SILENT_PLAYER_LIST_DEPTH.set(depth - 1);
        }
        FakePlayerSpawner.unmarkSilentLogout(player);
    }

    private static boolean rbf$shouldSilence() {
        return FakePlayerSpawner.isSilenceEnabled() || rbf$SILENT_PLAYER_LIST_DEPTH.get() > 0;
    }
}

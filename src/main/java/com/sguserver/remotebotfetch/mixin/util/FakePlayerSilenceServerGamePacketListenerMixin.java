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

import net.minecraft.network.DisconnectionDetails;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import com.sguserver.remotebotfetch.bot.FakePlayerSpawner;

import org.slf4j.Logger;

@Mixin(ServerGamePacketListenerImpl.class)
public class FakePlayerSilenceServerGamePacketListenerMixin {
    @Shadow
    public ServerPlayer player;

    @Unique
    private boolean rbf$silentDisconnect;

    @Inject(method = "onDisconnect", at = @At("HEAD"))
    private void rbf$enterSilentDisconnect(DisconnectionDetails details, CallbackInfo ci) {
        if (FakePlayerSpawner.shouldSilencePlayer(player)) {
            rbf$silentDisconnect = true;
            FakePlayerSpawner.enterSilenceScope();
        }
    }

    @Inject(method = "onDisconnect", at = @At("RETURN"))
    private void rbf$exitSilentDisconnect(DisconnectionDetails details, CallbackInfo ci) {
        if (rbf$silentDisconnect) {
            rbf$silentDisconnect = false;
            FakePlayerSpawner.exitSilenceScope();
        }
    }

    @WrapWithCondition(method = "onDisconnect", at = @org.spongepowered.asm.mixin.injection.At(value = "INVOKE",
        remap = false, target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
    private boolean rbf$silenceDisconnectLog(Logger instance, String format, Object first, Object second) {
        return !FakePlayerSpawner.shouldSilencePlayer(player);
    }
}

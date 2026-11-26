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

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.sguserver.remotebotfetch.menu.BotInventorySessions;
import com.sguserver.remotebotfetch.menu.RbfBotInventoryMenu;

@Mixin(PlayerList.class)
public abstract class BotInventoryPlayerListMixin {
    @Inject(method = "remove", at = @At("HEAD"))
    private void rbf$closeBotInventoryBeforeSaving(ServerPlayer player, CallbackInfo ci) {
        BotInventorySessions.beforePlayerLeaves(player);
    }

    @Inject(method = "respawn", at = @At("HEAD"))
    private void rbf$returnCarriedBeforeRespawn(ServerPlayer player, boolean keepEverything,
        Entity.RemovalReason reason, CallbackInfoReturnable<ServerPlayer> cir) {
        if (player.containerMenu instanceof RbfBotInventoryMenu) {
            player.closeContainer();
        }
        BotInventorySessions.beforePlayerLeaves(player);
    }
}

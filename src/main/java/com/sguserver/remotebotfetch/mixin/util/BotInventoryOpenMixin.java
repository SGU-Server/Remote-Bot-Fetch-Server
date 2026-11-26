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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.sguserver.remotebotfetch.Mod;
import com.sguserver.remotebotfetch.menu.BotInventorySessions;
import com.sguserver.remotebotfetch.menu.RbfBotInventoryMenu;

@Mixin(AbstractContainerMenu.class)
public abstract class BotInventoryOpenMixin {
    @Inject(method = "clickMenuButton", at = @At("HEAD"), cancellable = true)
    private void rbf$openBotInventory(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (id != RbfBotInventoryMenu.OPEN_BUTTON || !(player instanceof ServerPlayer viewer)
            || (Object)this != viewer.containerMenu || !((Object)this instanceof InventoryMenu)) {
            return;
        }
        Mod.LOGGER.info("[RBF] bot inventory button pressed by {} (menu {}, container {})",
            viewer.getName().getString(), this.getClass().getSimpleName(), viewer.containerMenu.containerId);
        BotInventorySessions.openFromInventory(viewer);
        cir.setReturnValue(false);
    }
}

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
package com.sguserver.remotebotfetch.menu;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;

import carpet.patches.EntityPlayerMPFake;
import carpet.utils.CommandHelper;

import com.sguserver.remotebotfetch.Mod;
import com.sguserver.remotebotfetch.lib.MessageComponent;
import com.sguserver.remotebotfetch.lib.Utils;
import com.sguserver.remotebotfetch.rule.Settings;

public final class BotInventorySessions {
    private static final Map<UUID, RbfBotInventoryMenu> VIEWERS = new HashMap<>();

    private BotInventorySessions() {}

    public static boolean openFromInventory(ServerPlayer viewer) {
        if (!CommandHelper.canUseCommand(viewer.createCommandSourceStack(), Settings.botInventoryAccess)) {
            Mod.LOGGER.info("[RBF] refusing bot inventory for {}: no permission ({})", viewer.getName().getString(),
                Settings.botInventoryAccess);
            new MessageComponent("rbfBotInventory.noPermission", Settings.botInventoryAccess).sendSystemMessage(viewer);
            return false;
        }
        if (!canOpen(viewer)) {
            Mod.LOGGER.info("[RBF] refusing bot inventory for {}: cannot open (spectator or dead)",
                viewer.getName().getString());
            return false;
        }
        AbstractContainerMenu currentMenu = viewer.containerMenu;
        if (currentMenu != viewer.inventoryMenu) {
            Mod.LOGGER.info("[RBF] refusing bot inventory for {}: current menu is {}", viewer.getName().getString(),
                currentMenu.getClass().getSimpleName());
            return false;
        }
        if (!currentMenu.getCarried().isEmpty()) {
            Mod.LOGGER.info("[RBF] refusing bot inventory for {}: cursor is not empty", viewer.getName().getString());
            new MessageComponent("rbfBotInventory.emptyCursor").sendSystemMessage(viewer);
            return false;
        }
        List<EntityPlayerMPFake> online = Utils.getServer().getPlayerList().getPlayers().stream()
            .filter(EntityPlayerMPFake.class::isInstance).map(EntityPlayerMPFake.class::cast)
            .filter(bot -> bot != viewer && RbfBotInventoryMenu.canView(viewer, bot))
            .sorted(Comparator.comparing(BotInventorySessions::name, String.CASE_INSENSITIVE_ORDER)).toList();
        if (online.isEmpty()) {
            Mod.LOGGER.info("[RBF] refusing bot inventory for {}: no fake player available",
                viewer.getName().getString());
            new MessageComponent("rbfBotInventory.noBot").sendSystemMessage(viewer);
            return false;
        }
        List<EntityPlayerMPFake> choices = online;
        for (EntityPlayerMPFake bot : choices) {
            if (!VIEWERS.containsKey(bot.getUUID())) {
                Mod.LOGGER.info("[RBF] opening {} inventory for {}", name(bot), viewer.getName().getString());
                return open(viewer, bot, choices);
            }
        }
        Mod.LOGGER.info("[RBF] refusing bot inventory for {}: every fake player is busy", viewer.getName().getString());
        new MessageComponent("rbfBotInventory.busyAll").sendSystemMessage(viewer);
        return false;
    }

    static boolean select(ServerPlayer viewer, EntityPlayerMPFake bot, List<EntityPlayerMPFake> choices) {
        if (!canOpen(viewer) || !(viewer.containerMenu instanceof RbfBotInventoryMenu menu) || !menu.stillValid(viewer)
            || !menu.isBotVisible()) {
            return false;
        }
        if (!menu.getCarried().isEmpty()) {
            new MessageComponent("rbfBotInventory.emptyCursor").sendSystemMessage(viewer);
            return false;
        }
        if (!RbfBotInventoryMenu.canView(viewer, bot)
            || Utils.getServer().getPlayerList().getPlayer(bot.getUUID()) != bot) {
            new MessageComponent("rbfBotInventory.unavailable", name(bot)).sendSystemMessage(viewer);
            return false;
        }
        RbfBotInventoryMenu occupied = VIEWERS.get(bot.getUUID());
        if (occupied != null) {
            if (occupied == menu) {
                return true;
            }
            new MessageComponent("rbfBotInventory.busy", name(bot)).sendSystemMessage(viewer);
            return false;
        }
        return open(viewer, bot, choices);
    }

    private static boolean canOpen(ServerPlayer viewer) {
        return viewer.isAlive() && !viewer.isSpectator()
            && CommandHelper.canUseCommand(viewer.createCommandSourceStack(), Settings.botInventoryAccess);
    }

    private static boolean open(ServerPlayer viewer, EntityPlayerMPFake bot, List<EntityPlayerMPFake> choices) {
        Object[] titleArguments = new Object[choices.size() + 1];
        titleArguments[0] = name(bot);
        for (int i = 0; i < choices.size(); i++) {
            titleArguments[i + 1] = name(choices.get(i));
        }
        Component title = Component.translatable(RbfBotInventoryMenu.TITLE_KEY, titleArguments);
        if (viewer.openMenu(new SimpleMenuProvider(
            (id, inventory, player) -> new RbfBotInventoryMenu(id, inventory, bot, choices), title)).isEmpty()) {
            return false;
        }
        if (viewer.containerMenu instanceof RbfBotInventoryMenu menu) {
            VIEWERS.put(bot.getUUID(), menu);
        }
        return true;
    }

    public static void beforePlayerLeaves(ServerPlayer player) {
        if (player.containerMenu instanceof RbfBotInventoryMenu) {
            player.doCloseContainer();
        }
    }

    static void release(EntityPlayerMPFake bot, RbfBotInventoryMenu menu) {
        VIEWERS.remove(bot.getUUID(), menu);
    }

    public static void clear() {
        VIEWERS.clear();
    }

    private static String name(EntityPlayerMPFake bot) {
        return bot.getName().getString();
    }
}

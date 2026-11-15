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
package com.sguserver.remotebotfetch.storage.getitem.clean;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import carpet.fakes.ServerPlayerInterface;
import carpet.patches.EntityPlayerMPFake;

import com.sguserver.remotebotfetch.bot.FakePlayerSpawner;
import com.sguserver.remotebotfetch.lib.Utils;
import com.sguserver.remotebotfetch.storage.getitem.GetItemBotHelper;
import com.sguserver.remotebotfetch.storage.getitem.OfflineInvCheck;

public final class CleanGetItemBot {
    private CleanGetItemBot() {}

    public static List<String> listUnspawnedGetItemBotsWithInventory() {
        return listBots();
    }

    public static List<String> listBots() {
        String prefix = GetItemBotHelper.getBotPrefix();
        Set<String> onlineNames = Utils.runOnServerThread(() -> Utils.getServer().getPlayerList().getPlayers().stream()
            .map(ServerPlayer::getScoreboardName).collect(Collectors.toSet()));

        return IntStream.rangeClosed(1, GetItemBotHelper.BOT_SCAN_LIMIT).parallel().mapToObj(i -> prefix + i)
            .filter(name -> !onlineNames.contains(name)).filter(name -> !OfflineInvCheck.isInventoryEmpty(name))
            .toList();
    }

    public static synchronized List<String> cleanBots(ServerPlayer player, int max) {
        SpawnData spawnData = new SpawnData(player);
        List<String> bots = listBots();
        List<String> result = new ArrayList<>();
        for (String botName : bots) {
            if (max <= 0) {
                return result;
            }
            max -= cleanSingleBot(spawnData, botName, max);
            result.add(botName);
        }
        return result;
    }

    private static int cleanSingleBot(SpawnData spawnData, String name, int max) {
        if (max <= 0) {
            return 0;
        }
        EntityPlayerMPFake bot = spawnData.spawn(name);
        try {
            return Utils.runOnServerThread(() -> {
                int dropped = 0;
                ServerPlayerInterface playerInterface = (ServerPlayerInterface)bot;
                Inventory inv = bot.getInventory();
                for (int slot = inv.getContainerSize(); slot >= 0 && dropped < max; slot--) {
                    if (inv.getItem(slot).isEmpty()) {
                        continue;
                    }
                    playerInterface.getActionPack().drop(slot, true);
                    dropped++;
                }
                return dropped;
            });
        } finally {
            FakePlayerSpawner.silenceLogout(bot);
        }
    }

    static class SpawnData {
        Vec3 spawnPos;
        float yaw;
        float pitch;
        ResourceKey<Level> dimension;

        SpawnData(ServerPlayer player) {
            this.spawnPos = player.position();
            this.yaw = player.getYRot();
            this.pitch = player.getXRot();
            this.dimension = player.level().dimension();
        }

        EntityPlayerMPFake spawn(String name) {
            return FakePlayerSpawner.spawnSurvivalFakeWithName(name, this.dimension, spawnPos, yaw, pitch, true, true);
        }
    }
}

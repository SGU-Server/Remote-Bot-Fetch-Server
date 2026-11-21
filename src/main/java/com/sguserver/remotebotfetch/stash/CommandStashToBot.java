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
package com.sguserver.remotebotfetch.stash;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import carpet.patches.EntityPlayerMPFake;

import com.sguserver.remotebotfetch.lib.AsyncTasks;
import com.sguserver.remotebotfetch.lib.MessageComponent;
import com.sguserver.remotebotfetch.lib.Utils;
import com.sguserver.remotebotfetch.rule.Settings;
import com.sguserver.remotebotfetch.rule.util.command.BaseCommand;
import com.sguserver.remotebotfetch.rule.util.command.CommandUtils;

/**
 * Moves the slots selected in the player's currently open container into the inventory of a fake player. When the fake
 * player is offline it is brought back with Carpet's {@code /player <name>
 * rejoin} command and the items are inserted as soon as it is online again.
 */
public final class CommandStashToBot implements BaseCommand {
    private static final int MAX_SLOTS = 128;
    private static final int BOT_INVENTORY_SIZE = 36;
    private static final String BOT_NAME_PATTERN = "[A-Za-z0-9_]{1,16}";
    private static final int REJOIN_POLLS = 60;
    private static final long REJOIN_POLL_INTERVAL_MS = 250L;

    private static boolean checkPermission(CommandSourceStack source) {
        return CommandUtils.hasPermission(source, Settings.commandStashToBot, "/stashToBot", "commandStashToBot");
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("stashToBot").executes(ctx -> tutor(ctx.getSource()))
            .then(Commands.argument("bot", StringArgumentType.word()).then(
                Commands.argument("slots", StringArgumentType.greedyString()).executes(ctx -> execute(ctx.getSource(),
                    StringArgumentType.getString(ctx, "bot"), StringArgumentType.getString(ctx, "slots"))))));
    }

    private int tutor(CommandSourceStack source) {
        if (!checkPermission(source)) {
            return 0;
        }
        CommandUtils.tutor(source, "stashToBot", 4);
        return 1;
    }

    private int execute(CommandSourceStack source, String botName, String rawSlots) {
        if (!checkPermission(source)) {
            return 0;
        }
        ServerPlayer player = CommandUtils.getPlayer(source, "/stashToBot");
        if (player == null) {
            return 0;
        }
        if (!botName.matches(BOT_NAME_PATTERN)) {
            new MessageComponent("stashToBot.invalidBot").sendFailure(source);
            return 0;
        }
        Set<Integer> slotIndexes = parseSlots(rawSlots);
        if (slotIndexes.isEmpty()) {
            new MessageComponent("stashToBot.noSlot").sendFailure(source);
            return 0;
        }
        MinecraftServer server = Utils.getServer();
        ServerPlayer online = server.getPlayerList().getPlayerByName(botName);
        if (online != null && !(online instanceof EntityPlayerMPFake)) {
            new MessageComponent("stashToBot.busy").sendFailure(source);
            return 0;
        }

        List<ItemStack> collected = collectFromMenu(player.containerMenu, slotIndexes);
        if (collected.isEmpty()) {
            new MessageComponent("stashToBot.nothing").sendFailure(source);
            return 0;
        }

        if (online != null) {
            int moved = insertIntoBot(online, collected);
            new MessageComponent("stashToBot.done", botName, moved).send(source, true);
            return 1;
        }

        UUID playerId = player.getUUID();
        ServerLevel level = player.level();
        Vec3 position = player.position();
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(),
            "player %s rejoin".formatted(botName));
        new MessageComponent("stashToBot.pending", botName).send(source, true);
        AsyncTasks.run(() -> awaitRejoin(botName, playerId, level, position, collected));
        return 1;
    }

    private static List<ItemStack> collectFromMenu(AbstractContainerMenu menu, Set<Integer> slotIndexes) {
        List<ItemStack> collected = new ArrayList<>();
        for (int index : slotIndexes) {
            if (index < 0 || index >= menu.slots.size()) {
                continue;
            }
            Slot slot = menu.getSlot(index);
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) {
                continue;
            }
            collected.add(stack.copy());
            slot.set(ItemStack.EMPTY);
        }
        if (!collected.isEmpty()) {
            menu.broadcastChanges();
        }
        return collected;
    }

    private static void awaitRejoin(String botName, UUID playerId, ServerLevel level, Vec3 position,
        List<ItemStack> collected) {
        for (int i = 0; i < REJOIN_POLLS; i++) {
            try {
                Thread.sleep(REJOIN_POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            ServerPlayer bot =
                Utils.runOnServerThread(() -> Utils.getServer().getPlayerList().getPlayerByName(botName));
            if (bot instanceof EntityPlayerMPFake fake) {
                Utils.runOnServerThread(() -> {
                    int moved = insertIntoBot(fake, collected);
                    notifyPlayer(playerId, "stashToBot.done", botName, moved);
                });
                return;
            }
        }
        Utils.runOnServerThread(() -> refund(playerId, level, position, collected));
    }

    private static void refund(UUID playerId, ServerLevel level, Vec3 position, List<ItemStack> collected) {
        ServerPlayer player = Utils.getServer().getPlayerList().getPlayer(playerId);
        boolean refunded = false;
        if (player != null) {
            for (ItemStack stack : collected) {
                ItemStack remaining = stack.copy();
                player.getInventory().add(remaining);
                if (!remaining.isEmpty()) {
                    ItemEntity entity =
                        new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), remaining);
                    player.level().addFreshEntity(entity);
                    refunded = true;
                }
            }
        } else {
            for (ItemStack stack : collected) {
                ItemEntity entity = new ItemEntity(level, position.x, position.y, position.z, stack.copy());
                level.addFreshEntity(entity);
            }
            refunded = true;
        }
        if (refunded || player != null) {
            notifyPlayer(playerId, "stashToBot.timeout");
        }
    }

    private static void notifyPlayer(UUID playerId, String key, Object... args) {
        ServerPlayer player = Utils.getServer().getPlayerList().getPlayer(playerId);
        if (player != null) {
            new MessageComponent(key, args).sendSystemMessage(player);
        }
    }

    private static int insertIntoBot(ServerPlayer bot, List<ItemStack> stacks) {
        int moved = 0;
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            int before = stack.getCount();
            ItemStack remaining = insertIntoInventory(bot, stack);
            stack.setCount(remaining.getCount());
            moved += before - remaining.getCount();
        }
        bot.getInventory().setChanged();
        return moved;
    }

    private static Set<Integer> parseSlots(String rawSlots) {
        Set<Integer> indexes = new LinkedHashSet<>();
        for (String part : rawSlots.trim().split("[\\s,]+")) {
            if (part.isEmpty()) {
                continue;
            }
            try {
                indexes.add(Integer.parseInt(part));
            } catch (NumberFormatException ignored) {
                // Ignore unparsable entries, the client only sends slot indexes.
            }
            if (indexes.size() >= MAX_SLOTS) {
                break;
            }
        }
        return indexes;
    }

    private static ItemStack insertIntoInventory(ServerPlayer bot, ItemStack stack) {
        ItemStack remaining = stack;
        for (int i = 0; i < BOT_INVENTORY_SIZE && !remaining.isEmpty(); i++) {
            ItemStack existing = bot.getInventory().getItem(i);
            if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, remaining)) {
                continue;
            }
            int maxStackSize = Math.min(existing.getMaxStackSize(), remaining.getMaxStackSize());
            int space = maxStackSize - existing.getCount();
            if (space <= 0) {
                continue;
            }
            int amount = Math.min(space, remaining.getCount());
            existing.grow(amount);
            remaining.shrink(amount);
        }
        for (int i = 0; i < BOT_INVENTORY_SIZE && !remaining.isEmpty(); i++) {
            if (!bot.getInventory().getItem(i).isEmpty()) {
                continue;
            }
            int amount = Math.min(remaining.getMaxStackSize(), remaining.getCount());
            ItemStack placed = remaining.copy();
            placed.setCount(amount);
            bot.getInventory().setItem(i, placed);
            remaining.shrink(amount);
        }
        return remaining;
    }
}

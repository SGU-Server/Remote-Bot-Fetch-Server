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
package com.sguserver.remotebotfetch.storage.data;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.Item;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import com.sguserver.remotebotfetch.Mod;
import com.sguserver.remotebotfetch.lib.AsyncTasks;
import com.sguserver.remotebotfetch.lib.MessageComponent;
import com.sguserver.remotebotfetch.lib.NameRateLimiter;
import com.sguserver.remotebotfetch.lib.Utils;
import com.sguserver.remotebotfetch.rule.Settings;
import com.sguserver.remotebotfetch.rule.util.command.BaseCommandWithContext;
import com.sguserver.remotebotfetch.rule.util.command.CommandRateLimitNbt;
import com.sguserver.remotebotfetch.rule.util.command.CommandUtils;
import com.sguserver.remotebotfetch.storage.StorageSlotCounter;

public final class CommandGetStorageData implements BaseCommandWithContext {
    private static boolean checkPermission(CommandSourceStack source) {
        return CommandUtils.hasPermission(source, Settings.commandGetStorageData, "/getStorageData",
            "commandGetStorageData");
    }

    private static int executeGetAll(CommandSourceStack source) {
        if (!checkPermission(source)) {
            return 0;
        }
        if (!checkRateLimit(source)) {
            return 0;
        }
        sendCountsAsync(source, counts -> {
            ListTag list = new ListTag();
            counts.entrySet().stream()
                .sorted(Comparator.comparing(entry -> BuiltInRegistries.ITEM.getKey(entry.getKey()).toString()))
                .forEach(entry -> list.add(toResultTag(entry.getKey(), entry.getValue())));
            return list;
        });
        return 1;
    }

    private static int executeGetOne(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        if (!checkPermission(source)) {
            return 0;
        }
        if (!checkRateLimit(source)) {
            return 0;
        }
        Item item = StorageSlotCounter.normalize(Utils.itemFromInput(ItemArgument.getItem(ctx, "id")));
        sendCountsAsync(source, counts -> {
            int count = counts.getOrDefault(item, 0);
            return toResultTag(item, count);
        });
        return 1;
    }

    private static void sendCountsAsync(CommandSourceStack source,
        Function<Map<Item, Integer>, net.minecraft.nbt.Tag> formatter) {
        generateStorageItemCountsAsync().thenApply(formatter).thenAccept(tag -> {
            source.getServer().execute(() -> new MessageComponent(tag).sendSuccess(source));
        }).exceptionally(e -> {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            Mod.LOGGER.warn("getStorageData failed", cause);
            source.getServer().execute(() -> new MessageComponent("common.unknownError").sendFailure(source));
            return null;
        });
    }

    private static boolean checkRateLimit(CommandSourceStack source) {
        try {
            Storage.checkGetDataRateLimit(source.getTextName());
            return true;
        } catch (NameRateLimiter.RateLimitException e) {
            CommandRateLimitNbt.sendWaitSecond(source, e.waitSeconds());
            return false;
        }
    }

    private static CompoundTag toResultTag(Item item, int count) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", BuiltInRegistries.ITEM.getKey(item).toString());
        tag.putInt("count", count);
        return tag;
    }

    private static CompletableFuture<Map<Item, Integer>> generateStorageItemCountsAsync() {
        return AsyncTasks.supply(Storage::generateStorageItemCounts);
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher,
        final CommandBuildContext commandBuildContext) {
        dispatcher.register(Commands.literal("getStorageData").executes(ctx -> {
            return executeGetAll(ctx.getSource());
        }).then(Commands.argument("id", ItemArgument.item(commandBuildContext)).executes(ctx -> {
            return executeGetOne(ctx);
        })));
    }
}

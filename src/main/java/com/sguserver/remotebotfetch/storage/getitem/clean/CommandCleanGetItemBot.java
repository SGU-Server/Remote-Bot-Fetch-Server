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

import java.util.List;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import com.sguserver.remotebotfetch.Mod;
import com.sguserver.remotebotfetch.lib.AsyncTasks;
import com.sguserver.remotebotfetch.lib.MessageComponent;
import com.sguserver.remotebotfetch.rule.Settings;
import com.sguserver.remotebotfetch.rule.util.command.BaseCommand;
import com.sguserver.remotebotfetch.rule.util.command.CommandUtils;

public final class CommandCleanGetItemBot implements BaseCommand {
    private static boolean checkPermission(CommandSourceStack source) {
        return CommandUtils.hasPermission(source, Settings.commandCleanGetItemBot, "/cleanGetItemBot",
            "commandCleanGetItemBot");
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cleanGetItemBot").executes(ctx -> tutor(ctx.getSource()))
            .then(Commands.literal("view").executes(ctx -> executeView(ctx.getSource())))
            .then(Commands.literal("clean").then(Commands.argument("max", IntegerArgumentType.integer(1))
                .executes(ctx -> executeClean(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "max"))))));
    }

    private int executeView(CommandSourceStack source) {
        if (!checkPermission(source)) {
            return 0;
        }
        AsyncTasks.run(() -> {
            int size = CleanGetItemBot.listUnspawnedGetItemBotsWithInventory().size();
            source.getServer()
                .execute(() -> new MessageComponent("cleanGetItemBot.botsWithItem", size).sendSuccess(source));
        });
        return 1;
    }

    private int tutor(CommandSourceStack source) {
        if (!checkPermission(source)) {
            return 0;
        }
        CommandUtils.tutor(source, "cleanGetItemBot", 6);
        return 1;
    }

    private int executeClean(CommandSourceStack source, int max) {
        if (!checkPermission(source)) {
            return 0;
        }
        ServerPlayer player = CommandUtils.getPlayer(source, "/cleanGetItemBot clean");
        if (player == null) {
            return 0;
        }

        new MessageComponent("cleanGetItemBot.started").sendSuccess(source);
        AsyncTasks.run(() -> {
            try {
                List<String> cleaned = CleanGetItemBot.cleanBots(player, max);
                source.getServer()
                    .execute(() -> new MessageComponent("cleanGetItemBot.cleaned", String.join(", ", cleaned))
                        .sendSuccess(source));
            } catch (Throwable throwable) {
                Mod.LOGGER.warn("cleanGetItemBot failed", throwable);
                source.getServer().execute(() -> new MessageComponent("common.unknownError").sendFailure(source));
            }
        });
        return 1;
    }
}

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
package com.sguserver.remotebotfetch.rule.util.command;

import java.util.List;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

import com.mojang.brigadier.CommandDispatcher;

import com.sguserver.remotebotfetch.storage.data.CommandGetStorageData;
import com.sguserver.remotebotfetch.storage.getitem.CommandGetItem;
import com.sguserver.remotebotfetch.storage.getitem.clean.CommandCleanGetItemBot;

public final class SetupCommands {
    private static final List<BaseCommand> COMMANDS = List.of(new CommandCleanGetItemBot());
    private static final List<BaseCommandWithContext> COMMAND_WITH_CONTEXTS =
        List.of(new CommandGetItem(), new CommandGetStorageData());

    public static void registerAll(CommandDispatcher<CommandSourceStack> dispatcher,
        final CommandBuildContext commandBuildContext) {
        for (BaseCommand cmd : COMMANDS) {
            cmd.register(dispatcher);
        }
        for (BaseCommandWithContext cmd : COMMAND_WITH_CONTEXTS) {
            cmd.register(dispatcher, commandBuildContext);
        }
    }
}

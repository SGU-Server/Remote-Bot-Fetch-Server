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
package com.sguserver.remotebotfetch;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import carpet.CarpetExtension;
import carpet.CarpetServer;

import com.sguserver.remotebotfetch.lib.AsyncTasks;
import com.sguserver.remotebotfetch.menu.BotInventorySessions;
import com.sguserver.remotebotfetch.rule.Settings;
import com.sguserver.remotebotfetch.rule.util.command.SetupCommands;
import com.sguserver.remotebotfetch.storage.data.Storage;
import com.sguserver.remotebotfetch.translations.Translations;

import org.slf4j.Logger;

public class Mod implements ModInitializer, CarpetExtension {
    public static final String MOD_ID = "remote-bot-fetch-server";
    public static final String COMPACT_NAME = "remotebotfetch";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static String getVersion() {
        return ModInfoHolder.VERSION;
    }

    public static String getModName() {
        return ModInfoHolder.NAME;
    }

    public static ModContainer getModContainer() {
        return ModInfoHolder.CONTAINER;
    }

    @Override
    public void onInitialize() {
        LOGGER.info("{} version {}", getModName(), getVersion());
        CarpetServer.manageExtension(this);
        Init.initAll();
    }

    @Override
    public String version() {
        return getVersion();
    }

    @Override
    public void onGameStarted() {
        CarpetServer.settingsManager.parseSettingsClass(Settings.class);
    }

    @Override
    public java.util.Map<String, String> canHasTranslations(String lang) {
        return Translations.translations(lang);
    }

    @Override
    public void onServerLoadedWorlds(MinecraftServer server) {
        AsyncTasks.init();
        Storage.prepareDefaultFilesOnWorldLoad();
    }

    @Override
    public void onServerClosed(MinecraftServer server) {
        BotInventorySessions.clear();
        AsyncTasks.shutdown();
    }

    @Override
    public void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher,
        final CommandBuildContext commandBuildContext) {
        SetupCommands.registerAll(dispatcher, commandBuildContext);
    }

    private static class ModInfoHolder {
        private static final ModContainer CONTAINER;
        private static final String NAME;
        private static final String VERSION;

        static {
            CONTAINER = FabricLoader.getInstance().getModContainer(MOD_ID)
                .orElseThrow(() -> new RuntimeException("Mod not found: " + MOD_ID));
            NAME = CONTAINER.getMetadata().getName();
            VERSION = CONTAINER.getMetadata().getVersion().getFriendlyString();
        }
    }
}

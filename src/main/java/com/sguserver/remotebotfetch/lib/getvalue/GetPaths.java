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
package com.sguserver.remotebotfetch.lib.getvalue;

import java.nio.file.Path;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import net.fabricmc.loader.api.FabricLoader;

import com.sguserver.remotebotfetch.Mod;
import com.sguserver.remotebotfetch.lib.Utils;

public final class GetPaths {
    public static Path getWorldPath() {
        MinecraftServer server = Utils.getServer();
        return server.getWorldPath(LevelResource.ROOT);
    }

    public static Path getWorldPath(LevelResource levelResource) {
        MinecraftServer server = Utils.getServer();
        return server.getWorldPath(levelResource);
    }

    public static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static Path getLmsWorldPath() {
        return getWorldPath().resolve(Mod.COMPACT_NAME);
    }

    public static Path getLmsConfigPath() {
        return getConfigPath().resolve(Mod.COMPACT_NAME);
    }

    public static Path getLmsWorldDataPath() {
        return getLmsWorldPath().resolve("data");
    }

    public static Path getLmsConfigDataPath() {
        return getLmsConfigPath().resolve("data");
    }

    public static Path getLmsConfigSecretPath() {
        return getLmsConfigDataPath().resolve("secret");
    }
}

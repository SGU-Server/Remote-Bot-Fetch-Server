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
package com.sguserver.remotebotfetch.storage.getitem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.storage.LevelResource;

import com.sguserver.remotebotfetch.lib.PlayerUtils;
import com.sguserver.remotebotfetch.lib.getvalue.GetPaths;

public final class OfflineInvCheck {
    private OfflineInvCheck() {}

    public static boolean isInventoryEmpty(String name) {
        UUID uuid = UUIDUtil.createOfflinePlayerUUID(name);
        Path playerDat = GetPaths.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(uuid.toString() + ".dat");
        if (!Files.exists(playerDat)) {
            return true;
        }

        try {
            return PlayerUtils.isInventoryEmpty(playerDat);
        } catch (IOException e) {
            return true;
        }
    }
}

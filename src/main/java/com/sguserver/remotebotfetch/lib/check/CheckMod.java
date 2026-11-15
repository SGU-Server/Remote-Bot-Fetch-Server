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
package com.sguserver.remotebotfetch.lib.check;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;

public final class CheckMod {
    private static final Map<String, Boolean> CACHE = new ConcurrentHashMap<>();

    public static boolean checkMod(String modid, String version) {
        String key = modid + "@" + version;
        return CACHE.computeIfAbsent(key, k -> doCheck(modid, version));
    }

    private static boolean doCheck(String modid, String version) {
        ModContainer container = FabricLoader.getInstance().getModContainer(modid).orElse(null);
        if (container == null) {
            return false;
        }

        try {
            VersionPredicate predicate = VersionPredicate.parse(version);
            return predicate.test(container.getMetadata().getVersion());
        } catch (VersionParsingException e) {
            return false;
        }
    }
}

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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sguserver.remotebotfetch.lib.NameRateLimiter;
import com.sguserver.remotebotfetch.lib.getvalue.GetPaths;
import com.sguserver.remotebotfetch.rule.Settings;

import org.jspecify.annotations.Nullable;

public class Storage {
    static final Path configJsonPath = GetPaths.getLmsWorldPath().resolve("checkStorageConfig.json");
    static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();
    static final List<StorageDimension> DIMENSIONS = List.of(new StorageDimension("overworld", Level.OVERWORLD, "0"),
        new StorageDimension("nether", Level.NETHER, "-1"), new StorageDimension("end", Level.END, "1"));
    static final Path storageDir = GetPaths.getLmsWorldPath().resolve("checkStorageList");
    private static final NameRateLimiter GET_DATA_RATE_LIMITER = new NameRateLimiter();

    public static void prepareDefaultFilesOnWorldLoad() {
        StorageJsonService.prepareDefaultFilesOnWorldLoad();
    }

    public static JsonArray generateStorageDataJson() {
        return new StorageDataCollector().generateStorageDataJson();
    }

    public static Map<Item, Integer> generateStorageItemCounts() {
        return new StorageDataCollector().generateStorageItemCounts();
    }

    public static void checkGetDataRateLimit(@Nullable String playerName) {
        GET_DATA_RATE_LIMITER.check("getStorageData", playerName, Settings.getStorageDataCooldownSeconds);
    }

    public static StorageContainerReader.ConfiguredStorageSnapshots collectAllConfiguredStorageSnapshots() {
        List<StorageContainerReader.SlotSnapshot> snapshots = new ArrayList<>();
        for (StorageContainerReader.ConfiguredStorageSnapshots storageSnapshots : StorageContainerReader
            .collectConfiguredStorageSnapshots()) {
            snapshots.addAll(storageSnapshots.snapshots());
        }
        return new StorageContainerReader.ConfiguredStorageSnapshots(null, snapshots, null);
    }

    public static final class Position {
        public final ResourceKey<Level> dimension;
        public final BlockPos pos;

        Position(ResourceKey<Level> dimension, BlockPos pos) {
            this.dimension = dimension;
            this.pos = pos;
        }
    }

    static final class PreparedStorage {
        final String fileName;
        final JsonObject pos;

        PreparedStorage(String fileName, JsonObject pos) {
            this.fileName = fileName;
            this.pos = pos;
        }
    }

    static final class PreparedInputs {
        static final PreparedInputs EMPTY = new PreparedInputs(0, List.of());
        final int total;
        final List<PreparedStorage> inputs;

        PreparedInputs(int total, List<PreparedStorage> inputs) {
            this.total = total;
            this.inputs = inputs;
        }
    }

    static final class ItemCount {
        int count = 0;

        synchronized void add(int count) {
            this.count += count;
        }
    }

    record StorageDimension(String configName, ResourceKey<Level> key, String outputKey) {
    }
}
